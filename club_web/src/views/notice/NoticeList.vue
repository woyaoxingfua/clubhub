<template>
  <div class="notice-list-container">
    <div class="toolbar">
      <h2>📢 公告管理</h2>
      <el-button v-if="canPublish" type="primary" @click="openDialog()">
        <el-icon style="margin-right: 5px"><Plus /></el-icon> 发布新公告
      </el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="noticeId" label="ID" width="60" align="center" />
      <el-table-column prop="title" label="标题" min-width="200">
        <template #default="scope">
          <span style="font-weight: bold">
            <el-icon v-if="scope.row.isPinned === 1" color="#f56c6c" style="margin-right: 5px"><Flag /></el-icon>
            {{ scope.row.title }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="内容" show-overflow-tooltip min-width="250" />
      <el-table-column prop="targetType" label="可见范围" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.targetType === 0" type="success">全校可见</el-tag>
          <el-tag v-else-if="scope.row.targetType === 1" type="warning">本院可见</el-tag>
          <el-tag v-else-if="scope.row.targetType === 2" type="info">本社可见</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="clubName" label="所属社团" width="140">
        <template #default="scope">
          <span>{{ scope.row.clubName || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column prop="publishTime" label="发布时间" width="170">
        <template #default="scope">
          {{ formatTime(scope.row.publishTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="scope">
          <template v-if="canManage(scope.row)">
            <el-button 
              v-if="isAdmin" 
              size="small" 
              :type="scope.row.isPinned === 1 ? 'warning' : 'success'" 
              link 
              @click="handlePin(scope.row)">
              {{ scope.row.isPinned === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button size="small" type="primary" link @click="openDialog(scope.row)">编辑</el-button>
            <el-popconfirm title="确定删除这条公告？" @confirm="handleDelete(scope.row.noticeId)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.noticeId ? '编辑公告' : '发布公告'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="公告标题">
          <el-input v-model="form.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="可见范围">
          <el-select v-model="form.targetType" placeholder="请选择" style="width: 100%" @change="handleTargetTypeChange">
            <el-option label="全校可见" :value="0" v-if="isAdmin" />
            <el-option label="本院可见" :value="1" v-if="isAdmin" />
            <el-option label="本社可见" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属社团" v-if="form.targetType === 2">
          <el-select v-model="form.clubId" placeholder="请选择社团" style="width: 100%">
            <el-option v-for="club in myClubList" :key="club.clubId" :label="club.clubName" :value="club.clubId" />
          </el-select>
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input v-model="form.content" type="textarea" :rows="6" placeholder="请输入公告内容" />
        </el-form-item>
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
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Plus, Flag } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const clubList = ref([])
const myClubs = ref([])
const dialogVisible = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')

const isAdmin = computed(() => {
  return user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN'
})

const isPresident = computed(() => {
  return myClubs.value.length > 0
})

const canPublish = computed(() => {
  return isAdmin.value || isPresident.value
})

const myClubList = computed(() => {
  if (isAdmin.value) {
    return clubList.value
  }
  return myClubs.value
})

const canManage = (notice) => {
  if (isAdmin.value) return true
  return notice.publisherId === user.userId
}

const form = reactive({
  noticeId: null,
  title: '',
  content: '',
  targetType: 2,
  clubId: null,
  publisherId: null
})

const loadData = async () => {
  loading.value = true
  try {
    const clubRes = await request.get('/club/list')
    clubList.value = clubRes.data || []
    
    if (!isAdmin.value) {
      const myClubRes = await request.get('/club/my-clubs', { params: { userId: user.userId } })
      myClubs.value = myClubRes.data || []
    }
    
    const noticeRes = await request.get('/notice/list')
    tableData.value = noticeRes.data || []
  } finally {
    loading.value = false
  }
}

const openDialog = (row = null) => {
  if (row) {
    Object.assign(form, row)
  } else {
    form.noticeId = null
    form.title = ''
    form.content = ''
    form.targetType = isAdmin.value ? 0 : 2
    form.clubId = myClubs.value.length === 1 ? myClubs.value[0].clubId : null
    form.publisherId = user.userId
  }
  dialogVisible.value = true
}

const handleTargetTypeChange = () => {
  if (form.targetType !== 2) {
    form.clubId = null
  } else if (myClubs.value.length === 1) {
    form.clubId = myClubs.value[0].clubId
  }
}

const submitForm = () => {
  if (!form.title) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!form.content) {
    ElMessage.warning('请输入公告内容')
    return
  }
  if (form.targetType === 2 && !form.clubId) {
    ElMessage.warning('请选择社团')
    return
  }
  
  const url = form.noticeId ? '/notice/edit' : '/notice/add'
  const method = form.noticeId ? 'put' : 'post'
  
  request[method](url, form).then(res => {
    ElMessage.success(res.msg)
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (noticeId) => {
  request.delete(`/notice/delete/${noticeId}`).then(res => {
    ElMessage.success('删除成功')
    loadData()
  })
}

const handlePin = (row) => {
  const newPinStatus = row.isPinned === 1 ? 0 : 1
  request.put('/notice/pin', {
    noticeId: row.noticeId,
    isPinned: newPinStatus
  }).then(res => {
    ElMessage.success(res.msg)
    loadData()
  })
}

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ') : ''
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.notice-list-container {
  padding: 20px;
  background: white;
  border-radius: 8px;
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
</style>
