package com.tastyhouse.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

class ShopPhoneNumberTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태다")
    void of_createsTransientPhoneNumber() {
        ShopPhoneNumber phoneNumber = ShopPhoneNumber.of(ShopId.of(1L), "02-1234-5678", true, false);

        assertThat(phoneNumber.getId()).isNull();
        assertThat(phoneNumber.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(phoneNumber.getPhoneNumber()).isEqualTo("02-1234-5678");
        assertThat(phoneNumber.isPrimary()).isTrue();
        assertThat(phoneNumber.isVirtual()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ShopPhoneNumber phoneNumber = ShopPhoneNumber.reconstitute(1L, ShopId.of(2L), "02-1234-5678", true, false, null, null);

        assertThat(phoneNumber.getId()).isEqualTo(1L);
        assertThat(phoneNumber.getShopId()).isEqualTo(ShopId.of(2L));
    }

    @Test
    @DisplayName("가상번호 조건을 충족하는 번호는 정상 생성된다")
    void of_withValidVirtualNumber_succeeds() {
        ShopPhoneNumber phoneNumber = ShopPhoneNumber.of(ShopId.of(1L), "070-1234-5678", false, true);

        assertThat(phoneNumber.isVirtual()).isTrue();
    }

    @Test
    @DisplayName("가상번호 조건(지정 국번)을 충족하지 않으면 예외가 발생한다")
    void of_withInvalidVirtualNumberPrefix_throws() {
        assertThatThrownBy(() -> ShopPhoneNumber.of(ShopId.of(1L), "999-1234-5678", false, true))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("가상번호 조건(8~13자리)을 충족하지 않으면 예외가 발생한다")
    void of_withInvalidVirtualNumberLength_throws() {
        assertThatThrownBy(() -> ShopPhoneNumber.of(ShopId.of(1L), "02-123", false, true))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("isValidVirtualNumber는 국번과 자릿수 조건을 함께 검사한다")
    void isValidVirtualNumber_checksPrefixAndLength() {
        assertThat(ShopPhoneNumber.isValidVirtualNumber("02-1234-5678")).isTrue();
        assertThat(ShopPhoneNumber.isValidVirtualNumber("010-1234-5678")).isTrue();
        assertThat(ShopPhoneNumber.isValidVirtualNumber("999-1234-5678")).isFalse();
        assertThat(ShopPhoneNumber.isValidVirtualNumber("02-12")).isFalse();
        assertThat(ShopPhoneNumber.isValidVirtualNumber(null)).isFalse();
    }

    @Test
    @DisplayName("markPrimary/unmarkPrimary는 대표 여부를 전환한다")
    void markPrimary_and_unmarkPrimary_togglePrimaryFlag() {
        ShopPhoneNumber phoneNumber = ShopPhoneNumber.of(ShopId.of(1L), "02-1234-5678", false, false);

        phoneNumber.markPrimary();
        assertThat(phoneNumber.isPrimary()).isTrue();

        phoneNumber.unmarkPrimary();
        assertThat(phoneNumber.isPrimary()).isFalse();
    }
}
