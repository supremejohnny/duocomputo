import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Home from './views/Home.vue'
import DailyTask from './views/DailyTask.vue'
import Complete from './views/Complete.vue'
import './style.css'

const routes = [
  { path: '/', component: Home },
  { path: '/daily', component: DailyTask },
  { path: '/complete', component: Complete },
  { path: '/category', component: () => import('./views/CategorySelect.vue') },
  { path: '/category/:category', component: () => import('./views/CategoryTask.vue'), props: true },
  { path: '/dev', component: () => import('./views/DevMode.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const app = createApp(App)
app.use(router)
app.mount('#app')
