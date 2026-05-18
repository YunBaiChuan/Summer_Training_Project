<template>
  <div class="second-hand-assistant">
    <div class="chat-container">
      <!-- 左侧历史 -->
      <div class="sidebar">
        <div class="history-header">
          <h2>对话记录</h2>
          <el-button type="primary" size="small" @click="startNewChat">
            + 新对话
          </el-button>
        </div>
        <div class="history-list">
          <div v-if="isLoadingHistory" class="loading-history">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span>加载中...</span>
          </div>
          <div
            v-for="chat in chatHistory"
            :key="chat.id"
            class="history-item"
            :class="{ active: currentChatId === chat.id }"
            @click="loadChat(chat.id)"
          >
            <el-icon><ChatDotRound /></el-icon>
            <span class="title">对话 {{ chat.id.slice(-6) }}</span>
            <span class="time" v-if="chat.lastMessageTime">
              {{ formatTimeToNow(chat.lastMessageTime) }}
            </span>
          </div>
          <div v-if="chatHistory.length === 0 && !isLoadingHistory" class="empty-history">
            暂无对话记录
          </div>
        </div>
      </div>

      <!-- 右侧聊天 -->
      <div class="chat-main">
        <!-- 聊天消息区域 -->
        <div class="chat-header">
          <h2>🤖 AI助手</h2>
          <p class="subtitle">智能对话，为您推荐二手商品</p>
        </div>
        
        <div class="messages" ref="messagesRef">
          <!-- 欢迎消息 -->
          <div v-if="currentMessages.length === 0" class="welcome-message">
            <div class="welcome-card">
              <h3>欢迎使用二手交易助手</h3>
              <p>我可以帮助您：</p>
              <ul class="features-list">
                <li>📦 查找商品信息</li>
                <li>💡 推荐二手商品</li>
                <li>💰 提供交易建议</li>
                <li>❓ 解答相关问题</li>
              </ul>
              <p class="hint">请在下方输入框中输入您的问题...</p>
            </div>
          </div>
          
          <!-- 消息列表 -->
          <div
            v-for="(message, index) in currentMessages"
            :key="index"
            class="message"
            :class="message.role"
          >
            <div class="avatar">
              <el-icon>
                <User v-if="message.role === 'user'" />
                <Service v-else />
              </el-icon>
            </div>
            <div class="message-wrapper">
              <div class="message-header">
                <span class="role-label">{{ message.role === 'user' ? '我' : '小助手' }}</span>
                <span class="message-time" v-if="message.timestamp">
                  {{ formatTimeToNow(message.timestamp) }}
                </span>
              </div>
              <div class="message-content-wrapper">
                <div
                  v-if="message.isStreaming"
                  class="message-content streaming"
                  v-html="getStreamingContent(message)"
                ></div>
                <div
                  v-else
                  class="message-content"
                  v-html="message.content"
                ></div>
              </div>
            </div>
          </div>
          
          <!-- 正在输入提示 -->
          <div v-if="isStreaming && currentMessages.filter(m => m.isStreaming).length === 0" class="message assistant">
            <div class="avatar">
              <el-icon><Service /></el-icon>
            </div>
            <div class="message-wrapper">
              <div class="message-content typing">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
                <span>正在思考中...</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 底部输入 -->
        <div class="input-area">
          <div class="input-container">
            <div class="input-wrapper">
              <textarea
                v-model="userInput"
                @keydown.enter.exact.prevent="sendMessage"
                @keydown.enter.shift.exact.prevent="userInput += '\n'"
                @input="adjustTextareaHeight"
                placeholder="请输入你想找的商品或问题..."
                rows="1"
                ref="inputRef"
                :disabled="isStreaming"
              ></textarea>
            </div>
            <div class="input-actions">
              <el-tooltip content="发送消息 (Enter)" placement="top">
                <el-button
                  class="send-btn"
                  :disabled="isStreaming || !userInput.trim()"
                  @click="sendMessage"
                  :loading="isStreaming"
                >
                  发送
                </el-button>
              </el-tooltip>
            </div>
          </div>
          <div class="input-tips">
            <span>按 Enter 发送，Shift + Enter 换行</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/* ------------------  引入  ------------------ */
import { ref, onMounted, nextTick, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound,
  User,
  Service,
  Paperclip,
  Position,
  Loading
} from '@element-plus/icons-vue'

import { formatTimeToNow } from '@/utils/date'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { tradeAPI } from '@/api/trade-assistant'

