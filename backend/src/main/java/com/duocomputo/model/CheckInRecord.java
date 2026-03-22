package com.duocomputo.model;

import java.util.List;

public class CheckInRecord {
    private String date;
    private boolean completed;
    private List<AnswerRecord> answers;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public List<AnswerRecord> getAnswers() { return answers; }
    public void setAnswers(List<AnswerRecord> answers) { this.answers = answers; }
}
