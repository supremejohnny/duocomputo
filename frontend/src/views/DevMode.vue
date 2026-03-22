<template>
  <div class="dev">
    <div class="top-bar">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <div class="dev-badge">⚙️ Dev Mode</div>
    </div>

    <!-- Section A: API Tester -->
    <section class="section">
      <h2 class="section-title">API 接口测试</h2>
      <div class="api-list">
        <div v-for="endpoint in endpoints" :key="endpoint.label" class="api-card">
          <div class="api-header">
            <span class="method-badge" :class="endpoint.method.toLowerCase()">{{ endpoint.method }}</span>
            <span class="api-path">{{ endpoint.path }}</span>
            <span class="api-label">{{ endpoint.label }}</span>
            <button
              class="run-btn"
              :disabled="endpoint.disabled || endpoint.loading"
              @click="runEndpoint(endpoint)"
            >
              {{ endpoint.loading ? '...' : '执行' }}
            </button>
          </div>
          <div v-if="endpoint.disabled" class="api-note">通过下方「添加题目」表单测试</div>
          <div v-if="endpoint.result !== undefined" class="api-result">
            <pre>{{ formatJson(endpoint.result) }}</pre>
          </div>
          <div v-if="endpoint.error" class="api-error">{{ endpoint.error }}</div>
        </div>
      </div>
    </section>

    <!-- Section B: Frequency Test -->
    <section class="section">
      <h2 class="section-title">选题频率测试</h2>
      <div class="freq-card">
        <div class="freq-controls">
          <label class="form-label">模拟轮数</label>
          <input v-model.number="freqRounds" type="number" class="text-input freq-input" min="10" max="10000" />
          <button class="run-btn" :disabled="freqLoading" @click="runFrequencyTest">
            {{ freqLoading ? '运行中...' : '运行' }}
          </button>
        </div>
        <div v-if="freqError" class="api-error">{{ freqError }}</div>

        <div v-if="freqResult" class="freq-results">
          <div class="freq-info">共模拟 {{ freqResult.rounds }} 轮</div>

          <h3 class="freq-subtitle">MC 选择题（每轮选 2 题）</h3>
          <div class="freq-table">
            <div v-for="item in freqResult.mcResults" :key="item.id" class="freq-row">
              <div class="freq-title" :title="item.title">{{ truncate(item.title, 28) }}</div>
              <div class="freq-meta">
                <span class="freq-priority">P: {{ item.priority.toFixed(1) }}</span>
                <span class="freq-count">{{ item.count }}次</span>
                <span class="freq-pct">{{ item.percentage }}%</span>
              </div>
              <div class="freq-bar-bg">
                <div class="freq-bar" :style="{ width: item.percentage + '%' }"></div>
              </div>
            </div>
          </div>

          <h3 class="freq-subtitle">Flashcard 闪卡（每轮选 1 题）</h3>
          <div class="freq-table">
            <div v-for="item in freqResult.fcResults" :key="item.id" class="freq-row">
              <div class="freq-title" :title="item.title">{{ truncate(item.title, 28) }}</div>
              <div class="freq-meta">
                <span class="freq-priority">P: {{ item.priority.toFixed(1) }}</span>
                <span class="freq-count">{{ item.count }}次</span>
                <span class="freq-pct">{{ item.percentage }}%</span>
              </div>
              <div class="freq-bar-bg">
                <div class="freq-bar fc" :style="{ width: item.percentage + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Section C: Add Question -->
    <section class="section">
      <h2 class="section-title">添加题目</h2>
      <div class="form-card">
        <!-- Type -->
        <div class="form-row">
          <label class="form-label">类型</label>
          <div class="radio-group">
            <label class="radio-label">
              <input type="radio" v-model="form.type" value="mc" /> MC 选择题
            </label>
            <label class="radio-label">
              <input type="radio" v-model="form.type" value="flashcard" /> 闪卡
            </label>
          </div>
        </div>

        <!-- Common fields -->
        <div class="form-row">
          <label class="form-label">分类</label>
          <input v-model="form.category" class="text-input" placeholder="例如 计算机网络、Java 基础" />
        </div>
        <div class="form-row">
          <label class="form-label">难度</label>
          <select v-model="form.difficulty" class="select-input">
            <option value="easy">easy</option>
            <option value="medium">medium</option>
          </select>
        </div>
        <div class="form-row">
          <label class="form-label">题目</label>
          <textarea v-model="form.question" class="textarea-input" rows="3" placeholder="输入题目..." />
        </div>

        <!-- MC-only fields -->
        <template v-if="form.type === 'mc'">
          <div class="form-row" v-for="(_, i) in form.options" :key="i">
            <label class="form-label">选项 {{ ['A','B','C','D'][i] }}</label>
            <input v-model="form.options[i]" class="text-input" :placeholder="`选项 ${['A','B','C','D'][i]}`" />
          </div>
          <div class="form-row">
            <label class="form-label">正确答案</label>
            <select v-model="form.correctAnswerIdx" class="select-input">
              <option value="">-- 请选择 --</option>
              <option v-for="(opt, i) in filledOptions" :key="i" :value="['A','B','C','D'][i]">
                {{ ['A','B','C','D'][i] }}. {{ opt }}
              </option>
            </select>
          </div>
          <div class="form-row">
            <label class="form-label">解析</label>
            <textarea v-model="form.explanation" class="textarea-input" rows="3" placeholder="答案解析..." />
          </div>
        </template>

        <!-- Flashcard-only fields -->
        <template v-if="form.type === 'flashcard'">
          <div class="form-row">
            <label class="form-label">答案</label>
            <textarea v-model="form.answer" class="textarea-input" rows="5" placeholder="详细答案..." />
          </div>
          <div class="form-row">
            <label class="form-label">解析</label>
            <textarea v-model="form.explanation" class="textarea-input" rows="2" placeholder="学习提示..." />
          </div>
        </template>

        <button class="submit-btn" :disabled="submitLoading" @click="submitQuestion">
          {{ submitLoading ? '提交中...' : '提交题目' }}
        </button>

        <div v-if="submitResult !== undefined" class="api-result">
          <div class="success-msg">✅ 添加成功（ID 由后端生成）</div>
          <pre>{{ formatJson(submitResult) }}</pre>
        </div>
        <div v-if="submitError" class="api-error">{{ submitError }}</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import api from '../api'

