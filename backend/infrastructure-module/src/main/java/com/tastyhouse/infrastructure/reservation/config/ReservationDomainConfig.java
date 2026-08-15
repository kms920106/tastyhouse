package com.tastyhouse.infrastructure.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.reservation.repository.ReservationRepository;
import com.tastyhouse.domain.reservation.repository.ReservationSlotRepository;
import com.tastyhouse.domain.reservation.service.ReservationBookingService;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;

/**
 * reservation 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class ReservationDomainConfig {

    /**
     * 예약 예약/취소 — 예약 애그리거트의 생성·상태전이와 슬롯 정원 차감·반납을 한 트랜잭션에서 함께
     * 처리하는 오케스트레이션. 슬롯 정원 차감은 낙관적 락으로 보호되며, 충돌 시 재시도는 트랜잭션 경계
     * 바깥(web-api {@code ReservationCommandService})이 담당한다.
     */
    @Bean
    public ReservationBookingService reservationBookingService(
        ReservationRepository reservationRepository,
        ReservationSlotRepository reservationSlotRepository,
        ShopRepository shopRepository,
        MemberRepository memberRepository,
        ShopOrderAvailabilityService shopOrderAvailabilityService
    ) {
        return new ReservationBookingService(
            reservationRepository,
            reservationSlotRepository,
            shopRepository,
            memberRepository,
            shopOrderAvailabilityService
        );
    }
}
