package com.tastyhouse.domain.reservation.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.domain.exception.AccessDeniedException;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ReservationTest {

    private static final MemberId MEMBER_ID = MemberId.of(1L);
    private static final Long SHOP_ID = 10L;
    private static final LocalDate DATE = LocalDate.of(2026, 8, 1);
    private static final LocalTime TIME = LocalTime.of(11, 0);

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 상태는 PENDING이다")
    void of_createsTransientReservation() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, "창가 자리 부탁드려요");

        assertThat(reservation.getId()).isNull();
        assertThat(reservation.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(reservation.getShopId()).isEqualTo(SHOP_ID);
        assertThat(reservation.getReservationDate()).isEqualTo(DATE);
        assertThat(reservation.getReservationTime()).isEqualTo(TIME);
        assertThat(reservation.getPartySize()).isEqualTo(2);
        assertThat(reservation.getRequest()).isEqualTo("창가 자리 부탁드려요");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("confirm은 PENDING 상태를 CONFIRMED로 전이한다")
    void confirm_transitionsPendingToConfirmed() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);

        reservation.confirm();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirm은 PENDING이 아니면 RESERVATION_INVALID_STATUS 예외를 던진다")
    void confirm_onNonPending_throws() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        reservation.confirm();

        assertThatThrownBy(reservation::confirm)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.RESERVATION_INVALID_STATUS);
    }

    @Test
    @DisplayName("reject는 PENDING 상태를 REJECTED로 전이한다")
    void reject_transitionsPendingToRejected() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);

        reservation.reject();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REJECTED);
    }

    @Test
    @DisplayName("reject는 PENDING이 아니면 RESERVATION_INVALID_STATUS 예외를 던진다")
    void reject_onNonPending_throws() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        reservation.confirm();

        assertThatThrownBy(reservation::reject)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.RESERVATION_INVALID_STATUS);
    }

    @Test
    @DisplayName("complete는 CONFIRMED 상태를 COMPLETED로 전이한다")
    void complete_transitionsConfirmedToCompleted() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        reservation.confirm();

        reservation.complete();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("complete는 CONFIRMED가 아니면 RESERVATION_INVALID_STATUS 예외를 던진다")
    void complete_onNonConfirmed_throws() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);

        assertThatThrownBy(reservation::complete)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.RESERVATION_INVALID_STATUS);
    }

    @Test
    @DisplayName("cancel은 PENDING·CONFIRMED 상태를 CANCELED로 전이한다")
    void cancel_transitionsPendingOrConfirmedToCanceled() {
        Reservation pending = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        pending.cancel();
        assertThat(pending.getStatus()).isEqualTo(ReservationStatus.CANCELED);

        Reservation confirmed = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        confirmed.confirm();
        confirmed.cancel();
        assertThat(confirmed.getStatus()).isEqualTo(ReservationStatus.CANCELED);
    }

    @Test
    @DisplayName("cancel은 이미 CANCELED면 RESERVATION_ALREADY_CANCELED 예외를 던진다")
    void cancel_onAlreadyCanceled_throws() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        reservation.cancel();

        assertThatThrownBy(reservation::cancel)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.RESERVATION_ALREADY_CANCELED);
    }

    @Test
    @DisplayName("cancel은 REJECTED 상태면 RESERVATION_ALREADY_REJECTED 예외를 던진다")
    void cancel_onRejected_throws() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        reservation.reject();

        assertThatThrownBy(reservation::cancel)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.RESERVATION_ALREADY_REJECTED);
    }

    @Test
    @DisplayName("cancel은 COMPLETED 상태면 RESERVATION_ALREADY_COMPLETED 예외를 던진다")
    void cancel_onCompleted_throws() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);
        reservation.confirm();
        reservation.complete();

        assertThatThrownBy(reservation::cancel)
            .isInstanceOf(BusinessException.class)
            .extracting(e -> ((BusinessException) e).getErrorCode())
            .isEqualTo(ErrorCode.RESERVATION_ALREADY_COMPLETED);
    }

    @Test
    @DisplayName("validateOwnership은 본인이 아니면 AccessDeniedException을 던진다")
    void validateOwnership_onOtherMember_throws() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);

        assertThatThrownBy(() -> reservation.validateOwnership(MemberId.of(999L)))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("validateOwnership은 본인이면 예외를 던지지 않는다")
    void validateOwnership_onSameMember_doesNotThrow() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 2, null);

        reservation.validateOwnership(MEMBER_ID);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·상태·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        Reservation reservation = Reservation.reconstitute(
            1L, MEMBER_ID, SHOP_ID, DATE, TIME, 2, ReservationStatus.CONFIRMED, "요청사항", createdAt
        );

        assertThat(reservation.getId()).isEqualTo(1L);
        assertThat(reservation.getReservationId()).isEqualTo(ReservationId.of(1L));
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("of는 인원수 경계값 1명을 통과시킨다")
    void of_partySizeOne_passes() {
        Reservation reservation = Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 1, null);

        assertThat(reservation.getPartySize()).isEqualTo(1);
    }

    @Test
    @DisplayName("of는 인원수가 0·음수·null이면 RESERVATION_PARTY_SIZE_INVALID로 거부한다")
    @SuppressWarnings("DataFlowIssue") // null partySize 호출이 항상 실패하는 것이 이 테스트의 검증 대상이다
    void of_invalidPartySize_throws() {
        assertThatThrownBy(() -> Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, 0, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RESERVATION_PARTY_SIZE_INVALID);

        assertThatThrownBy(() -> Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, -1, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RESERVATION_PARTY_SIZE_INVALID);

        assertThatThrownBy(() -> Reservation.of(MEMBER_ID, SHOP_ID, DATE, TIME, null, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RESERVATION_PARTY_SIZE_INVALID);
    }

    @Test
    @DisplayName("reconstitute는 인원수 검증을 하지 않는다(불변식 위반 레거시 행도 로드 가능)")
    void reconstitute_bypassesPartySizeValidation() {
        Reservation reservation = Reservation.reconstitute(
            1L, MEMBER_ID, SHOP_ID, DATE, TIME, 0, ReservationStatus.CONFIRMED, null, null
        );

        assertThat(reservation.getPartySize()).isZero();
    }
}
