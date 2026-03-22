package com.duocomputo.model;

import java.util.List;

public class DailyTask {
    private String date;
    private List<McQuestion> mcQuestions;
    private List<FlashcardQuestion> flashcardQuestions;
    private boolean checkedIn;

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<McQuestion> getMcQuestions() { return mcQuestions; }
    public void setMcQuestions(List<McQuestion> mcQuestions) { this.mcQuestions = mcQuestions; }

    public List<FlashcardQuestion> getFlashcardQuestions() { return flashcardQuestions; }
    public void setFlashcardQuestions(List<FlashcardQuestion> flashcardQuestions) { this.flashcardQuestions = flashcardQuestions; }

    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
}