/* ------------------  配置  ------------------ */
marked.setOptions({ breaks: true, gfm: true })

/* ------------------  响应式数据  ------------------ */
const messagesRef = ref(null)
const inputRef = ref(null)
const userInput = ref('')
const isStreaming = ref(false)
const currentChatId = ref(null)
const currentMessages = ref([])
const chatHistory = ref([])
const isLoadingHistory = ref(false)

// SSE相关
let currentReader = null
let currentDecoder = null

/* ------------------  方法  ------------------ */
// 新增：获取流式内容
const getStreamingContent = (message) => {
  const cursor = '<span class="cursor">▌</span>'
  return (message.content || '') + cursor
}

const adjustTextareaHeight = () => {
  const ta = inputRef.value
  if (!ta) return
  ta.style.height = 'auto'
  const height = Math.min(ta.scrollHeight, 150)
  ta.style.height = height + 'px'
}

const scrollToBottom = async () => {
  await nextTick()
  const box = messagesRef.value
  if (box) {
    box.scrollTop = box.scrollHeight
  }
}

const getPlaceholder = () => '请输入你想找的商品或问题...'

const formatMessage = (content) => {
  if (!content) return ''
  if (content.startsWith('<')) return DOMPurify.sanitize(content)
  
  // 添加代码高亮支持
  const renderer = new marked.Renderer()
  renderer.code = function(code, language) {
    return `<pre><code class="language-${language || 'text'}">${DOMPurify.sanitize(code)}</code></pre>`
  }
  
  const html = marked(content, { renderer })
  return DOMPurify.sanitize(html)
}

/* 发送消息（简化版） */
/* 发送消息（支持SSE格式） */
const sendMessage = async () => {
  if (isStreaming.value || !userInput.value.trim()) return
  if (!currentChatId.value) {
    await startNewChat()
  }

  const content = userInput.value.trim()
  userInput.value = ''
  adjustTextareaHeight()

  // 保存用户消息
  const userMsg = { 
    role: 'user', 
    content: DOMPurify.sanitize(content), 
    timestamp: new Date(), 
    isStreaming: false 
  }
  currentMessages.value.push(userMsg)
  await scrollToBottom()

  // 创建AI消息占位符
  const assistantMsg = { 
    role: 'assistant', 
    content: '', 
    timestamp: new Date(), 
    isStreaming: true 
  }
  currentMessages.value.push(assistantMsg)
  isStreaming.value = true

  try {
    // 使用API方法发送消息
    const streamBody = await tradeAPI.sendStreamMessage({ 
      content, 
      chatId: currentChatId.value 
    })
    
    if (!streamBody) {
      throw new Error('未获取到响应流')
    }

    const reader = streamBody.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''
    let accumulatedContent = ''

    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        console.log('流式响应结束')
        break
      }

      // 解码并添加到缓冲区
      buffer += decoder.decode(value, { stream: true })
      
      // 按行分割处理SSE格式
      const lines = buffer.split('\n')
      
      // 保留最后一行（可能是不完整的行）
      buffer = lines.pop() || ''
      
      for (const line of lines) {
        // 处理SSE格式的行
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim() // 去掉"data:"前缀
          
          // 跳过空消息或结束标记
          if (data === '[DONE]') {
            continue
          }
          
          try {
            // 这里假设服务器返回的是纯文本，如果返回JSON可以解析
            // const parsedData = JSON.parse(data)
            // if (parsedData.content) {
            //   accumulatedContent += parsedData.content
            // }
            
            // 直接拼接文本内容
            if (data) {
              accumulatedContent += data
            }
          } catch (e) {
            console.warn('解析SSE数据失败:', e)
            // 如果解析失败，尝试直接使用原始数据
            if (data && data !== '[DONE]') {
              accumulatedContent += data
            }
          }
        }
      }
      
      // 实时更新消息内容
      if (accumulatedContent) {
        assistantMsg.content = formatMessage(accumulatedContent)
        currentMessages.value = [...currentMessages.value]
        await scrollToBottom()
      }
    }

    // 流结束后处理
    assistantMsg.isStreaming = false
    assistantMsg.content = formatMessage(accumulatedContent).trim()
    assistantMsg.timestamp = new Date()
    currentMessages.value = [...currentMessages.value]
    
    // 更新聊天历史的时间
    const chat = chatHistory.value.find(c => c.id === currentChatId.value)
    if (chat) {
      chat.lastMessageTime = new Date()
    }

  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('发送失败，请稍后再试哦~')
    assistantMsg.isStreaming = false
    assistantMsg.content = '抱歉，发生了错误，请稍后再试。'
    currentMessages.value = [...currentMessages.value]
  } finally {
    isStreaming.value = false
    await scrollToBottom()
    
    // 重新聚焦输入框
    nextTick(() => {
      if (inputRef.value) {
        inputRef.value.focus()
      }
    })
  }
}

