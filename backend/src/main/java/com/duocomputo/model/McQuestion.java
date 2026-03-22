package com.duocomputo.model;

import java.util.List;

public class McQuestion {
    private String id;
    private String type;           // "choice" (future: "multi_choice")
    private String category;
    private String difficulty;     // "easy" or "medium"
    private String question;
    private List<McOption> options;
    private String correctAnswerIdx; // "A", "B", "C", "D"
    private String explanation;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<McOption> getOptions() { return options; }
    public void setOptions(List<McOption> options) { this.options = options; }

    public String getCorrectAnswerIdx() { return correctAnswerIdx; }
    public void setCorrectAnswerIdx(String correctAnswerIdx) { this.correctAnswerIdx = correctAnswerIdx; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
