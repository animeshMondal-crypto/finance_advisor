package com.krypto.financeadvisor.util;

import com.krypto.financeadvisor.entity.User;
import com.krypto.financeadvisor.exception.ResourceNotFountException;
import com.krypto.financeadvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final UserRepository userRepository;

    public User getCurrentUser(){
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(()->new ResourceNotFountException("Authenticated user not found"));
    }

    public Long getCurrentUserId(){
        return getCurrentUser().getId();
    }
}
