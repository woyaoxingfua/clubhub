<template>
  <div class="dashboard-container">
    <div class="page-header">
      <h2>数据统计Dashboard</h2>
      <el-button @click="refreshData" :loading="loading" type="primary" :icon="Refresh">刷新数据</el-button>
    </div>

    <!-- 系统总览卡片 -->
    <el-row :gutter="20" class="overview-cards">
      <el-col :xs="24" :sm="12" :md="6" v-for="card in overviewCards" :key="card.key">
        <el-card shadow="hover" class="overview-card">
          <div class="card-icon" :style="{ background: card.color }">
            <component :is="card.icon" style="width: 32px; height: 32px; color: white;" />
          </div>
          <div class="card-content">
            <div class="card-title">{{ card.title }}</div>
            <div class="card-value">{{ card.value }}</div>
            <div class="card-subtitle" v-if="card.subtitle">{{ card.subtitle }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <!-- 成员增长趋势 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>成员增长趋势（近12个月）</span>
            </div>
          </template>
          <div ref="memberGrowthChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>

      <!-- 社团成员分布 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>社团成员分布</span>
            </div>
          </template>
          <div ref="memberDistributionChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 活动统计 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>活动统计</span>
            </div>
          </template>
          <div ref="eventStatsChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>

      <!-- 财务统计 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>财务统计</span>
            </div>
          </template>
          <div ref="financeStatsChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts-row">
      <!-- 招新统计 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>招新统计</span>
            </div>
          </template>
          <div ref="recruitStatsChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>

      <!-- 社团活跃度排名 -->
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>社团活跃度排名 Top 10</span>
            </div>
          </template>
          <div ref="clubRankingChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 活动参与率 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>活动参与率趋势（近6个月）</span>
            </div>
          </template>
          <div ref="participationRateChart" style="width: 100%; height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, User, House, Calendar, Money } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const loading = ref(false)
const overviewCards = ref([])
const memberGrowthChart = ref(null)
const memberDistributionChart = ref(null)
const eventStatsChart = ref(null)
const financeStatsChart = ref(null)
const recruitStatsChart = ref(null)
const clubRankingChart = ref(null)
const participationRateChart = ref(null)

let charts = []

// 获取Dashboard数据
const fetchDashboardData = async () => {
  loading.value = true
  try {
    const response = await request.get('/statistics/dashboard')
    if (response.code === 200) {
      const data = response.data
      
      // 更新总览卡片
      updateOverviewCards(data.overview)
      
      // 更新各个图表
      initMemberGrowthChart(data.memberGrowth)
      initMemberDistributionChart(data.memberDistribution)
      initEventStatsChart(data.eventStats)
      initFinanceStatsChart(data.financeStats)
      initRecruitStatsChart(data.recruitStats)
      initClubRankingChart(data.clubRanking)
      initParticipationRateChart(data.participationRate)
      
      ElMessage.success('数据加载成功')
    } else {
      ElMessage.error(response.msg || '数据加载失败')
    }
  } catch (error) {
    console.error('获取Dashboard数据失败:', error)
    ElMessage.error('数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 更新总览卡片
const updateOverviewCards = (overview) => {
  overviewCards.value = [
    {
      key: 'clubs',
      title: '社团总数',
      value: overview.totalClubs || 0,
      subtitle: `活跃: ${overview.activeClubs || 0}`,
      icon: House,
      color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
    },
    {
      key: 'members',
      title: '成员总数',
      value: overview.totalMembers || 0,
      subtitle: `本月新增: ${overview.newMembersThisMonth || 0}`,
      icon: User,
      color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
    },
    {
      key: 'events',
      title: '活动总数',
      value: overview.totalEvents || 0,
      subtitle: `已审批: ${overview.approvedEvents || 0}`,
      icon: Calendar,
      color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
    },
    {
      key: 'applications',
      title: '招新申请',
      value: overview.totalApplications || 0,
      subtitle: `待审核: ${overview.pendingApplications || 0}`,
      icon: Money,
      color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)'
    }
  ]
}

// 成员增长趋势图
const initMemberGrowthChart = (data) => {
  if (!memberGrowthChart.value) return
  
  const chart = echarts.init(memberGrowthChart.value)
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新增成员', '退出成员']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: data.months || []
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '新增成员',
        type: 'line',
        smooth: true,
        data: data.newMembers || [],
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0.1)' }
          ])
        }
      },
      {
        name: '退出成员',
        type: 'line',
        smooth: true,
        data: data.quitMembers || [],
        itemStyle: {
          color: '#F56C6C'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.1)' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
  charts.push(chart)
}

