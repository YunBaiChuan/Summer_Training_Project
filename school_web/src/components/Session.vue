<template>
  <div class="session-history-viewer">
    <div class="container">
      <!-- 左侧会话列表 -->
      <div class="session-list">
        <div class="list-header">
          <h2>我的会话</h2>
        </div>
        <div class="list-content">
          <div v-if="isLoading" class="loading">
            <span class="loading-icon">⏳</span>
            <span>加载中...</span>
          </div>
          <div 
            v-for="session in sessions" 
            :key="session.sessionId"
            class="session-item"
            :class="{ active: selectedSessionId === session.sessionId }"
            @click="selectSession(session)"
          >
            <span class="session-icon">💬</span>
            <div class="session-info">
              <div class="session-title">{{ formatSessionTitle(session) }}</div>
              <div class="session-time">{{ formatTime(session.createTime) }}</div>
            </div>
            
            <!-- 更多操作按钮（三个点） -->
            <div 
              class="more-actions-btn"
              @click.stop="toggleActionMenu(session.sessionId)"
              ref="actionButtons"
            >
              <span class="more-icon">⋮</span>
            </div>
            
            <!-- 二级操作菜单 -->
            <div 
              v-if="activeActionMenu === session.sessionId"
              class="action-menu"
              @click.stop
              ref="actionMenus"
            >
              <div class="menu-item" @click="handleDeleteSession(session)">
                <span class="menu-icon delete-icon">🗑️</span>
                <span class="menu-text">删除会话</span>
              </div>
            </div>
          </div>
           
          <div v-if="sessions.length === 0 && !isLoading" class="empty-list">
            暂无会话记录
          </div>
        </div>
      </div>

      <!-- 右侧消息历史 -->
      <div class="history-content">
        <div class="content-header">
          <h3 v-if="selectedSession">会话详情</h3>
          <h3 v-else>请选择一个会话</h3>
          <div v-if="selectedSession" class="header-actions">
            <button
              class="delete-btn-large"
              @click="handleDeleteCurrentSession"
            >
              <span class="delete-icon">🗑️</span> 删除当前会话
            </button>
          </div>
        </div>
        <div class="content-body">
          <div v-if="loadingHistory" class="loading-history">
            <span class="loading-icon">⏳</span>
            <span>加载历史记录中...</span>
          </div>
          <div v-else-if="selectedSession">
            <div class="session-detail">
              <div class="detail-item">
                <span class="label">会话ID：</span>
                <span class="value">{{ selectedSession.sessionId }}</span>
              </div>
              <div class="detail-item">
                <span class="label">用户ID：</span>
                <span class="value">{{ selectedSession.userId }}</span>
              </div>
              <div class="detail-item">
                <span class="label">创建时间：</span>
                <span class="value">{{ formatTime(selectedSession.createTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="label">会话内容：</span>
                <span class="value">{{ selectedSession.content }}</span>
              </div>
            </div>

            <div class="message-list">
              <h4>历史消息 ({{ messages.length }})</h4>
              <div v-if="messages.length === 0" class="empty-messages">
                暂无消息记录
              </div>
              <div 
                v-for="message in messages" 
                :key="message.id"
                class="message-item"
              >
                <div class="message-header">
                  <span class="sender">{{ getSenderName(message.senderType) }}</span>
                  <span class="time">{{ formatTime(message.createTime) }}</span>
                </div>
                <div class="message-content">
                  {{ message.content }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-selection">
            <span class="no-data-icon">💬</span>
            <p>请从左侧选择一个会话查看详情</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const sessions = ref([])
const messages = ref([])
const selectedSession = ref(null)
const selectedSessionId = ref(null)
const isLoading = ref(false)
const loadingHistory = ref(false)

// 控制哪个会话的操作菜单是打开的
const activeActionMenu = ref(null)
const actionMenus = ref([])
const actionButtons = ref([])

// 获取用户ID
const getUserId = () => {
  return localStorage.getItem('userId') || 'demo'
}

// 格式化会话标题
const formatSessionTitle = (session) => {
  if (session.content && session.content !== '新会话' && session.content !== '已有会话') {
    return session.content.length > 30 ? session.content.substring(0, 30) + '...' : session.content
  }
  return `会话 ${session.sessionId.slice(-8)}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 获取发送者名称
const getSenderName = (senderType) => {
  return senderType === 'user' ? '用户' : '助手'
}

// 切换操作菜单显示/隐藏
const toggleActionMenu = (sessionId) => {
  if (activeActionMenu.value === sessionId) {
    activeActionMenu.value = null
  } else {
    activeActionMenu.value = sessionId
  }
}

// 点击其他地方关闭操作菜单
const handleClickOutside = (event) => {
  // 检查点击是否在操作菜单内或操作按钮上
  let clickedOnMenuOrButton = false
  
  // 检查是否点击在操作菜单上
  actionMenus.value.forEach(menu => {
    if (menu && menu.contains(event.target)) {
      clickedOnMenuOrButton = true
    }
  })
  
  // 检查是否点击在操作按钮上
  actionButtons.value.forEach(button => {
    if (button && button.contains(event.target)) {
      clickedOnMenuOrButton = true
    }
  })
  
  // 如果点击在菜单或按钮之外，关闭所有菜单
  if (!clickedOnMenuOrButton) {
    activeActionMenu.value = null
  }
}

// 加载用户会话列表
const loadUserSessions = async () => {
  isLoading.value = true
  try {
    const userId = getUserId()
    const response = await axios.get('/api/ai/history/sessions', {
      params: { userId }
    })
    
    sessions.value = response.data || []
  } catch (error) {
    console.error('加载会话列表失败:', error)
    alert('加载会话列表失败')
  } finally {
    isLoading.value = false
  }
}

// 选择会话
const selectSession = async (session) => {
  selectedSession.value = session
  selectedSessionId.value = session.sessionId
  
  // 关闭操作菜单
  activeActionMenu.value = null
  
  // 加载该会话的历史消息
  loadSessionHistory(session.sessionId)
}

// 加载会话历史
const loadSessionHistory = async (sessionId) => {
  loadingHistory.value = true
  messages.value = []
  
  try {
    const response = await axios.get('/api/ai/history/chat/history', {
      params: { sessionId }
    })
    
    const data = response.data
    if (data && data.messages) {
      messages.value = data.messages
    }
  } catch (error) {
    console.error('加载历史消息失败:', error)
    alert('加载历史消息失败')
  } finally {
    loadingHistory.value = false
  }
}

// 删除会话
const deleteSession = async (sessionId) => {
  try {
    const userId = getUserId()
    const response = await axios.delete(`/api/ai/history/sessions/${sessionId}`, {
      params: { userId }
    })
    
    if (response.data.success) {
      // 从列表中移除已删除的会话
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId)
      
      // 如果删除的是当前选中的会话，清空选中状态
      if (selectedSessionId.value === sessionId) {
        selectedSession.value = null
        selectedSessionId.value = null
        messages.value = []
      }
      
      // 关闭操作菜单
      activeActionMenu.value = null
      
      return true
    } else {
      throw new Error(response.data.message || '删除失败')
    }
  } catch (error) {
    console.error('删除会话失败:', error)
    throw error
  }
}

// 处理删除会话（从操作菜单）
const handleDeleteSession = async (session) => {
  if (confirm(`确定要删除会话 "${formatSessionTitle(session)}" 吗？此操作将删除该会话的所有消息记录，且不可恢复。`)) {
    try {
      await deleteSession(session.sessionId)
      alert('会话删除成功')
    } catch (error) {
      alert('删除失败: ' + error.message)
    }
  }
}

// 处理删除当前选中的会话（从右侧详情区域）
const handleDeleteCurrentSession = () => {
  if (!selectedSession.value) {
    alert('请先选择一个会话')
    return
  }
  
  handleDeleteSession(selectedSession.value)
}

onMounted(() => {
  loadUserSessions()
  // 添加全局点击事件监听器，用于点击其他地方关闭菜单
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  // 移除全局点击事件监听器
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.session-history-viewer {
  height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

.container {
  display: flex;
  height: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.session-list {
  width: 300px;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.list-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
}

.list-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.list-content {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.loading {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.loading-icon {
  font-size: 24px;
  animation: spin 1s linear infinite;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  margin-bottom: 8px;
  gap: 12px;
  transition: all 0.2s;
  position: relative;
}

.session-item:hover {
  background-color: #f5f7fa;
}

.session-item.active {
  background-color: #ecf5ff;
  border: 1px solid #409eff;
}

.session-icon {
  color: #409eff;
  font-size: 18px;
  flex-shrink: 0;
}

.session-info {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.session-title {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 12px;
  color: #909399;
}

/* 更多操作按钮样式 */
.more-actions-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 4px;
  opacity: 0;
  transition: all 0.2s;
  cursor: pointer;
  flex-shrink: 0;
  z-index: 2;
  position: relative;
  font-weight: bold;
  font-size: 18px;
}

.session-item:hover .more-actions-btn,
.session-item.active .more-actions-btn,
.more-actions-btn:hover {
  opacity: 1;
  background-color: #f0f0f0;
}

.more-actions-btn:hover {
  background-color: #e4e7ed;
}

.more-icon {
  color: #606266;
}

/* 操作菜单样式 */
.action-menu {
  position: absolute;
  right: 8px;
  top: calc(100% - 4px);
  background: white;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  width: 140px;
  z-index: 1000;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
  font-size: 14px;
}

.menu-item:hover {
  background-color: #f5f7fa;
}

.menu-icon {
  margin-right: 8px;
  font-size: 16px;
}

.delete-icon {
  color: #f56c6c;
}

.menu-text {
  color: #303133;
}

.empty-list {
  text-align: center;
  padding: 40px 20px;
  color: #c0c4cc;
  font-size: 14px;
}

.history-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.content-header {
  padding: 16px;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.content-header h3 {
  margin: 0;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.delete-btn-large {
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: background-color 0.2s;
}

.delete-btn-large:hover {
  background-color: #e55a5a;
}

.content-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.loading-history {
  text-align: center;
  padding: 40px 0;
  color: #909399;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.session-detail {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}

.detail-item {
  margin-bottom: 8px;
  font-size: 14px;
}

.detail-item .label {
  color: #606266;
  font-weight: 500;
  display: inline-block;
  width: 80px;
}

.detail-item .value {
  color: #303133;
}

.message-list h4 {
  margin: 0 0 16px 0;
  color: #303133;
  font-size: 16px;
}

.message-item {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #fff;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.message-header .sender {
  color: #409eff;
  font-weight: 500;
}

.message-header .time {
  color: #909399;
}

.message-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  word-wrap: break-word;
}

.empty-messages {
  text-align: center;
  padding: 40px 0;
  color: #c0c4cc;
  font-size: 14px;
}

.no-selection {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
  text-align: center;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>