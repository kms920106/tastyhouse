package com.tastyhouse.infrastructure.holiday.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.holiday.repository.PublicHolidayRepository;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;

@Configuration(proxyBeanMethods = false)
public class HolidayDomainConfig {
    @Bean
    public PublicHolidayCalendar publicHolidayCalendar(PublicHolidayRepository publicHolidayRepository) {
        return new PublicHolidayCalendar(publicHolidayRepository);
    }
}
