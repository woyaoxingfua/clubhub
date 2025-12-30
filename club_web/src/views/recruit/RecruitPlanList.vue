<template>
  <div class="recruit-plan-container">
    <div class="toolbar">
      <div class="title-box">
        <h2>📢 招新计划管理</h2>
        <span class="sub-title">发布和管理社团的招新计划</span>
      </div>
      <!-- 管理员或社长可以发布计划 -->
      <el-button v-if="canManage" type="primary" @click="openDialog()">
        <el-icon style="margin-right: 5px"><Plus /></el-icon> 发布新计划
      </el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
      <el-table-column prop="planId" label="ID" width="60" align="center" />
      <el-table-column prop="clubName" label="所属社团" min-width="120" />
      <el-table-column prop="title" label="招新标题" min-width="150" />
      <el-table-column prop="positions" label="招聘岗位" min-width="120" />
      <el-table-column prop="startTime" label="发布时间" width="170">
        <template #default="scope">
          {{ formatTime(scope.row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
            {{ scope.row.status === 1 ? '进行中' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" align="center" fixed="right">
        <template #default="scope">
          
          <!-- 管理员或该社团社长操作 -->
          <template v-if="canManagePlan(scope.row)">
            <el-button size="small" type="primary" link @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(scope.row.planId)">删除</el-button>
            <el-button size="small" type="success" link @click="$router.push(`/recruit/applications?planId=${scope.row.planId}`)">查看申请</el-button>
          </template>

          <!-- 普通学生操作 -->
          <template v-else>
            <el-button 
              v-if="scope.row.status === 1" 
              size="small" 
              type="primary" 
              @click="openApplyDialog(scope.row)"
            >
              申请加入
            </el-button>
            <span v-else style="color: #999; font-size: 12px">已结束</span>
          </template>

        </template>
      </el-table-column>
    </el-table>

    <!-- 管理员或社长：编辑/新增弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑计划' : '发布新计划'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="所属社团">
          <el-select v-model="form.clubId" placeholder="请选择社团" style="width: 100%">
            <!-- 管理员可以选择所有社团，社长只能选择自己管理的社团 -->
            <el-option
              v-for="item in (isAdmin ? clubList : myClubs)"
              :key="item.clubId"
              :label="item.clubName"
              :value="item.clubId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="例如：2025秋季招新" />
        </el-form-item>
        <el-form-item label="岗位">
          <el-input v-model="form.positions" placeholder="例如：技术部干事, 宣传部干事" />
        </el-form-item>
        <el-form-item label="要求">
          <el-input v-model="form.requirements" type="textarea" :rows="3" placeholder="填写岗位要求..." />
        </el-form-item>
        <el-form-item label="状态" v-if="isEdit">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">进行中</el-radio>
            <el-radio :label="0">已结束</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>

    <!-- 学生：申请弹窗 -->
    <el-dialog v-model="applyDialogVisible" title="📝 填写申请信息" width="600px">
      <div style="margin-bottom: 15px;">
        <p>正在申请：<strong>{{ currentPlan.title }}</strong></p>
      </div>
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="自我介绍" required>
          <el-input 
            v-model="applyForm.selfIntro" 
            type="textarea" 
            :rows="5" 
            placeholder="请简单介绍一下你自己，以及你想加入的理由..." 
          />
        </el-form-item>
        
        <el-form-item label="简历附件">
          <div style="width: 100%;">
            <!-- 上传组件 -->
            <el-upload
              v-if="!applyForm.resumeFileUrl"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :before-upload="beforeResumeUpload"
              :on-success="handleResumeSuccess"
              :on-error="handleUploadError"
              :show-file-list="false"
              :limit="1"
              accept=".pdf,.doc,.docx"
              drag
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将简历文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 PDF、DOC、DOCX 格式，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
            
            <!-- 上传成功后的展示 -->
            <div v-else class="uploaded-resume">
              <el-icon class="file-icon" color="#67c23a" :size="20"><document /></el-icon>
              <span class="file-name">简历已上传</span>
              <el-button 
                type="danger" 
                size="small" 
                link 
                @click="removeResume"
              >
                删除
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelApply">取消</el-button>
        <el-button type="primary" @click="submitApply" :loading="submitting">提交申请</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, UploadFilled, Document } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const clubList = ref([]) // 所有社团列表
const myClubs = ref([]) // 我担任社长的社团列表
const user = JSON.parse(localStorage.getItem('user') || '{}')

// 权限控制：管理员或者是某个社团的社长
const isAdmin = computed(() => {
  return user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN'
})

// 判断是否是社长（担任至少一个社团的社长）
const isPresident = computed(() => {
  return myClubs.value.length > 0
})

// 判断是否可以管理（管理员或社长）
const canManage = computed(() => {
  return isAdmin.value || isPresident.value
})

// 判断是否可以管理某个招新计划（管理员 或 该社团的社长）
const canManagePlan = (plan) => {
  if (isAdmin.value) return true
  // 检查是否是该社团的社长
  return myClubs.value.some(club => club.clubId === plan.clubId)
}

// 管理员弹窗相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({
  planId: null,
  clubId: null, 
  title: '',
  positions: '',
  requirements: '',
  status: 1
})

// 学生申请弹窗相关
const applyDialogVisible = ref(false)
const currentPlan = ref({})
const applyForm = reactive({
  selfIntro: '',
  resumeFileUrl: ''
})
const submitting = ref(false)

// 简历上传配置
const uploadUrl = 'http://localhost:8080/club_system_war_exploded/api/file/upload/resume'
const uploadHeaders = {
  // 如果后端需要token，可在此添加
  // 'Authorization': 'Bearer ' + localStorage.getItem('token')
}

const loadData = () => {
  loading.value = true
  
  Promise.all([
    request.get('/recruit/plan/list'),
    request.get('/club/list'),
    request.get('/club/my-clubs', { params: { userId: user.userId } })
  ]).then(([planRes, allClubRes, myClubRes]) => {
    tableData.value = planRes.data
    clubList.value = allClubRes.data || [] // 所有社团（用于显示名称）
    myClubs.value = myClubRes.data || [] // 我担任社长的社团（用于权限判断和下拉选择）
    
    console.log('[DEBUG] 招新计划 - 当前用户:', user.userId, user.roleKey)
    console.log('[DEBUG] 招新计划 - 我管理的社团数量:', myClubs.value.length, myClubs.value)
    console.log('[DEBUG] 招新计划 - canManage:', canManage.value)
    
    // 把社团名称拼接到招新计划上
    const clubMap = new Map(clubList.value.map(c => [c.clubId, c.clubName]))
    tableData.value.forEach(p => {
      p.clubName = clubMap.get(p.clubId) || '未知社团'
    })
    
  }).finally(() => loading.value = false)
}

const formatTime = (timeStr) => {
  return timeStr ? timeStr.replace('T', ' ') : ''
}

// --- 管理员或社长操作 ---
const openDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    Object.assign(form, row)
  } else {
    isEdit.value = false
    form.planId = null
    // 如果是社长且只有一个社团，默认选中
    if (!isAdmin.value && myClubs.value.length === 1) {
      form.clubId = myClubs.value[0].clubId
    } else if (isAdmin.value && clubList.value.length > 0) {
      form.clubId = null // 管理员需要手动选择
    } else {
      form.clubId = null
    }
    form.title = ''
    form.positions = ''
    form.requirements = ''
    form.status = 1
  }
  dialogVisible.value = true
}

