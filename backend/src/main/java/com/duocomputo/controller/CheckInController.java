package com.duocomputo.controller;

import com.duocomputo.model.AnswerRecord;
import com.duocomputo.model.CheckInRecord;
import com.duocomputo.service.CheckInService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @GetMapping("/today")
    public CheckInRecord getTodayRecord() {
        return checkInService.getTodayRecord();
    }

    @GetMapping("/records")
    public List<CheckInRecord> getAllRecords() {
        return checkInService.getAllRecords();
    }

    @PostMapping("/submit")
    public CheckInRecord submitAnswer(@RequestBody AnswerRecord answer) {
        return checkInService.submitAnswer(answer);
    }

    @GetMapping("/streak")
    public Map<String, Object> getStreak() {
        Map<String, Object> result = new HashMap<>();
        result.put("streak", checkInService.getStreakDays());
        var today = checkInService.getTodayRecord();
        result.put("todayCompleted", today != null && today.isCompleted());
        return result;
    }
}
