<template>
  <div class="home">
    <h1>Aegis Auth Vue 演示</h1>
    
    <div v-if="user">
      <div class="success-box">
        <h2>欢迎, {{ user.profile.sub }}!</h2>
        <p>您已通过 OIDC + PKCE 模式成功登录。</p>
        <button @click="logout" class="btn logout">退出登录</button>
      </div>

      <div class="info-box">
        <h3>用户凭据 (Profile)</h3>
        <pre>{{ JSON.stringify(user.profile, null, 2) }}</pre>
      </div>
      
      <div class="info-box">
        <h3>访问令牌 (Access Token)</h3>
        <div class="token">{{ user.access_token }}</div>
      </div>
    </div>

    <div v-else>
      <p>当前未登录。</p>
      <button @click="login" class="btn login">使用 Aegis Auth 登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { userManager, signIn, signOut } from '../auth';

const user = ref(null);

onMounted(async () => {
  try {
    user.value = await userManager.getUser();
  } catch (e) {
    console.error(e);
  }
});

const login = () => signIn();
const logout = () => signOut();
</script>

<style scoped>
.home { font-family: sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
.btn { padding: 10px 20px; cursor: pointer; border: none; border-radius: 4px; font-size: 16px; margin: 10px 0; }
.login { background-color: #42b983; color: white; }
.logout { background-color: #e74c3c; color: white; }
.success-box { background: #e8f8f5; padding: 20px; border-radius: 8px; color: #0e6655; }
.info-box { background: #f4f6f7; padding: 15px; border-radius: 8px; margin-top: 20px; }
.token { word-break: break-all; font-family: monospace; font-size: 12px; color: #555; }
pre { white-space: pre-wrap; word-wrap: break-word; }
</style>
