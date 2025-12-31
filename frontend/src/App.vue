<template>
  <div class="app-container" :class="{ 'chat-active': hasStarted, 'panel-visible': showAgentPanel }">
    <!-- 左侧对话区域 -->
    <div class="chat-section">
      <!-- 欢迎界面 (未开始对话时) -->
      <div v-if="!hasStarted" class="welcome-screen">
        <div class="welcome-content">
          <div class="logo">🤖</div>
          <h1>Crypto Agent</h1>
          <p>智能加密货币分析助手，为您提供专业的市场洞察</p>
        </div>
      </div>

      <!-- 对话内容区域 -->
      <div v-else class="chat-messages" ref="messagesContainer">
        <div v-for="(msg, index) in messages" :key="index" 
             :class="['message', msg.role]">
          <div class="message-avatar">
            {{ msg.role === 'user' ? '👤' : '🤖' }}
          </div>
          <div class="message-content" v-html="renderMarkdown(msg.content)"></div>
        </div>
        <div v-if="isLoading && !currentResponse" class="message assistant">
          <div class="message-avatar">🤖</div>
          <div class="message-content loading-dots">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-section" :class="{ centered: !hasStarted }">
        <div class="input-wrapper">
          <textarea 
            v-model="inputMessage" 
            @keydown.enter.exact.prevent="sendMessage"
            placeholder="输入您的问题，例如：分析一下BTC近期走势..."
            :disabled="isLoading"
            rows="1"
            ref="inputRef"
          ></textarea>
          <button @click="sendMessage" :disabled="isLoading || !inputMessage.trim()" class="send-btn">
            <span v-if="isLoading">⏳</span>
            <span v-else>发送</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 右侧智能体状态面板 -->
    <transition name="slide">
      <div v-if="showAgentPanel" class="agent-panel">
        <div class="panel-header">
          <h3>🔄 智能体执行状态</h3>
        </div>
        <div class="agent-steps">
          <div v-for="(step, index) in agentSteps" :key="index" 
               :class="['step-item', step.status]">
            <div class="step-icon">{{ step.icon }}</div>
            <div class="step-info">
              <div class="step-name">{{ step.name }}</div>
              <div class="step-desc">{{ step.description }}</div>
            </div>
            <div class="step-status-icon">
              <span v-if="step.status === 'completed'">✅</span>
              <span v-else-if="step.status === 'running'" class="spinner">⚙️</span>
              <span v-else>⏸️</span>
            </div>
          </div>
        </div>
        <div v-if="currentAgentOutput" class="current-output">
          <h4>当前输出</h4>
          <div class="output-content" v-html="renderMarkdown(currentAgentOutput)"></div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { marked } from 'marked'

const inputMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const hasStarted = ref(false)
const showAgentPanel = ref(false)
const currentResponse = ref('')
const currentAgentOutput = ref('')
const messagesContainer = ref(null)
const inputRef = ref(null)

const agentSteps = reactive([
  { name: '协调智能体', icon: '📋', description: '任务规划与分解', status: 'pending' },
  { name: '研究智能体', icon: '🔍', description: '数据收集与研究', status: 'pending' },
  { name: '分析智能体', icon: '🧠', description: '深度分析处理', status: 'pending' },
  { name: '合成智能体', icon: '✨', description: '生成最终报告', status: 'pending' }
])

const renderMarkdown = (text) => {
  if (!text) return ''
  return marked.parse(text)
}

const resetAgentSteps = () => {
  agentSteps.forEach(step => step.status = 'pending')
}

const updateAgentStatus = (chunk) => {
  // 根据每个chunk内容判断当前执行到哪个智能体
  if (chunk.includes('开始分析您的问题')) {
    agentSteps[0].status = 'running'
  }
  if (chunk.includes('任务规划完成')) {
    agentSteps[0].status = 'completed'
  }
  if (chunk.includes('开始数据研究')) {
    agentSteps[1].status = 'running'
  }
  if (chunk.includes('数据研究完成')) {
    agentSteps[1].status = 'completed'
  }
  if (chunk.includes('开始深度分析')) {
    agentSteps[2].status = 'running'
  }
  if (chunk.includes('深度分析完成')) {
    agentSteps[2].status = 'completed'
  }
  if (chunk.includes('开始生成最终报告')) {
    agentSteps[3].status = 'running'
  }
  if (chunk.includes('最终报告')) {
    agentSteps[3].status = 'completed'
  }
}

