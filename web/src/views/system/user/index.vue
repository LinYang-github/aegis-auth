<template>
  <div class="view-container">
    <!-- Header -->
    <div class="header">
      <h2>User Management</h2>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="handleAdd">Create User</el-button>
        <el-button icon="Refresh" circle @click="fetchData" :loading="loading" />
      </div>
    </div>

    <!-- Content -->
    <el-card shadow="never" class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%; height: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="Username" />
        <el-table-column prop="nickname" label="Nickname" />
        <el-table-column prop="status" label="Status">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="dark">
              {{ row.status === 1 ? 'Active' : 'Disabled' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="Created At" width="180">
            <template #default="{ row }">
                {{ formatTime(row.createdAt) }}
            </template>
        </el-table-column>
        <el-table-column label="Actions" width="200" fixed="right">
          <template #default>
            <el-button link type="primary" size="small">Edit</el-button>
            <el-button link type="danger" size="small">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Dialog Placeholder -->
    <el-dialog v-model="dialogVisible" title="User">
      <span>Form goes here...</span>
      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="dialogVisible = false">Confirm</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../../utils/request'
import { Plus, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    // Backend is mapped to /api/users.
    // baseURL is '/api'.
    // So request.get('/users') -> '/api/users'.
    const res = await request.get('/users')
    tableData.value = res || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogVisible.value = true
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
  flex-shrink: 0; 
}
.table-card { 
  flex: 1; 
  border: none; 
  overflow: hidden; 
  display: flex;
  flex-direction: column;
}
.table-card :deep(.el-card__body) {
  flex: 1;
  padding: 0;
  overflow: hidden;
}
</style>
