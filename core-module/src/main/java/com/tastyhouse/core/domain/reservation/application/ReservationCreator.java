package com.tastyhouse.core.domain.reservation.application;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.reservation.application.dto.command.CreateReservationCommand;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import com.tastyhouse.core.domain.reservation.domain.model.ReservationSlot;
import com.tastyhouse.core.domain.reservation.domain.model.ShopReservationSlot;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationRepository;
import com.tastyhouse.core.domain.reservation.domain.repository.ShopReservationSlotRepository;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * 예약 생성의 실제 트랜잭션 단위.
 * {@link ReservationCommandService}의 재시도 루프와 별도 빈으로 분리해야
 * 낙관적 락 충돌 시 "새 트랜잭션"으로 재시도가 동작한다(self-invocation 회피).
 */
@Service
@RequiredArgsConstructor
public class ReservationCreator {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationRepository reservationRepository;
    private final ShopReservationSlotRepository slotRepository;
    private final ShopQueryService shopQueryService;
    private final MemberQueryService memberQueryService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ReservationResult createInNewTx(Long memberId, CreateReservationCommand cmd) {
        // 1. 필수 약관 동의 검증
        if (!cmd.agreedRequiredTerms()) {
            throw new BusinessException(ErrorCode.RESERVATION_TERMS_NOT_AGREED);
        }

        // 2. 슬롯 시간 유효성 검증
        if (!ReservationSlot.isValidSlot(cmd.time())) {
            throw new BusinessException(ErrorCode.RESERVATION_INVALID_TIME);
        }

        // 3. 과거 일시 검증 (Asia/Seoul 고정)
        LocalDateTime reservationDateTime = LocalDateTime.of(cmd.date(), cmd.time());
        if (reservationDateTime.isBefore(LocalDateTime.now(KST))) {
            throw new BusinessException(ErrorCode.RESERVATION_PAST_NOT_ALLOWED);
        }

        // 4. 가게/회원 검증 (가게 이름 확보)
        Shop shop = shopQueryService.findShopById(cmd.shopId());
        memberQueryService.getById(new MemberId(memberId));

        // 5. 본인 중복 차단 (같은 가게 + 같은 날짜에 재예약 차단 예약 1건 제한)
        if (reservationRepository.existsBlockingByMemberShopDate(memberId, cmd.shopId(), cmd.date())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESERVATION);
        }

        // 6. 슬롯 get-or-create
        ShopReservationSlot slot = slotRepository
            .findByShopAndDateAndTime(cmd.shopId(), cmd.date(), cmd.time())
            .orElseGet(() -> ShopReservationSlot.of(cmd.shopId(), cmd.date(), cmd.time(), ReservationSlot.CAPACITY_PER_SLOT));

        // 7. 정원 차감 + 명시적 flush
        //    - reserve()가 마감이면 RESERVATION_SLOT_FULL 즉시 전파(재시도 대상 아님)
        //    - 신규 슬롯 동시 insert  → 유니크 충돌(DataIntegrityViolationException)
        //    - 기존 슬롯 동시 update  → 낙관적 락 충돌(ObjectOptimisticLockingFailureException)
        //    위 두 경합 예외는 호출자(ReservationCommandService)의 재시도 루프가 처리한다.
        slot.reserve();
        slotRepository.save(slot);
        entityManager.flush(); // 커밋 전 메서드 내부에서 충돌을 유발해야 재시도 루프가 잡을 수 있음

        // 8. 예약 저장
        Reservation reservation = Reservation.of(
            memberId,
            cmd.shopId(),
            cmd.date(),
            cmd.time(),
            cmd.partySize(),
            cmd.request()
        );
        Reservation saved = reservationRepository.save(reservation);

        return ReservationResult.from(saved, shop.getName());
    }
}
