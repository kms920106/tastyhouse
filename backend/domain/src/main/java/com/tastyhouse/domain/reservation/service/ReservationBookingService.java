package com.tastyhouse.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.reservation.model.Reservation;
import com.tastyhouse.domain.reservation.model.ReservationSlot;
import com.tastyhouse.domain.reservation.repository.ReservationRepository;
import com.tastyhouse.domain.reservation.repository.ReservationSlotRepository;
import com.tastyhouse.domain.reservation.vo.ReservationId;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 예약 예약/취소 불변식(도메인 서비스).
 *
 * <p>예약 생성은 "슬롯 정원 검증 → 정원 차감 → 예약 저장"이, 취소·거절은 "예약 상태 전이 → 슬롯 정원
 * 반납"이 반드시 함께 일어나야 하는 원자 연산이다. {@code Reservation}과 {@code ReservationSlot} 두
 * 애그리거트 타입을 한 트랜잭션에서 함께 load &amp; save 하는 불변식 오케스트레이션(분류 C)이므로 도메인
 * 계층에 두어, 트리거 액터(회원 취소 · 점주 거절)가 달라도 "예약 건수와 슬롯 점유 수는 항상 함께
 * 움직인다"는 규칙이 갈리지 않게 한다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며(공통 지침 패턴 1), 빈 등록은
 * infrastructure-module의 {@code ReservationDomainConfig}가 담당한다. 트랜잭션 경계는 이 서비스를 호출하는
 * 소비 모듈의 command 서비스가 선언한다.
 *
 * <p><b>동시성</b>: 정원 차감은 슬롯의 낙관적 락({@code @Version})으로 보호한다. 이 서비스는 차감 직후
 * {@code saveAndFlush}로 충돌을 트랜잭션 커밋 전에 노출시키기만 하고, 재시도는 하지 않는다 — 재시도는
 * 매 시도마다 새 트랜잭션이 필요하므로 트랜잭션 경계 <b>바깥</b>(소비 모듈의 command 서비스)에서
 * 수행해야 한다. 충돌 예외는 프레임워크-프리
 * {@link com.tastyhouse.domain.shared.exception.OptimisticLockConflictException}으로 번역되어 올라간다
 * (infrastructure 어댑터가 번역).
 *
 * <p>두 도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 */
public class ReservationBookingService {

    /**
     * 과거 일시 판정 기준 시간대 — 서비스 운영 지역(한국) 고정.
     */
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository slotRepository;
    private final ShopRepository shopRepository;
    private final MemberRepository memberRepository;
    private final ShopOrderAvailabilityService shopOrderAvailabilityService;

    public ReservationBookingService(
        ReservationRepository reservationRepository,
        ReservationSlotRepository slotRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository,
        ShopOrderAvailabilityService shopOrderAvailabilityService
    ) {
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
        this.shopRepository = shopRepository;
        this.memberRepository = memberRepository;
        this.shopOrderAvailabilityService = shopOrderAvailabilityService;
    }

