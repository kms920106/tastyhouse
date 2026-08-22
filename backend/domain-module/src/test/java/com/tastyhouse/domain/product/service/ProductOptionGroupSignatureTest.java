package com.tastyhouse.domain.product.service;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.model.ProductOptionGroupType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 옵션그룹 동일성 서명의 순수 단위 테스트.
 *
 * <p>이 서명이 흔들리면 <b>점주가 [X]로 제외한 묶음이 다시 추천되거나, 반대로 엉뚱한 묶음이 영구히
 * 숨는다.</b> 서명은 저장되는 값이므로 계산 규칙이 바뀌면 과거 제외 기록이 통째로 무효가 된다.
 */
class ProductOptionGroupSignatureTest {

    @Test
    @DisplayName("★ 옵션 정렬 순서(sort)가 달라도 같은 서명이 나온다 — 진열 순서는 동일성과 무관하다")
    void signature_ignoresOptionSort() {
        ProductOptionGroup group = group("토핑", 1, 3);
        List<ProductOption> ascending = List.of(
            option(1L, "치즈", 500, 0),
            option(2L, "베이컨", 1000, 1)
        );
        List<ProductOption> reversed = List.of(
            option(2L, "베이컨", 1000, 5),
            option(1L, "치즈", 500, 9)
        );

        assertThat(ProductOptionGroupSignature.of(group, ascending))
            .isEqualTo(ProductOptionGroupSignature.of(group, reversed));
    }

    @Test
    @DisplayName("★ 숨은 옵션은 서명에 참여하지 않는다 — 눈에 똑같은 두 그룹이 유령 옵션으로 갈리면 안 된다")
    void signature_excludesHiddenOptions() {
        ProductOptionGroup group = group("토핑", 1, 3);
        ProductOption hidden = option(3L, "단종된옵션", 2000, 2);
        hidden.hide();

        String withoutHidden = ProductOptionGroupSignature.of(group, List.of(option(1L, "치즈", 500, 0)));
        String withHidden = ProductOptionGroupSignature.of(group,
            List.of(option(1L, "치즈", 500, 0), hidden));

        assertThat(withHidden).isEqualTo(withoutHidden);
    }

    @Test
    @DisplayName("★ 옵션 개수가 서명에 반영된다 — GROUP_CONCAT 잘림으로 인한 조용한 오탐을 막는다")
    void signature_reflectsOptionCount() {
        ProductOptionGroup group = group("토핑", 1, 3);

        String one = ProductOptionGroupSignature.of(group, List.of(option(1L, "치즈", 500, 0)));
        String two = ProductOptionGroupSignature.of(group,
            List.of(option(1L, "치즈", 500, 0), option(2L, "베이컨", 1000, 1)));

        assertThat(one).isNotEqualTo(two);
    }

    @Test
    @DisplayName("가격이 다르면 다른 서명이다 — 이름만 같은 옵션은 같은 묶음이 아니다")
    void signature_reflectsPrice() {
        ProductOptionGroup group = group("토핑", 1, 3);

        String cheap = ProductOptionGroupSignature.of(group, List.of(option(1L, "치즈", 500, 0)));
        String pricey = ProductOptionGroupSignature.of(group, List.of(option(1L, "치즈", 900, 0)));

        assertThat(cheap).isNotEqualTo(pricey);
    }

    @Test
    @DisplayName("그룹명·min·max가 다르면 다른 서명이다")
    void signature_reflectsGroupAttributes() {
        List<ProductOption> options = List.of(option(1L, "치즈", 500, 0));

        String base = ProductOptionGroupSignature.of(group("토핑", 1, 3), options);
        assertThat(ProductOptionGroupSignature.of(group("사이드", 1, 3), options)).isNotEqualTo(base);
        assertThat(ProductOptionGroupSignature.of(group("토핑", 2, 3), options)).isNotEqualTo(base);
        assertThat(ProductOptionGroupSignature.of(group("토핑", 1, 2), options)).isNotEqualTo(base);
    }

    @Test
    @DisplayName("min/max가 null인 것과 0인 것은 다른 서명이다 — '미지정'과 '0'은 다른 의미다")
    void signature_distinguishesNullFromZero() {
        List<ProductOption> options = List.of(option(1L, "치즈", 500, 0));

        assertThat(ProductOptionGroupSignature.of(group("토핑", null, null), options))
            .isNotEqualTo(ProductOptionGroupSignature.of(group("토핑", 0, 0), options));
    }

    @Test
    @DisplayName("서명은 SHA-256 hex 64자다 — CHAR(64) 컬럼과 길이가 맞아야 한다")
    void signature_isSha256Hex() {
        String signature = ProductOptionGroupSignature.of(
            group("토핑", 1, 3), List.of(option(1L, "치즈", 500, 0)));

        assertThat(signature).hasSize(64).matches("[0-9a-f]{64}");
    }

    @Test
    @DisplayName("★ SQL이 만든 payload를 해싱한 값이 Java 계산 결과와 같다 — 두 경로가 갈리면 제외가 깨진다")
    void signature_hashOfPayloadMatchesDirectCalculation() {
        ProductOptionGroup group = group("토핑", 1, 3);
        List<ProductOption> options = List.of(option(1L, "치즈", 500, 0), option(2L, "베이컨", 1000, 1));

        String payload = ProductOptionGroupSignature.payloadOf(group, options);

        assertThat(ProductOptionGroupSignature.hash(payload))
            .isEqualTo(ProductOptionGroupSignature.of(group, options));
    }

    private static ProductOptionGroup group(String name, Integer minSelect, Integer maxSelect) {
        return ProductOptionGroup.reconstitute(
            10L, ProductId.of(1L), name, null, false, false, minSelect, maxSelect, 0, true,
            ProductOptionGroupType.NORMAL
        );
    }

    private static ProductOption option(Long id, String name, Integer additionalPrice, Integer sort) {
        return ProductOption.reconstitute(
            id, ProductOptionGroupId.of(10L), name, additionalPrice, sort, false, null, true, null, null
        );
    }
}
