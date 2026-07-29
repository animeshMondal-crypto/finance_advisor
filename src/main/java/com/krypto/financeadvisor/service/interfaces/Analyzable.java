package com.krypto.financeadvisor.service.interfaces;

import java.util.Map;

public interface Analyzable {
    Map<String, Object> analyze(Long userId);
}
