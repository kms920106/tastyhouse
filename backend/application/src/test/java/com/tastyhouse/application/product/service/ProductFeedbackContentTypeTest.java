package com.tastyhouse.application.product.service;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductFeedbackType;

import static org.assertj.core.api.Assertions.assertThat;

class ProductFeedbackContentTypeTest {

    @Test
    @DisplayName("내용을 요구하는 피드백 유형은 ETC 하나다 — 피드백 요약 조회는 이 유형 하나만 인자로 넘긴다")
    void onlyEtcRequiresContent() {
        assertThat(Arrays.stream(ProductFeedbackType.values()).filter(ProductFeedbackType::requiresContent))
            .containsExactly(ProductFeedbackType.ETC);
    }
}
