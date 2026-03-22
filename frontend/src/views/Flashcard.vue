<template>
  <div class="flashcard-page">
    <div class="top-bar">
      <router-link to="/daily" class="back-btn">← 返回</router-link>
      <div class="badge">💡 翻面答案题</div>
    </div>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else-if="question" class="flashcard-content">
      <div class="category-tag">{{ question.category }}</div>

      <div class="card" :class="{ flipped: showAnswer }">
        <div class="card-front" v-if="!showAnswer">
          <h2 class="question-text">{{ question.content }}</h2>
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
          <div class="explanation" v-if="question.explanation">
            <strong>💬 小提示：</strong>{{ question.explanation }}
          </div>
        </div>
      </div>

      <div v-if="showAnswer" class="difficulty-section">
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../api'

const props = defineProps({ id: String })
const router = useRouter()
const question = ref(null)
const loading = ref(true)
const showAnswer = ref(false)

const answerLines = computed(() =>
  question.value?.answer?.split('\n').filter(l => l.trim()) || []
)

onMounted(async () => {
  try {
    const res = await api.getFlashcardQuestion(props.id)
    question.value = res.data
  } catch (e) {
    console.error('Failed to load question', e)
  }
  loading.value = false
})

async function submitAndGoBack(difficulty) {
  try {
    await api.submitAnswer({
      questionId: props.id,
      difficulty,
      correct: true,
    })
  } catch (e) {
    console.error('Failed to submit answer', e)
  }
  router.push('/daily')
}
</script>

<style scoped>
.flashcard-page {
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
  background: #fff3e0;
  color: #ff9600;
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
  margin-bottom: 16px;
}

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

.question-text {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  line-height: 1.5;
  margin: 0 0 16px;
}

.hint-text {
  color: #bbb;
  font-size: 14px;
  margin-bottom: 24px;
}

.btn-block {
  display: block;
  width: 100%;
  padding: 14px;
  font-size: 16px;
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
