package com.krypto.financeadvisor.service.ai;

import com.krypto.financeadvisor.dto.request.CategorySummaryDto;
import com.krypto.financeadvisor.entity.AiInsight;
import com.krypto.financeadvisor.entity.InsightType;
import com.krypto.financeadvisor.entity.User;
import com.krypto.financeadvisor.exception.ResourceNotFountException;
import com.krypto.financeadvisor.repository.AiInsightRepository;
import com.krypto.financeadvisor.repository.TransactionRepository;
import com.krypto.financeadvisor.repository.UserRepository;
import com.krypto.financeadvisor.service.interfaces.Analyzable;
import com.krypto.financeadvisor.service.interfaces.InsightGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonthlySummaryGenerator implements InsightGenerator, Analyzable {

    private final ChatClient chatClient;
    private final TransactionRepository transactionRepository;
    private final AiInsightRepository aiInsightRepository;
    private final UserRepository userRepository;


    @Override
    public Map<String, Object> analyze(Long userId) {

        YearMonth current = YearMonth.now();
        LocalDateTime start = current.atDay(1).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        List<CategorySummaryDto> breakdown = transactionRepository
                .getCategoryBreakdown(userId, start, end);

        double totalExpenses = breakdown.stream()
                .mapToDouble(c -> c.total().doubleValue())
                .sum();

        // Get total income from CREDIT transactions
        List<CategorySummaryDto> incomeBreakdown = transactionRepository
                .getCategoryBreakdown(userId, start, end);

        // Format category breakdown as readable string for the prompt
        String categoryText = breakdown.stream()
                .map(c -> "  - %s: ₹%s".formatted(c.categoryName(), c.total()))
                .collect(Collectors.joining("\n"));

        double totalIncome = transactionRepository
                .findByUserAndDateRange(userId, start, end)
                .stream()
                .filter(t -> t.getType().name().equals("CREDIT"))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        double netSavings = totalIncome - totalExpenses;
        double savingsRate = totalIncome > 0
                ? Math.round((netSavings / totalIncome) * 100)
                : 0;

        Map<String, Object> data = new HashMap<>();
        data.put("monthYear", current.toString());
        data.put("totalIncome", totalIncome);
        data.put("totalExpenses", totalExpenses);
        data.put("netSavings", netSavings);
        data.put("savingsRate", savingsRate);
        data.put("categoryBreakdown", categoryText);

        return data;
    }

    @Override
    @Transactional
    public AiInsight generate(Long userId) {
        String monthYear = YearMonth.now().toString();

        // Prevent duplicate summaries for same month
        if (aiInsightRepository.existsByUserIdAndMonthYearAndType(
                userId, monthYear, InsightType.MONTHLY_SUMMARY)) {
            throw new IllegalArgumentException(
                    "Monthly summary for " + monthYear + " already exists");
        }

        // Build context from real transaction data
        Map<String, Object> analysisData = analyze(userId);

        String prompt = """
                You are a personal finance advisor for Indian users.
                
                Here is the user's financial data for %s:
                
                Total Income:   ₹%s
                Total Expenses: ₹%s
                Net Savings:    ₹%s
                Savings Rate:   %s%%
                
                Spending by category:
                %s
                
                Write a 4-5 sentence financial health summary that:
                1. States the overall financial health clearly
                2. Highlights the top 2 spending categories
                3. Comments on the savings rate (good if > 20%%)
                4. Gives one specific actionable tip for next month
                
                Keep the tone friendly and encouraging.
                Use ₹ for currency amounts.
                """.formatted(
                analysisData.get("monthYear"),
                analysisData.get("totalIncome"),
                analysisData.get("totalExpenses"),
                analysisData.get("netSavings"),
                analysisData.get("savingsRate"),
                analysisData.get("categoryBreakdown")
        );

        String insightText = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFountException.of("User", userId));

        AiInsight insight = AiInsight.builder()
                .insightText(insightText)
                .type(InsightType.MONTHLY_SUMMARY)
                .monthYear(monthYear)
                .user(user)
                .build();

        return aiInsightRepository.save(insight);
    }

    @Override
    public InsightType supportedType() {
        return InsightType.MONTHLY_SUMMARY;
    }
}
