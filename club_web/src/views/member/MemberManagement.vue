<template>
  <div class="member-management-container">
    <div class="toolbar">
      <div class="title-box">
        <h2>👥 成员花名册管理</h2>
        <span class="sub-title">管理社团成员信息、部门分配、权限组设置</span>
      </div>
      
      <div class="filter-box">
        <el-select v-model="currentClubId" placeholder="选择社团" style="width: 200px; margin-right: 10px" @change="loadMembers">
          <el-option
            v-for="club in myClubs"
            :key="club.clubId"
            :label="club.clubName"
            :value="club.clubId"
          />
        </el-select>
        
        <el-select v-model="filterStatus" placeholder="成员状态" style="width: 120px; margin-right: 10px" @change="loadMembers" clearable>
          <el-option label="全部" :value="null" />
          <el-option label="在社" :value="1" />
          <el-option label="已离社" :value="0" />
        </el-select>
        
        <el-input
          v-model="searchKeyword"
          placeholder="搜索成员姓名"
          style="width: 200px; margin-right: 10px"
          clearable
          @clear="loadMembers"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-button type="primary" @click="loadMembers">
          <el-icon style="margin-right: 5px"><Refresh /></el-icon>
          刷新
        </el-button>
        
        <el-button type="success" @click="showStatistics">
          <el-icon style="margin-right: 5px"><DataAnalysis /></el-icon>
          统计
        </el-button>
      </div>
    </div>

    <el-table :data="filteredMembers" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="studentName" label="姓名" width="100" fixed="left" />
      
      <el-table-column prop="position" label="职务" width="120">
        <template #default="scope">
          <el-tag size="small" :type="getPositionType(scope.row.position)">
            {{ scope.row.position }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="department" label="部门" width="120">
        <template #default="scope">
          <el-tag size="small" type="info">{{ scope.row.department }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="roleGroup" label="权限组" width="110">
        <template #default="scope">
          <el-tag size="small" :type="getRoleGroupType(scope.row.roleGroup)">
            {{ getRoleGroupText(scope.row.roleGroup) }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="joinTime" label="加入时间" width="170">
        <template #default="scope">
          {{ formatTime(scope.row.joinTime) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '在社' : '已离社' }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="quitTime" label="离社时间" width="170" v-if="filterStatus === 0">
        <template #default="scope">
          {{ scope.row.quitTime ? formatTime(scope.row.quitTime) : '-' }}
        </template>
      </el-table-column>
      
      <el-table-column prop="quitType" label="离社类型" width="100" v-if="filterStatus === 0">
        <template #default="scope">
          <el-tag v-if="scope.row.quitType === 1" type="warning" size="small">主动退出</el-tag>
          <el-tag v-else-if="scope.row.quitType === 2" type="danger" size="small">被开除</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      
      <el-table-column prop="quitReason" label="离社原因" show-overflow-tooltip v-if="filterStatus === 0" />
      
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="scope">
          <!-- 在社成员的操作 -->
          <template v-if="scope.row.status === 1">
            <el-button size="small" type="primary" link @click="openEditDialog(scope.row)">编辑</el-button>
            <!-- 权限组分配功能已移除，权限组由职位自动决定 -->
            <el-button size="small" type="danger" link @click="openQuitDialog(scope.row, 'quit')">退出</el-button>
            <el-button size="small" type="danger" link @click="openQuitDialog(scope.row, 'dismiss')">开除</el-button>
          </template>
          
          <!-- 已离社成员的操作 -->
          <template v-else>
            <el-button size="small" type="success" @click="handleTransferIn(scope.row.id)">转入</el-button>
            <span style="color: #999; font-size: 12px; margin-left: 10px">已离社</span>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑成员信息对话框 -->
    <el-dialog v-model="editDialogVisible" title="✏️ 编辑成员信息" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="职务">
          <el-select v-model="editForm.position" placeholder="请选择职务" style="width: 100%">
            <el-option label="成员" value="成员" />
            <el-option label="干事" value="干事" />
            <el-option label="部长" value="部长" />
            <el-option label="副部长" value="副部长" />
            <el-option label="副社长" value="副社长" />
            <!-- 只有系统管理员可以看到"社长"选项 -->
            <el-option v-if="isAdmin" label="社长" value="社长" />
          </el-select>
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="editForm.department" placeholder="请选择部门" style="width: 100%" allow-create filterable>
            <el-option label="未分配" value="未分配" />
            <el-option label="管理层" value="管理层" />
            <el-option label="技术部" value="技术部" />
            <el-option label="宣传部" value="宣传部" />
            <el-option label="组织部" value="组织部" />
            <el-option label="外联部" value="外联部" />
            <el-option label="财务部" value="财务部" />
            <el-option label="人事部" value="人事部" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdate">保存</el-button>
      </template>
    </el-dialog>

    <!-- 权限组分配对话框已移除，权限组由职位自动决定 -->
    <!-- 职位与权限组的对应关系：
         社长/副社长 → admin
         部长/副部长 → leader
         干事/成员 → member
    -->

    <!-- 退出/开除对话框 -->
    <el-dialog v-model="quitDialogVisible" :title="quitType === 'quit' ? '📤 成员退出' : '⚠️ 开除成员'" width="500px">
      <el-form :model="quitForm" label-width="100px">
        <el-form-item label="成员姓名">
          <el-input v-model="quitForm.studentName" disabled />
        </el-form-item>
        <el-form-item :label="quitType === 'quit' ? '退出原因' : '开除原因'">
          <el-input
            v-model="quitForm.reason"
            type="textarea"
            :rows="4"
            :placeholder="quitType === 'quit' ? '请填写退出原因（选填）' : '请填写开除原因'"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quitDialogVisible = false">取消</el-button>
        <el-button :type="quitType === 'quit' ? 'warning' : 'danger'" @click="handleQuit">确认</el-button>
      </template>
    </el-dialog>

    <!-- 统计信息对话框 -->
    <el-dialog v-model="statsDialogVisible" title="📊 成员统计信息" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="在社成员">
          <el-tag type="success" size="large">{{ statistics.activeMembers }} 人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已离社">
          <el-tag type="info" size="large">{{ statistics.quitMembers }} 人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="主动退出">
          <el-tag type="warning">{{ statistics.voluntaryQuit }} 人</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="被开除">
          <el-tag type="danger">{{ statistics.dismissed }} 人</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider>各部门人数</el-divider>
      <el-table :data="statistics.departmentStats" border stripe>
        <el-table-column prop="department" label="部门" />
        <el-table-column prop="count" label="人数" align="center">
          <template #default="scope">
            <el-tag type="primary">{{ scope.row.count }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, DataAnalysis } from '@element-plus/icons-vue'

const loading = ref(false)
const members = ref([])
const myClubs = ref([])
const currentClubId = ref(null)
const filterStatus = ref(null)
const searchKeyword = ref('')

const user = JSON.parse(localStorage.getItem('user') || '{}')
const isAdmin = computed(() => {
  return user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN'
})

// 编辑对话框
const editDialogVisible = ref(false)
const editForm = ref({
  id: null,
  studentName: '',
  position: '',
  department: ''
})

// 权限组对话框 - 已移除，权限组由职位自动决定
// const roleDialogVisible = ref(false)
// const roleForm = ref({ ... })

// 退出/开除对话框
const quitDialogVisible = ref(false)
const quitType = ref('quit') // 'quit' or 'dismiss'
const quitForm = ref({
  id: null,
  studentName: '',
  reason: ''
})

// 统计对话框
const statsDialogVisible = ref(false)
const statistics = ref({
  activeMembers: 0,
  quitMembers: 0,
  voluntaryQuit: 0,
  dismissed: 0,
  departmentStats: []
})

// 过滤后的成员列表
const filteredMembers = computed(() => {
  if (!searchKeyword.value) {
    return members.value
  }
  return members.value.filter(m => 
    m.studentName && m.studentName.includes(searchKeyword.value)
  )
})

// 初始化：加载我管理的社团
const loadMyClubs = async () => {
  try {
    if (isAdmin.value) {
      const res = await request.get('/club/list')
      myClubs.value = res.data || []
    } else {
      const res = await request.get('/club/my-clubs', { params: { userId: user.userId } })
      myClubs.value = res.data || []
    }
    
    if (myClubs.value.length > 0) {
      currentClubId.value = myClubs.value[0].clubId
      loadMembers()
    }
  } catch (error) {
    console.error('加载社团列表失败:', error)
    ElMessage.error('加载社团列表失败')
  }
}

// 加载成员列表
const loadMembers = async () => {
  if (!currentClubId.value) {
    members.value = []
    return
  }
  
  loading.value = true
  try {
    const params = {}
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    
    const res = await request.get(`/member/filter/${currentClubId.value}`, { params })
    members.value = res.data || []
  } catch (error) {
    console.error('加载成员列表失败:', error)
    ElMessage.error('加载成员列表失败')
  } finally {
    loading.value = false
  }
}

// 打开编辑对话框
const openEditDialog = (row) => {
  editForm.value = {
    id: row.id,
    studentName: row.studentName,
    position: row.position,
    department: row.department
  }
  editDialogVisible.value = true
}

// 保存编辑
const handleUpdate = async () => {
  try {
    await request.put('/member/update', {
      memberId: editForm.value.id,
      position: editForm.value.position,
      department: editForm.value.department
    })
    ElMessage.success('成员信息更新成功')
    editDialogVisible.value = false
    loadMembers()
  } catch (error) {
    console.error('更新失败:', error)
    ElMessage.error('更新失败：' + (error.response?.data?.msg || error.message))
  }
}

// 打开权限组对话框 - 已移除
// const openRoleDialog = (row) => { ... }

// 分配权限组 - 已移除，权限组由职位自动决定
// const handleAssignRole = async () => { ... }

// 打开退出/开除对话框
const openQuitDialog = (row, type) => {
  quitType.value = type
  quitForm.value = {
    id: row.id,
    studentName: row.studentName,
    reason: ''
  }
  quitDialogVisible.value = true
}

// 确认退出/开除
const handleQuit = async () => {
  const url = quitType.value === 'quit' ? '/member/quit' : '/member/dismiss'
  const message = quitType.value === 'quit' ? '退出登记成功' : '开除操作已完成'
  
  try {
    await request.put(url, {
      memberId: quitForm.value.id,
      reason: quitForm.value.reason
    })
    ElMessage.success(message)
    quitDialogVisible.value = false
    loadMembers()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败：' + (error.response?.data?.msg || error.message))
  }
}

// 转入（重新激活）
const handleTransferIn = async (memberId) => {
  try {
    await ElMessageBox.confirm('确定要将该成员转入（重新激活）吗？', '提示', {
      type: 'warning'
    })
    
    await request.post(`/member/transfer-in/${memberId}`)
    ElMessage.success('成员转入成功，欢迎回归！')
    loadMembers()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('转入失败:', error)
      ElMessage.error('转入失败：' + (error.response?.data?.msg || error.message))
    }
  }
}

// 显示统计信息
const showStatistics = async () => {
  if (!currentClubId.value) {
    ElMessage.warning('请先选择社团')
    return
  }
  
  try {
    const res = await request.get(`/member/statistics/${currentClubId.value}`)
    statistics.value = res.data
    statsDialogVisible.value = true
  } catch (error) {
    console.error('加载统计信息失败:', error)
    ElMessage.error('加载统计信息失败')
  }
}

// 辅助函数
const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ').substring(0, 16) : ''
}

const getPositionType = (position) => {
  if (position === '社长') return 'danger'
  if (position === '副社长') return 'warning'
  if (position.includes('部长')) return 'success'
  return ''
}

const getRoleGroupType = (roleGroup) => {
  if (roleGroup === 'admin') return 'danger'
  if (roleGroup === 'leader') return 'warning'
  return ''
}

const getRoleGroupText = (roleGroup) => {
  const map = {
    'admin': '管理员',
    'leader': '部长级',
    'member': '普通成员'
  }
  return map[roleGroup] || roleGroup
}

onMounted(() => {
  loadMyClubs()
})
</script>

<style scoped>
.member-management-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
}

.toolbar {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.title-box h2 {
  margin: 0 0 5px 0;
  font-size: 20px;
  color: #303133;
}

.sub-title {
  font-size: 13px;
  color: #909399;
}

.filter-box {
  margin-top: 15px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
