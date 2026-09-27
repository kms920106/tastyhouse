package com.tastyhouse.application.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.member.store.MemberRepository;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotStatePort;
import com.tastyhouse.application.reservation.port.out.write.ReservationStatePort;
import com.tastyhouse.application.reservation.service.ReservationBookingService;
import com.tastyhouse.application.reservation.store.ReservationRepository;
import com.tastyhouse.application.reservation.store.ReservationSlotRepository;
import com.tastyhouse.application.reservation.store.ReservationSlotStore;
import com.tastyhouse.application.reservation.store.ReservationStore;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.service.ShopOrderAvailabilityService;
import com.tastyhouse.application.shop.store.ShopRepository;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ReservationServiceConfig {
    @Bean
    public ReservationRepository reservationRepository(ReservationStatePort reservationStatePort) {
        return new ReservationStore(reservationStatePort);
    }

    @Bean
    public ReservationSlotRepository reservationSlotRepository(ReservationSlotStatePort reservationSlotStatePort) {
        return new ReservationSlotStore(reservationSlotStatePort);
    }

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
