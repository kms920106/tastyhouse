package com.tastyhouse.webapi.verification;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationCodeGenerator {

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
