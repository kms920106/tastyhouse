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
import com.tastyhouse.application.ceo.port.out.write.CeoPersistencePort;
import com.tastyhouse.application.region.port.out.write.AdminDongPersistencePort;
import com.tastyhouse.application.review.port.out.write.ReviewBlindRequestPersistencePort;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangePolicy;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionLookupPort;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImagePersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticePersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticePersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuidePersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosurePersistencePort;
import com.tastyhouse.application.shop.port.out.write.StationPersistencePort;
import com.tastyhouse.application.shop.service.CachingProhibitedWordPersistencePort;
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
    public ProhibitedWordValidator prohibitedWordValidator(ProhibitedWordPersistencePort prohibitedWordPersistencePort) {
        return new ProhibitedWordValidator(new CachingProhibitedWordPersistencePort(prohibitedWordPersistencePort));
    }

    @Bean
    public ShopNoticeExposureService shopNoticeExposureService(ShopNoticePersistencePort shopNoticePersistencePort) {
        return new ShopNoticeExposureService(shopNoticePersistencePort);
    }

    @Bean
    public ShopOrderNoticeService shopOrderNoticeService(ShopOrderNoticePersistencePort shopOrderNoticePersistencePort) {
        return new ShopOrderNoticeService(shopOrderNoticePersistencePort);
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
        ShopPersistencePort shopPersistencePort,
        ShopDetailPersistencePort shopDetailPersistencePort,
        ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort,
        ShopSuspensionPersistencePort shopSuspensionPersistencePort,
        ShopOperatingStatusCalculator shopOperatingStatusCalculator
    ) {
        return new ShopOperatingStatusService(
            shopPersistencePort,
            shopDetailPersistencePort,
            shopTemporaryClosurePersistencePort,
            shopSuspensionPersistencePort,
            shopOperatingStatusCalculator
        );
    }

    @Bean
    public ShopOrderAvailabilityService shopOrderAvailabilityService(
        ShopOperatingStatusService shopOperatingStatusService,
        ShopDetailPersistencePort shopDetailPersistencePort
    ) {
        return new ShopOrderAvailabilityService(
            shopOperatingStatusService,
            shopDetailPersistencePort
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
        ShopPersistencePort shopPersistencePort,
        ShopDetailPersistencePort shopDetailPersistencePort,
        ShopTemporaryClosurePersistencePort shopTemporaryClosurePersistencePort,
        ShopSuspensionPersistencePort shopSuspensionPersistencePort,
        ScheduledOrderSlotCalculator scheduledOrderSlotCalculator
    ) {
        return new ScheduledOrderSlotService(
            shopPersistencePort,
            shopDetailPersistencePort,
            shopTemporaryClosurePersistencePort,
            shopSuspensionPersistencePort,
            scheduledOrderSlotCalculator
        );
    }

    @Bean
    public ShopImageApprovalService shopImageApprovalService(
        ShopImageChangeRequestPersistencePort shopImageChangeRequestPersistencePort,
        ShopPersistencePort shopPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopImageApprovalService(
            shopImageChangeRequestPersistencePort,
            shopPersistencePort,
            shopChangeHistoryRecorder,
            shopRequestIndexRecorder
        );
    }

    @Bean
    public ShopMenuCollectionImageService shopMenuCollectionImageService(
        ShopMenuCollectionImagePersistencePort shopMenuCollectionImagePersistencePort,
        ShopPersistencePort shopPersistencePort
    ) {
        return new ShopMenuCollectionImageService(shopMenuCollectionImagePersistencePort, shopPersistencePort);
    }

    @Bean
    public ShopPhoneNumberRegistryService shopPhoneNumberRegistryService(
        ShopPhoneNumberPersistencePort shopPhoneNumberPersistencePort,
        ShopPersistencePort shopPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopPhoneNumberRegistryService(
            shopPhoneNumberPersistencePort,
            shopPersistencePort,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopBusinessHourService shopBusinessHourService(
        ShopDetailPersistencePort shopDetailPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopBusinessHourService(shopDetailPersistencePort, shopChangeHistoryRecorder);
    }

    @Bean
    public ShopDeliveryTipService shopDeliveryTipService(
        ShopDeliveryTipPersistencePort shopDeliveryTipPersistencePort,
        ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort,
        AdminDongPersistencePort adminDongPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryTipService(
            shopDeliveryTipPersistencePort,
            shopDeliveryAreaPersistencePort,
            adminDongPersistencePort,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryTipCalculator shopDeliveryTipCalculator() {
        return new ShopDeliveryTipCalculator();
    }

    @Bean
    public ShopDeliveryAreaService shopDeliveryAreaService(
        ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort,
        AdminDongPersistencePort adminDongPersistencePort,
        ShopDeliveryTipRegionLookupPort shopDeliveryTipRegionLookupPort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaService(
            shopDeliveryAreaPersistencePort, adminDongPersistencePort, shopDeliveryTipRegionLookupPort, shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryAreaPolygonService shopDeliveryAreaPolygonService(
        ShopDeliveryAreaPolygonPersistencePort shopDeliveryAreaPolygonPersistencePort,
        ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort,
        AdminDongPersistencePort adminDongPersistencePort,
        ShopDeliveryTipRegionLookupPort shopDeliveryTipRegionLookupPort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaPolygonService(
            shopDeliveryAreaPolygonPersistencePort,
            shopDeliveryAreaPersistencePort,
            adminDongPersistencePort,
            shopDeliveryTipRegionLookupPort,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryAreaRadiusService shopDeliveryAreaRadiusService(
        ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort,
        AdminDongPersistencePort adminDongPersistencePort,
        ShopDeliveryAreaService shopDeliveryAreaService,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopDeliveryAreaRadiusService(
            shopDeliveryAreaPersistencePort, adminDongPersistencePort, shopDeliveryAreaService, shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopDeliveryAreaAdjustmentService shopDeliveryAreaAdjustmentService(
        ShopDeliveryAreaAdjustmentRequestPersistencePort shopDeliveryAreaAdjustmentRequestPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopDeliveryAreaAdjustmentService(
            shopDeliveryAreaAdjustmentRequestPersistencePort,
            shopChangeHistoryRecorder,
            shopRequestIndexRecorder
        );
    }

    @Bean
    public ShopLifecycleService shopLifecycleService(
        ShopPersistencePort shopPersistencePort,
        ShopDetailPersistencePort shopDetailPersistencePort,
        ShopBookmarkPersistencePort shopBookmarkPersistencePort,
        StationPersistencePort stationPersistencePort,
        ShopImageApprovalService shopImageApprovalService,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        return new ShopLifecycleService(
            shopPersistencePort,
            shopDetailPersistencePort,
            shopBookmarkPersistencePort,
            stationPersistencePort,
            shopImageApprovalService,
            prohibitedWordValidator,
            shopChangeHistoryRecorder,
            shopCeoAssignmentRecorder
        );
    }

    @Bean
    public ShopOriginInfoService shopOriginInfoService(
        ShopOriginInfoPersistencePort shopOriginInfoPersistencePort,
        ShopPersistencePort shopPersistencePort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopOriginInfoService(
            shopOriginInfoPersistencePort,
            shopPersistencePort,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopConvenienceInfoService shopConvenienceInfoService(
        ShopConvenienceInfoPersistencePort shopConvenienceInfoPersistencePort,
        ShopPersistencePort shopPersistencePort,
        ShopDetailPersistencePort shopDetailPersistencePort,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopConvenienceInfoService(
            shopConvenienceInfoPersistencePort,
            shopPersistencePort,
            shopDetailPersistencePort,
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
        ShopRiderGuidePersistencePort shopRiderGuidePersistencePort,
        ShopPersistencePort shopPersistencePort,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        return new ShopRiderGuideService(
            shopRiderGuidePersistencePort,
            shopPersistencePort,
            shopRiderGuideValidator,
            shopChangeHistoryRecorder
        );
    }

    @Bean
    public ShopChangeHistoryRecorder shopChangeHistoryRecorder(
        ShopChangeHistoryPersistencePort shopChangeHistoryPersistencePort
    ) {
        return new ShopChangeHistoryRecorder(shopChangeHistoryPersistencePort);
    }

    @Bean
    public ShopCeoAssignmentRecorder shopCeoAssignmentRecorder(
        ShopCeoAssignmentHistoryPersistencePort shopCeoAssignmentHistoryPersistencePort
    ) {
        return new ShopCeoAssignmentRecorder(shopCeoAssignmentHistoryPersistencePort);
    }

    @Bean
    public ShopCeoAssignmentService shopCeoAssignmentService(
        ShopPersistencePort shopPersistencePort,
        CeoPersistencePort ceoPersistencePort,
        ShopCeoAssignmentRecorder shopCeoAssignmentRecorder
    ) {
        return new ShopCeoAssignmentService(
            shopPersistencePort,
            ceoPersistencePort,
            shopCeoAssignmentRecorder
        );
    }

    @Bean
    public ShopRequestIndexRecorder shopRequestIndexRecorder(
        ShopRequestIndexPersistencePort shopRequestIndexPersistencePort
    ) {
        return new ShopRequestIndexRecorder(shopRequestIndexPersistencePort);
    }

    @Bean
    public ShopRequestCancelService shopRequestCancelService(
        ShopImageChangeRequestPersistencePort shopImageChangeRequestPersistencePort,
        ShopDeliveryAreaAdjustmentRequestPersistencePort shopDeliveryAreaAdjustmentRequestPersistencePort,
        ReviewBlindRequestPersistencePort reviewBlindRequestPersistencePort,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopRequestCancelService(
            shopImageChangeRequestPersistencePort,
            shopDeliveryAreaAdjustmentRequestPersistencePort,
            reviewBlindRequestPersistencePort,
            shopRequestIndexRecorder
        );
    }

    @Bean
    public ShopRequestCommentService shopRequestCommentService(
        ShopRequestCommentPersistencePort shopRequestCommentPersistencePort,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        return new ShopRequestCommentService(shopRequestCommentPersistencePort, shopRequestIndexRecorder);
    }

    @Bean
    public ShopOrderContextService shopOrderContextService(
        ShopPersistencePort shopPersistencePort,
        ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort,
        ShopDeliveryTipPersistencePort shopDeliveryTipPersistencePort,
        ShopOrderAvailabilityService shopOrderAvailabilityService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        ScheduledOrderSlotService scheduledOrderSlotService
    ) {
        return new ShopOrderContextService(
            shopPersistencePort,
            shopDeliveryAreaPersistencePort,
            shopDeliveryTipPersistencePort,
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
    public StorePriceVerificationAdapter storePriceVerificationAdapter(ShopPersistencePort shopPersistencePort) {
        return new StorePriceVerificationAdapter(shopPersistencePort);
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
