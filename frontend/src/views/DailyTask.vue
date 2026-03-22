<template>
  <div class="daily-task">
    <div class="top-bar">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>
      <span class="progress-text">{{ progress.current }}/{{ progress.total }}</span>
    </div>

    <div v-if="loading" class="loading">
      <div class="loading-icon">📚</div>
      <p>正在准备今日题目...</p>
    </div>

    <!-- Inline Quiz (MC question) -->
    <div v-else-if="currentQuestion && currentQuestion._type === 'mc'" class="quiz-content">
      <div class="category-tag">{{ currentQuestion.category }}</div>
      <h2 class="question-text">{{ currentQuestion.question }}</h2>

      <div class="options">
        <button
          v-for="(option, index) in shuffledOptions"
          :key="option.id"
          class="option-btn"
          :class="{
            selected: selectedIndex === index,
            correct: showResult && option.id === currentQuestion.correctAnswerIdx,
            wrong: showResult && selectedIndex === index && option.id !== currentQuestion.correctAnswerIdx,
          }"
          :disabled="showResult"
          @click="selectOption(index)"
        >
          <span class="option-letter">{{ ['A', 'B', 'C', 'D'][index] }}</span>
          <span class="option-text">{{ option.text }}</span>
        </button>
      </div>

      <div v-if="showResult" class="result-card" :class="isCorrect ? 'correct' : 'wrong'">
        <div class="result-header">
          {{ isCorrect ? '🎉 回答正确！' : '😅 再想想...' }}
        </div>
        <div class="result-explanation">{{ currentQuestion.explanation }}</div>
      </div>

      <button v-if="showResult" class="btn btn-primary btn-block" @click="submitMcAndNext">
        继续
      </button>
    </div>

    <!-- Inline Flashcard -->
    <div v-else-if="currentQuestion && currentQuestion._type === 'flashcard'" class="flashcard-content">
      <div class="category-tag">{{ currentQuestion.category }}</div>

      <div class="card">
        <div class="card-front" v-if="!showAnswer">
          <h2 class="question-text">{{ currentQuestion.content }}</h2>
          <p class="hint-text">先自己思考一下 🤔</p>
          <button class="btn btn-primary btn-block" @click="showAnswer = true">
            查看答案
          </button>
        </div>

        <div class="card-back" v-else>
          <h3 class="answer-label">📖 参考答案</h3>
          <div class="answer-text">
            <p v-for="(line, i) in answerLines" :key="i">{{ line }}</p>
          </div>
          <div class="explanation" v-if="currentQuestion.explanation">
            <strong>💬 小提示：</strong>{{ currentQuestion.explanation }}
          </div>
        </div>
      </div>

      <div v-if="showAnswer" class="difficulty-section">
        <p class="difficulty-label">这道题对你来说...</p>
        <div class="difficulty-buttons">
          <button class="diff-btn easy" @click="submitFlashcardAndNext('easy')">
            😊 Easy
          </button>
          <button class="diff-btn medium" @click="submitFlashcardAndNext('medium')">
            🤔 Medium
          </button>
          <button class="diff-btn hard" @click="submitFlashcardAndNext('hard')">
            😰 Hard
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'
import session, {
  resetSession, setQuestions, markAnswered,
  getCurrentQuestion, advanceToNext, isSessionComplete, getProgress
} from '../sessionStore'

const router = useRouter()
const loading = ref(true)

// MC state
const selectedIndex = ref(-1)
const showResult = ref(false)
const shuffledOptions = ref([])

// Flashcard state
const showAnswer = ref(false)

const currentQuestion = computed(() => getCurrentQuestion())
const progress = computed(() => getProgress())
const progressPercent = computed(() =>
  progress.value.total ? (progress.value.current / progress.value.total) * 100 : 0
)

const isCorrect = computed(() =>
  currentQuestion.value &&
  shuffledOptions.value[selectedIndex.value]?.id === currentQuestion.value.correctAnswerIdx
)

const answerLines = computed(() =>
  currentQuestion.value?.answer?.split('\n').filter(l => l.trim()) || []
)

function resetQuestionState() {
  selectedIndex.value = -1
  showResult.value = false
  showAnswer.value = false
  const q = getCurrentQuestion()
  if (q && q._type === 'mc' && q.options) {
    shuffledOptions.value = [...q.options].sort(() => Math.random() - 0.5)
  }
}

onMounted(async () => {
  try {
    const res = await api.getDailyTask()
    const mc = res.data.mcQuestions || []
    const fc = res.data.flashcardQuestions || []
    resetSession()
    setQuestions(mc, fc)
    resetQuestionState()
  } catch (e) {
    console.error('Failed to load daily task', e)
  }
  loading.value = false
})

function selectOption(index) {
  if (showResult.value) return
  selectedIndex.value = index
  showResult.value = true
}