// Section A
const endpoints = reactive([
  { label: '打卡连续天数',    method: 'GET',  path: '/api/checkin/streak',        call: () => api.getStreak() },
  { label: '今日任务',        method: 'GET',  path: '/api/daily',                 call: () => api.getDailyTask() },
  { label: '所有 MC 题目',    method: 'GET',  path: '/api/mc-questions',          call: () => api.getAllMcQuestions() },
  { label: '所有闪卡题目',    method: 'GET',  path: '/api/flashcard-questions',   call: () => api.getAllFlashcardQuestions() },
  { label: '今日打卡记录',    method: 'GET',  path: '/api/checkin/today',         call: () => api.getTodayRecord() },
  { label: '历史打卡记录',    method: 'GET',  path: '/api/checkin/records',       call: () => api.getCheckinRecords() },
  { label: '添加 MC 题目',    method: 'POST', path: '/api/mc-questions',          disabled: true },
  { label: '添加闪卡题目',    method: 'POST', path: '/api/flashcard-questions',   disabled: true },
])

async function runEndpoint(endpoint) {
  endpoint.loading = true
  endpoint.result = undefined
  endpoint.error = undefined
  try {
    const res = await endpoint.call()
    endpoint.result = res.data
  } catch (e) {
    endpoint.error = e.message || '请求失败'
  }
  endpoint.loading = false
}

function formatJson(val) {
  return JSON.stringify(val, null, 2)
}

// Section B: Frequency Test
const freqRounds = ref(1000)
const freqResult = ref(null)
const freqLoading = ref(false)
const freqError = ref('')

async function runFrequencyTest() {
  freqLoading.value = true
  freqResult.value = null
  freqError.value = ''
  try {
    const res = await api.getFrequencyTest(freqRounds.value)
    freqResult.value = res.data
  } catch (e) {
    freqError.value = e.message || '测试失败'
  }
  freqLoading.value = false
}

function truncate(str, len) {
  return str && str.length > len ? str.slice(0, len) + '...' : str
}

// Section C
const form = reactive({
  type: 'mc',
  category: '',
  difficulty: 'easy',
  question: '',
  options: ['', '', '', ''],
  correctAnswerIdx: '',
  explanation: '',
  answer: '',
})

const filledOptions = computed(() => form.options.filter(o => o.trim()))

const submitLoading = ref(false)
const submitResult = ref(undefined)
const submitError = ref('')