const handleSubmit = () => {
  if (!form.clubId) {
    ElMessage.warning('请选择所属社团')
    return
  }
  const url = isEdit.value ? '/recruit/plan/update' : '/recruit/plan/add'
  const method = isEdit.value ? 'put' : 'post'
  request[method](url, form).then(res => {
    ElMessage.success(res.msg)
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该计划吗？', '提示', { type: 'warning' })
    .then(() => {
      request.delete(`/recruit/plan/${id}`).then(res => {
        ElMessage.success('删除成功')
        loadData()
      })
    })
}

// --- 学生操作 ---
const openApplyDialog = (row) => {
  currentPlan.value = row
  applyForm.selfIntro = ''
  applyForm.resumeFileUrl = ''
  submitting.value = false
  applyDialogVisible.value = true
}

// 简历上传前的校验
const beforeResumeUpload = (file) => {
  const isValidType = file.type === 'application/pdf' || 
                     file.type === 'application/msword' || 
                     file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('简历文件只能是 PDF、DOC、DOCX 格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('简历文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 简历上传成功
const handleResumeSuccess = (response, file) => {
  console.log('简历上传响应:', response)
  if (response.code === 200) {
    applyForm.resumeFileUrl = response.data
    ElMessage.success('简历上传成功')
  } else {
    ElMessage.error(response.msg || '简历上传失败')
  }
}

// 上传失败
const handleUploadError = (error) => {
  console.error('简历上传失败:', error)
  ElMessage.error('简历上传失败，请重试')
}

// 删除简历
const removeResume = () => {
  applyForm.resumeFileUrl = ''
  ElMessage.info('已删除简历')
}

// 取消申请
const cancelApply = () => {
  applyDialogVisible.value = false
  applyForm.selfIntro = ''
  applyForm.resumeFileUrl = ''
}

const submitApply = () => {
  if (!applyForm.selfIntro) {
    ElMessage.warning('请填写自我介绍')
    return
  }
  
  submitting.value = true
  
  const postData = {
    planId: currentPlan.value.planId,
    userId: user.userId,
    selfIntro: applyForm.selfIntro,
    resumeFileUrl: applyForm.resumeFileUrl || '' // 简历可选，但至少传空字符串防止DB字段非空约束
  }

  request.post('/recruit/apply', postData).then(res => {
    ElMessage.success('申请提交成功，请耐心等待审核')
    applyDialogVisible.value = false
    applyForm.selfIntro = ''
    applyForm.resumeFileUrl = ''
  }).catch(err => {
    ElMessage.error('提交失败：' + (err.response?.data?.msg || err.message))
  }).finally(() => {
    submitting.value = false
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.recruit-plan-container {
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

/* 简历上传成功后的样式 */
.uploaded-resume {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.uploaded-resume .file-icon {
  margin-right: 8px;
}
.uploaded-resume .file-name {
  flex: 1;
  color: #606266;
  font-size: 14px;
}
</style>