// 获取用户ID - 根据你的实际用户系统调整
const getUserId = () => {
  // 这里应该从你的用户系统中获取
  // 暂时使用一个默认值或从localStorage获取
  return localStorage.getItem('userId') || 'default_user'
}

/* 加载聊天历史记录 */
const loadChatHistory = async () => {
  isLoadingHistory.value = true
  try {
    // 这里应该调用你的API获取历史对话列表
    // 暂时模拟数据
    const mockHistory = [
      { id: '1', lastMessageTime: new Date(Date.now() - 3600000) },
    ]
    
    chatHistory.value = mockHistory.map(item => ({
      id: item.id.toString(),
      title: `对话 ${item.id.toString().slice(-6)}`,
      lastMessageTime: item.lastMessageTime,
      messages: []
    }))
    
    if (chatHistory.value.length > 0) {
      await loadChat(chatHistory.value[0].id)
    } else {
      startNewChat()
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载历史记录失败')
    startNewChat()
  } finally {
    isLoadingHistory.value = false
  }
}

/* 加载特定聊天 */
const loadChat = async (chatId) => {
  // 保存当前聊天记录
  if (currentChatId.value && currentMessages.value.length > 0) {
    const existingChat = chatHistory.value.find(c => c.id === currentChatId.value)
    if (existingChat) {
      existingChat.messages = [...currentMessages.value]
    }
  }

  // 设置新的聊天ID
  currentChatId.value = chatId.toString()
  
  // 查找是否已有缓存的消息
  const targetChat = chatHistory.value.find(c => c.id === chatId)
  
  if (targetChat && targetChat.messages && targetChat.messages.length > 0) {
    // 使用缓存的消息
    currentMessages.value = targetChat.messages
  } else {
    // 从服务器加载消息
    try {
      // 这里应该调用你的API获取聊天消息
      // 暂时模拟数据
      currentMessages.value = []
    } catch (error) {
      console.error('加载聊天消息失败:', error)
      currentMessages.value = []
    }
    
    // 更新缓存
    if (targetChat) {
      targetChat.messages = [...currentMessages.value]
    }
  }
  
  await scrollToBottom()
}

/* 开始新对话 */
const startNewChat = () => {
  // 保存当前聊天记录
  if (currentChatId.value && currentMessages.value.length > 0) {
    const existingChat = chatHistory.value.find(c => c.id === currentChatId.value)
    if (existingChat) {
      existingChat.messages = [...currentMessages.value]
    }
  }

  // 创建新对话
  const newChatId = `chat_${Date.now()}`
  const newChat = {
    id: newChatId,
    title: `对话 ${newChatId.slice(-6)}`,
    lastMessageTime: new Date(),
    messages: []
  }
  
  chatHistory.value.unshift(newChat)
  currentChatId.value = newChatId
  currentMessages.value = []
}

/* 监听输入变化自动调整高度 */
watch(userInput, () => {
  nextTick(() => adjustTextareaHeight())
})

/* ------------------  生命周期  ------------------ */
onMounted(() => {
  loadChatHistory()
  adjustTextareaHeight()
  
  // 初始聚焦输入框
  nextTick(() => {
    if (inputRef.value) {
      inputRef.value.focus()
    }
  })
})

onUnmounted(() => {
  // 清理SSE连接
  if (currentReader) {
    currentReader.cancel()
    currentReader = null
  }
})
</script>

<style lang="scss" scoped>
.second-hand-assistant {
  position: fixed;
  top: 0;
  left: 270px;
  right: 0;
  bottom: 0;
  background: #f5f7fa;
  overflow: hidden;
  
  transform-origin: top left; 
  transform: scale(0.95); 
}

.chat-container {
  display: flex;
  height: 100vh;
  max-width: 1400px;
  margin: 0 auto;
  overflow: hidden;
}

/* 左侧侧边栏 */
.sidebar {
  width: 280px;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  flex-shrink: 0;

  .history-header {
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px solid #e4e7ed;

    h2 {
      font-size: 18px;
      color: #303133;
      margin: 0;
      font-weight: 600;
    }
  }

  .history-list {
    flex: 1;
    overflow-y: auto;
    padding: 16px;

    .loading-history {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 40px 0;
      color: #909399;
      flex-direction: column;
      gap: 12px;
      
      .loading-icon {
        animation: spin 1s linear infinite;
        font-size: 24px;
      }
    }

    .empty-history {
      text-align: center;
      padding: 40px 20px;
      color: #c0c4cc;
      font-size: 14px;
    }

    .history-item {
      display: flex;
      align-items: center;
      padding: 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
      margin-bottom: 8px;
      border: 1px solid transparent;
      gap: 12px;

      &:hover {
        background: #f5f7fa;
        border-color: #e4e7ed;
      }

      &.active {
        background: #ecf5ff;
        border-color: #409eff;
        
        .el-icon {
          color: #409eff;
        }
        
        .title {
          color: #409eff;
          font-weight: 500;
        }
      }

      .el-icon {
        font-size: 18px;
        color: #909399;
        flex-shrink: 0;
      }

      .title {
        flex: 1;
        font-size: 14px;
        color: #606266;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .time {
        font-size: 12px;
        color: #c0c4cc;
        flex-shrink: 0;
      }
    }
  }
}

/* 右侧聊天区域 */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  overflow: hidden;

  .chat-header {
    padding: 20px 24px;
    border-bottom: 1px solid #e4e7ed;
    background: #fff;
    flex-shrink: 0;

    h2 {
      font-size: 24px;
      color: #303133;
      margin: 0 0 8px 0;
      font-weight: 600;
    }

    .subtitle {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .messages {
    flex: 1;
    overflow-y: auto;
    padding: 24px;
    background: #fafbfc;
    display: flex;
    flex-direction: column;
    gap: 20px;

    .welcome-message {
      display: flex;
      justify-content: center;
      align-items: center;
      flex: 1;

      .welcome-card {
        max-width: 500px;
        width: 100%;
        padding: 40px 32px;
        background: #fff;
        border-radius: 16px;
        text-align: center;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);

        h3 {
          font-size: 24px;
          color: #303133;
          margin-bottom: 20px;
          font-weight: 600;
        }

        p {
          font-size: 15px;
          color: #606266;
          margin-bottom: 16px;
          line-height: 1.5;
        }

        .features-list {
          list-style: none;
          padding: 0;
          margin: 24px 0;
          text-align: left;
          display: inline-block;

          li {
            margin-bottom: 12px;
            font-size: 15px;
            color: #606266;
            padding-left: 24px;
            position: relative;

            &::before {
              content: "•";
              position: absolute;
              left: 8px;
              color: #409eff;
              font-size: 20px;
            }
          }
        }

        .hint {
          color: #909399;
          font-size: 14px;
          margin-top: 24px;
          padding-top: 20px;
          border-top: 1px solid #e4e7ed;
        }
      }
    }

    .message {
      display: flex;
      gap: 16px;
      max-width: 800px;

      &.user {
        align-self: flex-end;
        flex-direction: row-reverse;

        .message-wrapper {
          align-items: flex-end;
        }

        .avatar {
          background: #079ce1;
          
          .el-icon {
            color: #1890ff;
          }
        }

        .message-content {
          background: linear-gradient(135deg, #1890ff, #40a9ff);
          color: #fff;
          border-radius: 18px 4px 18px 18px;
          text-align: left;
        }
      }

      &.assistant {
        align-self: flex-start;

        .message-wrapper {
          align-items: flex-start;
        }

        .avatar {
          background: #51b915;
          
          .el-icon {
            color: #666;
          }
        }

        .message-content {
          background: #fff;
          border: 1px solid #e4e7ed;
          border-radius: 4px 18px 18px 18px;
          color: #303133;
          text-align: left;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        }
      }

      .avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        margin-top: 4px;

        .el-icon {
          font-size: 20px;
        }
      }

      .message-wrapper {
        display: flex;
        flex-direction: column;
        gap: 8px;
        max-width: 600px;

        .message-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          width: 100%;

          .role-label {
            font-size: 12px;
            color: #909399;
            font-weight: 500;
          }

          .message-time {
            font-size: 12px;
            color: #c0c4cc;
          }
        }

        .message-content-wrapper {
          position: relative;
        }

        .message-content {
          padding: 12px 16px;
          font-size: 14px;
          line-height: 1.6;
          word-wrap: break-word;
          white-space: pre-wrap;

          &.streaming {
            min-height: 20px;

            .cursor {
              display: inline-block;
              animation: blink 1s infinite;
              color: currentColor;
              font-weight: bold;
            }
          }

          &.typing {
            color: #909399;
            display: flex;
            align-items: center;
            gap: 8px;

            .typing-indicator {
              display: flex;
              gap: 4px;

              span {
                width: 6px;
                height: 6px;
                background: #909399;
                border-radius: 50%;
                animation: typing 1.4s infinite ease-in-out;

                &:nth-child(1) { animation-delay: 0s; }
                &:nth-child(2) { animation-delay: 0.2s; }
                &:nth-child(3) { animation-delay: 0.4s; }
              }
            }
          }

          :deep(p) {
            margin: 0.5em 0;
            text-align: left;
          }

          :deep(pre) {
            background: #f8f9fa;
            padding: 16px;
            border-radius: 8px;
            overflow-x: auto;
            margin: 12px 0;
            border: 1px solid #e9ecef;
            text-align: left;

            code {
              font-family: 'JetBrains Mono', Consolas, Monaco, 'Andale Mono', monospace;
              font-size: 13px;
              line-height: 1.5;
              color: #495057;
              text-align: left;
            }
          }

          :deep(code:not(pre code)) {
            background: #f1f3f5;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 13px;
            color: #d63384;
            font-family: 'JetBrains Mono', Consolas, Monaco, 'Andale Mono', monospace;
          }

          :deep(ul), :deep(ol) {
            margin: 0.5em 0;
            padding-left: 1.5em;

            li {
              margin: 0.3em 0;
            }
          }

          :deep(blockquote) {
            margin: 12px 0;
            padding: 10px 16px;
            border-left: 4px solid #42b983;
            background: #f8f9fa;
            color: #495057;

            p {
              margin: 0.3em 0;
            }
          }

          :deep(table) {
            border-collapse: collapse;
            margin: 12px 0;
            width: 100%;

            th, td {
              border: 1px solid #dee2e6;
              padding: 8px 12px;
            }

            th {
              background: #f8f9fa;
            }

            tr:nth-child(2n) {
              background: #f8f9fa;
            }
          }
        }
      }
    }
  }

  .input-area {
    padding: 20px 24px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
    flex-shrink: 0;

    .input-container {
      display: flex;
      align-items: flex-end;
      gap: 12px;
      margin-bottom: 12px;

      .input-wrapper {
        flex: 1;
        background: #f5f7fa;
        border-radius: 8px;
        border: 1px solid #dcdfe6;
        transition: all 0.3s;
        padding: 8px 12px;

        &:focus-within {
          border-color: #409eff;
          box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
        }

        textarea {
          width: 100%;
          border: none;
          background: transparent;
          font-size: 14px;
          line-height: 1.5;
          color: #303133;
          resize: none;
          min-height: 24px;
          max-height: 120px;
          padding: 0;
          font-family: inherit;

          &:focus {
            outline: none;
          }

          &::placeholder {
            color: #c0c4cc;
          }

          &:disabled {
            cursor: not-allowed;
            color: #c0c4cc;
          }
        }
      }

      .input-actions {
        display: flex;
        gap: 8px;
        align-items: center;
        flex-shrink: 0;

        .attach-btn, .send-btn {
          width: 50px;
          height: 50px;
          padding: 0;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
        }

        .send-btn {
          background: #409eff;
          border-color: #409eff;
          color: white;

          &:hover:not(:disabled) {
            background: #66b1ff;
            border-color: #66b1ff;
          }

          &:disabled {
            background: #c0c4cc;
            border-color: #c0c4cc;
            cursor: not-allowed;
          }
        }
      }
    }

    .input-tips {
      text-align: center;

      span {
        font-size: 12px;
        color: #c0c4cc;
      }
    }
  }
}

/* 动画 */
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.6; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .sidebar {
    width: 240px;
  }
  
  .chat-main .messages .message {
    max-width: 90%;
  }
}

@media (max-width: 768px) {
  .chat-container {
    flex-direction: column;
  }
  
  .sidebar {
    width: 100%;
    height: auto;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .chat-main {
    flex: 1;
    min-height: 0;
  }
  
  .chat-main .messages .message {
    max-width: 100%;
  }
}
</style>