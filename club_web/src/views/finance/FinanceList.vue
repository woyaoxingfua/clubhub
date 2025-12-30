<template>
  <div class="finance-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card income-card">
          <div class="stat-icon">💰</div>
          <div class="stat-content">
            <div class="stat-label">总收入</div>
            <div class="stat-value">¥{{ stats.totalIncome || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card expense-card">
          <div class="stat-icon">📊</div>
          <div class="stat-content">
            <div class="stat-label">总支出</div>
            <div class="stat-value">¥{{ stats.totalExpense || '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card balance-card">
          <div class="stat-icon">💳</div>
          <div class="stat-content">
            <div class="stat-label">当前余额</div>
            <div class="stat-value" :class="{ negative: parseFloat(stats.balance || 0) < 0 }">
              ¥{{ stats.balance || '0.00' }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <h2>💵 财务管理</h2>
        <el-select v-model="filterClubId" placeholder="筛选社团" clearable style="width: 200px; margin-left: 20px" @change="loadData">
          <el-option label="全部社团" :value="null" />
          <el-option v-for="club in clubList" :key="club.clubId" :label="club.clubName" :value="club.clubId" />
        </el-select>
      </div>
      <el-button v-if="canManage" type="primary" @click="openDialog()">
        <el-icon style="margin-right: 5px"><Plus /></el-icon> 添加财务记录
      </el-button>
    </div>

    <!-- 财务列表表格 -->
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="financeId" label="ID" width="60" align="center" />
      <el-table-column prop="clubName" label="社团" width="150" />
      <el-table-column prop="type" label="类型" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.type === 1 ? 'success' : 'warning'">
            {{ scope.row.type === 1 ? '收入' : '支出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="120" align="right">
        <template #default="scope">
          <span :class="scope.row.type === 1 ? 'income-text' : 'expense-text'">
            {{ scope.row.type === 1 ? '+' : '-' }}¥{{ scope.row.amount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="用途说明" show-overflow-tooltip min-width="180" />
      <el-table-column prop="eventName" label="关联活动" width="120">
        <template #default="scope">
          <span>{{ scope.row.eventName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="凭证" width="80" align="center">
        <template #default="scope">
          <el-image 
            v-if="scope.row.proofUrl" 
            :src="scope.row.proofUrl" 
            style="width: 40px; height: 40px; border-radius: 4px; cursor: pointer;"
            :preview-src-list="[scope.row.proofUrl]"
            fit="cover">
            <template #error>
              <div style="font-size: 12px; color: #999;">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <span v-else style="color: #999; font-size: 12px;">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="记录时间" width="170">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <!-- 管理员：可以编辑所有记录 -->
          <!-- 社长：只能编辑审批中的记录 -->
          <template v-if="isAdmin || scope.row.status === 0">
            <el-button size="small" type="primary" link @click="openDialog(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除该记录？" @confirm="handleDelete(scope.row.financeId)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
          <span v-else style="color: #999; font-size: 12px">已审批</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.financeId ? '编辑财务记录' : '添加财务记录'" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="所属社团">
          <el-select v-model="form.clubId" placeholder="请选择社团" style="width: 100%" :disabled="!isAdmin && !!form.financeId">
            <el-option v-for="club in myClubList" :key="club.clubId" :label="club.clubName" :value="club.clubId" />
          </el-select>
        </el-form-item>
        <el-form-item label="收支类型">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">收入</el-radio>
            <el-radio :label="2">支出</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="金额">
          <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入用途说明" />
        </el-form-item>
        <el-form-item label="关联活动">
          <el-select v-model="form.eventId" placeholder="无关联活动（选填）" clearable style="width: 100%">
            <el-option v-for="event in eventList" :key="event.eventId" :label="event.title" :value="event.eventId" />
          </el-select>
        </el-form-item>
        <el-form-item label="凭证图片">
          <div style="display: flex; flex-direction: column; gap: 10px;">
            <el-upload
              :action="uploadAction + '/proof'"
              :headers="{ 'X-User-Id': user.userId }"
              :show-file-list="false"
              :on-success="handleProofSuccess"
              :before-upload="beforeProofUpload"
              :disabled="uploading">
              <el-button type="primary" size="small" :loading="uploading">
                <el-icon style="margin-right: 5px"><Upload /></el-icon>
                {{ uploading ? '上传中...' : '上传凭证' }}
              </el-button>
            </el-upload>
            <div v-if="form.proofUrl" style="display: flex; align-items: center; gap: 10px;">
              <el-image 
                :src="form.proofUrl" 
                style="width: 100px; height: 100px; border-radius: 4px; cursor: pointer;"
                :preview-src-list="[form.proofUrl]"
                fit="cover" />
              <el-button size="small" type="danger" @click="removeProof">
                <el-icon><Delete /></el-icon> 删除
              </el-button>
            </div>
            <el-text type="info" size="small">支持 JPG、PNG、PDF 格式，文件大小不超过 5MB</el-text>
          </div>
        </el-form-item>
        <el-form-item label="审批状态" v-if="isAdmin">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="审批中" :value="0" />
            <el-option label="通过" :value="1" />
            <el-option label="驳回" :value="2" />
          </el-select>
        </el-form-item>
        <el-alert v-else type="info" :closable="false" style="margin-bottom: 10px">
          <template #default>
            <span style="font-size: 13px">📌 社长添加的财务记录需要管理员审批后生效</span>
          </template>
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import request, { baseURL } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Plus, Upload, Delete, Picture } from '@element-plus/icons-vue'

const loading = ref(false)
const uploading = ref(false) // 凭证上传状态
const tableData = ref([])
const clubList = ref([]) // 所有社团列表
const myClubList = ref([]) // 我可以管理的社团列表
const eventList = ref([]) // 活动列表
const filterClubId = ref(null) // 筛选社团ID
const dialogVisible = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')
const uploadAction = baseURL + '/file/upload' // 上传URL基础路径

// 统计数据
const stats = ref({
  totalIncome: '0.00',
  totalExpense: '0.00',
  balance: '0.00'
})

const isAdmin = computed(() => {
  return user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN'
})

const canManage = computed(() => {
  return isAdmin.value || myClubList.value.length > 0
})

const form = reactive({
  financeId: null,
  clubId: null,
  type: 1,
  amount: null,
  description: '',
  eventId: null,
  proofUrl: '',
  status: 1
})

// 加载数据
const loadData = async () => {
  loading.value = true
  
  try {
    // 1. 加载所有社团
    const clubRes = await request.get('/club/list')
    clubList.value = clubRes.data || []
    
    // 2. 加载我管理的社团
    if (!isAdmin.value) {
      const myClubRes = await request.get('/club/my-clubs', { params: { userId: user.userId } })
      myClubList.value = myClubRes.data || []
    } else {
      myClubList.value = clubList.value
    }
    
    // 3. 加载活动列表（用于关联活动）
    const eventRes = await request.get('/event/list')
    eventList.value = eventRes.data || []
    
    // 4. 加载财务记录
    const params = filterClubId.value ? { clubId: filterClubId.value } : {}
    const financeRes = await request.get('/finance/list', { params })
    tableData.value = financeRes.data || []
    
    // 5. 加载统计数据（如果筛选了社团）
    if (filterClubId.value) {
      loadSummary(filterClubId.value)
    } else if (myClubList.value.length === 1) {
      // 如果只管理一个社团，默认显示该社团的统计
      loadSummary(myClubList.value[0].clubId)
    } else {
      // 多个社团或管理员，手动计算总统计
      calculateTotalStats()
    }
  } finally {
    loading.value = false
  }
}

// 加载某个社团的统计
const loadSummary = (clubId) => {
  request.get(`/finance/summary/${clubId}`).then(res => {
    stats.value = {
      totalIncome: res.data.totalIncome || '0.00',
      totalExpense: res.data.totalExpense || '0.00',
      balance: res.data.balance || '0.00'
    }
  })
}

// 计算总统计（从表格数据）
const calculateTotalStats = () => {
  let totalIncome = 0
  let totalExpense = 0
  
  tableData.value.forEach(item => {
    if (item.status === 1) { // 只统计已通过的
      if (item.type === 1) {
        totalIncome += parseFloat(item.amount || 0)
      } else if (item.type === 2) {
        totalExpense += parseFloat(item.amount || 0)
      }
    }
  })
  
  stats.value = {
    totalIncome: totalIncome.toFixed(2),
    totalExpense: totalExpense.toFixed(2),
    balance: (totalIncome - totalExpense).toFixed(2)
  }
}

const openDialog = (row = null) => {
  if (row) {
    Object.assign(form, row)
  } else {
    form.financeId = null
    form.clubId = myClubList.value.length === 1 ? myClubList.value[0].clubId : null
    form.type = 1
    form.amount = null
    form.description = ''
    form.eventId = null
    form.proofUrl = ''
    // 社长添加时不需要设置 status，后端会自动设置为0（审批中）
    // 管理员添加时默认为通过
    form.status = isAdmin.value ? 1 : 0
  }
  dialogVisible.value = true
}

const submitForm = () => {
  if (!form.clubId) {
    ElMessage.warning('请选择社团')
    return
  }
  if (!form.amount || form.amount <= 0) {
    ElMessage.warning('请输入正确的金额')
    return
  }
  
  const url = form.financeId ? '/finance/update' : '/finance/add'
  const method = form.financeId ? 'put' : 'post'
  
  request[method](url, form).then(res => {
    ElMessage.success(res.msg)
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (financeId) => {
  request.delete(`/finance/delete/${financeId}`).then(res => {
    ElMessage.success('删除成功')
    loadData()
  })
}

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ') : ''
}

// 凭证上传前验证
const beforeProofUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'application/pdf'].includes(file.type)
  const isValidSize = file.size / 1024 / 1024 < 5
  
  if (!isValidType) {
    ElMessage.error('仅支持上传 JPG、PNG、PDF 格式的凭证文件')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('凭证文件大小不能超过 5MB')
    return false
  }
  
  uploading.value = true
  return true
}

// 凭证上传成功回调
const handleProofSuccess = (response) => {
  uploading.value = false
  if (response.code === 200) {
    form.proofUrl = response.data
    ElMessage.success('凭证上传成功')
  } else {
    ElMessage.error(response.msg || '凭证上传失败')
  }
}

// 删除凭证
const removeProof = () => {
  form.proofUrl = ''
  ElMessage.success('已删除凭证')
}

const getStatusText = (status) => {
  const map = { 0: '审批中', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.finance-container {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.income-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.expense-card {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.balance-card {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 26px;
  font-weight: bold;
}

.stat-value.negative {
  color: #ff6b6b;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background: white;
  border-radius: 8px;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-left h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.income-text {
  color: #67c23a;
  font-weight: bold;
}

.expense-text {
  color: #f56c6c;
  font-weight: bold;
}
</style>
