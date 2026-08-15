package com.tastyhouse.infrastructure.holiday.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.holiday.repository.PublicHolidayRepository;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;

/**
 * holiday 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class HolidayDomainConfig {

    /**
     * 법정 공휴일 판정 — 배달팁 공휴일 부과 여부를 캘린더 테이블로 답한다.
     * 영업상태 판정({@code ShopOperatingStatusService})에는 아직 연결하지 않는다(파급 격리).
     */
    @Bean
    public PublicHolidayCalendar publicHolidayCalendar(PublicHolidayRepository publicHolidayRepository) {
        return new PublicHolidayCalendar(publicHolidayRepository);
    }
}
