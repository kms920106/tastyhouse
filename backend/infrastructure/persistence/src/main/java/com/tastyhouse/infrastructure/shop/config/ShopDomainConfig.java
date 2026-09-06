package com.tastyhouse.infrastructure.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.ceo.repository.CeoRepository;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.review.repository.ReviewBlindRequestRepository;
import com.tastyhouse.domain.shop.repository.ProhibitedWordRepository;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopCeoAssignmentHistoryRepository;
import com.tastyhouse.domain.shop.repository.ShopChangeHistoryRepository;
import com.tastyhouse.domain.shop.repository.ShopConvenienceInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopDetailRepository;
import com.tastyhouse.domain.shop.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.domain.shop.repository.ShopMenuCollectionImageRepository;
import com.tastyhouse.domain.shop.repository.ShopNoticeRepository;
import com.tastyhouse.domain.shop.repository.ShopOrderNoticeRepository;
import com.tastyhouse.domain.shop.repository.ShopOriginInfoRepository;
import com.tastyhouse.domain.shop.repository.ShopPhoneNumberRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.repository.ShopRequestCommentRepository;
import com.tastyhouse.domain.shop.repository.ShopRequestIndexRepository;
import com.tastyhouse.domain.shop.repository.ShopRiderGuideRepository;
import com.tastyhouse.domain.shop.repository.ShopSuspensionRepository;
import com.tastyhouse.domain.shop.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.domain.shop.repository.StationRepository;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotService;
import com.tastyhouse.domain.shop.service.ShopBusinessHourService;
import com.tastyhouse.domain.shop.service.ShopCeoAssignmentRecorder;
import com.tastyhouse.domain.shop.service.ShopCeoAssignmentService;
import com.tastyhouse.domain.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.domain.shop.service.ShopConvenienceInfoService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaAdjustmentService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolygonService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaRadiusService;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipService;
import com.tastyhouse.domain.shop.service.ShopImageApprovalService;
import com.tastyhouse.domain.shop.service.ShopLifecycleService;
import com.tastyhouse.domain.shop.service.ShopMenuCollectionImageService;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.service.ShopNoticeExposureService;
import com.tastyhouse.domain.shop.service.ShopOriginInfoService;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.domain.shop.service.ShopOrderAvailabilityService;
import com.tastyhouse.domain.shop.service.ShopOrderContextService;
import com.tastyhouse.domain.shop.service.ShopOrderNoticeService;
import com.tastyhouse.domain.shop.service.ShopPhoneNumberRegistryService;
import com.tastyhouse.domain.shop.service.ShopRequestCancelService;
import com.tastyhouse.domain.shop.service.ShopRequestCommentService;
import com.tastyhouse.domain.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.domain.shop.service.ShopRiderGuideService;
import com.tastyhouse.domain.shop.service.ShopRiderGuideValidator;
import com.tastyhouse.infrastructure.shop.persistence.CachingProhibitedWordRepository;

@Configuration(proxyBeanMethods = false)
public class ShopDomainConfig {
    @Bean
    public ProhibitedWordValidator prohibitedWordValidator(ProhibitedWordRepository prohibitedWordRepository) {
        return new ProhibitedWordValidator(new CachingProhibitedWordRepository(prohibitedWordRepository));
    }

    @Bean
    public ShopNoticeExposureService shopNoticeExposureService(ShopNoticeRepository shopNoticeRepository) {
        return new ShopNoticeExposureService(shopNoticeRepository);
    }

    @Bean
    public ShopOrderNoticeService shopOrderNoticeService(ShopOrderNoticeRepository shopOrderNoticeRepository) {
        return new ShopOrderNoticeService(shopOrderNoticeRepository);
    }

    @Bean
    public ShopNextOpenTimeCalculator shopNextOpenTimeCalculator(
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ShopNextOpenTimeCalculator(shopOperatingStatusCalculator);
    }

    @Bean
    public ShopOperatingStatusCalculator shopOperatingStatusCalculator() {
        return new ShopOperatingStatusCalculator();
    }

