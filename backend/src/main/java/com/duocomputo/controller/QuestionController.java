package com.duocomputo.controller;

import com.duocomputo.model.DailyTask;
import com.duocomputo.model.FlashcardQuestion;
import com.duocomputo.model.McQuestion;
import com.duocomputo.service.CheckInService;
import com.duocomputo.service.QuestionService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class QuestionController {

    private final QuestionService questionService;
    private final CheckInService checkInService;

    public QuestionController(QuestionService questionService, CheckInService checkInService) {
        this.questionService = questionService;
        this.checkInService = checkInService;
    }

    @GetMapping("/daily")
    public DailyTask getDailyTask() {
        DailyTask task = new DailyTask();
        task.setDate(LocalDate.now().toString());

        QuestionService.DailySelection selection = questionService.getDailyQuestions();
        task.setMcQuestions(selection.mcQuestions());
        task.setFlashcardQuestions(selection.flashcardQuestions());

        var todayRecord = checkInService.getTodayRecord();
        task.setCheckedIn(todayRecord != null && todayRecord.isCompleted());

        return task;
    }

    // MC Questions
    @GetMapping("/mc-questions")
    public List<McQuestion> getAllMcQuestions() {
        return questionService.getAllMcQuestions();
    }

    @GetMapping("/mc-questions/{id}")
    public McQuestion getMcQuestion(@PathVariable String id) {
        return questionService.getMcQuestionById(id);
    }

    @PostMapping("/mc-questions")
    public McQuestion addMcQuestion(@RequestBody McQuestion question) {
        return questionService.addMcQuestion(question);
    }

    // Flashcard Questions
    @GetMapping("/flashcard-questions")
    public List<FlashcardQuestion> getAllFlashcardQuestions() {
        return questionService.getAllFlashcardQuestions();
    }

    @GetMapping("/flashcard-questions/{id}")
    public FlashcardQuestion getFlashcardQuestion(@PathVariable String id) {
        return questionService.getFlashcardQuestionById(id);
    }

    @PostMapping("/flashcard-questions")
    public FlashcardQuestion addFlashcardQuestion(@RequestBody FlashcardQuestion question) {
        return questionService.addFlashcardQuestion(question);
    }

    // Categories
    @GetMapping("/categories")
    public List<String> getCategories() {
        return questionService.getAllCategories();
    }

    @GetMapping("/daily/category/{category}")
    public DailyTask getCategoryDailyTask(@PathVariable String category) {
        DailyTask task = new DailyTask();
        task.setDate(LocalDate.now().toString());

        QuestionService.DailySelection selection = questionService.getCategoryQuestions(category);
        task.setMcQuestions(selection.mcQuestions());
        task.setFlashcardQuestions(selection.flashcardQuestions());
        task.setCheckedIn(false);

        return task;
    }

    // Dev tools
    @GetMapping("/dev/frequency-test")
    public Map<String, Object> frequencyTest(@RequestParam(defaultValue = "1000") int rounds) {
        return questionService.simulateSelectionFrequency(rounds);
    }
}
