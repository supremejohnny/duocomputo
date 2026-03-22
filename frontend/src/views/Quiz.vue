<template>
  <div class="quiz">
    <div class="top-bar">
      <router-link to="/daily" class="back-btn">← 返回</router-link>
      <div class="badge">📝 选择题</div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="question" class="quiz-content">
      <div class="category-tag">{{ question.category }}</div>
      <h2 class="question-text">{{ question.question }}</h2>

      <div class="options">
        <button
          v-for="(option, index) in shuffledOptions"
          :key="option.id"
          class="option-btn"
          :class="{
            selected: selectedIndex === index,
            correct: showResult && option.id === question.correctAnswerIdx,
            wrong: showResult && selectedIndex === index && option.id !== question.correctAnswerIdx,
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
        <div class="result-explanation">{{ question.explanation }}</div>
      </div>

      <div v-if="showResult" class="difficulty-section">
        <p class="difficulty-label">这道题对你来说...</p>
        <div class="difficulty-buttons">
          <button class="diff-btn easy" @click="submitAndGoBack('easy')">
            😊 Easy
          </button>
          <button class="diff-btn medium" @click="submitAndGoBack('medium')">
            🤔 Medium
          </button>
          <button class="diff-btn hard" disabled>
            🔒 Hard
            <span class="dev-tag">开发中</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const props = defineProps({ id: String })
const router = useRouter()
const question = ref(null)
const shuffledOptions = ref([])
const loading = ref(true)
const selectedIndex = ref(-1)
const showResult = ref(false)

const isCorrect = computed(() =>
  question.value && shuffledOptions.value[selectedIndex.value]?.id === question.value.correctAnswerIdx
)

onMounted(async () => {
  try {
    const res = await api.getMcQuestion(props.id)
    question.value = res.data
    if (res.data.options) {
      shuffledOptions.value = [...res.data.options].sort(() => Math.random() - 0.5)
    }
  } catch (e) {
    console.error('Failed to load question', e)
  }
  loading.value = false
})

function selectOption(index) {
  if (showResult.value) return
  selectedIndex.value = index
  showResult.value = true
}

async function submitAndGoBack(difficulty) {
  try {
    await api.submitAnswer({
      questionId: props.id,
      difficulty,
      correct: isCorrect.value,
    })
  } catch (e) {
    console.error('Failed to submit answer', e)
  }
  router.push('/daily')
}
</script>

<style scoped>
.quiz {
  max-width: 480px;
  margin: 0 auto;
  padding: 20px;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.back-btn {
  color: #777;
  text-decoration: none;
  font-size: 14px;
}

.badge {
  font-size: 13px;
  background: #e8f5e9;
  color: #58cc02;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

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

.difficulty-section {
  text-align: center;
  margin-top: 16px;
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
  border-color: #e5e5e5;
  background: #f5f5f5;
  color: #ccc;
  cursor: not-allowed;
  position: relative;
}

.dev-tag {
  display: block;
  font-size: 10px;
  font-weight: 400;
  margin-top: 2px;
}
</style>
