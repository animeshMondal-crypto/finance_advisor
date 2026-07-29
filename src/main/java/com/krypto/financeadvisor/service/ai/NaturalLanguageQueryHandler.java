package com.krypto.financeadvisor.service.ai;

import com.krypto.financeadvisor.service.interfaces.Analyzable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaturalLanguageQueryHandler {

    private final ChatClient chatClient;
    private final Analyzable analyzable;

    public String query(Long userId, String userQuestion){
        Map<String, Object> context = analyzable.analyze(userId);

        String prompt = """
                You are a personal finance assistant for an Indian user.
                
                Here is their current financial context:
                
                Month: %s
                Total Income:   ₹%s
                Total Expenses: ₹%s
                Net Savings:    ₹%s
                Savings Rate:   %s%%
                
                Spending by category:
                %s
                
                User question: "%s"
                
                Answer the question naturally and concisely using the data above.
                If the answer is not in the data, say so honestly.
                Use ₹ for amounts. Keep the response under 3 sentences.
                """.formatted(
                context.get("monthYear"),
                context.get("totalIncome"),
                context.get("totalExpenses"),
                context.get("netSavings"),
                context.get("savingsRate"),
                context.get("categoryBreakdown"),
                userQuestion
        );

        log.info("NL Query from user {}: {}", userId, userQuestion);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}
