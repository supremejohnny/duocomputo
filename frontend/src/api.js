import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
})

export default {
  getDailyTask() {
    return api.get('/daily')
  },
  getMcQuestion(id) {
    return api.get(`/mc-questions/${id}`)
  },
  getFlashcardQuestion(id) {
    return api.get(`/flashcard-questions/${id}`)
  },
  submitAnswer(answer) {
    return api.post('/checkin/submit', answer)
  },
  getStreak() {
    return api.get('/checkin/streak')
  },
  getTodayRecord() {
    return api.get('/checkin/today')
  },
  // Dev Mode
  getAllMcQuestions() {
    return api.get('/mc-questions')
  },
  getAllFlashcardQuestions() {
    return api.get('/flashcard-questions')
  },
  getCheckinRecords() {
    return api.get('/checkin/records')
  },
  addMcQuestion(question) {
    return api.post('/mc-questions', question)
  },
  addFlashcardQuestion(question) {
    return api.post('/flashcard-questions', question)
  },
  // Categories
  getCategories() {
    return api.get('/categories')
  },
  getCategoryDailyTask(category) {
    return api.get(`/daily/category/${encodeURIComponent(category)}`)
  },
  // Dev tools
  getFrequencyTest(rounds = 1000) {
    return api.get(`/dev/frequency-test?rounds=${rounds}`)
  },
}
