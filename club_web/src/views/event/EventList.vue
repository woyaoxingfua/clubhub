<template>
  <div class="event-list-container">
    <div class="toolbar">
      <div class="title-box">
        <h2>📅 活动管理</h2>
        <span class="sub-title">在这里管理社团活动的申请与审批流程</span>
      </div>
      <div style="display: flex; gap: 10px;">
        <!-- 如果用户看不到发起按钮，提示刷新 -->
        <el-tooltip v-if="!canApply && user.roleKey === 'STUDENT'" 
          content="如果您刚被任命为社长，请刷新页面以加载最新权限" 
          placement="bottom">
          <el-button @click="refreshMyClubs" type="info" size="large">
            <el-icon style="margin-right: 5px"><Refresh /></el-icon> 刷新权限
          </el-button>
        </el-tooltip>
        <el-button v-if="canApply" type="primary" size="large" @click="openApplyDialog">
          <el-icon style="margin-right: 5px"><Plus /></el-icon> 发起活动申请
        </el-button>
      </div>
    </div>

    <el-table :data="tableData" style="width: 100%" v-loading="loading" border stripe>
      <el-table-column prop="eventId" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="活动主题" min-width="150" />
      <el-table-column prop="content" label="活动内容" show-overflow-tooltip min-width="150" />
      <el-table-column prop="location" label="地点" width="120" />
      <el-table-column prop="budget" label="预算(元)" width="100" />
      <el-table-column prop="startTime" label="开始时间" width="170" sortable>
        <template #default="scope">
          {{ scope.row.startTime ? scope.row.startTime.replace('T', ' ') : '' }}
        </template>
      </el-table-column>
      
      <el-table-column label="当前状态" width="140" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.auditStatus)" effect="dark">
            {{ getStatusText(scope.row.auditStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="220" fixed="right" align="center">
        <template #default="scope">
          
          <template v-if="isStudent">
            <!-- 提交审批 -->
            <el-popconfirm 
              v-if="scope.row.auditStatus === 0"
              title="确定要提交给指导老师审批吗？" 
              @confirm="handleSubmit(scope.row.eventId)"
            >
              <template #reference>
                <el-button size="small" type="primary" plain>提交审批</el-button>
              </template>
            </el-popconfirm>

            <!-- 1. 撤销申请 (仅针对自己发起的待审核活动) -->
            <!-- 注意：这里简单假设只有待审核的才显示撤销。严谨应判断 createBy -->
            <el-popconfirm 
              v-if="scope.row.auditStatus === 0"
              title="确定要撤销这个活动申请吗？" 
              @confirm="handleDelete(scope.row.eventId)"
            >
              <template #reference>
                <el-button size="small" type="warning" plain>撤销申请</el-button>
              </template>
            </el-popconfirm>

            <!-- 2. 报名/取消 (针对已发布的活动) -->
            <template v-else-if="scope.row.auditStatus === 3">
              <el-button 
                v-if="!scope.row.isSignedUp" 
                size="small" 
                type="primary" 
                @click="handleSignup(scope.row.eventId)">
                立即报名
              </el-button>
              <el-popconfirm 
                v-else
                title="确定要取消报名吗？" 
                @confirm="handleCancelSignup(scope.row.signupId)"
              >
                <template #reference>
                  <el-button size="small" type="danger" plain>取消报名</el-button>
                </template>
              </el-popconfirm>
            </template>

            <span v-else style="color: #999; font-size: 12px">审核中/已结束</span>
          </template>

          <template v-else>
            <template v-if="scope.row.auditStatus < 3">
              <el-button 
                size="small" 
                type="success" 
                @click="openAuditDialog(scope.row, true)">
                通过
              </el-button>
              <el-button 
                size="small" 
                type="danger" 
                @click="openAuditDialog(scope.row, false)">
                驳回
              </el-button>
            </template>
            <span v-else style="color: #999; font-size: 12px">已归档</span>
          </template>

        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="auditDialogVisible" title="✅ 活动审批" width="400px">
      <div style="margin-bottom: 20px">
        <p>活动主题：<strong>{{ currentEvent.title }}</strong></p>
        <p>您的操作：
          <el-tag :type="isPass ? 'success' : 'danger'">{{ isPass ? '通过' : '驳回' }}</el-tag>
        </p>
      </div>
      <el-form>
        <el-form-item label="审批意见">
          <el-input v-model="auditReason" type="textarea" :rows="3" placeholder="请输入理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit">确认提交</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="applyDialogVisible" title="📝 发起活动申请" width="500px">
      <el-form :model="applyForm" label-width="80px">
        <el-form-item label="主办社团">
          <el-select v-model="applyForm.clubId" placeholder="请选择主办社团">
            <el-option v-for="c in myClubs" :key="c.clubId" :label="c.clubName" :value="c.clubId" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="面向范围">
          <el-radio-group v-model="applyForm.targetType">
            <el-radio :label="0">全校学生</el-radio>
            <el-radio :label="1">特定院系</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="选择院系" v-if="applyForm.targetType === 1">
          <el-select v-model="applyForm.targetDeptId" placeholder="请选择目标院系" style="width: 100%">
            <el-option v-for="d in deptList" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>

        <el-form-item label="活动主题">
          <el-input v-model="applyForm.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动内容">
          <div style="display: flex; gap: 10px; width: 100%; align-items: flex-start;">
            <el-input 
              v-model="applyForm.content" 
              type="textarea" 
              :rows="5"
              placeholder="简述活动流程和意义（输入草稿，点击右侧 AI 按钮自动润色）" 
            />
            <el-tooltip content="AI 智能润色：让活动描述更具吸引力" placement="top">
              <el-button type="warning" :loading="aiLoading" @click="handleAiPolish" circle>
                <el-icon><MagicStick /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </el-form-item>
        <el-form-item label="活动地点">
          <el-input v-model="applyForm.location" placeholder="例如：大礼堂" />
        </el-form-item>
        <el-form-item label="活动预算">
          <el-input-number v-model="applyForm.budget" :min="0" :step="100" />
        </el-form-item>
        <el-form-item label="最大人数">
          <el-input-number v-model="applyForm.maxPeople" :min="0" :step="1" placeholder="0表示不限制"/>
        </el-form-item>
        <el-form-item label="起止时间">
          <el-date-picker
            v-model="applyForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Plus, Refresh, MagicStick } from '@element-plus/icons-vue'

const loading = ref(false)
const aiLoading = ref(false) // AI loading state
const tableData = ref([])
const user = JSON.parse(localStorage.getItem('user') || '{}')
const myClubs = ref([])
const deptList = ref([])

// ... (existing computed properties)

// ... (existing functions)

// 【新增】AI 润色逻辑
const handleAiPolish = async () => {
  if (!applyForm.content || applyForm.content.trim().length < 5) {
    ElMessage.warning('请至少输入 5 个字的草稿内容，AI 才能帮您润色哦')
    return
  }
  
  aiLoading.value = true
  try {
    const res = await request.post('/ai/polish', {
      title: applyForm.title,
      content: applyForm.content
    })
    
    // 使用打字机效果逐字显示（前端模拟，增强体验）
    const fullText = res.data
    applyForm.content = ''
    let index = 0
    const interval = setInterval(() => {
      if (index < fullText.length) {
        applyForm.content += fullText.charAt(index)
        index++
      } else {
        clearInterval(interval)
        aiLoading.value = false
        ElMessage.success('AI 润色完成！')
      }
    }, 30) // 30ms 一个字
    
  } catch (error) {
    aiLoading.value = false
    // 错误已经在 request.js 中处理了，这里可以不做额外提示
  }
}

// ... (existing functions)

// 计算属性：判断当前用户是不是学生
const isStudent = computed(() => {
  return user.roleKey === 'STUDENT'
})

// 计算属性：是否有权发起申请 (必须是某社团社长)
const canApply = computed(() => {
  return myClubs.value.length > 0
})

const auditDialogVisible = ref(false)
const currentEvent = ref({})
const isPass = ref(true)
const auditReason = ref('')

const applyDialogVisible = ref(false)
const applyForm = reactive({
  title: '',
  content: '',
  location: '',
  budget: 0,
  maxPeople: 0,  // 【新增】最大人数
  timeRange: [],
  clubId: null,      // 新增
  targetType: 0,     // 0:全校, 1:指定院系
  targetDeptId: null // 院系ID
})

const loadData = async () => {
  loading.value = true
  try {
    // 1. 获取所有活动（加时间戳避免潜在缓存）
    const res = await request.get('/event/list', { params: { _: Date.now() } })
    let events = res.data

    // 2. 如果是学生，额外获取该学生的报名记录，用于判断“是否已报名”
    if (isStudent.value) {
      // 学生只能看到自己发起的（待审核/已驳回）或者已发布的活动
      events = events.filter(e => {
        return e.createBy === user.userId || e.auditStatus === 3
      })

      const signupRes = await request.get(`/event/signup/list/user/${user.userId}`)
      const mySignups = signupRes.data || []
      const signupMap = new Set(mySignups.map(s => s.eventId))

      events.forEach(ev => {
        ev.isSignedUp = signupMap.has(ev.eventId)
        const record = mySignups.find(s => s.eventId === ev.eventId)
        if (record) ev.signupId = record.signupId
      })
      tableData.value = events
    } else {
      tableData.value = events
    }

    // 3. 获取我管理的社团（社长或管理员）
    // 注意：学生被设置为社长后，roleKey 仍然是 STUDENT，
    // 所以需要检查 my-clubs 接口来确定是否是社长
    if (user.roleKey === 'CLUB_ADMIN' || user.roleKey === 'STUDENT') {
      try {
        const [clubsRes, deptRes] = await Promise.all([
          request.get('/club/my-clubs', { params: { userId: user.userId } }),
          request.get('/dept/list')
        ])
        myClubs.value = clubsRes.data || []
        deptList.value = deptRes.data || []
      } catch (error) {
        console.error('加载社团列表失败:', error)
      }
    }
  } finally {
    loading.value = false
  }
}

// 刷新我管理的社团列表（用于刚被任命为社长的情况）
const refreshMyClubs = async () => {
  try {
    const res = await request.get('/club/my-clubs', { params: { userId: user.userId } })
    myClubs.value = res.data || []
    if (myClubs.value.length > 0) {
      ElMessage.success('权限已刷新！您现在可以发起活动申请了')
    } else {
      ElMessage.info('您暂时还不是任何社团的社长')
    }
  } catch (error) {
    ElMessage.error('刷新失败：' + (error.response?.data?.msg || error.message))
  }
}

const getStatusText = (status) => {
  const map = { 0: '待提交', 1: '待指导老师审', 2: '待院系审', 3: '已发布', 4: '已驳回' }
  return map[status] || '未知'
}
const getStatusType = (status) => {
  if (status === 3) return 'success'
  if (status === 4) return 'danger'
  if (status === 0) return 'info'
  return 'warning'
}

const openAuditDialog = (row, pass) => {
  currentEvent.value = row
  isPass.value = pass
  auditReason.value = pass ? '同意' : ''
  auditDialogVisible.value = true
}

const submitAudit = () => {
  const params = {
    eventId: currentEvent.value.eventId,
    userId: user.userId,
    pass: isPass.value,
    reason: auditReason.value
  }
  request.post('/event/audit', params).then(res => {
    ElMessage.success(res.msg)
    auditDialogVisible.value = false
    loadData()
  })
}

const openApplyDialog = () => {
  applyForm.title = ''
  applyForm.content = ''
  applyForm.location = ''
  applyForm.budget = 0
  applyForm.maxPeople = 0  // 【新增】重置最大人数
  applyForm.timeRange = []
  applyForm.clubId = myClubs.value.length > 0 ? myClubs.value[0].clubId : null // 默认选中第一个
  applyForm.targetType = 0
  applyForm.targetDeptId = null
  applyDialogVisible.value = true
}

const submitApply = async () => {
  if(!applyForm.title || !applyForm.timeRange || applyForm.timeRange.length < 2) {
    ElMessage.warning('请补全活动信息（主题、时间必填）')
    return
  }
  if (!applyForm.clubId) {
    ElMessage.warning('请选择主办社团')
    return
  }
  if (applyForm.targetType === 1 && !applyForm.targetDeptId) {
    ElMessage.warning('请选择目标院系')
    return
  }

  const postData = {
    title: applyForm.title,
    content: applyForm.content,
    location: applyForm.location,
    budget: applyForm.budget,
    maxPeople: applyForm.maxPeople,  // 【新增】最大人数
    startTime: applyForm.timeRange[0], 
    endTime: applyForm.timeRange[1],
    clubId: applyForm.clubId,
    targetType: applyForm.targetType,
    targetDeptId: applyForm.targetDeptId,
    // 后端根据 X-User-Id 自动获取当前登录用户，无需前端传 createBy
  }

  try {
    const res = await request.post('/event/add', postData)
    ElMessage.success(res.msg || '申请提交成功！')
    applyDialogVisible.value = false
    await loadData()
  } catch (e) {
    // 已在拦截器中提示错误，这里无需重复处理
  }
}

// 新增：撤销/删除逻辑
// 【新增】撤销逻辑修正
const handleDelete = (eventId) => {
  // 这里使用了 params 传参，后端用 @RequestParam 接收
  request.delete(`/event/${eventId}`, {
    params: {
      userId: user.userId // 告诉后端是谁在删
    }
  }).then(res => {
    ElMessage.success('活动已撤销')
    loadData()
  })
}

// 【新增】报名逻辑
const handleSignup = (eventId) => {
  request.post('/event/signup/add', {
    eventId: eventId,
    userId: user.userId
  }).then(res => {
    ElMessage.success('报名成功！')
    loadData() // 刷新列表以更新状态
  })
}

// 【新增】提交审批
const handleSubmit = (eventId) => {
  request.post('/event/submit', {
    eventId: eventId,
    userId: user.userId
  }).then(res => {
    ElMessage.success(res.msg || '已提交审批')
    loadData()
  })
}

// 【新增】取消报名
const handleCancelSignup = (signupId) => {
  request.delete(`/event/signup/${signupId}`).then(res => {
    ElMessage.success('已取消报名')
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.event-list-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
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