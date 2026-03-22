package com.duocomputo.model;

/**
 * Tracks spaced repetition weight for a question.
 * Higher weight = user finds it harder = should appear more often.
 *
 * Weight adjustments:
 * - MC: correct → weight -1, wrong → weight +1
 * - Flashcard: easy → -1, medium → 0, hard → +1
 *
 * TODO: 后续需要更新/improve - 当前使用最简单的线性权重，
 * 未来可以引入 SM-2 或 Leitner 等成熟的间隔重复算法
 */
public class QuestionWeight {
    private String questionId;
    private int weight;         // higher = needs more practice
    private String lastAnswered; // ISO date string, e.g. "2026-03-21"

    public QuestionWeight() {}

    public QuestionWeight(String questionId, int weight, String lastAnswered) {
        this.questionId = questionId;
        this.weight = weight;
        this.lastAnswered = lastAnswered;
    }

    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }

    public String getLastAnswered() { return lastAnswered; }
    public void setLastAnswered(String lastAnswered) { this.lastAnswered = lastAnswered; }
}
