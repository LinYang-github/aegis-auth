<template>
  <div class="view-container">
    <div class="header">
      <h2>角色管理</h2>
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
        <el-button type="primary" icon="Plus" @click="handleAdd">新建角色</el-button>
        <el-button icon="Refresh" circle @click="fetchData" :loading="loading" />
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%; height: 100%">
        <el-table-column prop="name" label="角色名称" />
        <el-table-column prop="code" label="角色编码" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="createdAt" label="创建时间">
             <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAssignPermission(row)">分配权限</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Role Form Dialog -->
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
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="ROLE_USER" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确认</el-button>
      </template>
    </el-dialog>

    <!-- Permission Assign Dialog -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="600px">
        <el-tree
            ref="treeRef"
            :data="permissionTree"
            show-checkbox
            node-key="id"
            :props="{ label: 'name' }"
            default-expand-all
        />
        <template #footer>
            <el-button @click="permDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleSubmitPerm" :loading="submittingPerm">确认</el-button>
        </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import request from '../../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const submitting = ref(false)

// App Filter
const appList = ref([])
const currentAppCode = ref('')

// Permission Assign State
const permDialogVisible = ref(false)
const permissionTree = ref([])
const treeRef = ref(null)
const currentRoleId = ref(null)
const submittingPerm = ref(false)

const form = reactive({
  id: undefined,
  name: '',
  code: '',
  description: '',
  appCode: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  appCode: [{ required: true, message: '缺少应用上下文', trigger: 'change' }]
}

const dialogTitle = computed(() => form.id ? '编辑角色' : '新建角色')

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
    const res = await request.get(`/roles?appCode=${currentAppCode.value}`)
    tableData.value = res || []
  } catch (e) {console.error(e)} finally {loading.value = false}
}

const handleAdd = () => {
  resetForm()
  form.appCode = currentAppCode.value
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除?', '警告', { confirmButtonText: '确认', type: 'warning' })
    .then(async () => {
      await request.delete(`/roles/${row.id}`)
      ElMessage.success('删除成功')
      fetchData()
    })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
      if (valid) {
          submitting.value = true
          try {
              if (form.id) await request.put('/roles', form)
              else await request.post('/roles', form)
              ElMessage.success('保存成功')
              dialogVisible.value = false
              fetchData()
          } catch(e){} finally{ submitting.value = false}
      }
  })
}

// Permission Assignment Logic
const handleAssignPermission = async (row) => {
    currentRoleId.value = row.id
    permDialogVisible.value = true
    submittingPerm.value = true 
    
    try {
        // 1. Fetch permission tree (Filtered by Role's AppCode)
        const appCode = row.appCode || currentAppCode.value // Use role's appCode if available
        const treeRes = await request.get(`/permissions?appCode=${appCode}`)
        permissionTree.value = treeRes || []
        
        // 2. Fetch current role permissions
        const keysRes = await request.get(`/roles/${row.id}/permissions`)
        const checkedKeys = keysRes || []
        
        setTimeout(() => {
            if(treeRef.value) treeRef.value.setCheckedKeys(checkedKeys, false) 
        }, 100)
    } finally {
        submittingPerm.value = false
    }
}

const handleSubmitPerm = async () => {
    if(!treeRef.value) return
    const checkedKeys = treeRef.value.getCheckedKeys()
    const halfCheckedKeys = treeRef.value.getHalfCheckedKeys()
    const allKeys = [...checkedKeys, ...halfCheckedKeys]
    
    submittingPerm.value = true
    try {
        await request.post(`/roles/${currentRoleId.value}/permissions`, allKeys)
        ElMessage.success('授权成功')
        permDialogVisible.value = false
    } catch(e){} finally{ submittingPerm.value = false }
}

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.code = ''
  form.description = ''
  form.appCode = ''
}

const formatTime = (time) => {
    if(!time) return ''
    return new Date(time).toLocaleString()
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
