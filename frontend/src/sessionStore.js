import { reactive } from 'vue'

/**
 * Simple reactive store to manage the daily session state.
 * Tracks the question sequence and current progress for Duolingo-style flow.
 */
const session = reactive({
  questions: [],       // Array of { id, type: 'mc'|'flashcard', ...questionData }
  currentIndex: 0,     // Index of current question in the sequence
  answered: new Set(),  // Set of answered question IDs
  loaded: false,
})

export function resetSession() {
  session.questions = []
  session.currentIndex = 0
  session.answered = new Set()
  session.loaded = false
}

export function setQuestions(mcQuestions, flashcardQuestions) {
  // Interleave: MC first, then flashcard (can shuffle order later)
  const all = [
    ...mcQuestions.map(q => ({ ...q, _type: 'mc' })),
    ...flashcardQuestions.map(q => ({ ...q, _type: 'flashcard' })),
  ]
  session.questions = all
  session.currentIndex = 0
  session.loaded = true
}

export function markAnswered(questionId) {
  session.answered.add(questionId)
}

export function getCurrentQuestion() {
  return session.questions[session.currentIndex] || null
}

export function advanceToNext() {
  session.currentIndex++
}

export function isSessionComplete() {
  return session.currentIndex >= session.questions.length
}

export function getProgress() {
  return {
    current: session.currentIndex,
    total: session.questions.length,
  }
}

export default session
