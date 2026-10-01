package com.tastyhouse.application.review.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestAttachmentPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewImagePersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewLikePersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewOwnerReplyPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewTagPersistencePort;
import com.tastyhouse.application.review.service.ReviewBlindRequestService;
import com.tastyhouse.application.review.service.ReviewLifecycleService;
import com.tastyhouse.application.review.service.ReviewOwnerReplyService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.write.TagPersistencePort;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ShopRequestIndexRecorder;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ReviewServiceConfig {

    @Bean
    public ReviewLifecycleService reviewLifecycleService(
        ReviewPersistencePort reviewPersistencePort,
        ReviewImagePersistencePort reviewImagePersistencePort,
        ReviewTagPersistencePort reviewTagPersistencePort,
        ReviewLikePersistencePort reviewLikePersistencePort,
        TagPersistencePort tagPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReviewLifecycleService(
            reviewPersistencePort,
            reviewImagePersistencePort,
            reviewTagPersistencePort,
            reviewLikePersistencePort,
            tagPersistencePort,
            domainEventPublisher
        );
    }

    @Bean
    public ReviewOwnerReplyService reviewOwnerReplyService(
        ReviewOwnerReplyPersistencePort reviewOwnerReplyPersistencePort,
        ReviewPersistencePort reviewPersistencePort,
        ProhibitedWordValidator prohibitedWordValidator,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReviewOwnerReplyService(
            reviewOwnerReplyPersistencePort,
            reviewPersistencePort,
            prohibitedWordValidator,
            domainEventPublisher
        );
    }

    @Bean
    public ReviewBlindRequestService reviewBlindRequestService(
        ReviewBlindRequestPersistencePort reviewBlindRequestPersistencePort,
        ReviewBlindRequestAttachmentPersistencePort reviewBlindRequestAttachmentPersistencePort,
        ReviewPersistencePort reviewPersistencePort,
        ReviewLifecycleService reviewLifecycleService,
        ShopRequestIndexRecorder shopRequestIndexRecorder,
        DomainEventPublisher domainEventPublisher
    ) {
        return new ReviewBlindRequestService(
            reviewBlindRequestPersistencePort,
            reviewBlindRequestAttachmentPersistencePort,
            reviewPersistencePort,
            reviewLifecycleService,
            shopRequestIndexRecorder,
            domainEventPublisher
        );
    }
}
