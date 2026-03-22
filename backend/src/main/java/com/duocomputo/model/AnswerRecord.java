package com.duocomputo.model;

public class AnswerRecord {
    private String questionId;
    private String questionType; // "mc" or "flashcard"
    private String difficulty;   // "easy", "medium", or "hard" (flashcard only)
    private boolean correct;     // for choice questions

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
