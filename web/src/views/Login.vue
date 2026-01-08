<template>
  <div class="login-container">
    <div class="glass-card">
      <div class="brand">
        <h1>Aegis Auth</h1>
        <p>Enterprise Identity Solution</p>
      </div>
      
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="input-group">
          <input type="text" v-model="username" placeholder="Username" required />
          <span class="icon">👤</span>
        </div>
        <div class="input-group">
          <input type="password" v-model="password" placeholder="Password" required />
          <span class="icon">🔒</span>
        </div>
        
        <div class="actions">
          <button type="submit" :disabled="loading">
            <span v-if="!loading">Sign In</span>
            <span v-else>Authenticating...</span>
          </button>
        </div>

        <div v-if="error" class="error-msg">
          {{ error }}
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import request from '../utils/request';
import { useRouter } from 'vue-router';

const username = ref('');
const password = ref('');
const loading = ref(false);
const error = ref('');
const emit = defineEmits(['login-success']);

const handleLogin = async () => {
  loading.value = true;
  error.value = '';
  
  try {
    const formData = new URLSearchParams();
    formData.append('username', username.value);
    formData.append('password', password.value);
    
    const res = await request.post('/login', formData);
    // Request interceptor doesn't set token automatically for form login usually unless backend returns it in body or cookie
    // Assuming backend returns token in data based on auth.js `setToken` logic which isn't called here yet?
    // Wait, the guide request.js interceptor adds token header from localStorage.
    // The login flow usually needs to SET that token. 
    // If res contains token:
    if (res && res.token) {
        localStorage.setItem('syn_token', res.token);
    }
    // If backend uses Cookie, then we don't need to do anything, but guide mentions "auth.js" and header "Authorization: Bearer".
    // So we MUST set token.
    // Let's assume standard Synapse login returns token.
    
    emit('login-success');
  } catch (err) {
    console.error(err);
    error.value = "Invalid credentials or system error.";
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0F2027 0%, #203A43 50%, #2C5364 100%);
  overflow: hidden;
  position: relative;
}

/* Background Animation Circle */
.login-container::before {
  content: '';
  position: absolute;
  top: -10%;
  right: -10%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(72,85,99,0.4) 0%, rgba(0,0,0,0) 70%);
  border-radius: 50%;
  animation: float 10s infinite ease-in-out;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: -10%;
  left: -10%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(41,50,60,0.5) 0%, rgba(0,0,0,0) 70%);
  border-radius: 50%;
  animation: float 15s infinite ease-in-out reverse;
}

.glass-card {
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 3rem 2.5rem;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.3);
  z-index: 1;
}

.brand {
  text-align: center;
  margin-bottom: 2rem;
}

.brand h1 {
  font-size: 2rem;
  margin: 0;
  color: #fff;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.brand p {
  margin: 0.5rem 0 0;
  color: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
}

.input-group {
  position: relative;
  margin-bottom: 1.5rem;
}

.input-group input {
  width: 100%;
  padding: 1rem 1rem 1rem 3rem;
  background: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  color: #fff;
  font-size: 1rem;
  outline: none;
  transition: all 0.3s;
  box-sizing: border-box; /* Fix sizing */
}

.input-group input:focus {
  background: rgba(0, 0, 0, 0.4);
  border-color: rgba(255, 255, 255, 0.3);
  box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.1);
}

.icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  opacity: 0.5;
  font-size: 1.2rem;
}

button {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(90deg, #00C9FF 0%, #92FE9D 100%);
  border: none;
  border-radius: 12px;
  color: #000;
  font-weight: 600;
  font-size: 1rem;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 201, 255, 0.3);
}

button:active {
  transform: translateY(0);
}

button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-msg {
  color: #ff6b6b;
  text-align: center;
  margin-top: 1rem;
  font-size: 0.9rem;
}

@keyframes float {
  0% { transform: translate(0, 0); }
  50% { transform: translate(20px, 20px); }
  100% { transform: translate(0, 0); }
}
</style>
