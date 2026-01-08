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
        <template v-for="menu in menuList" :key="menu.id">
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.code">
                <template #title>
                    <el-icon><MenuIcon /></el-icon>
                    <span>{{ menu.name }}</span>
                </template>
                <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.code">
                    {{ child.name }}
                </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.code">
                <el-icon><MenuIcon /></el-icon>
                <span>{{ menu.name }}</span>
            </el-menu-item>
        </template>
      </el-menu>
      

    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">
            系统管理 / {{ mapMenuName(activeMenu) }}
        </div>
        <div class="header-right">
             <div class="theme-switch" @click="toggleDark()">
                 <el-icon v-if="isDark"><Moon /></el-icon>
                 <el-icon v-else><Sunny /></el-icon>
             </div>
             <div class="user-info">
                 <el-avatar size="small" style="background-color: var(--el-color-primary)">A</el-avatar>
                 <span>管理员</span>
             </div>
             <el-button type="danger" link icon="SwitchButton" @click="handleLogout">退出登录</el-button>
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
import request from './utils/request'
import Login from './views/Login.vue'
import UserManagement from './views/system/user/index.vue'
import RoleManagement from './views/system/role/index.vue'
import MenuManagement from './views/system/menu/index.vue'
import { getToken, removeToken } from './utils/auth'
import { User, SwitchButton, Moon, Sunny, Monitor, Key, Menu as MenuIcon } from '@element-plus/icons-vue'

// State
const isLoggedIn = ref(!!getToken())
const activeMenu = ref('UserManagement')
const menuList = ref([])

// Component Map
const componentMap = {
    'UserManagement': UserManagement,
    'RoleManagement': RoleManagement,
    'MenuManagement': MenuManagement
}

// Map backend permission code/path to component key if needed, or just use code as key
// Simple mapping: code "user:list" -> key "UserManagement"
// Or just let activeMenu be the index from backend?
// Let's assume backend `code` or `path` maps to our component keys.
// For now, I'll map common codes manually or use the `code` field from backend as the index.

const mapMenuName = (key) => {
    // Try to find name in menuList
    const findName = (menus) => {
        for(const m of menus) {
            if(m.code === key) return m.name
            if(m.children) {
                const found = findName(m.children)
                if(found) return found
            }
        }
        return null
    }
    const name = findName(menuList.value)
    if(name) return name

    const map = {
        'UserManagement': '用户管理',
        'RoleManagement': '角色管理',
        'MenuManagement': '菜单管理'
    }
    return map[key] || key
}

const currentPageComponent = computed(() => {
    // Map activeMenu (which is role code or similar) to Component
    // Since we don't have dynamic component loading yet, we map known codes.
    if(activeMenu.value === 'sys:user:list' || activeMenu.value === 'UserManagement') return UserManagement
    if(activeMenu.value === 'sys:role:list' || activeMenu.value === 'RoleManagement') return RoleManagement
    if(activeMenu.value === 'sys:menu:list' || activeMenu.value === 'MenuManagement') return MenuManagement
    return UserManagement
})

// Theme
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
    fetchMenus()
}

const handleLogout = () => {
    removeToken()
    isLoggedIn.value = false
    menuList.value = []
}

const handleSelect = (key) => {
    activeMenu.value = key
}

const fetchMenus = async () => {
    if(!isLoggedIn.value) return
    try {
        const res = await request.get('/user/menus')
        menuList.value = res || []
        // Default select first menu if activeMenu is not valid?
    } catch(e) {
        console.error(e)
    }
}

onMounted(() => {
    if(isLoggedIn.value) {
        fetchMenus()
    }
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
    display: none;
}
.header-right {
    display: flex;
    align-items: center;
    gap: 16px;
}
.theme-switch {
    cursor: pointer;
    color: var(--gdos-text-primary);
    display: flex;
    align-items: center;
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
