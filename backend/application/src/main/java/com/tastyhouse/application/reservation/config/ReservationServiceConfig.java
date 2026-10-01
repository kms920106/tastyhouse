package com.tastyhouse.application.reservation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.reservation.port.out.write.ReservationPersistencePort;
import com.tastyhouse.application.reservation.port.out.write.ReservationSlotPersistencePort;
import com.tastyhouse.application.reservation.service.ReservationBookingService;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;
import com.tastyhouse.application.shop.service.ShopOrderAvailabilityService;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ReservationServiceConfig {

    @Bean
    public ReservationBookingService reservationBookingService(
        ReservationPersistencePort reservationPersistencePort,
        ReservationSlotPersistencePort reservationSlotPersistencePort,
        ShopPersistencePort shopPersistencePort,
        MemberPersistencePort memberPersistencePort,
        ShopOrderAvailabilityService shopOrderAvailabilityService
    ) {
        return new ReservationBookingService(
            reservationPersistencePort,
            reservationSlotPersistencePort,
            shopPersistencePort,
            memberPersistencePort,
            shopOrderAvailabilityService
        );
    }
}
