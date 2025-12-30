<template>
  <div class="home-container">
    <div class="welcome-card">
      <div class="text-content">
        <h2>👋 欢迎回来，{{ user.realName }}！</h2>
        <p>今天是 {{ currentDate }}，准备好处理社团事务了吗？</p>
      </div>
      <img src="https://img.icons8.com/clouds/200/000000/desk.png" class="illustration" alt="welcome" />
    </div>

    <el-row :gutter="20" class="data-row">
      <el-col :span="6">
        <el-card shadow="hover" class="data-card bg-blue" @click="$router.push('/event/list')">
          <div class="card-content">
            <el-icon><Trophy /></el-icon>
            <div>
              <div class="num">{{ eventCount }}</div>
              <div class="label">活动总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card bg-green" @click="$router.push('/club/list')">
          <div class="card-content">
            <el-icon><School /></el-icon>
            <div>
              <div class="num">{{ clubCount }}</div>
              <div class="label">入驻社团</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card bg-orange" @click="$router.push('/member/management')">
          <div class="card-content">
            <el-icon><User /></el-icon>
            <div>
              <div class="num">{{ memberCount }}</div>
              <div class="label">活跃成员</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="data-card bg-purple" @click="$router.push('/event/list')">
          <div class="card-content">
            <el-icon><Timer /></el-icon>
            <div>
              <div class="num">{{ pendingCount }}</div>
              <div class="label">待办审批</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告栏 -->
    <el-card shadow="never" class="notice-card" style="margin-bottom: 20px;">
      <template #header>
        <div class="card-header">
          <span>📢 最新公告</span>
          <el-button link type="primary" @click="$router.push('/notice/list')">查看更多</el-button>
        </div>
      </template>
      <el-table :data="noticeList" style="width: 100%" :show-header="false" size="small">
        <el-table-column prop="title" label="标题">
          <template #default="scope">
            <span style="font-weight: bold;">{{ scope.row.title }}</span>
            <span style="margin-left: 10px; color: #999;">- {{ scope.row.content.substring(0, 50) }}...</span>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="时间" width="180" align="right" />
      </el-table>
    </el-card>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>📊 活动审批状态分布</span></template>
          <div id="pieChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card shadow="never">
          <template #header><span>🏢 社团分类统计</span></template>
          <div id="barChart" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import * as echarts from 'echarts' // 引入 ECharts
import request from '@/utils/request'
import { Trophy, School, User, Timer } from '@element-plus/icons-vue'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const currentDate = new Date().toLocaleDateString()

const eventCount = ref(0)
const clubCount = ref(0)
const memberCount = ref(0)
const pendingCount = ref(0)
const noticeList = ref([])

// 1. 初始化饼图 (活动状态)
const initPieChart = (events) => {
  const chartDom = document.getElementById('pieChart')
  const myChart = echarts.init(chartDom)
  
  // 统计各个状态的数量
  let statusCounts = { 0: 0, 1: 0, 2: 0, 3: 0, 4: 0 }
  events.forEach(e => {
    if (statusCounts[e.auditStatus] !== undefined) {
      statusCounts[e.auditStatus]++
    }
  })

  const option = {
    tooltip: { trigger: 'item' },
    legend: { bottom: '5%', left: 'center' },
    series: [
      {
        name: '活动状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        data: [
          { value: statusCounts[0], name: '待提交' },
          { value: statusCounts[1], name: '指导老师审核中' },
          { value: statusCounts[2], name: '院系审核中' },
          { value: statusCounts[3], name: '已发布' },
          { value: statusCounts[4], name: '已驳回' }
        ]
      }
    ]
  }
  myChart.setOption(option)
}

// 2. 初始化柱状图 (社团分类)
const initBarChart = (clubs) => {
  const chartDom = document.getElementById('barChart')
  const myChart = echarts.init(chartDom)

  // 统计分类
  let categories = {}
  clubs.forEach(c => {
    const cat = c.category || '未分类'
    categories[cat] = (categories[cat] || 0) + 1
  })

  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { 
      type: 'category', 
      data: Object.keys(categories),
      axisLabel: { interval: 0 } 
    },
    yAxis: { type: 'value' },
    series: [
      {
        data: Object.values(categories),
        type: 'bar',
        showBackground: true,
        backgroundStyle: { color: 'rgba(180, 180, 180, 0.2)' },
        itemStyle: { color: '#5470c6' }
      }
    ]
  }
  myChart.setOption(option)
}

// 3. 加载所有数据
const loadAllData = async () => {
  // 并行发多个请求，加快速度
  const [eventRes, clubRes, noticeRes, statsRes] = await Promise.all([
    request.get('/event/list'),
    request.get('/club/list'),
    request.get('/notice/list'),
    request.get('/statistics/overview')
  ])

  const events = eventRes.data || []
  const clubs = clubRes.data || []
  noticeList.value = (noticeRes.data || []).slice(0, 5) // 取前5条
  const stats = statsRes.data || {}

  // 更新数字卡片
  eventCount.value = events.length
  clubCount.value = clubs.length
  // 使用真实成员数据
  memberCount.value = stats.totalMembers || 0
  // 统计待办：如果是管理员，统计所有非'已发布/已驳回'的；如果是普通学生，统计自己的'待提交'
  pendingCount.value = events.filter(e => e.auditStatus < 3).length

  // 渲染图表
  initPieChart(events)
  initBarChart(clubs)
}

onMounted(() => {
  loadAllData()
})
</script>

<style scoped>
.home-container {
  padding: 0;
}
/* 欢迎头部 */
.welcome-card {
  background: white;
  padding: 30px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.welcome-card h2 {
  margin: 0 0 10px 0;
  color: #303133;
}
.welcome-card p {
  color: #909399;
  margin: 0;
}
.illustration {
  height: 100px;
}

/* 数据卡片 */
.data-row {
  margin-bottom: 20px;
}
.data-card {
  color: white;
  border: none;
  cursor: pointer;
  transition: transform 0.3s;
}
.data-card:hover {
  transform: translateY(-5px);
}
.card-content {
  display: flex;
  align-items: center;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-content .el-icon {
  font-size: 48px;
  margin-right: 20px;
  opacity: 0.8;
}
.num {
  font-size: 24px;
  font-weight: bold;
}
.label {
  font-size: 12px;
  opacity: 0.9;
}

/* 颜色类 */
.bg-blue { background: linear-gradient(135deg, #36d1dc, #5b86e5); }
.bg-green { background: linear-gradient(135deg, #11998e, #38ef7d); }
.bg-orange { background: linear-gradient(135deg, #fce38a, #f38181); }
.bg-purple { background: linear-gradient(135deg, #c33764, #1d2671); }
</style>