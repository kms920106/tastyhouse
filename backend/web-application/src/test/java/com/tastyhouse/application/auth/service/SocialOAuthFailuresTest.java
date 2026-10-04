package com.tastyhouse.application.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.application.auth.port.out.SocialOAuthFailure;
import com.tastyhouse.application.auth.port.out.SocialOAuthResult;
import com.tastyhouse.application.shared.exception.WebErrorCode;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialOAuthFailuresTest {

    @ParameterizedTest
    @CsvSource({
        "APPLE_ID_TOKEN_INVALID, APPLE_ID_TOKEN_INVALID",
        "ACCESS_TOKEN_REJECTED, SOCIAL_OAUTH_FAILED"
    })
    @DisplayName("소셜 인증 실패 사유는 기존과 같은 ErrorCode의 BusinessException으로 번역된다")
    void translatesFailureToErrorCode(SocialOAuthFailure failure, WebErrorCode expected) {
        SocialOAuthResult<String> result = SocialOAuthResult.failed(failure);

        assertThatThrownBy(() -> result.orElseThrow(SocialOAuthFailures::toException))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", expected);
    }
}
