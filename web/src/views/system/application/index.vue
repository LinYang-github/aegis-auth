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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @open="handleDialogOpen">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="basic">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="margin-top: 20px">
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
        </el-tab-pane>
        <el-tab-pane label="SSO 配置 (单点登录)" name="sso" :disabled="!form.id">
            <el-form ref="oidcFormRef" :model="oidcForm" label-width="140px" style="margin-top: 20px">
                <el-alert 
                    title="什么是 SSO (单点登录)?" 
                    type="info" 
                    :closable="false" 
                    show-icon
                    style="margin-bottom: 20px"
                >
                    <template #default>
                        启用 SSO 后，该应用可以作为“客户端”委托 Aegis Auth 进行用户认证。<br/>
                        用户只需在 Aegis 登录一次，即可访问所有已接入的应用，无需重复输入密码。
                    </template>
                </el-alert>
                
                <el-form-item label="启用 SSO 登录">
                    <el-switch v-model="oidcForm.enabled" active-text="开启" inactive-text="关闭" />
                </el-form-item>
                
                <div v-if="oidcForm.enabled">
                    <el-divider content-position="left">对接信息 (复制到您的应用中)</el-divider>
                    
                    <el-form-item label="Client ID">
                        <template #label>
                            Client ID
                            <el-tooltip content="应用的唯一标识账号，相当于应用的“用户名”" placement="top">
                                <el-icon style="margin-left: 4px"><QuestionFilled /></el-icon>
                            </el-tooltip>
                        </template>
                        <el-input v-model="oidcForm.clientId" disabled>
                            <template #append>
                                <el-button @click="copyToClipboard(oidcForm.clientId)">复制</el-button>
                            </template>
                        </el-input>
                    </el-form-item>
                    
                    <el-form-item label="Client Secret">
                         <template #label>
                            Client Secret
                            <el-tooltip content="应用的认证密钥，相当于应用的“密码”，请妥善保管" placement="top">
                                <el-icon style="margin-left: 4px"><QuestionFilled /></el-icon>
                            </el-tooltip>
                        </template>
                         <div style="display: flex; gap: 10px; width: 100%">
                             <el-input v-model="oidcForm.clientSecret" type="password" show-password disabled placeholder="点击重置生成密钥" />
                             <el-button type="warning" size="small" @click="handleRotateSecret">重置并查看</el-button>
                         </div>
                    </el-form-item>

                    <el-divider content-position="left">安全设置</el-divider>

                    <el-form-item>
                        <template #label>
                            回调地址
                            <el-tooltip content="用户登录成功后，浏览器将跳转回的地址 (白名单)。例如：http://localhost:8080/login/oauth2/code/custom" placement="top">
                                <el-icon style="margin-left: 4px"><QuestionFilled /></el-icon>
                            </el-tooltip>
                        </template>
                        <div v-for="(uri, index) in oidcForm.redirectUris" :key="index" style="display: flex; gap: 10px; margin-bottom: 10px; width: 100%">
                             <el-input v-model="oidcForm.redirectUris[index]" placeholder="例如: http://localhost:8080/callback" />
                             <el-button icon="Delete" circle type="danger" @click="removeUri(index)" />
                        </div>
                        <el-button size="small" icon="Plus" @click="addUri">添加回调地址</el-button>
                        <div class="form-help-text">必须完全匹配，支持配置多个。</div>
                    </el-form-item>
                    
                    <el-form-item label="授权模式">
                         <template #label>
                            授权模式
                            <el-tooltip content="决定应用如何获取令牌。通常后端应用使用 'Authorization Code'，前端SPA可能用到 'PKCE' (此处包含在Code模式中)。" placement="top">
                                <el-icon style="margin-left: 4px"><QuestionFilled /></el-icon>
                            </el-tooltip>
                        </template>
                        <el-checkbox-group v-model="oidcForm.grantTypes">
                            <el-checkbox label="authorization_code">
                                Authorization Code
                                <el-tag size="small" type="info" style="margin-left:5px">最常用/推荐</el-tag>
                            </el-checkbox>
                            <el-checkbox label="refresh_token">Refresh Token</el-checkbox>
                            <el-checkbox label="client_credentials">Client Credentials (机器通信)</el-checkbox>
                        </el-checkbox-group>
                    </el-form-item>
                </div>
            </el-form>
        </el-tab-pane>
      </el-tabs>

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
import { Delete, Plus, QuestionFilled } from '@element-plus/icons-vue' 

const loading = ref(false)
const submitting = ref(false)
// ... existing state ...

const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text).then(() => {
        ElMessage.success('复制成功')
    })
}
const tableData = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const activeTab = ref('basic')

const form = reactive({
  id: undefined,
  code: '',
  name: '',
  baseUrl: '',
  status: 1
})

const oidcForm = reactive({
    enabled: false,
    clientId: '',
    clientSecret: '',
    redirectUris: [],
    grantTypes: [],
    scopes: ['openid', 'profile']
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

const fetchOidcConfig = async (id) => {
    try {
        const res = await request.get(`/applications/${id}/oidc`)
        if(res) {
            oidcForm.enabled = res.enabled || false
            oidcForm.clientId = res.clientId || form.code
            oidcForm.clientSecret = res.clientSecret || ''
            oidcForm.redirectUris = res.redirectUris || []
            oidcForm.grantTypes = res.grantTypes || ['authorization_code', 'refresh_token']
        }
    } catch(e) { console.error(e) }
}

const handleAdd = () => {
  resetForm()
  activeTab.value = 'basic'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  activeTab.value = 'basic'
  dialogVisible.value = true
  // Fetch OIDC config
  fetchOidcConfig(row.id)
}

const handleDialogOpen = () => {
    // Optional hook
}

const addUri = () => {
    oidcForm.redirectUris.push('')
}
const removeUri = (index) => {
    oidcForm.redirectUris.splice(index, 1)
}

const handleRotateSecret = async () => {
    try {
        const res = await request.post(`/applications/${form.id}/oidc/rotate-secret`)
        oidcForm.clientSecret = res
        ElMessage.success('密钥已重置，请妥善保存')
    } catch(e){}
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
        // 1. Save Basic Info
        let savedAppId = form.id
        if (form.id) {
          await request.put('/applications', form)
        } else {
          // Note: If creating new, we can't save OIDC yet because we need ID.
          // Since we disable SSO tab for new, this is fine. User must save then edit.
          await request.post('/applications', form)
          // We don't get ID back easily unless we change API return.
          // Assuming user has to re-open to edit SSO.
        }
        
        // 2. Save OIDC Info if ID exists and Enabled/Configured
        if (form.id) {
             const oidcData = {
                 ...oidcForm,
                 scopes: ['openid', 'profile'] // force scopes for now
             }
             await request.post(`/applications/${form.id}/oidc`, oidcData)
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
  
  oidcForm.enabled = false
  oidcForm.clientId = ''
  oidcForm.clientSecret = ''
  oidcForm.redirectUris = []
  oidcForm.grantTypes = ['authorization_code', 'refresh_token']
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
.form-help-text {
    font-size: 12px;
    color: var(--el-text-color-secondary);
    margin-top: 5px;
}
</style>
