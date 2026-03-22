<template>
  <div class="category-select">
    <div class="top-bar">
      <router-link to="/" class="back-btn">← 返回</router-link>
      <div class="badge">📂 分类学习</div>
    </div>

    <h2 class="page-title">选择学习分类</h2>
    <p class="page-desc">专注某一类知识，深入练习</p>

    <div v-if="loading" class="loading">加载中...</div>

    <div v-else class="category-grid">
      <router-link
        v-for="cat in categories"
        :key="cat.name"
        :to="`/category/${encodeURIComponent(cat.name)}`"
        class="category-card"
      >
        <div class="category-icon">{{ cat.icon }}</div>
        <div class="category-info">
          <div class="category-name">{{ cat.name }}</div>
          <div class="category-count">{{ cat.total }} 道题目</div>
        </div>
        <div class="category-arrow">→</div>
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const categories = ref([])
const loading = ref(true)

const categoryIcons = {
  '计算机网络': '🌐',
  'Java 基础': '☕',
  '操作系统': '🖥️',
  '数据结构': '🌳',
}

onMounted(async () => {
  try {
    const [catRes, mcRes, fcRes] = await Promise.all([
      api.getCategories(),
      api.getAllMcQuestions(),
      api.getAllFlashcardQuestions(),
    ])

    const mcQuestions = mcRes.data || []
    const fcQuestions = fcRes.data || []

    categories.value = catRes.data.map(name => {
      const mcCount = mcQuestions.filter(q => q.category === name).length
      const fcCount = fcQuestions.filter(q => q.category === name).length
      return {
        name,
        icon: categoryIcons[name] || '📚',
        total: mcCount + fcCount,
      }
    })
  } catch (e) {
    console.error('Failed to load categories', e)
  }
  loading.value = false
})
</script>

<style scoped>
.category-select {
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
  background: #e8f4fd;
  color: #1cb0f6;
  padding: 4px 12px;
  border-radius: 20px;
  font-weight: 600;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  margin: 0 0 4px;
}

.page-desc {
  color: #999;
  font-size: 14px;
  margin: 0 0 20px;
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
}

.category-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.category-card {
  background: white;
  border-radius: 16px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border: 2px solid transparent;
  text-decoration: none;
  color: inherit;
  transition: all 0.2s;
}

.category-card:hover {
  border-color: #58cc02;
  transform: translateY(-1px);
}

.category-icon {
  font-size: 36px;
  flex-shrink: 0;
}

.category-info {
  flex: 1;
}

.category-name {
  font-size: 16px;
  font-weight: 700;
  color: #333;
}

.category-count {
  font-size: 13px;
  color: #999;
  margin-top: 2px;
}

.category-arrow {
  font-size: 20px;
  color: #ccc;
}
</style>
