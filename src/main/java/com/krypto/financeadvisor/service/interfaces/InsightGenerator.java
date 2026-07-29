package com.krypto.financeadvisor.service.interfaces;

import com.krypto.financeadvisor.entity.AiInsight;
import com.krypto.financeadvisor.entity.InsightType;

public interface InsightGenerator {

    AiInsight generate(Long userId);

    InsightType supportedType();
}