const extractFinalReport = (fullResponse) => {
  // 提取最终报告内容
  const reportIndex = fullResponse.lastIndexOf('**最终报告**')
  if (reportIndex !== -1) {
    return fullResponse.substring(reportIndex)
  }
  return fullResponse
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const sendMessage = async () => {
  const message = inputMessage.value.trim()
  if (!message || isLoading.value) return

  hasStarted.value = true
  showAgentPanel.value = true
  isLoading.value = true
  resetAgentSteps()
  
  messages.value.push({ role: 'user', content: message })
  inputMessage.value = ''
  currentResponse.value = ''
  currentAgentOutput.value = ''
  
  await scrollToBottom()

  try {
    const response = await fetch(`/agent/chat?message=${encodeURIComponent(message)}&sessionId=${Date.now()}`, {
      method: 'GET',
      headers: { 'Accept': 'text/event-stream' }
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let fullResponse = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const chunk = decoder.decode(value, { stream: true })
      const lines = chunk.split('\n')
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5).trim()
          if (data) {
            fullResponse += data
            currentResponse.value = fullResponse
            currentAgentOutput.value = data
            updateAgentStatus(data)
            await scrollToBottom()
          }
        }
      }
    }

    // 提取最终报告作为消息内容
    const finalReport = extractFinalReport(fullResponse)
    messages.value.push({ role: 'assistant', content: finalReport })
    currentResponse.value = ''
    
    // 执行完成后收起右侧面板
    setTimeout(() => {
      showAgentPanel.value = false
    }, 1500)
    
  } catch (error) {
    console.error('请求失败:', error)
    messages.value.push({ role: 'assistant', content: '❌ 请求失败，请稍后重试' })
    showAgentPanel.value = false
  } finally {
    isLoading.value = false
    await scrollToBottom()
  }
}

onMounted(() => {
  inputRef.value?.focus()
})
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  transition: all 0.3s ease;
}

.chat-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.chat-active .chat-section {
  flex: 1;
}

.chat-active.panel-visible .chat-section {
  flex: 0 0 60%;
}

/* 欢迎界面 */
.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content {
  text-align: center;
  animation: fadeIn 0.5s ease;
}

.logo {
  font-size: 80px;
  margin-bottom: 20px;
}

.welcome-content h1 {
  font-size: 2.5rem;
  background: linear-gradient(90deg, #60a5fa, #a78bfa);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 12px;
}

.welcome-content p {
  color: #9ca3af;
  font-size: 1.1rem;
}

/* 对话消息区域 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  gap: 12px;
  max-width: 85%;
  animation: slideUp 0.3s ease;
}

.message.user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.message-content {
  background: rgba(255, 255, 255, 0.08);
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.6;
}

.message.user .message-content {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
}

.message-content :deep(p) {
  margin: 0 0 8px 0;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(code) {
  background: rgba(0, 0, 0, 0.3);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}

.message-content :deep(pre) {
  background: rgba(0, 0, 0, 0.3);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

/* 加载动画 */
.loading-dots {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}

.loading-dots span {
  width: 8px;
  height: 8px;
  background: #60a5fa;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }

/* 输入区域 */
.input-section {
  padding: 20px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.input-section.centered {
  position: absolute;
  bottom: 30%;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 700px;
  border: none;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 16px;
  padding: 8px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: border-color 0.2s;
}

.input-wrapper:focus-within {
  border-color: #3b82f6;
}

.input-wrapper textarea {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #e4e4e7;
  font-size: 1rem;
  padding: 10px 12px;
  resize: none;
  font-family: inherit;
}

.input-wrapper textarea::placeholder {
  color: #6b7280;
}

.send-btn {
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.02);
  box-shadow: 0 4px 20px rgba(59, 130, 246, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 右侧智能体面板 */
.agent-panel {
  width: 40%;
  background: rgba(0, 0, 0, 0.2);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.panel-header h3 {
  font-size: 1.1rem;
  color: #e4e4e7;
}

.agent-steps {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  transition: all 0.3s ease;
}

.step-item.running {
  background: rgba(59, 130, 246, 0.2);
  border: 1px solid rgba(59, 130, 246, 0.4);
}

.step-item.completed {
  background: rgba(34, 197, 94, 0.15);
  border: 1px solid rgba(34, 197, 94, 0.3);
}

.step-icon {
  font-size: 24px;
}

.step-info {
  flex: 1;
}

.step-name {
  font-weight: 600;
  margin-bottom: 2px;
}

.step-desc {
  font-size: 0.85rem;
  color: #9ca3af;
}

.step-status-icon {
  font-size: 18px;
}

.spinner {
  animation: spin 1s linear infinite;
  display: inline-block;
}

.current-output {
  flex: 1;
  padding: 16px;
  overflow-y: auto;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.current-output h4 {
  margin-bottom: 12px;
  color: #9ca3af;
  font-size: 0.9rem;
}

.output-content {
  font-size: 0.9rem;
  line-height: 1.6;
  color: #d1d5db;
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.slide-enter-active, .slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from, .slide-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

/* 响应式 */
@media (max-width: 768px) {
  .app-container {
    flex-direction: column;
  }
  
  .chat-active .chat-section {
    flex: 0 0 50%;
  }
  
  .agent-panel {
    width: 100%;
    height: 50%;
  }
}
</style>
