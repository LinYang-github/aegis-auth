<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="brand">
        Aegis Auth
      </div>
      <nav class="nav-links">
        <router-link to="/" class="nav-item" active-class="active">
          <span>👥</span> User Management
        </router-link>
        <!-- Add more links here later -->
      </nav>
      <div class="logout-section">
        <button @click="handleLogout" class="logout-btn">
           Sign Out
        </button>
      </div>
    </aside>
    
    <main class="main-content">
      <header class="top-bar">
        <div class="breadcrumb">System / User Management</div>
        <div class="user-profile">
          <span>Administrator</span>
          <div class="avatar">A</div>
        </div>
      </header>
      
      <div class="content-area">
        <router-view></router-view>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

const router = useRouter();

const handleLogout = () => {
    // In a real app, call logout API
    router.push('/login');
};
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  width: 100vw;
  background-color: #f5f7fa;
  color: #333;
}

.sidebar {
  width: 250px;
  background-color: #1a202c;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.brand {
  height: 64px;
  display: flex;
  align-items: center;
  padding-left: 1.5rem;
  font-size: 1.25rem;
  font-weight: bold;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.nav-links {
  flex: 1;
  padding: 1rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 0.75rem 1.5rem;
  color: rgba(255,255,255,0.7);
  text-decoration: none;
  transition: all 0.3s;
}

.nav-item:hover, .nav-item.active {
  background-color: rgba(255,255,255,0.1);
  color: #fff;
  border-right: 3px solid #00C9FF;
}

.nav-item span {
  margin-right: 0.75rem;
}

.logout-section {
  padding: 1rem;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.logout-btn {
  width: 100%;
  padding: 0.5rem;
  background: transparent;
  border: 1px solid rgba(255,255,255,0.2);
  color: #fff;
  border-radius: 4px;
  cursor: pointer;
  transition: 0.3s;
}

.logout-btn:hover {
  background: rgba(255,255,255,0.1);
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.top-bar {
  height: 64px;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.breadcrumb {
  font-size: 0.9rem;
  color: #718096;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.avatar {
  width: 32px;
  height: 32px;
  background-color: #00C9FF;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
}

.content-area {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}
</style>
