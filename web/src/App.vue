<template>
  <div v-if="!isLoggedIn" class="app-container">
    <Login @login-success="handleLoginSuccess" />
  </div>
  <el-container v-else class="app-layout">
    <el-aside width="220px" class="sidebar">
      <div class="brand">
        <el-icon size="24" color="#409EFF"><Monitor /></el-icon>
        <span class="brand-text">Aegis Auth</span>
      </div>
      <el-menu
        class="el-menu-vertical"
        :default-active="activeMenu"
        background-color="transparent"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        @select="handleSelect"
      >
        <el-menu-item index="UserManagement">
          <el-icon><User /></el-icon>
          <span>User Management</span>
        </el-menu-item>
        <!-- Add more menu items here -->
      </el-menu>
      
      <div class="sidebar-footer">
          <div class="theme-switch" @click="toggleDark()">
              <el-icon v-if="isDark"><Moon /></el-icon>
              <el-icon v-else><Sunny /></el-icon>
          </div>
          <el-button type="danger" text icon="SwitchButton" @click="handleLogout">Sign Out</el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
            System / {{ activeMenu }}
        </div>
        <div class="user-info">
            <el-avatar size="small" style="background-color: var(--el-color-primary)">A</el-avatar>
            <span>Admin</span>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <component :is="currentPageComponent" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, shallowRef, onMounted } from 'vue'
import { useDark, useToggle } from '@vueuse/core'
import Login from './views/Login.vue'
import UserManagement from './views/system/user/index.vue'
import { getToken, removeToken } from './utils/auth'
import { User, SwitchButton, Moon, Sunny, Monitor } from '@element-plus/icons-vue'

// State
const isLoggedIn = ref(!!getToken())
const activeMenu = ref('UserManagement')

// Component Map
const componentMap = {
    'UserManagement': UserManagement
}

const currentPageComponent = computed(() => {
    return componentMap[activeMenu.value] || UserManagement
})

// Theme
// Using standard manual toggle for simplicity as per guide or useDark
// Guide says: document.documentElement.classList.add('dark')
const isDark = ref(false)
const toggleDark = () => {
    isDark.value = !isDark.value
    if (isDark.value) {
        document.documentElement.classList.add('dark')
    } else {
        document.documentElement.classList.remove('dark')
    }
}

// Actions
const handleLoginSuccess = () => {
    isLoggedIn.value = true
    activeMenu.value = 'UserManagement'
}

const handleLogout = () => {
    removeToken()
    isLoggedIn.value = false
}

const handleSelect = (key) => {
    activeMenu.value = key
}

onMounted(() => {
    // Check system preference
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
       // isDark.value = true
       // toggleDark() // Sync
    }
})
</script>

<style scoped>
.app-container, .app-layout {
  height: 100vh;
  width: 100vw;
  background-color: var(--gdos-bg-page);
}

.sidebar {
  background-color: #1d1e1f; /* Dark sidebar */
  border-right: 1px solid var(--gdos-border-color);
  display: flex;
  flex-direction: column;
}
.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid #333;
  color: #fff;
  font-weight: 600;
  font-size: 18px;
}
.sidebar-footer {
    padding: 20px;
    border-top: 1px solid #333;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.theme-switch {
    cursor: pointer;
    color: #fff;
}

.header {
  background-color: var(--gdos-header-bg);
  border-bottom: 1px solid var(--gdos-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}
.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--gdos-text-primary);
}
.breadcrumb {
    color: var(--el-text-color-secondary);
}

.main-content {
  padding: 20px;
  background-color: var(--el-bg-color-page);
  overflow: hidden; /* Prevent double scroll */
}

/* Sidebar Menu Overrides */
:deep(.el-menu) {
    border-right: none;
}
</style>