// 社团成员分布饼图
const initMemberDistributionChart = (data) => {
  if (!memberDistributionChart.value) return
  
  const chart = echarts.init(memberDistributionChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      type: 'scroll'
    },
    series: [
      {
        name: '成员分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: data.pieData || []
      }
    ]
  }
  chart.setOption(option)
  charts.push(chart)
}

// 活动统计柱状图
const initEventStatsChart = (data) => {
  if (!eventStatsChart.value) return
  
  const chart = echarts.init(eventStatsChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['待审核', '已通过', '已驳回']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: ['活动状态']
    },
    series: [
      {
        name: '待审核',
        type: 'bar',
        stack: 'total',
        data: [data.pending || 0],
        itemStyle: { color: '#E6A23C' }
      },
      {
        name: '已通过',
        type: 'bar',
        stack: 'total',
        data: [data.approved || 0],
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '已驳回',
        type: 'bar',
        stack: 'total',
        data: [data.rejected || 0],
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }
  chart.setOption(option)
  charts.push(chart)
}

// 财务统计图
const initFinanceStatsChart = (data) => {
  if (!financeStatsChart.value) return
  
  const chart = echarts.init(financeStatsChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['收入', '支出']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['财务总览']
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: [data.totalIncome || 0],
        itemStyle: { color: '#67C23A' }
      },
      {
        name: '支出',
        type: 'bar',
        data: [data.totalExpense || 0],
        itemStyle: { color: '#F56C6C' }
      }
    ]
  }
  chart.setOption(option)
  charts.push(chart)
}

// 招新统计图
const initRecruitStatsChart = (data) => {
  if (!recruitStatsChart.value) return
  
  const chart = echarts.init(recruitStatsChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 10
    },
    series: [
      {
        name: '招新统计',
        type: 'pie',
        radius: '60%',
        data: [
          { value: data.accepted || 0, name: '已录用', itemStyle: { color: '#67C23A' } },
          { value: data.rejected || 0, name: '已拒绝', itemStyle: { color: '#F56C6C' } },
          { value: data.pending || 0, name: '待审核', itemStyle: { color: '#E6A23C' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  chart.setOption(option)
  charts.push(chart)
}

// 社团活跃度排名
const initClubRankingChart = (data) => {
  if (!clubRankingChart.value) return
  
  const chart = echarts.init(clubRankingChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value'
    },
    yAxis: {
      type: 'category',
      data: (data.clubNames || []).reverse(),
      axisLabel: {
        interval: 0,
        formatter: function(value) {
          return value.length > 8 ? value.substring(0, 8) + '...' : value
        }
      }
    },
    series: [
      {
        name: '活跃度分数',
        type: 'bar',
        data: (data.scores || []).reverse(),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#83bff6' },
            { offset: 0.5, color: '#188df0' },
            { offset: 1, color: '#188df0' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}'
        }
      }
    ]
  }
  chart.setOption(option)
  charts.push(chart)
}

// 活动参与率趋势
const initParticipationRateChart = (data) => {
  if (!participationRateChart.value) return
  
  const chart = echarts.init(participationRateChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: function(params) {
        return params[0].name + '<br/>平均报名人数: ' + params[0].value
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.months || []
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}'
      }
    },
    series: [
      {
        name: '参与率',
        type: 'line',
        smooth: true,
        data: data.participationRates || [],
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
          ])
        }
      }
    ]
  }
  chart.setOption(option)
  charts.push(chart)
}

// 刷新数据
const refreshData = () => {
  fetchDashboardData()
}

// 窗口大小改变时重绘图表
const handleResize = () => {
  charts.forEach(chart => {
    if (chart && !chart.isDisposed()) {
      chart.resize()
    }
  })
}

onMounted(() => {
  fetchDashboardData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach(chart => {
    if (chart && !chart.isDisposed()) {
      chart.dispose()
    }
  })
  charts = []
})
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.overview-cards {
  margin-bottom: 20px;
}

.overview-card {
  cursor: pointer;
  transition: transform 0.3s;
}

.overview-card:hover {
  transform: translateY(-5px);
}

.overview-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
}

.card-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;
}

.card-content {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.card-subtitle {
  font-size: 12px;
  color: #606266;
}

.charts-row {
  margin-bottom: 20px;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>
