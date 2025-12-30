<template>
  <div class="club-detail-container" v-loading="loading">
    <el-page-header @back="goBack" title="返回社团黄页">
      <template #content>
        <span class="page-title">{{ club.clubName }}</span>
      </template>
    </el-page-header>

    <div class="club-header">
      <img :src="club.logoUrl || 'https://img.icons8.com/clouds/200/000000/community.png'" class="club-logo-big" />
      <div class="club-basic-info">
        <h1>{{ club.clubName }}</h1>
        <div class="tags">
          <el-tag type="success">{{ club.category || '综合类' }}</el-tag>
          <el-tag type="info">{{ club.deptName || '校级社团' }}</el-tag>
        </div>
        <div class="stats">
          <span>👥 {{ memberCount }} 名成员</span>
          <span style="margin-left: 20px">🎯 {{ recentEvents.length }} 个近期活动</span>
        </div>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card title="社团简介" style="margin-bottom: 20px">
          <template #header><h3>📖 社团简介</h3></template>
          <p style="line-height: 1.8">{{ club.description || '暂无简介' }}</p>
        </el-card>

        <el-card>
          <template #header><h3>🎉 近期活动</h3></template>
          <el-timeline>
            <el-timeline-item v-for="event in recentEvents" :key="event.eventId" :timestamp="formatTime(event.startTime)">
              <el-link type="primary" @click="$router.push(`/event/list`)">{{ event.title }}</el-link>
              <p style="color: #909399; margin: 5px 0">{{ event.location }}</p>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-if="recentEvents.length === 0" description="暂无近期活动" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card style="margin-bottom: 20px">
          <template #header><h3>👔 组织架构</h3></template>
          <div class="org-item">
            <span class="label">社长：</span>
            <span>{{ club.presidentName || '待确定' }}</span>
          </div>
          <div class="org-item">
            <span class="label">指导老师：</span>
            <span>{{ club.advisorName || '待确定' }}</span>
          </div>
          <div class="org-item" v-if="club.deptName">
            <span class="label">所属院系：</span>
            <span>{{ club.deptName }}</span>
          </div>
        </el-card>

        <el-card style="margin-bottom: 20px" v-if="club.honors">
          <template #header><h3>🏆 社团荣誉</h3></template>
          <div style="line-height: 2; white-space: pre-line; color: #606266;">
            {{ club.honors }}
          </div>
        </el-card>

        <el-card style="margin-bottom: 20px">
          <template #header><h3>📞 联系方式</h3></template>
          <div class="contact-item" v-if="presidentContact.phone">
            <el-icon><Phone /></el-icon>
            <span>{{ presidentContact.phone }}</span>
          </div>
          <div class="contact-item" v-if="presidentContact.email">
            <el-icon><Message /></el-icon>
            <span>{{ presidentContact.email }}</span>
          </div>
          <el-empty v-if="!presidentContact.phone && !presidentContact.email" 
                    description="暂无联系方式" 
                    :image-size="60" />
        </el-card>

        <el-button type="primary" size="large" style="width: 100%" @click="handleJoin">
          <el-icon><CirclePlus /></el-icon>
          <span style="margin-left: 5px">申请加入</span>
        </el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Phone, Message, CirclePlus } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const club = ref({})
const memberCount = ref(0)
const recentEvents = ref([])
const presidentContact = ref({}) // 社长联系方式
const clubId = route.params.clubId
const user = JSON.parse(localStorage.getItem('user') || '{}')

const goBack = () => {
  const inPortal = route.path.startsWith('/portal')
  const target = inPortal ? '/portal/clubs' : '/view/clubs'
  router.push(target)
}

const loadData = () => {
  loading.value = true
  request.get(`/club/detail/${clubId}`).then(res => {
    club.value = res.data.club || {}
    memberCount.value = res.data.memberCount || 0
    recentEvents.value = res.data.recentEvents || []
    presidentContact.value = res.data.presidentContact || {}
  }).finally(() => {
    loading.value = false
  })
}

const handleJoin = () => {
  if (!user.userId) {
    ElMessage.warning('请先登录')
    return
  }
  request.post('/member/join', {
    clubId: parseInt(clubId),
    userId: user.userId
  }).then(res => {
    ElMessage.success(res.msg)
    loadData()
  })
}

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.club-detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.club-header {
  display: flex;
  align-items: center;
  padding: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  margin: 20px 0;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  color: white;
}

.club-logo-big {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  margin-right: 40px;
  border: 5px solid white;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
  object-fit: cover;
}

.club-basic-info h1 {
  margin: 0 0 15px 0;
  font-size: 32px;
  color: white;
  font-weight: bold;
}

.tags {
  margin: 15px 0;
}

.tags .el-tag {
  margin-right: 10px;
  font-size: 13px;
}

.stats {
  margin-top: 20px;
  font-size: 15px;
  color: rgba(255,255,255,0.95);
  font-weight: 500;
}

.el-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
}

.el-card :deep(.el-card__header) {
  background-color: #f8f9fa;
  border-bottom: 2px solid #e9ecef;
}

.el-card h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
  font-weight: 600;
}

.org-item {
  margin-bottom: 12px;
  font-size: 14px;
  padding: 8px 0;
}

.org-item .label {
  color: #909399;
  margin-right: 10px;
  font-weight: 500;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.contact-item:hover {
  background-color: #f0f9ff;
}

.contact-item .el-icon {
  color: #409eff;
  font-size: 16px;
}

.el-timeline {
  padding: 10px 0;
}

:deep(.el-timeline-item__timestamp) {
  color: #909399;
  font-size: 13px;
}

:deep(.el-link) {
  font-size: 15px;
  font-weight: 500;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}
</style>
