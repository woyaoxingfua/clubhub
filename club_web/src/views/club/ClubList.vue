<template>
  <div class="club-list-container">
    <div class="toolbar">
      <h2>🏰 社团信息管理</h2>
      <el-button v-if="isAdmin" type="primary" @click="openDialog()">
        <el-icon style="margin-right: 5px"><Plus /></el-icon> 创建新社团
      </el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="clubId" label="ID" width="60" align="center" />
      <el-table-column label="Logo" width="80" align="center">
        <template #default="scope">
          <el-avatar :size="50" :src="scope.row.logoUrl || 'https://img.icons8.com/clouds/200/000000/community.png'" />
        </template>
      </el-table-column>
      <el-table-column prop="clubName" label="社团名称" width="150" font-weight="bold" />
      <el-table-column prop="category" label="分类" width="100">
        <template #default="scope">
          <el-tag>{{ scope.row.category || '综合类' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="deptName" label="所属院系" width="140" />
      <el-table-column prop="presidentName" label="社长" width="100" />
      <el-table-column prop="advisorName" label="指导老师" width="100" />
      <el-table-column prop="description" label="社团简介" show-overflow-tooltip />
      <el-table-column prop="honors" label="荣誉" width="120" show-overflow-tooltip />
      
      <el-table-column label="操作" width="280" align="center" fixed="right">
        <template #default="scope">
          
          <template v-if="canManage(scope.row)">
            <el-button size="small" type="primary" link @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="warning" link @click="openMemberDrawer(scope.row)">成员管理</el-button>
            <el-button v-if="isAdmin" size="small" type="success" link @click="openTransferDialog(scope.row)">转让社长</el-button>
            <el-popconfirm v-if="isAdmin" title="确定解散社团？" @confirm="handleDelete(scope.row.clubId)">
              <template #reference>
                <el-button size="small" type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>

          <template v-else>
            <!-- 已经是成员，显示提示 -->
            <template v-if="scope.row.isUserMember">
              <el-tag type="success">已加入</el-tag>
            </template>
            <!-- 不是成员，显示申请加入按钮 -->
            <template v-else>
              <el-popconfirm title="确定要加入这个社团吗？" @confirm="handleJoin(scope.row.clubId)">
                <template #reference>
                  <el-button size="small" type="success" plain>申请加入</el-button>
                </template>
              </el-popconfirm>
            </template>
          </template>

        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.clubId ? '编辑社团' : '创建社团'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="社团Logo">
          <div style="display: flex; align-items: center; gap: 15px;">
            <el-avatar :size="80" :src="form.logoUrl || 'https://img.icons8.com/clouds/200/000000/community.png'" />
            <el-upload
              :action="uploadAction"
              :headers="{ 'X-User-Id': user.userId }"
              :show-file-list="false"
              :on-success="handleLogoSuccess"
              :before-upload="beforeLogoUpload">
              <el-button type="primary" size="small">
                <el-icon style="margin-right: 5px"><Upload /></el-icon>
                上传Logo
              </el-button>
            </el-upload>
            <el-button v-if="form.logoUrl" size="small" type="danger" @click="form.logoUrl = ''">删除</el-button>
          </div>
          <div style="margin-top: 8px; font-size: 12px; color: #909399;">
            建议尺寸：200x200像素，支持JPG、PNG格式
          </div>
        </el-form-item>
        
        <el-form-item label="社团名称">
          <el-input v-model="form.clubName" :disabled="!isAdmin && !!form.clubId" />
        </el-form-item>
        
        <el-form-item label="社团分类">
          <el-select v-model="form.category" placeholder="请选择">
            <el-option label="科技类" value="科技类" />
            <el-option label="艺术类" value="艺术类" />
            <el-option label="体育类" value="体育类" />
            <el-option label="公益类" value="公益类" />
            <el-option label="综合类" value="综合类" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="所属院系">
          <el-select v-model="form.deptId" placeholder="请选择(不选默认为校级)" clearable :disabled="!isAdmin">
            <el-option v-for="d in deptList" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="社长">
          <el-select v-model="form.presidentId" placeholder="请选择社长" filterable :disabled="!isAdmin">
            <el-option v-for="u in userList" :key="u.userId" :label="u.realName" :value="u.userId" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="指导老师">
          <el-select v-model="form.advisorId" placeholder="请选择指导老师" filterable :disabled="!isAdmin">
            <el-option v-for="u in teacherList" :key="u.userId" :label="u.realName" :value="u.userId" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="社团简介">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入社团简介" />
        </el-form-item>
        
        <el-form-item label="社团荣誉">
          <el-input 
            v-model="form.honors" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入社团荣誉，如：2024年度优秀社团、全国大学生XX竞赛一等奖等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="drawerVisible" title="社团成员列表" size="40%">
      <div style="margin-bottom: 20px; font-weight: bold; font-size: 16px;">
        当前社团：{{ currentClubName }}
      </div>
      
      <el-table :data="memberList" stripe border height="500px">
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        
        <el-table-column prop="position" label="社内职位" width="100">
          <template #default="scope">
            <el-tag size="small" type="info">{{ scope.row.position }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="joinTime" label="加入时间" />
        
        <el-table-column label="操作" width="80" align="center">
          <template #default="scope">
            <el-popconfirm title="确定移除该成员？" @confirm="handleKick(scope.row.id)">
              <template #reference>
                <el-button type="danger" size="small" icon="Delete" circle />
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 社长转让对话框（仅管理员可用） -->
    <el-dialog v-model="transferDialogVisible" title="👑 社长转让" width="500px">
      <el-alert type="warning" :closable="false" style="margin-bottom: 20px;">
        <template #title>
          <strong>重要提示</strong>
        </template>
        此操作将转让社长职位，同时更新系统权限和成员记录。旧社长将自动降级为副社长。
      </el-alert>
      
      <el-form :model="transferForm" label-width="100px">
        <el-form-item label="当前社团">
          <el-input v-model="transferForm.clubName" disabled />
        </el-form-item>
        
        <el-form-item label="当前社长">
          <el-input v-model="transferForm.oldPresidentName" disabled />
        </el-form-item>
        
        <el-form-item label="新任社长" required>
          <el-select v-model="transferForm.newPresidentId" placeholder="从社团成员中选择" filterable style="width: 100%">
            <el-option
              v-for="member in clubMembersForTransfer"
              :key="member.userId"
              :label="member.studentName + (member.position ? ' (' + member.position + ')' : '')"
              :value="member.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="transferDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleTransferPresident">确认转让</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import request, { baseURL } from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Plus, Delete, Upload } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const userList = ref([]) // 所有用户
const deptList = ref([]) // 所有院系
const dialogVisible = ref(false)
const user = JSON.parse(localStorage.getItem('user') || '{}')
const token = localStorage.getItem('token') || ''
const uploadAction = baseURL + '/file/upload/logo' // 完整的上传URL

// 成员管理相关变量
const drawerVisible = ref(false)
const memberList = ref([])
const currentClubName = ref('')
const currentClubId = ref(null)

// 社长转让相关变量
const transferDialogVisible = ref(false)
const clubMembersForTransfer = ref([])
const transferForm = reactive({
  clubId: null,
  clubName: '',
  oldPresidentName: '',
  newPresidentId: null
})

const isAdmin = computed(() => {
  return user.roleKey === 'SYS_ADMIN' || user.roleKey === 'DEPT_ADMIN'
})

// 判断是否有管理权限（管理员 OR 本社社长）
const canManage = (clubRow) => {
  if (isAdmin.value) return true
  // 判断当前用户是否是该社团的社长
  // 使用 == 允许类型转换 (string vs number)
  return clubRow.presidentId == user.userId
}

// 过滤出老师列表（用于指导老师选择）
const teacherList = computed(() => {
  return userList.value.filter(u => 
    u.roleKey === 'TEACHER' || 
    u.roleKey === 'SYS_ADMIN' || 
    u.roleKey === 'DEPT_ADMIN' ||
    u.roleKey === 'ADVISOR'
  )
})

const form = reactive({
  clubId: null,
  clubName: '',
  category: '',
  logoUrl: '',
  presidentId: null, // 改为 ID
  advisorId: null,   // 改为 ID
  deptId: null,      // 新增 deptId
  description: '',
  honors: ''         // 新增荣誉字段
})

const loadData = () => {
  loading.value = true
  Promise.all([
    request.get('/club/list'),
    request.get('/auth/list'), // 获取用户列表
    request.get('/dept/list')  // 获取院系列表
  ]).then(async ([clubRes, userRes, deptRes]) => {
    userList.value = userRes.data
    deptList.value = deptRes.data
    const userMap = new Map(userList.value.map(u => [u.userId, u.realName]))
    const deptMap = new Map(deptList.value.map(d => [d.deptId, d.deptName]))

    // 填充名字并检查成员状态
    const clubs = clubRes.data.map(club => {
      club.presidentName = userMap.get(club.presidentId) || '未知'
      club.advisorName = userMap.get(club.advisorId) || '未知'
      club.deptName = deptMap.get(club.deptId) || '校级/未分配'
      return club
    })
    
    // 并行检查用户在每个社团的成员状态
    const memberCheckPromises = clubs.map(club => 
      request.get(`/member/list/${club.clubId}`)
        .then(res => {
          const members = res.data || []
          // 检查当前用户是否已经是成员
          club.isUserMember = members.some(m => m.userId === user.userId && m.status === 1)
          return club
        })
        .catch(() => {
          club.isUserMember = false
          return club
        })
    )
    
    tableData.value = await Promise.all(memberCheckPromises)
  }).finally(() => {
    loading.value = false
  })
}

const openDialog = (row = null) => {
  if (row) {
    // 赋值时注意字段对应
    Object.assign(form, row)
  } else {
    form.clubId = null
    form.clubName = ''
    form.category = ''
    form.logoUrl = ''
    form.presidentId = null
    form.advisorId = null
    form.deptId = null
    form.description = ''
    form.honors = ''
  }
  dialogVisible.value = true
}

// Logo上传成功回调
const handleLogoSuccess = (response) => {
  if (response.code === 200) {
    form.logoUrl = response.data
    ElMessage.success('Logo上传成功')
  } else {
    ElMessage.error('Logo上传失败：' + response.msg)
  }
}

// Logo上传前校验
const beforeLogoUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const submitForm = () => {
  if (!form.clubName) {
    ElMessage.warning('社团名称不能为空')
    return
  }
  const url = form.clubId ? '/club/update' : '/club/add'
  const method = form.clubId ? 'put' : 'post'
  request[method](url, form).then(res => {
    ElMessage.success(res.msg)
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = (id) => {
  request.delete(`/club/${id}`).then(res => {
    ElMessage.success('操作成功')
    loadData()
  })
}

// --- 新增：学生加入社团 ---
const handleJoin = (clubId) => {
  request.post('/member/join', {
    clubId: clubId,
    userId: user.userId
  }).then(res => {
    ElMessage.success(res.msg)
  })
}

// --- 新增：打开成员管理抽屉 ---
const openMemberDrawer = (row) => {
  currentClubName.value = row.clubName
  currentClubId.value = row.clubId
  drawerVisible.value = true
  loadMembers(row.clubId)
}

// 加载成员列表
const loadMembers = (clubId) => {
  request.get(`/member/list/${clubId}`).then(res => {
    memberList.value = res.data
  })
}

// 踢人
const handleKick = (memberId) => {
  request.delete(`/member/${memberId}`).then(res => {
    ElMessage.success('已移出该成员')
    loadMembers(currentClubId.value) // 刷新列表
  })
}

// 打开社长转让对话框
const openTransferDialog = (row) => {
  transferForm.clubId = row.clubId
  transferForm.clubName = row.clubName
  transferForm.oldPresidentName = row.presidentName
  transferForm.newPresidentId = null
  
  // 加载该社团的所有在社成员（排除当前社长）
  request.get(`/member/list/${row.clubId}`).then(res => {
    clubMembersForTransfer.value = res.data.filter(m => m.userId !== row.presidentId)
    transferDialogVisible.value = true
  })
}

// 执行社长转让
const handleTransferPresident = () => {
  if (!transferForm.newPresidentId) {
    ElMessage.warning('请选择新任社长')
    return
  }
  
  request.post('/club/transfer-president', {
    clubId: transferForm.clubId,
    newPresidentId: transferForm.newPresidentId
  }).then(res => {
    ElMessage.success(res.msg)
    transferDialogVisible.value = false
    loadData() // 刷新列表
  }).catch(err => {
    ElMessage.error(err.response?.data?.msg || '转让失败')
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.club-list-container {
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