package com.tastyhouse.domain.reservation.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.reservation.domain.service.SlotPolicy;
import com.tastyhouse.domain.shop.domain.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ReservationSlotTest {

    private static final ShopId SHOP_ID = ShopId.of(10L);
    private static final LocalDate DATE = LocalDate.of(2026, 8, 1);
    private static final LocalTime TIME = LocalTime.of(11, 0);

    @Test
    @DisplayName("of는 capacity가 주어지면 그 값을 쓰고 점유 0건·미영속·버전 없음 상태로 생성한다")
    void of_withCapacity_createsTransientSlot() {
        ReservationSlot slot = ReservationSlot.of(SHOP_ID, DATE, TIME, 5);

        assertThat(slot.getId()).isNull();
        assertThat(slot.getShopId()).isEqualTo(SHOP_ID);
        assertThat(slot.getSlotDate()).isEqualTo(DATE);
        assertThat(slot.getSlotTime()).isEqualTo(TIME);
        assertThat(slot.getCapacity()).isEqualTo(5);
        assertThat(slot.getReservedCount()).isZero();
        assertThat(slot.getVersion()).isNull();
    }

    @Test
    @DisplayName("of는 capacity가 null이면 기본 정원(CAPACITY_PER_SLOT)을 쓴다")
    void of_withNullCapacity_usesDefaultCapacity() {
        ReservationSlot slot = ReservationSlot.of(SHOP_ID, DATE, TIME, null);

        assertThat(slot.getCapacity()).isEqualTo(SlotPolicy.CAPACITY_PER_SLOT);
    }

    @Test
    @DisplayName("reserve는 정원 1팀을 차감한다")
    void reserve_incrementsReservedCount() {
        ReservationSlot slot = ReservationSlot.of(SHOP_ID, DATE, TIME, 2);

        slot.reserve();

        assertThat(slot.getReservedCount()).isEqualTo(1);
        assertThat(slot.remaining()).isEqualTo(1);
        assertThat(slot.isFull()).isFalse();
    }

    @Test
    @DisplayName("reserve는 마감(정원 초과)되면 RESERVATION_SLOT_FULL 예외를 던진다")
    void reserve_whenFull_throws() {
        ReservationSlot slot = ReservationSlot.of(SHOP_ID, DATE, TIME, 1);
        slot.reserve();

        assertThat(slot.isFull()).isTrue();
        assertThatThrownBy(slot::reserve)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.RESERVATION_SLOT_FULL);
    }

    @Test
    @DisplayName("release는 점유 팀 수를 1 반납한다")
    void release_decrementsReservedCount() {
        ReservationSlot slot = ReservationSlot.of(SHOP_ID, DATE, TIME, 2);
        slot.reserve();

        slot.release();

        assertThat(slot.getReservedCount()).isZero();
    }

    @Test
    @DisplayName("release는 점유가 0이면 더 내려가지 않는다")
    void release_whenZero_doesNotGoNegative() {
        ReservationSlot slot = ReservationSlot.of(SHOP_ID, DATE, TIME, 2);

        slot.release();

        assertThat(slot.getReservedCount()).isZero();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·점유수·낙관적 락 버전을 포함해 재구성한다")
    void reconstitute_restoresPersistedStateIncludingVersion() {
        ReservationSlot slot = ReservationSlot.reconstitute(1L, SHOP_ID, DATE, TIME, 10, 3, 7L);

        assertThat(slot.getId()).isEqualTo(1L);
        assertThat(slot.getReservedCount()).isEqualTo(3);
        assertThat(slot.getVersion()).isEqualTo(7L);
        assertThat(slot.remaining()).isEqualTo(7);
    }
}
