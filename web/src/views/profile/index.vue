<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- Basic Info Tab -->
        <el-tab-pane label="基本信息" name="info">
          <el-form :model="profileForm" ref="profileFormRef" label-width="100px" style="max-width: 500px">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" />
            </el-form-item>
             <el-form-item label="头像地址" prop="avatar">
              <el-input v-model="profileForm.avatar" placeholder="输入头像URL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- Security Settings Tab -->
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="passwordForm" ref="passwordFormRef" :rules="passwordRules" label-width="100px" style="max-width: 500px">
             <el-alert title="修改密码后需要重新登录" type="warning" :closable="false" style="margin-bottom: 20px" />
            <el-form-item label="当前密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../utils/request'
import { removeToken } from '../../utils/auth'

const activeTab = ref('info')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)

const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
      { required: true, message: '请确认新密码', trigger: 'blur' },
      {
          validator: (rule, value, callback) => {
              if (value !== passwordForm.newPassword) {
                  callback(new Error('两次输入的密码不一致'))
              } else {
                  callback()
              }
          },
          trigger: 'blur'
      }
  ]
}

const fetchProfile = async () => {
    try {
        const res = await request.get('/user/profile')
        Object.assign(profileForm, res)
    } catch (e) {
        console.error(e)
    }
}

const handleUpdateProfile = async () => {
    try {
        await request.put('/user/profile', profileForm)
        ElMessage.success('个人信息更新成功')
        fetchProfile()
    } catch (e) {
        console.error(e)
    }
}

const handleChangePassword = async () => {
    if (!passwordFormRef.value) return
    await passwordFormRef.value.validate(async (valid) => {
        if (valid) {
            try {
                await request.post('/user/password', passwordForm)
                ElMessageBox.alert('密码修改成功，请重新登录', '提示', {
                    confirmButtonText: '确定',
                    callback: () => {
                        removeToken()
                        window.location.reload()
                    }
                })
            } catch (e) {
                console.error(e)
            }
        }
    })
}

onMounted(() => {
    fetchProfile()
})
</script>

<style scoped>
.app-container {
    padding: 20px;
}
.card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
</style>
