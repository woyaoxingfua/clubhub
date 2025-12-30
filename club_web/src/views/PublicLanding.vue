<template>
  <div class="public-landing">
    <!-- 顶部导航栏 -->
    <div class="navbar">
      <div class="nav-container">
        <div class="nav-left">
          <img src="https://img.icons8.com/clouds/100/000000/school.png" alt="Logo" class="nav-logo"/>
          <h1 class="nav-title">校园社团管理系统</h1>
        </div>
        <div class="nav-right">
          <el-button @click="goToLogin" type="primary" plain>登录</el-button>
          <el-button @click="goToRegister" type="primary">注册</el-button>
        </div>
      </div>
    </div>

    <!-- 主要内容区 -->
    <div class="main-content">
      <!-- Hero Section -->
      <div class="hero-section">
        <h1 class="hero-title">探索精彩社团生活</h1>
        <p class="hero-subtitle">发现你感兴趣的社团，参与丰富多彩的校园活动</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="goToClubPortal">
            <el-icon><Collection /></el-icon>
            浏览社团
          </el-button>
          <el-button size="large" @click="goToEventCalendar">
            <el-icon><Calendar /></el-icon>
            活动日历
          </el-button>
        </div>
      </div>

      <!-- 功能卡片 -->
      <div class="feature-cards">
        <el-row :gutter="30">
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card" @click="goToClubPortal">
              <div class="card-icon">
                <el-icon :size="50" color="#409EFF"><OfficeBuilding /></el-icon>
              </div>
              <h3>社团黄页</h3>
              <p>浏览所有社团，了解社团信息、荣誉和活动</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card" @click="goToEventCalendar">
              <div class="card-icon">
                <el-icon :size="50" color="#67C23A"><Calendar /></el-icon>
              </div>
              <h3>活动日历</h3>
              <p>查看即将举办的社团活动，不错过任何精彩</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <div class="feature-card" @click="goToRegister">
              <div class="card-icon">
                <el-icon :size="50" color="#E6A23C"><UserFilled /></el-icon>
              </div>
              <h3>加入我们</h3>
              <p>注册账号，申请加入心仪的社团，开启社团之旅</p>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 统计信息 -->
      <div class="stats-section">
        <el-row :gutter="20">
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ stats.clubCount }}</div>
              <div class="stat-label">活跃社团</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ stats.eventCount }}</div>
              <div class="stat-label">本月活动</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ stats.memberCount }}</div>
              <div class="stat-label">社团成员</div>
            </div>
          </el-col>
          <el-col :xs="12" :sm="6">
            <div class="stat-item">
              <div class="stat-number">{{ stats.noticeCount }}</div>
              <div class="stat-label">最新公告</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>

    <!-- 页脚 -->
    <div class="footer">
      <p>© 2025 校园社团管理系统 - 让社团管理更简单</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Collection, Calendar, OfficeBuilding, UserFilled } from '@element-plus/icons-vue'
import request from '@/utils/request'

const router = useRouter()

const stats = ref({
  clubCount: 0,
  eventCount: 0,
  memberCount: 0,
  noticeCount: 0
})

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const goToClubPortal = () => {
  router.push('/portal/clubs')
}

const goToEventCalendar = () => {
  router.push('/portal/calendar')
}

// 加载统计数据（可选）
onMounted(() => {
  // 加载社团数量
  request.get('/club/list').then(res => {
    stats.value.clubCount = res.data?.length || 0
  }).catch(() => {
    stats.value.clubCount = 0
  })

  // 这里可以添加其他统计数据的加载
  // 由于没有专门的统计接口，我们先用默认值
  stats.value.eventCount = 0
  stats.value.memberCount = 0
  stats.value.noticeCount = 0
})
</script>

<style scoped>
.public-landing {
  min-height: 100vh;
  background: linear-gradient(to bottom, #f5f7fa 0%, #ffffff 100%);
}

/* 导航栏 */
.navbar {
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 15px 0;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.nav-right {
  display: flex;
  gap: 10px;
}

/* 主要内容 */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

/* Hero Section */
.hero-section {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  color: white;
  margin-bottom: 60px;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 20px;
  opacity: 0.95;
  margin-bottom: 40px;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
}

.hero-actions .el-button {
  padding: 15px 40px;
  font-size: 16px;
  background: white;
  color: #667eea;
  border: none;
}

.hero-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

/* 功能卡片 */
.feature-cards {
  margin-bottom: 60px;
}

.feature-card {
  background: white;
  border-radius: 15px;
  padding: 40px 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
  height: 100%;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.card-icon {
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 15px;
}

.feature-card p {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

/* 统计信息 */
.stats-section {
  background: white;
  border-radius: 15px;
  padding: 40px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  margin-bottom: 40px;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-number {
  font-size: 42px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  color: #666;
}

/* 页脚 */
.footer {
  text-align: center;
  padding: 30px 20px;
  color: #999;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .nav-title {
    font-size: 16px;
  }
  
  .stat-number {
    font-size: 32px;
  }
}
</style>