async function submitQuestion() {
  submitResult.value = undefined
  submitError.value = ''

  if (!form.category.trim() || !form.question.trim()) {
    submitError.value = '请填写分类和题目内容'
    return
  }

  if (form.type === 'mc') {
    const filled = form.options.map(o => o.trim()).filter(Boolean)
    if (filled.length < 2) {
      submitError.value = '请至少填写 2 个选项'
      return
    }
    if (!form.correctAnswerIdx) {
      submitError.value = '请选择正确答案'
      return
    }

    const payload = {
      type: 'choice',
      category: form.category.trim(),
      difficulty: form.difficulty,
      question: form.question.trim(),
      options: form.options
        .map((text, i) => ({ id: ['A','B','C','D'][i], text: text.trim() }))
        .filter(o => o.text),
      correctAnswerIdx: form.correctAnswerIdx,
      explanation: form.explanation.trim(),
    }

    submitLoading.value = true
    try {
      const res = await api.addMcQuestion(payload)
      submitResult.value = res.data
      Object.assign(form, { category: '', question: '', options: ['','','',''], correctAnswerIdx: '', explanation: '' })
    } catch (e) {
      submitError.value = e.message || '提交失败'
    }
  } else {
    if (!form.answer.trim()) {
      submitError.value = '请填写答案'
      return
    }

    const payload = {
      type: 'flashcard',
      category: form.category.trim(),
      difficulty: form.difficulty,
      content: form.question.trim(),
      answer: form.answer.trim(),
      explanation: form.explanation.trim(),
    }

    submitLoading.value = true
    try {
      const res = await api.addFlashcardQuestion(payload)
      submitResult.value = res.data
      Object.assign(form, { category: '', question: '', answer: '', explanation: '' })
    } catch (e) {
      submitError.value = e.message || '提交失败'
    }
  }

  submitLoading.value = false
}
</script>

<style scoped>
.dev {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
}

.back-btn {
  color: #777;
  text-decoration: none;
  font-size: 14px;
}

.dev-badge {
  font-size: 13px;
  background: #fff3cd;
  color: #856404;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.section {
  margin-bottom: 36px;
}

.section-title {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin: 0 0 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #e5e5e5;
}

.api-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.api-card {
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 12px 14px;
}

.api-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.method-badge {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 6px;
  flex-shrink: 0;
}

.method-badge.get {
  background: #e8f5e9;
  color: #2e7d32;
}

.method-badge.post {
  background: #e3f2fd;
  color: #1565c0;
}

.api-path {
  font-size: 12px;
  font-family: monospace;
  color: #555;
  flex: 1;
}

.api-label {
  font-size: 12px;
  color: #999;
}

.run-btn {
  padding: 5px 14px;
  background: #58cc02;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  flex-shrink: 0;
}

.run-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.api-note {
  margin-top: 6px;
  font-size: 11px;
  color: #aaa;
}

.api-result {
  margin-top: 10px;
  background: #f5f5f5;
  border-radius: 8px;
  padding: 10px;
  overflow: auto;
  max-height: 300px;
}

.api-result pre {
  margin: 0;
  font-size: 12px;
  font-family: monospace;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
}

.api-error {
  margin-top: 8px;
  font-size: 12px;
  color: #e53935;
}

.form-card {
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.form-label {
  width: 80px;
  flex-shrink: 0;
  font-size: 13px;
  color: #555;
  padding-top: 8px;
}

.text-input {
  flex: 1;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
}

.text-input:focus {
  border-color: #58cc02;
}

.select-input {
  flex: 1;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  outline: none;
}

.textarea-input {
  flex: 1;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  resize: vertical;
  outline: none;
  font-family: inherit;
}

.textarea-input:focus,
.select-input:focus {
  border-color: #58cc02;
}

.radio-group {
  display: flex;
  gap: 20px;
  padding-top: 8px;
}

.radio-label {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}

.submit-btn {
  padding: 12px;
  background: #58cc02;
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #46a301;
}

.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.success-msg {
  font-size: 14px;
  color: #2e7d32;
  font-weight: 600;
  margin-bottom: 6px;
}

/* Frequency Test */
.freq-card {
  background: white;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  padding: 14px;
}

.freq-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.freq-input {
  width: 100px;
  flex: none;
}

.freq-results {
  margin-top: 12px;
}

.freq-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.freq-subtitle {
  font-size: 13px;
  font-weight: 700;
  color: #555;
  margin: 16px 0 8px;
}

.freq-table {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.freq-row {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 8px 10px;
}

.freq-title {
  font-size: 12px;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.freq-meta {
  display: flex;
  gap: 12px;
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
  font-family: monospace;
}

.freq-priority {
  color: #1cb0f6;
}

.freq-pct {
  font-weight: 700;
  color: #555;
}

.freq-bar-bg {
  height: 6px;
  background: #e5e5e5;
  border-radius: 3px;
  overflow: hidden;
}

.freq-bar {
  height: 100%;
  background: #58cc02;
  border-radius: 3px;
  transition: width 0.3s;
}

.freq-bar.fc {
  background: #ff9600;
}
</style>
