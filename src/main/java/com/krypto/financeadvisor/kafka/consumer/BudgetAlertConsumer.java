package com.krypto.financeadvisor.kafka.consumer;

import com.krypto.financeadvisor.kafka.event.BudgetAlertEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BudgetAlertConsumer {
    @KafkaListener(topics = "${spring.kafka.topics.budget-alerts}")
    public void consume(BudgetAlertEvent event) {
        log.warn(
                "BUDGET ALERT | userId={} | category={} | spent={} | limit={} | alertType={}",
                event.getUserId(),
                event.getCategoryName(),
                event.getSpentThisMonth(),
                event.getMonthlyLimit(),
                event.getAlertType()
        );
    }
}
