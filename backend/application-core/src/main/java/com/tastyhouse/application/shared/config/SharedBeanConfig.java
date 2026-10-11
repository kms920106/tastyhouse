package com.tastyhouse.application.shared.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.DomainException;
import com.tastyhouse.domain.product.model.CupDepositPolicy;
import com.tastyhouse.domain.product.model.ProductExposureCalculator;
import com.tastyhouse.domain.product.model.StorePriceBadgePolicy;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.model.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.model.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.model.ShopOperatingStatusCalculator;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.event.SpringDomainEventPublisher;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangePolicy;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordLoadPort;
import com.tastyhouse.application.shop.service.CachingProhibitedWordLoadPort;
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
            code -> new DomainException(DomainErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN,
                DomainErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN.getDefaultMessage() + ": " + code),
            DeliveryTipExtraType.DISTANCE.name(),
            DeliveryTipExtraType.REGION.name()
        );
    }

    @Bean
    public ProhibitedWordValidator prohibitedWordValidator(ProhibitedWordLoadPort prohibitedWordLoadPort) {
        return new ProhibitedWordValidator(new CachingProhibitedWordLoadPort(prohibitedWordLoadPort));
    }
}
