<template>
  <div class="view-container">
    <div class="header">
      <h2>应用管理</h2>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd">新建应用</el-button>
        <el-button icon="Refresh" circle @click="fetchData" :loading="loading" />
      </div>
    </div>

    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" style="width: 100%" v-loading="loading">
        <el-table-column prop="code" label="应用编码" width="150" />
        <el-table-column prop="name" label="应用名称" width="200" />
        <el-table-column prop="baseUrl" label="基础URL" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)" :disabled="row.code === 'auth'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="应用编码" prop="code">
          <el-input v-model="form.code" placeholder="例如: crm, erp" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="form.name" placeholder="例如: CRM客户管理系统" />
        </el-form-item>
        <el-form-item label="基础URL" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="例如: http://crm.example.com" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
  id: undefined,
  code: '',
  name: '',
  baseUrl: '',
  status: 1
})

const rules = {
  code: [{ required: true, message: '请输入应用编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }]
}

const dialogTitle = computed(() => form.id ? '编辑应用' : '新建应用')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/applications')
    tableData.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
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
  if (row.code === 'auth') return
  ElMessageBox.confirm('确认删除该应用吗?', '警告', {
    confirmButtonText: '确认',
    type: 'warning'
  }).then(async () => {
    try {
      await request.delete(`/applications/${row.id}`)
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
        if (form.id) {
          await request.put('/applications', form)
        } else {
          await request.post('/applications', form)
        }
        ElMessage.success('保存成功')
        dialogVisible.value = false
        fetchData()
      } catch (e) {
        console.error(e)
      } finally {
        submitting.value = false
      }
    }
  })
}

const resetForm = () => {
  form.id = undefined
  form.code = ''
  form.name = ''
  form.baseUrl = ''
  form.status = 1
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.view-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
}
.header {
  padding: 15px 20px;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.table-card {
  flex: 1;
  border: none;
  overflow: auto;
}
</style>
