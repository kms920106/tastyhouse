package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.domain.shop.model.ShopHygieneBadge;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.shop.vo.ShopId;

class ShopHygieneBadgeTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·생성시각 없음)다")
    void of_createsTransientShopHygieneBadge() {
        LocalDate certifiedDate = LocalDate.of(2026, 7, 1);

        ShopHygieneBadge badge = ShopHygieneBadge.of(ShopId.of(1L), HygieneBadgeType.CESCO_BLUE, certifiedDate, "2026-03");

        assertThat(badge.getId()).isNull();
        assertThat(badge.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(badge.getBadgeType()).isEqualTo(HygieneBadgeType.CESCO_BLUE);
        assertThat(badge.getCertifiedDate()).isEqualTo(certifiedDate);
        assertThat(badge.getLastInspectionMonth()).isEqualTo("2026-03");
        assertThat(badge.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·생성시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDate certifiedDate = LocalDate.of(2026, 1, 1);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        ShopHygieneBadge badge = ShopHygieneBadge.reconstitute(
            1L, ShopId.of(2L), HygieneBadgeType.FOOD_SAFETY_CERTIFIED, certifiedDate, null, createdAt
        );

        assertThat(badge.getId()).isEqualTo(1L);
        assertThat(badge.getShopId()).isEqualTo(ShopId.of(2L));
        assertThat(badge.getBadgeType()).isEqualTo(HygieneBadgeType.FOOD_SAFETY_CERTIFIED);
        assertThat(badge.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("HygieneBadgeType.from은 정의된 코드를 enum으로 변환한다")
    void from_returnsMatchingEnum() {
        assertThat(HygieneBadgeType.from("FOOD_SAFETY_CERTIFIED")).isEqualTo(HygieneBadgeType.FOOD_SAFETY_CERTIFIED);
        assertThat(HygieneBadgeType.from("CESCO_BLUE")).isEqualTo(HygieneBadgeType.CESCO_BLUE);
        assertThat(HygieneBadgeType.from("CESCO_WHITE")).isEqualTo(HygieneBadgeType.CESCO_WHITE);
    }

    @Test
    @DisplayName("HygieneBadgeType.from은 정의되지 않은 코드에 BusinessException을 던진다")
    void from_onUnknownCode_throwsBusinessException() {
        assertThatThrownBy(() -> HygieneBadgeType.from("UNKNOWN_TYPE"))
            .isInstanceOf(BusinessException.class);
    }
}
