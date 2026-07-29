package com.krypto.financeadvisor.service.ai;

import com.krypto.financeadvisor.service.interfaces.Categorizable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionCategorizer implements Categorizable {

    private final ChatClient chatClient;

    @Override
    public String categorize(String description, String amount) {
        String prompt = """
                You are a personal finance assistant for Indian users.
                
                Categorize this transaction into EXACTLY one of these categories:
                Food, Transport, Shopping, Bills, Entertainment, Health, Salary, Investment, Other
                
                Transaction description: "%s"
                Amount: ₹%s
                
                Rules:
                - Reply with ONLY the category name, nothing else
                - No punctuation, no explanation
                - If unsure, reply with: Other
                
                Examples:
                "Swiggy order" → Food
                "Ola cab" → Transport
                "Netflix subscription" → Entertainment
                "Apollo pharmacy" → Health
                "Salary credited" → Salary
                """.formatted(description, amount);

        try {
            String result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content()
                    .trim();
            log.info("AI categorized '{}' as '{}'", description, result);
            return result;
        } catch (Exception e) {
            log.error("AI categorization failed for '{}', defaulting to Other", description, e);
            return "Other";
        }
    }
}