async function submitMcAndNext() {
  const q = currentQuestion.value
  try {
    await api.submitAnswer({
      questionId: q.id,
      questionType: 'mc',
      correct: isCorrect.value,
    })
  } catch (e) {
    console.error('Failed to submit answer', e)
  }
  markAnswered(q.id)
  goNext()
}

async function submitFlashcardAndNext(difficulty) {
  const q = currentQuestion.value
  try {
    await api.submitAnswer({
      questionId: q.id,
      questionType: 'flashcard',
      difficulty,
      correct: true,
    })
  } catch (e) {
    console.error('Failed to submit answer', e)
  }
  markAnswered(q.id)
  goNext()
}

function goNext() {
  advanceToNext()
  if (isSessionComplete()) {
    router.replace('/complete')
  } else {
    resetQuestionState()
  }
}
</script>

<style scoped>
.daily-task {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px;
}

.top-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.back-btn {
  color: #777;
  text-decoration: none;
  font-size: 14px;
  white-space: nowrap;
}

.progress-bar {
  flex: 1;
  height: 12px;
  background: #e5e5e5;
  border-radius: 6px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #58cc02;
  border-radius: 6px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  color: #777;
  white-space: nowrap;
}

.loading {
  text-align: center;
  padding: 80px 0;
  color: #999;
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

/* --- MC Quiz styles --- */
.category-tag {
  display: inline-block;
  font-size: 12px;
  color: #1cb0f6;
  background: #e8f4fd;
  padding: 4px 10px;
  border-radius: 12px;
  margin-bottom: 12px;
}

.question-text {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  line-height: 1.5;
  margin: 0 0 24px;
}

.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.option-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  background: white;
  border: 2px solid #e5e5e5;
  border-radius: 16px;
  padding: 14px 16px;
  cursor: pointer;
  font-size: 15px;
  text-align: left;
  transition: all 0.2s;
}

.option-btn:hover:not(:disabled) {
  border-color: #58cc02;
  background: #f9fff9;
}

.option-btn.selected {
  border-color: #1cb0f6;
  background: #e8f4fd;
}

.option-btn.correct {
  border-color: #58cc02;
  background: #e6f9e6;
}

.option-btn.wrong {
  border-color: #ff4b4b;
  background: #ffe6e6;
}

.option-letter {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #777;
  flex-shrink: 0;
}

.option-btn.correct .option-letter {
  background: #58cc02;
  color: white;
}

.option-btn.wrong .option-letter {
  background: #ff4b4b;
  color: white;
}

.option-text {
  flex: 1;
  color: #333;
}

.result-card {
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
}

.result-card.correct {
  background: #e6f9e6;
  border: 2px solid #58cc02;
}

.result-card.wrong {
  background: #ffe6e6;
  border: 2px solid #ff4b4b;
}

.result-header {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 8px;
}

.result-explanation {
  font-size: 14px;
  color: #555;
  line-height: 1.6;
}

/* --- Flashcard styles --- */
.card {
  background: white;
  border-radius: 20px;
  padding: 28px 24px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.08);
  margin-bottom: 24px;
  min-height: 200px;
}

.card-front {
  text-align: center;
}

.hint-text {
  color: #bbb;
  font-size: 14px;
  margin-bottom: 24px;
}

.answer-label {
  font-size: 16px;
  color: #58cc02;
  margin: 0 0 16px;
}

.answer-text {
  font-size: 14px;
  color: #444;
  line-height: 1.8;
  margin-bottom: 16px;
}

.answer-text p {
  margin: 0 0 8px;
}

.explanation {
  font-size: 13px;
  color: #888;
  background: #f9f9f9;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
}

/* --- Difficulty buttons (flashcard only) --- */
.difficulty-section {
  text-align: center;
}

.difficulty-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 12px;
}

.difficulty-buttons {
  display: flex;
  gap: 12px;
}

.diff-btn {
  flex: 1;
  padding: 12px 8px;
  border-radius: 16px;
  border: 2px solid;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.diff-btn.easy {
  border-color: #58cc02;
  background: #e6f9e6;
  color: #58cc02;
}

.diff-btn.easy:hover {
  background: #58cc02;
  color: white;
}

.diff-btn.medium {
  border-color: #ff9600;
  background: #fff3e0;
  color: #ff9600;
}

.diff-btn.medium:hover {
  background: #ff9600;
  color: white;
}

.diff-btn.hard {
  border-color: #ff4b4b;
  background: #ffe6e6;
  color: #ff4b4b;
}

.diff-btn.hard:hover {
  background: #ff4b4b;
  color: white;
}

/* --- Shared --- */
.btn-block {
  display: block;
  width: 100%;
  padding: 14px;
  font-size: 16px;
  text-align: center;
  margin-top: 16px;
}
</style>
