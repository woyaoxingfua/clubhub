<template>
  <div class="contacts-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>📇 社团通讯录</h2>
      <el-select v-model="selectedClubId" placeholder="选择社团" style="width: 250px" @change="loadContacts">
        <el-option v-for="club in myClubs" :key="club.clubId" :label="club.clubName" :value="club.clubId" />
      </el-select>
    </div>

    <!-- 搜索工具栏 -->
    <el-card class="search-card" v-if="selectedClubId">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input v-model="searchForm.userName" placeholder="搜索姓名" clearable @clear="loadContacts">
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.department" placeholder="筛选部门" clearable @change="loadContacts">
            <el-option label="全部部门" value="" />
            <el-option label="未分配" value="未分配" />
            <el-option label="管理层" value="管理层" />
            <el-option label="技术部" value="技术部" />
            <el-option label="宣传部" value="宣传部" />
            <el-option label="组织部" value="组织部" />
            <el-option label="外联部" value="外联部" />
            <el-option label="财务部" value="财务部" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="searchForm.position" placeholder="筛选职务" clearable @change="loadContacts">
            <el-option label="全部职务" value="" />
            <el-option label="社长" value="社长" />
            <el-option label="副社长" value="副社长" />
            <el-option label="部长" value="部长" />
            <el-option label="副部长" value="副部长" />
            <el-option label="干事" value="干事" />
            <el-option label="成员" value="成员" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="loadContacts">
            <el-icon style="margin-right: 5px"><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 提示信息 -->
    <el-empty v-if="!selectedClubId" description="请先选择一个社团查看通讯录" />

    <!-- 通讯录列表 -->
    <el-card v-else class="contacts-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>📋 成员通讯录 ({{ contactList.length }}人)</span>
          <el-button type="success" size="small" @click="exportContacts">
            <el-icon style="margin-right: 5px"><Download /></el-icon> 导出通讯录
          </el-button>
        </div>
      </template>

      <el-table :data="contactList" border stripe>
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="userName" label="姓名" width="120" fixed="left">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-avatar :size="30" style="margin-right: 8px">{{ scope.row.userName.charAt(0) }}</el-avatar>
              <span>{{ scope.row.userName }}</span>
              <el-tag v-if="isPresident(scope.row)" type="danger" size="small" style="margin-left: 5px">社长</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="department" label="部门" width="120" align="center">
          <template #default="scope">
            <el-tag type="info" effect="plain">{{ scope.row.department || '未分配' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="position" label="职务" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getPositionType(scope.row.position)">{{ scope.row.position || '成员' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 5px; color: #67c23a"><Phone /></el-icon>
              <span>{{ scope.row.phone || '未填写' }}</span>
              <el-button v-if="scope.row.phone" link type="primary" size="small" @click="copyToClipboard(scope.row.phone)">
                复制
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" width="200">
          <template #default="scope">
            <div style="display: flex; align-items: center;">
              <el-icon style="margin-right: 5px; color: #409eff"><Message /></el-icon>
              <span>{{ scope.row.email || '未填写' }}</span>
              <el-button v-if="scope.row.email" link type="primary" size="small" @click="copyToClipboard(scope.row.email)">
                复制
              </el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="studentId" label="学号" width="140">
          <template #default="scope">
            {{ scope.row.studentId || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="scope">
            {{ scope.row.gender === 1 ? '男' : scope.row.gender === 2 ? '女' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="joinTime" label="加入时间" width="170">
          <template #default="scope">
            {{ formatTime(scope.row.joinTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" size="small" @click="showContactDetail(scope.row)">
              <el-icon><View /></el-icon> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 联系人详情对话框 -->
    <el-dialog v-model="detailVisible" title="📇 联系人详情" width="500px">
      <el-descriptions :column="1" border v-if="currentContact">
        <el-descriptions-item label="姓名">{{ currentContact.userName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ currentContact.studentId || '未填写' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          {{ currentContact.gender === 1 ? '男' : currentContact.gender === 2 ? '女' : '未填写' }}
        </el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentContact.department || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="职务">{{ currentContact.position || '成员' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ currentContact.phone || '未填写' }}
          <el-button v-if="currentContact.phone" link size="small" @click="copyToClipboard(currentContact.phone)">复制</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ currentContact.email || '未填写' }}
          <el-button v-if="currentContact.email" link size="small" @click="copyToClipboard(currentContact.email)">复制</el-button>
        </el-descriptions-item>
        <el-descriptions-item label="加入时间">{{ formatTime(currentContact.joinTime) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Search, Download, Phone, Message, View } from '@element-plus/icons-vue'

const loading = ref(false)
const myClubs = ref([]) // 我加入的社团列表
const selectedClubId = ref(null)
const contactList = ref([])
const detailVisible = ref(false)
const currentContact = ref(null)
const user = JSON.parse(localStorage.getItem('user') || '{}')

const searchForm = reactive({
  userName: '',
  department: '',
  position: ''
})

// 加载我加入的社团
const loadMyClubs = async () => {
  try {
    //如果是管理员，加载所有社团
    if (user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN') {
      const res = await request.get('/club/list')
      myClubs.value = res.data || []
    } else {
      // 普通用户：先尝试获取我管理的社团（社长）
      const presidentRes = await request.get('/club/my-clubs', { params: { userId: user.userId } })
      const presidentClubs = presidentRes.data || []
      
      // 再获取我加入的社团（成员）
      const memberRes = await request.get('/member/my-memberships', { params: { userId: user.userId } })
      const memberClubs = memberRes.data || []
      
      // 合并并去重
      const allClubs = [...presidentClubs]
      memberClubs.forEach(club => {
        if (!allClubs.find(c => c.clubId === club.clubId)) {
          allClubs.push(club)
        }
      })
      
      myClubs.value = allClubs
    }
    
    // 如果只有一个社团，默认选中
    if (myClubs.value.length === 1) {
      selectedClubId.value = myClubs.value[0].clubId
      loadContacts()
    } else if (myClubs.value.length > 0 && (user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN')) {
       // 管理员默认不选中，或者选中第一个
       // selectedClubId.value = myClubs.value[0].clubId; 
       // loadContacts();
    }
  } catch (error) {
    console.error('加载社团列表失败:', error)
    ElMessage.warning('暂无可查看的社团通讯录')
  }
}

// 加载通讯录
const loadContacts = async () => {
  if (!selectedClubId.value) return
  
  loading.value = true
  try {
    const params = {
      clubId: selectedClubId.value,
      status: 1, // 只查询在社成员
      ...searchForm
    }
    
    const res = await request.get(`/member/filter/${selectedClubId.value}`, { params })
    contactList.value = res.data || []
  } catch (error) {
    ElMessage.error('加载通讯录失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.userName = ''
  searchForm.department = ''
  searchForm.position = ''
  loadContacts()
}

// 判断是否是社长
const isPresident = (member) => {
  const club = myClubs.value.find(c => c.clubId === selectedClubId.value)
  return club && club.presidentId === member.userId
}

// 获取职务标签类型
const getPositionType = (position) => {
  const typeMap = {
    '社长': 'danger',
    '副社长': 'warning',
    '部长': 'success',
    '副部长': 'info',
    '干事': 'primary',
    '成员': ''
  }
  return typeMap[position] || ''
}

// 显示详情
const showContactDetail = (contact) => {
  currentContact.value = contact
  detailVisible.value = true
}

// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch (err) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 导出通讯录（简单版本，导出为CSV）
const exportContacts = () => {
  if (contactList.value.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  
  // 构建CSV内容
  let csv = '姓名,学号,性别,部门,职务,手机号,邮箱,加入时间\n'
  contactList.value.forEach(item => {
    const gender = item.gender === 1 ? '男' : item.gender === 2 ? '女' : ''
    const studentId = item.studentId || ''
    csv += `${item.userName || item.studentName},${studentId},${gender},${item.department || '未分配'},${item.position || '成员'},${item.phone || ''},${item.email || ''},${formatTime(item.joinTime)}\n`
  })
  
  // 创建下载链接
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  const clubName = myClubs.value.find(c => c.clubId === selectedClubId.value)?.clubName || '社团'
  link.setAttribute('href', url)
  link.setAttribute('download', `${clubName}通讯录.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('导出成功')
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadMyClubs()
})
</script>

<style scoped>
.contacts-container {
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

.search-card {
  margin-bottom: 20px;
}

.contacts-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
