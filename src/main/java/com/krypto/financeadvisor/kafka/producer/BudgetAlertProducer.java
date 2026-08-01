package com.krypto.financeadvisor.kafka.producer;

import com.krypto.financeadvisor.kafka.event.BudgetAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertProducer {
    private final KafkaTemplate<String, BudgetAlertEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.budget-alerts}")
    private String budgetAlertTopic;

    public void publish(BudgetAlertEvent event){
        kafkaTemplate.send(budgetAlertTopic, event)
                .whenComplete((result, error)->{
                    if(error!=null){
                        log.error("failed to publish budget alert event");
                        return;
                    }

                    log.info("Budget alert published. topic={}", result.getRecordMetadata().topic());
                });
    }
}