    /**
     * 예약을 생성한다. 슬롯 정원 차감과 예약 저장이 한 트랜잭션에서 함께 일어난다.
     *
     * <p><b>주문가능 검증의 기준 시각은 예약 슬롯 시각이다</b>(4.5단계) — 지금이 영업시간 밖이어도
     * 영업시간 안의 미래 예약은 받아야 하기 때문이다. 반대로 폐업·노출정지는 시각과 무관하므로 4단계의
     * {@code findVisibleById}가 언제나 먼저 막는다.
     *
     * <p>슬롯이 마감이면 {@code RESERVATION_SLOT_FULL}로 즉시 실패하며(재시도 대상 아님), 동시 경합
     * (신규 슬롯 동시 insert = 유니크 충돌 / 기존 슬롯 동시 update = 낙관적 락 충돌)은 예외로 올라가
     * 호출자의 재시도 루프가 처리한다.
     */
    public ReservationId book(
        MemberId memberId,
        ShopId shopId,
        LocalDate date,
        LocalTime time,
        Integer partySize,
        String request,
        boolean agreedRequiredTerms
    ) {
        // 1. 필수 약관 동의 검증
        if (!agreedRequiredTerms) {
            throw new BusinessException(ErrorCode.RESERVATION_TERMS_NOT_AGREED);
        }

        // 2. 슬롯 시간 유효성 검증
        if (!SlotPolicy.isValidSlot(time)) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_TIME);
        }

        // 3. 과거 일시 검증 (Asia/Seoul 고정)
        if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now(KST))) {
            throw new BusinessException(ErrorCode.RESERVATION_PAST_NOT_ALLOWED);
        }

        // 4. 가게/회원 존재 검증
        //    회원 경로이므로 findVisibleById — 폐업·노출정지 가게 예약은 404가 된다(시각과 무관하게 차단).
        Shop shop = shopRepository.findVisibleById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (memberRepository.findById(memberId).isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 4.5. 주문가능 검증 — 기준 시각은 "지금"이 아니라 예약 슬롯 시각이다.
        //      지금이 휴게시간·영업시간 밖이라고 3시간 뒤 예약을 막으면 안 된다.
        shopOrderAvailabilityService.validateOrderable(
            shop, OrderMethod.RESERVATION, LocalDateTime.of(date, time)
        );

        // 5. 본인 중복 차단 (같은 가게 + 같은 날짜에 차단 예약 1건 제한)
        if (reservationRepository.existsBlockingByMemberShopDate(memberId, shopId, date)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        // 6. 슬롯 get-or-create
        ReservationSlot slot = slotRepository
            .findByShopAndDateAndTime(shopId, date, time)
            .orElseGet(() -> ReservationSlot.of(shopId, date, time, SlotPolicy.CAPACITY_PER_SLOT));

        // 7. 정원 차감 + 명시적 flush
        //    - reserve()가 마감이면 RESERVATION_SLOT_FULL 즉시 전파(재시도 대상 아님)
        //    - 신규 슬롯 동시 insert → 유니크 충돌, 기존 슬롯 동시 update → 낙관적 락 충돌
        //    두 경합 예외는 커밋 전에 노출되어야 호출자의 재시도 루프가 잡을 수 있으므로 여기서 flush한다.
        slot.reserve();
        slotRepository.saveAndFlush(slot);

        // 8. 예약 저장
        Reservation reservation = Reservation.of(memberId, shopId, date, time, partySize, request);
        Reservation saved = reservationRepository.save(reservation);

        return saved.getReservationId();
    }

    /**
     * 회원 본인이 예약을 취소한다(PENDING|CONFIRMED → CANCELED). 상태 전이와 슬롯 정원 반납이 함께 일어난다.
     */
    public void cancel(ReservationId reservationId, MemberId memberId) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateOwnership(memberId);
        reservation.cancel();
        reservationRepository.save(reservation);
        releaseSlot(reservation);
    }

    /**
     * 점주가 예약을 거절한다(PENDING → REJECTED). 상태 전이와 슬롯 정원 반납이 함께 일어난다.
     */
    public void reject(ReservationId reservationId) {
        Reservation reservation = getReservation(reservationId);
        reservation.reject();
        reservationRepository.save(reservation);
        releaseSlot(reservation);
    }

    private Reservation getReservation(ReservationId reservationId) {
        return reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    /**
     * 예약의 슬롯 좌표(shopId, date, time)로 슬롯을 역추적해 정원을 반납한다.
     * 슬롯 행이 없으면(과거 데이터 등) 반납할 점유가 없으므로 아무 것도 하지 않는다.
     */
    private void releaseSlot(Reservation reservation) {
        slotRepository
            .findByShopAndDateAndTime(
                reservation.getShopId(),
                reservation.getReservationDate(),
                reservation.getReservationTime()
            )
            .ifPresent(slot -> {
                slot.release();
                slotRepository.save(slot);
            });
    }
}
