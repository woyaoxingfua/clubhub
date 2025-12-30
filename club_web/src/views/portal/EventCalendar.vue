<template>
  <div class="event-calendar-container">
    <div class="calendar-header">
      <h2>📅 活动日历</h2>
      <div class="legend">
        <el-tag type="success" size="small">✓ 已发布活动</el-tag>
        <span style="margin-left: 10px; color: #909399; font-size: 13px">
          点击日期查看当天活动详情
        </span>
      </div>
    </div>
    
    <el-calendar v-model="currentDate">
      <template #date-cell="{ data }">
        <div class="calendar-day" @click="showDayEvents(data.day)">
          <span class="day-number">{{ data.day.split('-').slice(-1)[0] }}</span>
          <div class="events-indicator" v-if="getDayEvents(data.day).length > 0">
            <el-badge 
              :value="getDayEvents(data.day).length" 
              :type="getDayEvents(data.day).length > 3 ? 'danger' : 'primary'"
              class="item" />
          </div>
        </div>
      </template>
    </el-calendar>

    <!-- 当天活动详情对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="`${selectedDate} 的活动 (${selectedDayEvents.length}个)`" 
      width="700px"
      top="8vh">
      <el-scrollbar max-height="500px">
        <el-timeline>
          <el-timeline-item 
            v-for="event in selectedDayEvents" 
            :key="event.eventId"
            :timestamp="formatTime(event.startTime)"
            placement="top">
            <el-card shadow="hover" class="event-card">
              <div class="event-header">
                <h3>{{ event.title }}</h3>
                <el-tag size="small" type="success">已发布</el-tag>
              </div>
              <div class="event-info">
                <div class="info-item">
                  <el-icon><Location /></el-icon>
                  <span>{{ event.location || '地点待定' }}</span>
                </div>
                <div class="info-item" v-if="event.clubName">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{ event.clubName }}</span>
                </div>
              </div>
              <p class="event-content">{{ event.content || '暂无详情' }}</p>
              <el-button 
                size="small" 
                type="primary" 
                @click="goToEventList">
                查看更多活动
              </el-button>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </el-scrollbar>
      <el-empty v-if="selectedDayEvents.length === 0" description="当天无活动安排" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import { Location, OfficeBuilding } from '@element-plus/icons-vue'

const router = useRouter()
const currentDate = ref(new Date())
const allEvents = ref([])
const dialogVisible = ref(false)
const selectedDate = ref('')
const selectedDayEvents = ref([])

const loadData = () => {
  request.get('/event/calendar').then(res => {
    allEvents.value = res.data || []
  })
}

const getDayEvents = (day) => {
  return allEvents.value.filter(event => {
    if (!event.startTime) return false
    const eventDate = event.startTime.split('T')[0]
    return eventDate === day
  })
}

const showDayEvents = (day) => {
  selectedDate.value = day
  selectedDayEvents.value = getDayEvents(day)
  if (selectedDayEvents.value.length > 0) {
    dialogVisible.value = true
  }
}

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''
}

const goToEventList = () => {
  dialogVisible.value = false
  router.push('/event/list')
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.event-calendar-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.legend {
  display: flex;
  align-items: center;
}

.calendar-day {
  height: 100%;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5px;
  transition: background-color 0.3s;
}

.calendar-day:hover {
  background-color: #f0f9ff;
}

.day-number {
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.events-indicator {
  margin-top: 5px;
}

:deep(.el-calendar-day) {
  height: 90px;
}

.event-card {
  margin-bottom: 10px;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.event-header h3 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.event-info {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  color: #606266;
}

.info-item .el-icon {
  color: #409eff;
}

.event-content {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
  margin: 10px 0;
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}
</style>
