<template>
  <div class="portal-layout">
    <!-- 顶部导航栏 -->
    <div class="portal-navbar">
      <div class="nav-container">
        <div class="nav-left">
          <img src="https://img.icons8.com/clouds/100/000000/school.png" alt="Logo" class="nav-logo"/>
          <h1 class="nav-title">校园社团管理系统</h1>
        </div>
        <div class="nav-center">
          <el-menu mode="horizontal" :default-active="activeMenu" class="portal-menu">
            <el-menu-item index="/public" @click="goTo('/public')">
              <el-icon><HomeFilled /></el-icon>
              首页
            </el-menu-item>
            <el-menu-item index="/portal/clubs" @click="goTo('/portal/clubs')">
              <el-icon><OfficeBuilding /></el-icon>
              社团黄页
            </el-menu-item>
            <el-menu-item index="/portal/calendar" @click="goTo('/portal/calendar')">
              <el-icon><Calendar /></el-icon>
              活动日历
            </el-menu-item>
          </el-menu>
        </div>
        <div class="nav-right">
          <el-button @click="goToLogin" type="primary" plain>登录</el-button>
          <el-button @click="goToRegister" type="primary">注册</el-button>
        </div>
      </div>
    </div>

    <!-- 页面内容 -->
    <div class="portal-content">
      <router-view />
    </div>

    <!-- 页脚 -->
    <div class="portal-footer">
      <p>© 2025 校园社团管理系统 - 让社团管理更简单</p>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { HomeFilled, OfficeBuilding, Calendar } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const activeMenu = computed(() => route.path)

const goTo = (path) => {
  router.push(path)
}

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.portal-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 导航栏 */
.portal-navbar {
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 70px;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-logo {
  width: 50px;
  height: 50px;
}

.nav-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.nav-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.portal-menu {
  border-bottom: none;
  background: transparent;
}

.nav-right {
  display: flex;
  gap: 10px;
}

/* 内容区域 */
.portal-content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

/* 页脚 */
.portal-footer {
  background: white;
  border-top: 1px solid #e4e7ed;
  padding: 20px;
  text-align: center;
  color: #999;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-title {
    font-size: 14px;
  }
  
  .nav-logo {
    width: 40px;
    height: 40px;
  }
  
  .portal-menu {
    display: none;
  }
  
  .nav-container {
    height: 60px;
  }
}
</style>
