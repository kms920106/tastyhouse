package com.tastyhouse.application.shop.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;
import com.tastyhouse.application.ceo.port.out.write.CeoRepository;
import com.tastyhouse.application.region.port.out.write.AdminDongRepository;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestRepository;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangePolicy;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordRepository;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkRepository;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryRepository;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryRepository;
import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoRepository;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaRepository;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionLookup;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRepository;
import com.tastyhouse.application.shop.port.out.write.ShopDetailRepository;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestRepository;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageRepository;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeRepository;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeRepository;
import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoRepository;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberRepository;
import com.tastyhouse.application.shop.port.out.write.ShopRepository;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentRepository;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexRepository;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideRepository;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionRepository;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureRepository;
import com.tastyhouse.application.shop.port.out.write.StationRepository;
import com.tastyhouse.application.shop.service.CachingProhibitedWordRepository;
import com.tastyhouse.application.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.service.ReplyPhraseProhibitedWordValidatorAdapter;
import com.tastyhouse.application.shop.service.ScheduledOrderSlotService;
import com.tastyhouse.application.shop.service.ShopBusinessHourService;
import com.tastyhouse.application.shop.service.ShopCeoAssignmentRecorder;
import com.tastyhouse.application.shop.service.ShopCeoAssignmentService;
import com.tastyhouse.application.shop.service.ShopChangeHistoryRecorder;
import com.tastyhouse.application.shop.service.ShopConvenienceInfoService;
import com.tastyhouse.application.shop.service.ShopDeliveryAreaAdjustmentService;
import com.tastyhouse.application.shop.service.ShopDeliveryAreaPolygonService;
import com.tastyhouse.application.shop.service.ShopDeliveryAreaRadiusService;
import com.tastyhouse.application.shop.service.ShopDeliveryAreaService;
import com.tastyhouse.application.shop.service.ShopDeliveryTipService;
import com.tastyhouse.application.shop.service.ShopImageApprovalService;
import com.tastyhouse.application.shop.service.ShopLifecycleService;
import com.tastyhouse.application.shop.service.ShopMenuCollectionImageService;
import com.tastyhouse.application.shop.service.ShopNoticeExposureService;
import com.tastyhouse.application.shop.service.ShopOperatingStatusService;
import com.tastyhouse.application.shop.service.ShopOrderAvailabilityService;
import com.tastyhouse.application.shop.service.ShopOrderContextService;
import com.tastyhouse.application.shop.service.ShopOrderNoticeService;
import com.tastyhouse.application.shop.service.ShopOriginInfoService;
import com.tastyhouse.application.shop.service.ShopPhoneNumberRegistryService;
import com.tastyhouse.application.shop.service.ShopRequestCancelService;
import com.tastyhouse.application.shop.service.ShopRequestCommentService;
import com.tastyhouse.application.shop.service.ShopRequestIndexRecorder;
import com.tastyhouse.application.shop.service.ShopRequestIndexSyncAdapter;
import com.tastyhouse.application.shop.service.ShopRiderGuideService;
import com.tastyhouse.application.shop.service.ShopRiderGuideValidator;
import com.tastyhouse.application.shop.service.StorePriceVerificationAdapter;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class ShopServiceConfig {
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

    @Bean
    public ShopRequestIndexSyncAdapter shopRequestIndexSyncAdapter(ShopRequestIndexRecorder shopRequestIndexRecorder) {
        return new ShopRequestIndexSyncAdapter(shopRequestIndexRecorder);
    }

    @Bean
    public ReplyPhraseProhibitedWordValidatorAdapter replyPhraseProhibitedWordValidatorAdapter(
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        return new ReplyPhraseProhibitedWordValidatorAdapter(prohibitedWordValidator);
    }

    @Bean
    public StorePriceVerificationAdapter storePriceVerificationAdapter(ShopRepository shopRepository) {
        return new StorePriceVerificationAdapter(shopRepository);
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
}
