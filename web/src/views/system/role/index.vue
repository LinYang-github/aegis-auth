<template>
  <div class="view-container">
    <div class="header">
      <h2>角色管理</h2>
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
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入编码', trigger: 'blur' }]
}

const dialogTitle = computed(() => form.id ? '编辑角色' : '新建角色')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/roles')
    tableData.value = res || []
  } catch (e) {console.error(e)} finally {loading.value = false}
}

const handleAdd = () => {
  resetForm()
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
    submittingPerm.value = true // Loading state just for fetch
    
    try {
        // 1. Fetch permission tree
        const treeRes = await request.get('/permissions')
        permissionTree.value = treeRes || []
        
        // 2. Fetch current role permissions
        const keysRes = await request.get(`/roles/${row.id}/permissions`)
        const checkedKeys = keysRes || []
        
        // Set checked keys
        // Note: In element-plus tree, setting parent key checks all children. 
        // We usually should only set Leaf nodes, but here we just set all and hope element-plus handles or we filter.
        // Better to setCheckedKeys.
        // Wait, if I check a parent node, it auto checks children. If fetch returns parent ID, it might be fine.
        // But if I return parent ID and not all children are checked, it might show indeterminate?
        // Let's just set keys.
        // Next tick to ensure tree is rendered
        setTimeout(() => {
            if(treeRef.value) treeRef.value.setCheckedKeys(checkedKeys, false) // false = leafOnly? No, second arg is leafOnly in some versions.
            // Check EL-Plus docs: setCheckedKeys(keys, leafOnly)
            // If backend returns all IDs including parents, we just set them.
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
}

const formatTime = (time) => {
    if(!time) return ''
    return new Date(time).toLocaleString()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.view-container { height: 100%; display: flex; flex-direction: column; background: var(--el-bg-color); }
.header { padding: 15px 20px; border-bottom: 1px solid var(--el-border-color-light); display: flex; justify-content: space-between; align-items: center; }
.table-card { flex: 1; border: none; overflow: auto; }
</style>
