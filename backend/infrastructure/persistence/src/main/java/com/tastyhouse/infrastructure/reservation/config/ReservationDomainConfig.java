package com.tastyhouse.infrastructure.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.reservation.repository.ReservationRepository;
import com.tastyhouse.domain.reservation.repository.ReservationSlotRepository;
import com.tastyhouse.domain.reservation.service.ReservationBookingService;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;

@Configuration(proxyBeanMethods = false)
public class ReservationDomainConfig {
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
