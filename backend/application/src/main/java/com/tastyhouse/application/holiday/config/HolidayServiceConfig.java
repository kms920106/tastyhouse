package com.tastyhouse.application.holiday.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.holiday.port.out.write.PublicHolidayPersistencePort;
import com.tastyhouse.application.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class HolidayServiceConfig {

    @Bean
    public PublicHolidayCalendar publicHolidayCalendar(PublicHolidayPersistencePort publicHolidayPersistencePort) {
        return new PublicHolidayCalendar(publicHolidayPersistencePort);
    }
}
