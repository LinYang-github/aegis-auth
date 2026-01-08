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
        @select="handleSelect"
      >
        <template v-for="menu in menuList" :key="menu.id">
            <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.code">
                <template #title>
                    <el-icon><component :is="getMenuIcon(menu)" /></el-icon>
                    <span>{{ menu.name }}</span>
                </template>
                <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.code">
                    <el-icon><component :is="getMenuIcon(child)" /></el-icon>
                    <span>{{ child.name }}</span>
                </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="menu.code">
                <el-icon><component :is="getMenuIcon(menu)" /></el-icon>
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
import ApplicationManagement from './views/system/application/index.vue'
import { getToken, removeToken } from './utils/auth'
import { User, SwitchButton, Moon, Sunny, Monitor, Key, Menu as MenuIcon, Setting, Avatar, Platform, Tools } from '@element-plus/icons-vue'

// State
const isLoggedIn = ref(!!getToken())
const activeMenu = ref('UserManagement')
const menuList = ref([])

// Component Map
const componentMap = {
    'UserManagement': UserManagement,
    'RoleManagement': RoleManagement,
    'MenuManagement': MenuManagement,
    'ApplicationManagement': ApplicationManagement
}

// Map backend permission code/path to component key if needed, or just use code as key
// Simple mapping: code "user:list" -> key "UserManagement"
// Or just let activeMenu be the index from backend?
// Let's assume backend `code` or `path` maps to our component keys.
// For now, I'll map common codes manually or use the `code` field from backend as the index.

const getMenuIcon = (item) => {
    // 1. Try Code
    const codeMap = {
        'sys': Tools,
        'sys:user:list': User,
        'sys:role:list': Avatar,
        'sys:menu:list': MenuIcon,
        'sys:app:list': Platform
    }
    if (item.code && codeMap[item.code]) return codeMap[item.code]

    // 2. Try Path
    const pathMap = {
        '/system/user': User,
        '/system/role': Avatar,
        '/system/menu': MenuIcon,
        '/system/application': Platform
    }
    if (item.path && pathMap[item.path]) return pathMap[item.path]

    // 3. Fallback
    return MenuIcon
}

const mapMenuName = (key) => {
    // ... same as before
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
        'MenuManagement': '菜单管理',
        'ApplicationManagement': '应用管理'
    }
    return map[key] || key
}

const currentPageComponent = computed(() => {
    // Map activeMenu (which is role code or similar) to Component
    // Since we don't have dynamic component loading yet, we map known codes.
    if(activeMenu.value === 'sys:user:list' || activeMenu.value === 'UserManagement') return UserManagement
    if(activeMenu.value === 'sys:role:list' || activeMenu.value === 'RoleManagement') return RoleManagement
    if(activeMenu.value === 'sys:menu:list' || activeMenu.value === 'MenuManagement') return MenuManagement
    if(activeMenu.value === 'sys:app:list' || activeMenu.value === 'ApplicationManagement') return ApplicationManagement
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
  background-color: var(--app-bg-body);
}

.sidebar {
  background-color: var(--app-sidebar-bg);
  border-right: none; /* Removed border as sidebar is usually dark/distinct */
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  z-index: 10;
  transition: all 0.3s;
}
.brand {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background-color: var(--app-sidebar-bg); /* Ensure brand matches sidebar */
  border-bottom: 1px solid var(--app-sidebar-logo-border);
  color: var(--app-text-sidebar-active); /* Use active color (primary) for brand to pop on white, or just primary text */
  font-weight: 600;
  font-size: 18px;
}
.header-right {
    display: flex;
    align-items: center;
    gap: 16px;
}
.theme-switch {
    cursor: pointer;
    color: var(--app-text-primary);
    display: flex;
    align-items: center;
    padding: 5px;
    border-radius: 4px;
    transition: background 0.3s;
}
.theme-switch:hover {
    background-color: var(--app-border-color-light);
}

.header {
  height: 60px;
  background-color: var(--app-bg-header);
  border-bottom: 1px solid var(--app-border-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  z-index: 9;
}
.user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    color: var(--app-text-primary);
    cursor: pointer;
}
.breadcrumb {
    color: var(--app-text-regular);
    font-size: 14px;
}

.main-content {
  padding: 20px;
  background-color: var(--app-bg-body);
  overflow-y: auto;
}

/* Sidebar Menu Overrides */
:deep(.el-menu) {
    border-right: none;
    background-color: transparent !important;
}
:deep(.el-menu-item), :deep(.el-sub-menu__title) {
    color: var(--app-text-sidebar);
}

/* Active State */
:deep(.el-menu-item.is-active) {
    color: var(--app-text-sidebar-active);
    background-color: var(--app-sidebar-hover-bg); /* Use usage of light background for active state in light mode */
    border-right: 3px solid var(--app-text-sidebar-active); /* Optional: add a border indicator */
}
/* Hover State */
:deep(.el-menu-item:hover), :deep(.el-sub-menu__title:hover) {
    color: var(--app-sidebar-hover-text);
    background-color: var(--app-sidebar-hover-bg);
}
</style>
