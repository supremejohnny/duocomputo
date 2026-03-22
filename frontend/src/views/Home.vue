<template>
  <div class="home">
    <div class="home-header">
      <div class="mascot">🖥️</div>
      <h1 class="app-title">DuoComputo</h1>
      <p class="app-subtitle">计算机知识轻打卡</p>
    </div>

    <div class="home-card">
      <p class="home-desc">
        每天花几分钟，轻松复习计算机基础知识。<br />
        选择题热身 + 思考题深入，坚持打卡，积少成多。
      </p>
    </div>

    <div class="streak-card" v-if="streakLoaded">
      <div class="streak-icon">🔥</div>
      <div class="streak-info">
        <div class="streak-number">{{ streak }}</div>
        <div class="streak-label">天连续打卡</div>
      </div>
      <div class="today-status" :class="{ completed: todayCompleted }">
        {{ todayCompleted ? '✅ 今日已打卡' : '⏳ 今日未完成' }}
      </div>
    </div>

    <router-link to="/daily" class="btn btn-primary btn-large">
      {{ todayCompleted ? '继续学习' : '开始今日学习' }}
    </router-link>

    <router-link to="/category" class="btn btn-outline btn-large">
      按分类学习
    </router-link>

    <div class="features">
      <div class="feature">
        <div class="feature-icon">📝</div>
        <div class="feature-text">选择题热身</div>
      </div>
      <div class="feature">
        <div class="feature-icon">💡</div>
        <div class="feature-text">翻面答案题</div>
      </div>
      <div class="feature">
        <div class="feature-icon">📂</div>
        <div class="feature-text">分类学习</div>
      </div>
    </div>

    <router-link to="/dev" class="dev-link">DEV</router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const streak = ref(0)
const todayCompleted = ref(false)
const streakLoaded = ref(false)

onMounted(async () => {
  try {
    const res = await api.getStreak()
    streak.value = res.data.streak
    todayCompleted.value = res.data.todayCompleted
    streakLoaded.value = true
  } catch {
    streakLoaded.value = true
  }
})
</script>

<style scoped>
.home {
  max-width: 480px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
}

.home-header {
  margin-bottom: 24px;
}

.mascot {
  font-size: 64px;
  margin-bottom: 8px;
}

.app-title {
  font-size: 36px;
  font-weight: 800;
  color: #58cc02;
  margin: 0;
  letter-spacing: -1px;
}

.app-subtitle {
  font-size: 16px;
  color: #777;
  margin: 4px 0 0;
}

.home-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.home-desc {
  color: #555;
  font-size: 15px;
  line-height: 1.7;
  margin: 0;
}

.streak-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  gap: 16px;
}

.streak-icon {
  font-size: 40px;
}

.streak-info {
  flex: 1;
  text-align: left;
}

.streak-number {
  font-size: 28px;
  font-weight: 800;
  color: #ff9600;
}

.streak-label {
  font-size: 13px;
  color: #999;
}

.today-status {
  font-size: 14px;
  color: #999;
  padding: 6px 12px;
  border-radius: 20px;
  background: #f0f0f0;
}

.today-status.completed {
  background: #e6f9e6;
  color: #58cc02;
}

.btn-large {
  display: block;
  width: 100%;
  padding: 16px;
  font-size: 18px;
  margin-bottom: 16px;
  text-decoration: none;
}

.btn-large + .btn-large {
  margin-bottom: 32px;
}

.btn-outline {
  background: white;
  color: #58cc02;
  border: 2px solid #58cc02;
  border-radius: 16px;
  font-weight: 700;
}

.btn-outline:hover {
  background: #f0fff0;
}

.features {
  display: flex;
  justify-content: center;
  gap: 32px;
}

.feature {
  text-align: center;
}

.feature-icon {
  font-size: 32px;
  margin-bottom: 4px;
}

.feature-text {
  font-size: 13px;
  color: #777;
}

.dev-link {
  display: inline-block;
  margin-top: 32px;
  font-size: 11px;
  color: #ccc;
  text-decoration: none;
  letter-spacing: 1px;
}

.dev-link:hover {
  color: #999;
}
</style>
