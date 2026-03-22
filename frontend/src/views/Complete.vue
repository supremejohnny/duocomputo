<template>
  <div class="complete-page">
    <div class="celebration">
      <div class="confetti">🎉</div>
      <h1 class="title">太棒了！</h1>
      <p class="subtitle">你已完成今日学习任务</p>
    </div>

    <div class="stats-card" v-if="record">
      <div class="stat">
        <div class="stat-number">{{ record.answers?.length || 0 }}</div>
        <div class="stat-label">完成题目</div>
      </div>
      <div class="stat-divider"></div>
      <div class="stat">
        <div class="stat-number">{{ streak }}</div>
        <div class="stat-label">连续打卡天数</div>
      </div>
    </div>

    <div class="checkin-badge" v-if="record?.completed">
      <div class="badge-icon">✅</div>
      <div class="badge-text">今日打卡成功！</div>
    </div>

    <div class="message-card">
      <p>坚持就是胜利！每天几分钟，日积月累就是巨大的进步。</p>
      <p>休息一下，或继续挑战新题目！💪</p>
    </div>

    <router-link to="/daily" class="btn btn-primary btn-block">
      继续学习
    </router-link>
    <router-link to="/" class="btn btn-outline btn-block">
      返回首页
    </router-link>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const record = ref(null)
const streak = ref(0)

onMounted(async () => {
  try {
    const [recordRes, streakRes] = await Promise.all([
      api.getTodayRecord(),
      api.getStreak(),
    ])
    record.value = recordRes.data
    streak.value = streakRes.data.streak
  } catch {}
})
</script>

<style scoped>
.complete-page {
  max-width: 480px;
  margin: 0 auto;
  padding: 40px 20px;
  text-align: center;
}

.celebration {
  margin-bottom: 32px;
}

.confetti {
  font-size: 72px;
  margin-bottom: 8px;
  animation: bounce 0.6s ease-in-out;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.title {
  font-size: 32px;
  font-weight: 800;
  color: #58cc02;
  margin: 0;
}

.subtitle {
  font-size: 16px;
  color: #777;
  margin: 8px 0 0;
}

.stats-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.stat-number {
  font-size: 36px;
  font-weight: 800;
  color: #333;
}

.stat-label {
  font-size: 13px;
  color: #999;
  margin-top: 4px;
}

.stat-divider {
  width: 1px;
  height: 48px;
  background: #e5e5e5;
}

.checkin-badge {
  background: #e6f9e6;
  border: 2px solid #58cc02;
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.badge-icon {
  font-size: 24px;
}

.badge-text {
  font-size: 16px;
  font-weight: 700;
  color: #58cc02;
}

.message-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.message-card p {
  color: #666;
  font-size: 14px;
  line-height: 1.7;
  margin: 0 0 8px;
}

.message-card p:last-child {
  margin: 0;
}

.btn-block {
  display: block;
  width: 100%;
  text-align: center;
  text-decoration: none;
  padding: 16px;
  font-size: 16px;
}

.btn-block + .btn-block {
  margin-top: 12px;
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
</style>
