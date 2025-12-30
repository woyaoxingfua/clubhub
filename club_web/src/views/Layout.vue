<template>
  <div class="common-layout">
    <el-container>
      <el-aside width="220px" class="aside-menu">
        <div class="logo-box">
          <img src="https://img.icons8.com/clouds/100/000000/school.png" alt="Logo"/>
          <span>社团管理系统</span>
        </div>
        
        <el-menu
          active-text-color="#ffd04b"
          background-color="#304156"
          text-color="#fff"
          :default-active="activePath"
          router
          class="el-menu-vertical">
          
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <span>首页概览</span>
          </el-menu-item>
          
          <el-sub-menu index="1">
            <template #title>
              <el-icon><Trophy /></el-icon>
              <span>活动管理</span>
            </template>
            <el-menu-item index="/event/list">活动列表 & 审批</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/club/list">
            <el-icon><Files /></el-icon>
            <span>社团查询</span>
          </el-menu-item>

          <el-menu-item index="/notice/list">
            <el-icon><Bell /></el-icon>
            <span>公告管理</span>
          </el-menu-item>

          <el-sub-menu index="2">
            <template #title>
              <el-icon><UserFilled /></el-icon>
              <span>招新管理</span>
            </template>
            <el-menu-item index="/recruit/plans">招新计划</el-menu-item>
            <el-menu-item index="/recruit/applications" v-if="canManageRecruit">申请审核</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="4" v-if="canManageRecruit">
            <template #title>
              <el-icon><Avatar /></el-icon>
              <span>成员管理</span>
            </template>
            <el-menu-item index="/member/management">成员花名册</el-menu-item>
            <el-menu-item index="/member/contacts">社团通讯录</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/finance/list" v-if="canManageRecruit">
            <el-icon><Money /></el-icon>
            <span>财务管理</span>
          </el-menu-item>

          <el-menu-item index="/message/list">
            <el-icon><ChatDotRound /></el-icon>
            <span>站内消息</span>
            <el-badge v-if="unreadCount > 0" :value="unreadCount" class="message-badge" />
          </el-menu-item>

          <el-menu-item index="/statistics/dashboard" v-if="canManageRecruit">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </el-menu-item>

          <el-sub-menu index="3">
            <template #title>
              <el-icon><Promotion /></el-icon>
              <span>社团门户</span>
            </template>
            <el-menu-item index="/view/clubs">社团黄页</el-menu-item>
            <el-menu-item index="/view/calendar">活动日历</el-menu-item>
          </el-sub-menu>

        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="header">
          <div class="header-left">
            当前用户：{{ user.realName }} ({{ user.roleKey }})
          </div>
          <el-button type="danger" size="small" @click="logout">退出登录</el-button>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { House, Trophy, Files, UserFilled, Bell, Money, Promotion, Avatar, ChatDotRound, DataAnalysis } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const user = ref({})
const activePath = ref(route.path)
const myClubs = ref([]) // 我担任社长的社团列表
const unreadCount = ref(0) // 未读消息数量

const isAdmin = computed(() => {
  return user.value.roleKey === 'SYS_ADMIN' || user.value.roleKey === 'DEPT_ADMIN'
})

const isPresident = computed(() => {
  return myClubs.value.length > 0
})

const canManageRecruit = computed(() => {
  return isAdmin.value || isPresident.value
})

onMounted(async () => {
  // 从缓存读取用户信息
  const userStr = localStorage.getItem('user')
  if (userStr) {
    user.value = JSON.parse(userStr)
    
    // 加载我管理的社团（用于判断是否是社长）
    if (user.value.userId) {
      try {
        const res = await request.get('/club/my-clubs', { params: { userId: user.value.userId } })
        myClubs.value = res.data || []
        console.log('[DEBUG] Layout - 我管理的社团数量:', myClubs.value.length)
      } catch (error) {
        console.error('加载社团信息失败:', error)
      }
      
      // 加载未读消息数量
      loadUnreadCount()
    }
  } else {
    router.push('/login')
  }
})

// 加载未读消息数量
const loadUnreadCount = async () => {
  try {
    const res = await request.get('/message/unread/count')
    unreadCount.value = res.data || 0
  } catch (error) {
    console.error('加载未读消息数量失败:', error)
  }
}

// 每30秒刷新一次未读数量
setInterval(() => {
  if (user.value.userId) {
    loadUnreadCount()
  }
}, 30000)

const logout = () => {
  localStorage.removeItem('user')
  localStorage.removeItem('userId') // 也要清除userId
  router.push('/login').then(() => {
    // 强制刷新页面确保状态清空
    window.location.reload()
  })
}
</script>

<style scoped>
.common-layout, .el-container {
  height: 100vh;
}
.aside-menu {
  background-color: #304156;
  color: white;
}
.logo-box {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  background-color: #2b3649;
}
.logo-box img {
  width: 30px;
  margin-right: 10px;
}
.el-menu-vertical {
  border-right: none;
}
.header {
  background-color: #fff;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
.message-badge {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
}
</style>