<template>
  <div class="recruit-app-container">
    <div class="toolbar">
      <div class="title-box">
        <h2>📄 入社申请审核</h2>
        <span class="sub-title" v-if="planId">审批学生提交的入社申请</span>
        <span class="sub-title" v-else>显示所有我可以管理的招新申请</span>
      </div>
      <div>
        <el-select v-if="!planId" v-model="selectedPlanId" placeholder="筛选招新计划" clearable style="width: 200px; margin-right: 10px" @change="loadData">
          <el-option label="全部计划" :value="null" />
          <el-option v-for="plan in myPlans" :key="plan.planId" :label="plan.planTitle" :value="plan.planId" />
        </el-select>
        <el-button @click="$router.push('/recruit/plans')">返回计划列表</el-button>
      </div>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="appId" label="ID" width="60" align="center" />
      <el-table-column prop="planTitle" label="招新计划" min-width="150" v-if="!planId" />
      <el-table-column prop="studentName" label="学生姓名" width="120" />
      <el-table-column prop="selfIntro" label="自我介绍" show-overflow-tooltip min-width="200" />
      <el-table-column prop="resumeFileUrl" label="简历附件" width="120" align="center">
        <template #default="scope">
          <el-button 
            v-if="scope.row.resumeFileUrl" 
            type="primary" 
            link 
            size="small"
            @click="openResume(scope.row.resumeFileUrl)"
          >
            <el-icon style="margin-right: 3px;"><document /></el-icon>
            查看简历
          </el-button>
          <span v-else style="color: #999; font-size: 12px;">未上传</span>
        </template>
      </el-table-column>
      <el-table-column prop="applyTime" label="申请时间" width="170">
        <template #default="scope">
          {{ formatTime(scope.row.applyTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === 0">
            <el-button size="small" type="success" @click="handleAudit(scope.row, 1)">录用</el-button>
            <el-button size="small" type="danger" @click="handleAudit(scope.row, 2)">婉拒</el-button>
          </template>
          <span v-else style="color: #999; font-size: 12px">已审批</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const planId = route.query.planId // 可能为 undefined（从侧边栏进入）
const selectedPlanId = ref(null) // 用于下拉筛选
const user = JSON.parse(localStorage.getItem('user') || '{}')
const myClubs = ref([]) // 我担任社长的社团
const myPlans = ref([]) // 我可以管理的招新计划
const allPlans = ref([]) // 所有招新计划

// 判断是否是管理员
const isAdmin = user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN'

// 初始化数据：加载我管理的社团和计划
const initData = async () => {
  try {
    // 1. 加载所有招新计划
    const planRes = await request.get('/recruit/plan/list')
    allPlans.value = planRes.data || []
    
    // 2. 如果是管理员，可以管理所有计划
    if (isAdmin) {
      myPlans.value = allPlans.value
      return true
    }
    
    // 3. 如果是社长，加载我管理的社团
    const myClubsRes = await request.get('/club/my-clubs', { params: { userId: user.userId } })
    myClubs.value = myClubsRes.data || []
    
    // 4. 过滤出我可以管理的招新计划
    const myClubIds = myClubs.value.map(c => c.clubId)
    myPlans.value = allPlans.value.filter(plan => myClubIds.includes(plan.clubId))
    
    console.log('[DEBUG] 我可以管理的招新计划数量:', myPlans.value.length)
    
    // 5. 检查权限
    if (myClubs.value.length === 0 && !isAdmin) {
      ElMessage.error('权限不足：只有管理员或社长才能审批申请')
      router.push('/recruit/plans')
      return false
    }
    
    return true
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('加载数据失败')
    return false
  }
}

const loadData = () => {
  loading.value = true
  
  // 确定要查询的 planId
  let queryPlanId = planId // 如果是从招新计划页面跳转过来的
  if (!planId && selectedPlanId.value) {
    queryPlanId = selectedPlanId.value // 如果是从侧边栏进入，使用下拉选择的
  }
  
  // 如果没有指定 planId，则查询所有我可以管理的计划的申请
  const params = queryPlanId ? { planId: queryPlanId } : {}
  
  request.get('/recruit/application/list', { params }).then(res => {
    let applications = res.data || []
    
    // 如果没有指定 planId，需要过滤出我可以管理的申请
    if (!queryPlanId && !isAdmin) {
      const myPlanIds = myPlans.value.map(p => p.planId)
      applications = applications.filter(app => myPlanIds.includes(app.planId))
    }
    
    // 添加计划名称（用于显示）
    applications.forEach(app => {
      const plan = allPlans.value.find(p => p.planId === app.planId)
      app.planTitle = plan ? plan.planTitle : '未知计划'
    })
    
    tableData.value = applications
  }).finally(() => loading.value = false)
}

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ') : ''
}

const getStatusText = (status) => {
  const map = { 0: '待审核', 1: '已录用', 2: '已婉拒' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

const handleAudit = (row, status) => {
  request.post('/recruit/audit', {
    appId: row.appId,
    status: status
  }).then(res => {
    ElMessage.success(res.msg)
    loadData()
  })
}

// 打开简历（在新窗口中）
const openResume = (url) => {
  if (!url) {
    ElMessage.warning('该申请未上传简历')
    return
  }
  window.open(url, '_blank')
}

onMounted(async () => {
  // 先初始化数据（加载社团和计划信息）
  const hasPermission = await initData()
  
  if (!hasPermission) {
    return
  }
  
  // 加载申请列表
  loadData()
})
</script>

<style scoped>
.recruit-app-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}
.title-box h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}
.sub-title {
  font-size: 13px;
  color: #909399;
  margin-top: 5px;
  display: block;
}
</style>
