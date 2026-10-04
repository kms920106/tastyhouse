package com.tastyhouse.application.shared.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.service.CupDepositPolicy;
import com.tastyhouse.domain.product.service.ProductExposureCalculator;
import com.tastyhouse.domain.product.service.StorePriceBadgePolicy;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.event.SpringDomainEventPublisher;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangePolicy;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordPersistencePort;
import com.tastyhouse.application.shop.service.CachingProhibitedWordPersistencePort;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;

@Configuration(proxyBeanMethods = false)
class SharedBeanConfig {

    @Bean
    public DomainEventPublisher domainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        return new SpringDomainEventPublisher(applicationEventPublisher);
    }

    @Bean
    public ProductExposureCalculator productExposureCalculator() {
        return new ProductExposureCalculator();
    }

    @Bean
    public CupDepositPolicy cupDepositPolicy() {
        return new CupDepositPolicy();
    }

    @Bean
    public StorePriceBadgePolicy storePriceBadgePolicy() {
        return new StorePriceBadgePolicy();
    }

    @Bean
    public ShopOperatingStatusCalculator shopOperatingStatusCalculator() {
        return new ShopOperatingStatusCalculator();
    }

    @Bean
    public ShopNextOpenTimeCalculator shopNextOpenTimeCalculator(
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ShopNextOpenTimeCalculator(shopOperatingStatusCalculator);
    }

    @Bean
    public ScheduledOrderSlotCalculator scheduledOrderSlotCalculator(
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ScheduledOrderSlotCalculator(shopOperatingStatusCalculator);
    }

    @Bean
    public ShopDeliveryTipCalculator shopDeliveryTipCalculator() {
        return new ShopDeliveryTipCalculator();
    }

    @Bean
    public ShopDeliveryTipRangePolicy shopDeliveryTipRangePolicy() {
        return new ShopDeliveryTipRangePolicy(
            DeliveryTipPolicy.EXTRA_TIP_UPPER_BOUND,
            Arrays.stream(DeliveryTipDistanceUnit.values())
                .collect(Collectors.toMap(DeliveryTipDistanceUnit::name, DeliveryTipDistanceUnit::getUnitMeters)),
            code -> new BusinessException(ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN,
                ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN.getDefaultMessage() + ": " + code),
            DeliveryTipExtraType.DISTANCE.name(),
            DeliveryTipExtraType.REGION.name()
        );
    }

    @Bean
    public ProhibitedWordValidator prohibitedWordValidator(ProhibitedWordPersistencePort prohibitedWordPersistencePort) {
        return new ProhibitedWordValidator(new CachingProhibitedWordPersistencePort(prohibitedWordPersistencePort));
    }
}
