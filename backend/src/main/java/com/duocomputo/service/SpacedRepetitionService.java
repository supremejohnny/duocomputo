package com.duocomputo.service;

import com.duocomputo.model.QuestionWeight;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Simple spaced repetition service.
 *
 * Priority score = weight + daysSinceLastSeen * 0.3
 * - Higher priority → more likely to be selected for daily questions
 * - New questions (never seen) get a small bonus to ensure they appear
 *
 * TODO: 后续需要更新/improve - 当前为最简单的线性优先级计算，
 * 未来应考虑引入 SM-2 算法、遗忘曲线、或 Leitner 箱子系统，
 * 并支持根据用户整体正确率动态调整每日题目数量和难度配比
 */
@Service
public class SpacedRepetitionService {

    private Map<String, QuestionWeight> weights = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Path dataFilePath;

    @PostConstruct
    public void init() {
        try {
            String dataDir = System.getProperty("user.dir") + "/data";
            Files.createDirectories(Path.of(dataDir));
            dataFilePath = Path.of(dataDir, "question_weights.json");

            if (Files.exists(dataFilePath)) {
                List<QuestionWeight> list = objectMapper.readValue(
                        dataFilePath.toFile(),
                        new TypeReference<List<QuestionWeight>>() {});
                for (QuestionWeight w : list) {
                    weights.put(w.getQuestionId(), w);
                }
            }
        } catch (Exception e) {
            weights = new HashMap<>();
        }
    }

    private void save() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(dataFilePath.toFile(), new ArrayList<>(weights.values()));
        } catch (Exception e) {
            // Save failed — weights live in memory for this session
        }
    }

    /**
     * Update weight after answering a question.
     *
     * For MC questions: correct → -1, wrong → +1
     * For Flashcard questions: easy → -1, medium → 0, hard → +1
     */
    public void updateWeight(String questionId, String questionType, String difficulty, boolean correct) {
        QuestionWeight w = weights.getOrDefault(questionId,
                new QuestionWeight(questionId, 0, null));
        w.setQuestionId(questionId);
        w.setLastAnswered(LocalDate.now().toString());

        if ("mc".equals(questionType) || "choice".equals(questionType)) {
            w.setWeight(w.getWeight() + (correct ? -1 : 1));
        } else {
            // flashcard — use difficulty rating
            switch (difficulty != null ? difficulty : "medium") {
                case "easy" -> w.setWeight(w.getWeight() - 1);
                case "hard" -> w.setWeight(w.getWeight() + 1);
                default -> {} // medium: no change
            }
        }

        // Clamp weight to [-5, 5] to prevent runaway values
        w.setWeight(Math.max(-5, Math.min(5, w.getWeight())));

        weights.put(questionId, w);
        save();
    }

    /**
     * Calculate priority score for a question.
     * Higher score = should appear sooner.
     *
     * TODO: 后续需要更新/improve - 优先级公式过于简单
     */
    public double getPriority(String questionId) {
        QuestionWeight w = weights.get(questionId);
        if (w == null) {
            // Never seen question gets a moderate priority to ensure it appears
            return 2.0;
        }

        double daysSince = 0;
        if (w.getLastAnswered() != null) {
            try {
                LocalDate last = LocalDate.parse(w.getLastAnswered());
                daysSince = ChronoUnit.DAYS.between(last, LocalDate.now());
            } catch (Exception ignored) {}
        }

        // priority = weight + time decay bonus
        // Higher weight (harder for user) + longer since last seen = higher priority
        return w.getWeight() + daysSince * 0.3;
    }

    public Map<String, QuestionWeight> getAllWeights() {
        return weights;
    }
}
