package com.duocomputo.service;

import com.duocomputo.model.AnswerRecord;
import com.duocomputo.model.CheckInRecord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CheckInService {

    private List<CheckInRecord> records = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpacedRepetitionService spacedRepetitionService;
    private Path dataFilePath;

    public CheckInService(SpacedRepetitionService spacedRepetitionService) {
        this.spacedRepetitionService = spacedRepetitionService;
    }

    @PostConstruct
    public void init() {
        try {
            // Use a writable location for check-in data
            String dataDir = System.getProperty("user.dir") + "/data";
            Files.createDirectories(Path.of(dataDir));
            dataFilePath = Path.of(dataDir, "checkin_records.json");

            if (Files.exists(dataFilePath)) {
                records = objectMapper.readValue(dataFilePath.toFile(),
                        new TypeReference<List<CheckInRecord>>() {});
            }
        } catch (Exception e) {
            records = new ArrayList<>();
        }
    }

    private void saveRecords() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(dataFilePath.toFile(), records);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save check-in records", e);
        }
    }

    public CheckInRecord getTodayRecord() {
        String today = LocalDate.now().toString();
        return records.stream()
                .filter(r -> today.equals(r.getDate()))
                .findFirst()
                .orElse(null);
    }

    public List<CheckInRecord> getAllRecords() {
        return records;
    }

    public CheckInRecord submitAnswer(AnswerRecord answer) {
        String today = LocalDate.now().toString();
        CheckInRecord todayRecord = records.stream()
                .filter(r -> today.equals(r.getDate()))
                .findFirst()
                .orElseGet(() -> {
                    CheckInRecord newRecord = new CheckInRecord();
                    newRecord.setDate(today);
                    newRecord.setCompleted(false);
                    newRecord.setAnswers(new ArrayList<>());
                    records.add(newRecord);
                    return newRecord;
                });

        todayRecord.getAnswers().add(answer);

        // Update spaced repetition weight
        spacedRepetitionService.updateWeight(
                answer.getQuestionId(),
                answer.getQuestionType(),
                answer.getDifficulty(),
                answer.isCorrect()
        );

        // Mark as completed if answered 3 or more questions
        if (todayRecord.getAnswers().size() >= 3) {
            todayRecord.setCompleted(true);
        }

        saveRecords();
        return todayRecord;
    }

    public int getStreakDays() {
        if (records.isEmpty()) return 0;

        List<String> completedDates = records.stream()
                .filter(CheckInRecord::isCompleted)
                .map(CheckInRecord::getDate)
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        if (completedDates.isEmpty()) return 0;

        int streak = 0;
        LocalDate checkDate = LocalDate.now();

        for (int i = completedDates.size() - 1; i >= 0; i--) {
            LocalDate recordDate = LocalDate.parse(completedDates.get(i));
            if (recordDate.equals(checkDate) || recordDate.equals(checkDate.minusDays(1))) {
                streak++;
                checkDate = recordDate;
            } else {
                break;
            }
        }

        return streak;
    }
}
