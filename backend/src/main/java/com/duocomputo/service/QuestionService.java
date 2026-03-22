package com.duocomputo.service;

import com.duocomputo.model.FlashcardQuestion;
import com.duocomputo.model.McQuestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private List<McQuestion> mcQuestions = new ArrayList<>();
    private List<FlashcardQuestion> flashcardQuestions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SpacedRepetitionService spacedRepetitionService;

    public QuestionService(SpacedRepetitionService spacedRepetitionService) {
        this.spacedRepetitionService = spacedRepetitionService;
    }

    @PostConstruct
    public void init() {
        try {
            InputStream mcStream = getClass().getResourceAsStream("/data/mc_questions.json");
            mcQuestions = objectMapper.readValue(mcStream, new TypeReference<List<McQuestion>>() {});

            InputStream fcStream = getClass().getResourceAsStream("/data/flashcard_questions.json");
            flashcardQuestions = objectMapper.readValue(fcStream, new TypeReference<List<FlashcardQuestion>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load question files", e);
        }
    }

    public List<McQuestion> getAllMcQuestions() {
        return mcQuestions;
    }

    public List<FlashcardQuestion> getAllFlashcardQuestions() {
        return flashcardQuestions;
    }

    /**
     * Select daily questions using spaced repetition priorities.
     * Picks 2 MC + 1 flashcard, weighted by priority score.
     *
     * TODO: 后续需要更新/improve - 当前每日题目数量固定为 2MC+1FC，
     * 未来应根据用户正确率动态调整（如正确率高则加题或出新题）
     */
    public DailySelection getDailyQuestions() {
        List<McQuestion> selectedMc = selectByPriority(mcQuestions, 2);
        List<FlashcardQuestion> selectedFc = selectByPriority(flashcardQuestions, 1);

        return new DailySelection(selectedMc, selectedFc);
    }

    /**
     * Select questions weighted by spaced repetition priority.
     * Higher priority questions are more likely to be chosen.
     *
     * TODO: 后续需要更新/improve - 加权随机选择算法较简单，
     * 未来可优化为更精确的概率分布
     */
    private <T> List<T> selectByPriority(List<T> questions, int count) {
        if (questions.size() <= count) {
            return new ArrayList<>(questions);
        }

        // Calculate priorities and normalize to positive values for weighted random
        List<double[]> priorities = new ArrayList<>();
        double minPriority = Double.MAX_VALUE;
        for (int i = 0; i < questions.size(); i++) {
            T q = questions.get(i);
            String id = getQuestionId(q);
            double priority = spacedRepetitionService.getPriority(id);
            priorities.add(new double[]{i, priority});
            minPriority = Math.min(minPriority, priority);
        }

        // Shift all priorities to be positive (minimum becomes 1.0)
        double shift = 1.0 - minPriority;
        for (double[] p : priorities) {
            p[1] += shift;
        }

        // Weighted random selection without replacement
        List<T> selected = new ArrayList<>();
        Random random = new Random();
        Set<Integer> usedIndices = new HashSet<>();

        for (int pick = 0; pick < count; pick++) {
            double totalWeight = 0;
            for (double[] p : priorities) {
                if (!usedIndices.contains((int) p[0])) {
                    totalWeight += p[1];
                }
            }

            double roll = random.nextDouble() * totalWeight;
            double cumulative = 0;
            for (double[] p : priorities) {
                int idx = (int) p[0];
                if (usedIndices.contains(idx)) continue;
                cumulative += p[1];
                if (roll <= cumulative) {
                    selected.add(questions.get(idx));
                    usedIndices.add(idx);
                    break;
                }
            }
        }

        return selected;
    }

    private <T> String getQuestionId(T question) {
        if (question instanceof McQuestion mc) return mc.getId();
        if (question instanceof FlashcardQuestion fc) return fc.getId();
        return "";
    }

    public McQuestion getMcQuestionById(String id) {
        return mcQuestions.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public FlashcardQuestion getFlashcardQuestionById(String id) {
        return flashcardQuestions.stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public McQuestion addMcQuestion(McQuestion q) {
        if (q.getId() == null || q.getId().isBlank()) {
            q.setId(UUID.randomUUID().toString());
        }
        mcQuestions.add(q);
        persistMcQuestions();
        return q;
    }

    public FlashcardQuestion addFlashcardQuestion(FlashcardQuestion q) {
        if (q.getId() == null || q.getId().isBlank()) {
            q.setId(UUID.randomUUID().toString());
        }
        flashcardQuestions.add(q);
        persistFlashcardQuestions();
        return q;
    }

    // --- Category methods ---

    public List<String> getAllCategories() {
        Set<String> categories = new LinkedHashSet<>();
        mcQuestions.forEach(q -> categories.add(q.getCategory()));
        flashcardQuestions.forEach(q -> categories.add(q.getCategory()));
        return new ArrayList<>(categories);
    }

    public List<McQuestion> getMcQuestionsByCategory(String category) {
        return mcQuestions.stream()
                .filter(q -> category.equals(q.getCategory()))
                .collect(Collectors.toList());
    }

    public List<FlashcardQuestion> getFlashcardQuestionsByCategory(String category) {
        return flashcardQuestions.stream()
                .filter(q -> category.equals(q.getCategory()))
                .collect(Collectors.toList());
    }

    public DailySelection getCategoryQuestions(String category) {
        List<McQuestion> filteredMc = getMcQuestionsByCategory(category);
        List<FlashcardQuestion> filteredFc = getFlashcardQuestionsByCategory(category);

        List<McQuestion> selectedMc = selectByPriority(filteredMc, Math.min(2, filteredMc.size()));
        List<FlashcardQuestion> selectedFc = selectByPriority(filteredFc, Math.min(1, filteredFc.size()));

        return new DailySelection(selectedMc, selectedFc);
    }

    // --- Frequency simulation (dev tool) ---

    public Map<String, Object> simulateSelectionFrequency(int rounds) {
        Map<String, Integer> mcCounts = new LinkedHashMap<>();
        Map<String, Integer> fcCounts = new LinkedHashMap<>();
        Map<String, String> mcTitles = new LinkedHashMap<>();
        Map<String, String> fcTitles = new LinkedHashMap<>();

        for (McQuestion q : mcQuestions) {
            mcCounts.put(q.getId(), 0);
            mcTitles.put(q.getId(), q.getQuestion());
        }
        for (FlashcardQuestion q : flashcardQuestions) {
            fcCounts.put(q.getId(), 0);
            fcTitles.put(q.getId(), q.getContent());
        }

        for (int i = 0; i < rounds; i++) {
            List<McQuestion> selectedMc = selectByPriority(mcQuestions, 2);
            List<FlashcardQuestion> selectedFc = selectByPriority(flashcardQuestions, 1);
            for (McQuestion q : selectedMc) mcCounts.merge(q.getId(), 1, Integer::sum);
            for (FlashcardQuestion q : selectedFc) fcCounts.merge(q.getId(), 1, Integer::sum);
        }

        List<Map<String, Object>> mcResults = new ArrayList<>();
        for (var entry : mcCounts.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", entry.getKey());
            item.put("title", mcTitles.get(entry.getKey()));
            item.put("count", entry.getValue());
            item.put("percentage", Math.round(entry.getValue() * 10000.0 / (rounds * 2)) / 100.0);
            item.put("priority", spacedRepetitionService.getPriority(entry.getKey()));
            mcResults.add(item);
        }

        List<Map<String, Object>> fcResults = new ArrayList<>();
        for (var entry : fcCounts.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", entry.getKey());
            item.put("title", fcTitles.get(entry.getKey()));
            item.put("count", entry.getValue());
            item.put("percentage", Math.round(entry.getValue() * 10000.0 / rounds) / 100.0);
            item.put("priority", spacedRepetitionService.getPriority(entry.getKey()));
            fcResults.add(item);
        }

        mcResults.sort((a, b) -> ((Integer) b.get("count")).compareTo((Integer) a.get("count")));
        fcResults.sort((a, b) -> ((Integer) b.get("count")).compareTo((Integer) a.get("count")));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("rounds", rounds);
        result.put("mcResults", mcResults);
        result.put("fcResults", fcResults);
        return result;
    }

    private void persistMcQuestions() {
        persist(mcQuestions, "/src/main/resources/data/mc_questions.json");
    }

    private void persistFlashcardQuestions() {
        persist(flashcardQuestions, "/src/main/resources/data/flashcard_questions.json");
    }

    private void persist(Object data, String relativePath) {
        try {
            File file = new File(System.getProperty("user.dir") + relativePath);
            if (file.exists()) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            }
        } catch (Exception ignored) {
            // Persist failed — data lives in memory for this session
        }
    }

    /** Simple DTO to return both lists together from getDailyQuestions */
    public record DailySelection(List<McQuestion> mcQuestions, List<FlashcardQuestion> flashcardQuestions) {}
}