    @Bean
    public ShopOperatingStatusService shopOperatingStatusService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ShopOperatingStatusService(
            shopRepository,
            shopDetailRepository,
            shopTemporaryClosureRepository,
            shopSuspensionRepository,
            shopOperatingStatusCalculator
        );
    }

    @Bean
    public ShopOrderAvailabilityService shopOrderAvailabilityService(
        ShopOperatingStatusService shopOperatingStatusService,
        ShopDetailRepository shopDetailRepository
    ) {
        return new ShopOrderAvailabilityService(
            shopOperatingStatusService,
            shopDetailRepository
        );
    }

    @Bean
    public ScheduledOrderSlotCalculator scheduledOrderSlotCalculator(
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ScheduledOrderSlotCalculator(shopOperatingStatusCalculator);
    }

    @Bean
    public ScheduledOrderSlotService scheduledOrderSlotService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopTemporaryClosureRepository shopTemporaryClosureRepository,
        ShopSuspensionRepository shopSuspensionRepository,
        ScheduledOrderSlotCalculator scheduledOrderSlotCalculator
    ) {
        return new ScheduledOrderSlotService(
            shopRepository,
            shopDetailRepository,
            shopTemporaryClosureRepository,
            shopSuspensionRepository,
            scheduledOrderSlotCalculator
        );
    }

    @Bean
    public ShopImageApprovalService shopImageApprovalService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopImageApprovalService(
            shopImageChangeRequestRepository,
            shopRepository,
            shopChangeHistoryRecorder,
            shopRequestIndexRecorder
        );
    }

    @Bean
    public ShopMenuCollectionImageService shopMenuCollectionImageService(
        ShopMenuCollectionImageRepository shopMenuCollectionImageRepository,
        ShopRepository shopRepository
    ) {
        return new ShopMenuCollectionImageService(shopMenuCollectionImageRepository, shopRepository);
    }

    @Bean
    public ShopPhoneNumberRegistryService shopPhoneNumberRegistryService(
        ShopPhoneNumberRepository shopPhoneNumberRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopPhoneNumberRegistryService(
            shopPhoneNumberRepository,
            shopRepository,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopBusinessHourService shopBusinessHourService(
        ShopDetailRepository shopDetailRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopBusinessHourService(shopDetailRepository, shopChangeHistoryRecorder);
    }

    @Bean
    public ShopDeliveryTipService shopDeliveryTipService(
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryTipService(
            shopDeliveryTipRepository,
            shopDeliveryAreaRepository,
            adminDongRepository,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryTipCalculator shopDeliveryTipCalculator() {
        return new ShopDeliveryTipCalculator();
    }

    @Bean
    public ShopDeliveryAreaService shopDeliveryAreaService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaService(
            shopDeliveryAreaRepository, adminDongRepository, shopDeliveryTipRegionLookup, shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService(
        ShopDeliveryAreaPolygonRepository shopDeliveryAreaPolygonRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaPolygonService(
            shopDeliveryAreaPolygonRepository,
            shopDeliveryAreaRepository,
            adminDongRepository,
            shopDeliveryTipRegionLookup,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaRadiusService(
            shopDeliveryAreaRepository, adminDongRepository, shopDeliveryAreaService, shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService(
        ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopDeliveryAreaAdjustmentService(
            shopDeliveryAreaAdjustmentRequestRepository,
            shopChangeHistoryRecorder,
            shopRequestIndexRecorder
        );
    }

    @Bean
    public ShopLifecycleService shopLifecycleService(
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ShopBookmarkRepository shopBookmarkRepository,
        StationRepository stationRepository,
        ShopImageApprovalService shopImageApprovalService,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        return new ShopLifecycleService(
            shopRepository,
            shopDetailRepository,
            shopBookmarkRepository,
            stationRepository,
            shopImageApprovalService,
            prohibitedWordValidator,
            shopChangeHistoryRecorder,
            shopCeoAssignmentRecorder
        );
    }

    @Bean
    public ShopOriginInfoService shopOriginInfoService(
        ShopOriginInfoRepository shopOriginInfoRepository,
        ShopRepository shopRepository,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopOriginInfoService(
            shopOriginInfoRepository,
            shopRepository,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopConvenienceInfoService shopConvenienceInfoService(
        ShopConvenienceInfoRepository shopConvenienceInfoRepository,
        ShopRepository shopRepository,
        ShopDetailRepository shopDetailRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopConvenienceInfoService(
            shopConvenienceInfoRepository,
            shopRepository,
            shopDetailRepository,
            prohibitedWordValidator,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopRiderGuideValidator shopRiderGuideValidator(ProhibitedWordValidator prohibitedWordValidator) {
        return new ShopRiderGuideValidator(prohibitedWordValidator);
    }

    @Bean
    public ShopRiderGuideService shopRiderGuideService(
        ShopRiderGuideRepository shopRiderGuideRepository,
        ShopRepository shopRepository,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopRiderGuideService(
            shopRiderGuideRepository,
            shopRepository,
            shopRiderGuideValidator,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopChangeHistoryRecorder shopChangeHistoryRecorder(
        ShopChangeHistoryRepository shopChangeHistoryRepository
    ) {
        return new ShopChangeHistoryRecorder(shopChangeHistoryRepository);
    }

    @Bean
    public ShopCeoAssignmentRecorder shopCeoAssignmentRecorder(
        ShopCeoAssignmentHistoryRepository shopCeoAssignmentHistoryRepository
    ) {
        return new ShopCeoAssignmentRecorder(shopCeoAssignmentHistoryRepository);
    }

    @Bean
    public ShopCeoAssignmentService shopCeoAssignmentService(
        ShopRepository shopRepository,
        CeoRepository ceoRepository,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        return new ShopCeoAssignmentService(
            shopRepository,
            ceoRepository,
            shopCeoAssignmentRecorder
        );
    }

    @Bean
    public ShopRequestIndexRecorder shopRequestIndexRecorder(
        ShopRequestIndexRepository shopRequestIndexRepository
    ) {
        return new ShopRequestIndexRecorder(shopRequestIndexRepository);
    }

    @Bean
    public ShopRequestCancelService shopRequestCancelService(
        ShopImageChangeRequestRepository shopImageChangeRequestRepository,
        ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository,
        ReviewBlindRequestRepository reviewBlindRequestRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopRequestCancelService(
            shopImageChangeRequestRepository,
            shopDeliveryAreaAdjustmentRequestRepository,
            reviewBlindRequestRepository,
            shopRequestIndexRecorder
        );
    }

    @Bean
    public ShopRequestCommentService shopRequestCommentService(
        ShopRequestCommentRepository shopRequestCommentRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopRequestCommentService(shopRequestCommentRepository, shopRequestIndexRecorder);
    }

    @Bean
    public ShopOrderContextService shopOrderContextService(
        ShopRepository shopRepository,
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopOrderAvailabilityService shopOrderAvailabilityService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        ScheduledOrderSlotService scheduledOrderSlotService
    ) {
        return new ShopOrderContextService(
            shopRepository,
            shopDeliveryAreaRepository,
            shopDeliveryTipRepository,
            shopOrderAvailabilityService,
            shopDeliveryTipCalculator,
            scheduledOrderSlotService
        );
    }
}
