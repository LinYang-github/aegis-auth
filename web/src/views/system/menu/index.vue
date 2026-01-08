<template>
  <div class="view-container">
    <div class="header">
      <h2>菜单管理</h2>
      <div class="header-left" style="margin-left: 20px;">
          <el-select v-model="currentAppCode" placeholder="选择应用" style="width: 200px" @change="fetchData">
              <el-option
                v-for="item in appList"
                :key="item.code"
                :label="item.name"
                :value="item.code"
              />
          </el-select>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd()">新建菜单</el-button>
        <el-button icon="Refresh" circle @click="fetchData" :loading="loading" />
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        style="width: 100%; margin-bottom: 20px;"
        row-key="id"
        border
        default-expand-all
      >
        <el-table-column prop="name" label="名称" width="200" />
        <el-table-column prop="code" label="编码" width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.type === 1">菜单</el-tag>
            <el-tag v-else type="info">按钮</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路径/URL" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button link type="primary" size="small" icon="Plus" @click="handleAdd(row.id)">新增子项</el-button>
            <el-button link type="primary" size="small" icon="Edit" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="所属应用" prop="appCode">
            <el-select v-model="form.appCode" disabled placeholder="自动绑定">
                 <el-option
                    v-for="item in appList"
                    :key="item.code"
                    :label="item.name"
                    :value="item.code"
                  />
            </el-select>
        </el-form-item>
        <el-form-item label="父级菜单" prop="parentId">
             <el-tree-select
                v-model="form.parentId"
                :data="tableData"
                check-strictly
                :render-after-expand="false"
                node-key="id" 
                :props="{ label: 'name' }"
                placeholder="选择父级 (留空为顶级)"
                clearable
             />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="user:list" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮/API</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="路径" prop="path" v-if="form.type === 1">
          <el-input v-model="form.path" placeholder="/system/user" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '../../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([]) // Tree data
const dialogVisible = ref(false)
const formRef = ref(null)

const appList = ref([])
const currentAppCode = ref('')

const form = reactive({
  id: undefined,
  parentId: undefined,
  name: '',
  code: '',
  type: 1,
  path: '',
  sortOrder: 0,
  appCode: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  appCode: [{ required: true, message: '缺少应用上下文', trigger: 'change' }]
}

const dialogTitle = computed(() => form.id ? '编辑菜单' : '新建菜单')

const fetchApps = async () => {
    try {
        const res = await request.get('/applications')
        appList.value = res || []
        if(appList.value.length > 0 && !currentAppCode.value) {
            currentAppCode.value = appList.value[0].code
            fetchData()
        }
    } catch(e) { console.error(e) }
}

const fetchData = async () => {
  if(!currentAppCode.value) return
  loading.value = true
  try {
    const res = await request.get(`/permissions?appCode=${currentAppCode.value}`) // Ensure backend supports filtering by appCode if passed
    // NOTE: Backend SysPermissionController.list() needs update to support filtering.
    // For now assuming we might receive all and filter frontend or update backend soon.
    // Let's rely on backend filtering.
    console.log('Permissions:', res)
    tableData.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = (parentId) => {
  resetForm()
  form.parentId = parentId 
  form.appCode = currentAppCode.value
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  if (form.parentId === 0) form.parentId = undefined
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该菜单及其子项吗?', '警告', {
    confirmButtonText: '确认', type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/permissions/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    } catch (e) { console.error(e) }
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (!form.parentId) form.parentId = 0
        if (form.id) {
          await request.put('/permissions', form)
        } else {
          await request.post('/permissions', form)
        }
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) { console.error(e) } finally { submitting.value = false }
    }
  })
}

const resetForm = () => {
  form.id = undefined
  form.parentId = undefined
  form.name = ''
  form.code = ''
  form.type = 1
  form.path = ''
  form.sortOrder = 0
}

onMounted(() => {
  fetchApps()
})
</script>

<style scoped>
.view-container { height: 100%; display: flex; flex-direction: column; background: var(--el-bg-color); }
.header { padding: 15px 20px; border-bottom: 1px solid var(--el-border-color-light); display: flex; justify-content: space-between; align-items: center; }
.table-card { flex: 1; border: none; overflow: auto; }
</style>
