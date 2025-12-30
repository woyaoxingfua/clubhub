<template>
  <div class="message-container">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <h2>💬 站内消息</h2>
      <div class="header-actions">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" type="danger">
          <el-button @click="activeTab = 'received'" :type="activeTab === 'received' ? 'primary' : ''">
            <el-icon style="margin-right: 5px"><Message /></el-icon> 收件箱
          </el-button>
        </el-badge>
        <el-button @click="activeTab = 'sent'" :type="activeTab === 'sent' ? 'primary' : ''" style="margin-left: 10px">
          <el-icon style="margin-right: 5px"><Promotion /></el-icon> 发件箱
        </el-button>
        <el-button type="success" @click="showComposeDialog" style="margin-left: 20px">
          <el-icon style="margin-right: 5px"><EditPen /></el-icon> 写信
        </el-button>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-click="handleTabChange">
      <!-- 收件箱 -->
      <el-tab-pane label="收件箱" name="received">
        <div class="toolbar">
          <el-button size="small" @click="batchMarkAsRead" :disabled="selectedMessages.length === 0">
            <el-icon style="margin-right: 5px"><Check /></el-icon> 标记已读
          </el-button>
          <el-button size="small" type="danger" @click="batchDelete" :disabled="selectedMessages.length === 0">
            <el-icon style="margin-right: 5px"><Delete /></el-icon> 删除
          </el-button>
        </div>

        <el-table :data="receivedMessages" v-loading="loading" border stripe 
                  @selection-change="handleSelectionChange" @row-click="handleRowClick">
          <el-table-column type="selection" width="55" />
          <el-table-column label="状态" width="80" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.isRead === 0" type="danger" size="small">未读</el-tag>
              <el-tag v-else type="info" size="small">已读</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="senderName" label="发件人" width="120" />
          <el-table-column prop="subject" label="主题" min-width="200" show-overflow-tooltip>
            <template #default="scope">
              <span :style="{ fontWeight: scope.row.isRead === 0 ? 'bold' : 'normal' }">
                {{ scope.row.subject }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="clubName" label="关联社团" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.clubName" type="info" size="small">{{ scope.row.clubName }}</el-tag>
              <span v-else style="color: #999">-</span>
            </template>
          </el-table-column>
          <el-table-column prop="sendTime" label="时间" width="170">
            <template #default="scope">
              {{ formatTime(scope.row.sendTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" @click.stop="viewMessage(scope.row)">
                <el-icon><View /></el-icon> 查看
              </el-button>
              <el-popconfirm title="确定删除？" @confirm="deleteMessage(scope.row.messageId)">
                <template #reference>
                  <el-button link type="danger" size="small" @click.stop="">
                    <el-icon><Delete /></el-icon> 删除
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 发件箱 -->
      <el-tab-pane label="发件箱" name="sent">
        <el-table :data="sentMessages" v-loading="loading" border stripe @row-click="viewMessage">
          <el-table-column prop="receiverName" label="收件人" width="120" />
          <el-table-column prop="subject" label="主题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="clubName" label="关联社团" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.clubName" type="info" size="small">{{ scope.row.clubName }}</el-tag>
              <span v-else style="color: #999">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.isRead === 1" type="success" size="small">已读</el-tag>
              <el-tag v-else type="info" size="small">未读</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sendTime" label="发送时间" width="170">
            <template #default="scope">
              {{ formatTime(scope.row.sendTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button link type="primary" size="small" @click.stop="viewMessage(scope.row)">
                <el-icon><View /></el-icon> 查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 写信对话框 -->
    <el-dialog v-model="composeVisible" title="✉️ 写信" width="600px">
      <el-form :model="composeForm" label-width="80px">
        <el-form-item label="收件人">
          <el-select v-model="composeForm.receiverId" placeholder="请选择收件人" filterable style="width: 100%">
            <el-option-group v-for="club in myClubs" :key="club.clubId" :label="club.clubName">
              <el-option v-for="member in club.members" :key="member.userId" 
                         :label="member.userName" :value="member.userId">
                <span>{{ member.userName }}</span>
                <span style="color: #999; margin-left: 10px">{{ member.position || '成员' }}</span>
              </el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="关联社团">
          <el-select v-model="composeForm.clubId" placeholder="无关联（选填）" clearable style="width: 100%">
            <el-option v-for="club in myClubs" :key="club.clubId" 
                       :label="club.clubName" :value="club.clubId" />
          </el-select>
        </el-form-item>
        <el-form-item label="主题">
          <el-input v-model="composeForm.subject" placeholder="请输入主题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="composeForm.content" type="textarea" :rows="6" placeholder="请输入消息内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="composeVisible = false">取消</el-button>
        <el-button type="primary" @click="sendMessage">
          <el-icon style="margin-right: 5px"><Promotion /></el-icon> 发送
        </el-button>
      </template>
    </el-dialog>

    <!-- 查看消息对话框 -->
    <el-dialog v-model="viewVisible" :title="currentMessage?.subject || '消息详情'" width="600px">
      <el-descriptions v-if="currentMessage" :column="1" border>
        <el-descriptions-item label="发件人">{{ currentMessage.senderName }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ currentMessage.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="关联社团">
          <el-tag v-if="currentMessage.clubName" type="info" size="small">{{ currentMessage.clubName }}</el-tag>
          <span v-else style="color: #999">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="发送时间">{{ formatTime(currentMessage.sendTime) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentMessage.readTime" label="阅读时间">
          {{ formatTime(currentMessage.readTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="消息内容">
          <div style="white-space: pre-wrap; max-height: 300px; overflow-y: auto; padding: 10px; background: #f5f5f5; border-radius: 4px;">
            {{ currentMessage.content }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="viewVisible = false">关闭</el-button>
        <el-button v-if="activeTab === 'received' && currentMessage.isRead === 0" 
                   type="primary" @click="markAsRead(currentMessage.messageId)">
          <el-icon style="margin-right: 5px"><Check /></el-icon> 标记已读
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'
import { Message, Promotion, EditPen, Check, Delete, View } from '@element-plus/icons-vue'

const loading = ref(false)
const activeTab = ref('received')
const receivedMessages = ref([])
const sentMessages = ref([])
const selectedMessages = ref([])
const composeVisible = ref(false)
const viewVisible = ref(false)
const currentMessage = ref(null)
const unreadCount = ref(0)
const myClubs = ref([]) // 我加入的社团及其成员
const user = JSON.parse(localStorage.getItem('user') || '{}')

const composeForm = reactive({
  receiverId: null,
  clubId: null,
  subject: '',
  content: ''
})

// 加载收件箱
const loadReceivedMessages = async () => {
  loading.value = true
  try {
    const res = await request.get('/message/received')
    receivedMessages.value = res.data || []
    
    // 统计未读数量
    unreadCount.value = receivedMessages.value.filter(m => m.isRead === 0).length
  } catch (error) {
    ElMessage.error('加载收件箱失败')
  } finally {
    loading.value = false
  }
}

// 加载发件箱
const loadSentMessages = async () => {
  loading.value = true
  try {
    const res = await request.get('/message/sent')
    sentMessages.value = res.data || []
  } catch (error) {
    ElMessage.error('加载发件箱失败')
  } finally {
    loading.value = false
  }
}

// 加载我加入的社团及成员
const loadMyClubsAndMembers = async () => {
  try {
    // 获取我加入的社团
    const clubRes = await request.get('/member/my-memberships', { params: { userId: user.userId } })
    const presidentRes = await request.get('/club/my-clubs', { params: { userId: user.userId } })
    
    const allClubs = [...(clubRes.data || []), ...(presidentRes.data || [])]
    
    // 去重
    const uniqueClubs = allClubs.filter((club, index, self) => 
      index === self.findIndex((c) => c.clubId === club.clubId)
    )
    
    // 为每个社团加载成员列表
    for (const club of uniqueClubs) {
      const memberRes = await request.get(`/member/list/${club.clubId}`)
      club.members = (memberRes.data || []).map(m => ({
        userId: m.userId,
        userName: m.studentName,
        position: m.position
      }))
    }
    
    myClubs.value = uniqueClubs
  } catch (error) {
    console.error('加载社团成员失败:', error)
  }
}

// 切换标签页
const handleTabChange = () => {
  if (activeTab.value === 'received') {
    loadReceivedMessages()
  } else {
    loadSentMessages()
  }
}

// 显示写信对话框
const showComposeDialog = () => {
  composeForm.receiverId = null
  composeForm.clubId = null
  composeForm.subject = ''
  composeForm.content = ''
  composeVisible.value = true
}

// 发送消息
const sendMessage = async () => {
  if (!composeForm.receiverId) {
    ElMessage.warning('请选择收件人')
    return
  }
  if (!composeForm.content.trim()) {
    ElMessage.warning('请输入消息内容')
    return
  }
  
  try {
    const res = await request.post('/message/send', composeForm)
    ElMessage.success(res.msg)
    composeVisible.value = false
    loadSentMessages()
  } catch (error) {
    ElMessage.error('发送失败')
  }
}

// 查看消息
const viewMessage = async (message) => {
  currentMessage.value = message
  viewVisible.value = true
  
  // 如果是未读的收件，自动标记为已读
  if (activeTab.value === 'received' && message.isRead === 0) {
    await request.put(`/message/read/${message.messageId}`)
    message.isRead = 1
    unreadCount.value--
  }
}

// 标记已读
const markAsRead = async (messageId) => {
  try {
    await request.put(`/message/read/${messageId}`)
    ElMessage.success('已标记为已读')
    viewVisible.value = false
    loadReceivedMessages()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 批量标记已读
const batchMarkAsRead = async () => {
  const messageIds = selectedMessages.value.map(m => m.messageId)
  try {
    await request.put('/message/read/batch', { messageIds })
    ElMessage.success('批量标记成功')
    loadReceivedMessages()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除消息
const deleteMessage = async (messageId) => {
  try {
    await request.delete(`/message/delete/${messageId}`)
    ElMessage.success('删除成功')
    if (activeTab.value === 'received') {
      loadReceivedMessages()
    } else {
      loadSentMessages()
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 批量删除
const batchDelete = async () => {
  const messageIds = selectedMessages.value.map(m => m.messageId)
  for (const id of messageIds) {
    await deleteMessage(id)
  }
}

// 处理表格选择
const handleSelectionChange = (selection) => {
  selectedMessages.value = selection
}

// 处理行点击
const handleRowClick = (row) => {
  viewMessage(row)
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadReceivedMessages()
  loadMyClubsAndMembers()
})
</script>

<style scoped>
.message-container {
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

.header-actions {
  display: flex;
  align-items: center;
}

.toolbar {
  margin-bottom: 15px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}
</style>
