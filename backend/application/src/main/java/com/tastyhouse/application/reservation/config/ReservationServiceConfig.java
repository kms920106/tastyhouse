package com.tastyhouse.application.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.member.port.out.write.MemberRepository;
import com.tastyhouse.application.reservation.port.out.write.ReservationRepository;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotRepository;
import com.tastyhouse.application.reservation.service.ReservationBookingService;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.write.ShopRepository;
import com.tastyhouse.application.shop.service.ShopOrderAvailabilityService;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ReservationServiceConfig {
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
