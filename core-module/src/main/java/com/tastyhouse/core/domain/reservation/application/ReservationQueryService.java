package com.tastyhouse.core.domain.reservation.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.reservation.domain.model.Reservation;
import com.tastyhouse.core.domain.reservation.domain.model.ReservationSlot;
import com.tastyhouse.core.domain.reservation.domain.model.ShopReservationSlot;
import com.tastyhouse.core.domain.reservation.domain.repository.ReservationRepository;
import com.tastyhouse.core.domain.reservation.domain.repository.ShopReservationSlotRepository;
import com.tastyhouse.core.domain.reservation.domain.vo.ReservationId;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.reservation.application.dto.result.DailySlotAvailabilityResult;
import com.tastyhouse.core.domain.reservation.application.dto.result.SlotAvailability;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReservationQueryService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ReservationRepository reservationRepository;
    private final ShopReservationSlotRepository slotRepository;
    private final ShopQueryService shopQueryService;

    public List<ReservationResult> findMyReservations(Long memberId) {
        return reservationRepository.findByMemberId(memberId).stream()
            .map(this::toResult)
            .toList();
    }

    public ReservationResult findDetail(Long memberId, ReservationId reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        reservation.validateOwnership(memberId);
        return toResult(reservation);
    }

    public List<ReservationResult> findShopReservations(Long shopId) {
        return reservationRepository.findByShopId(shopId).stream()
            .map(this::toResult)
            .toList();
    }

    /**
     * 특정 가게·날짜의 슬롯별 가용성.
     * 슬롯 행이 없으면 예약 0건이므로 전부 가용으로 간주한다.
     */
    public DailySlotAvailabilityResult findSlotAvailability(Long shopId, LocalDate date, Long memberId) {
        // 가게 존재 검증
        shopQueryService.findShopById(ShopId.of(shopId));

        Map<LocalTime, ShopReservationSlot> slotByTime = slotRepository.findByShopAndDate(shopId, date).stream()
            .collect(Collectors.toMap(ShopReservationSlot::getSlotTime, Function.identity()));

        // 회원+가게+날짜당 차단 예약은 최대 1건. 존재 여부로 그 날짜 전체 슬롯 비활성화를 판단한다.
        boolean hasMyReservation = reservationRepository.findBlockingByMemberShopDate(memberId, shopId, date).isPresent();

        LocalDateTime now = LocalDateTime.now(KST);

        List<SlotAvailability> slots = ReservationSlot.allSlots().stream()
            .map(time -> {
                ShopReservationSlot slot = slotByTime.get(time);
                int remaining = slot != null ? slot.remaining() : ReservationSlot.CAPACITY_PER_SLOT;
                boolean notPast = LocalDateTime.of(date, time).isAfter(now);
                // 이미 그 날짜에 예약이 있으면(가게+날짜 1건 제약) 모든 슬롯을 비활성화한다.
                boolean available = remaining > 0 && notPast && !hasMyReservation;
                return new SlotAvailability(time, remaining, available);
            })
            .toList();

        return new DailySlotAvailabilityResult(date, hasMyReservation, slots);
    }

    private ReservationResult toResult(Reservation reservation) {
        Shop shop = shopQueryService.findShopById(ShopId.of(reservation.getShopId()));
        String shopImageUrl = shopQueryService.findThumbnailFilePath(shop.getThumbnailImageFileId())
            .orElse(null);
        return ReservationResult.from(
            reservation,
            shop.getName(),
            shopImageUrl,
            shop.getRoadAddress(),
            shop.getLotAddress()
        );
    }
}
