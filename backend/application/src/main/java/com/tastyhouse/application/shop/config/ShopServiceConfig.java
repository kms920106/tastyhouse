package com.tastyhouse.application.shop.config;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.ceo.store.CeoRepository;
import com.tastyhouse.application.region.store.AdminDongRepository;
import com.tastyhouse.application.review.store.ReviewBlindRequestRepository;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangePolicy;
import com.tastyhouse.application.shop.port.out.write.ProhibitedWordStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopBookmarkStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopCeoAssignmentHistoryStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopChangeHistoryStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopChoiceStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopContentBoardStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopConvenienceInfoStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaAdjustmentRequestStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPolygonStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopDetailStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopHygieneBadgeStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopImageChangeRequestStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeImageStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopNoticeStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopOriginInfoStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopPhoneNumberStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopRequestCommentStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopRequestIndexStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopRiderGuideStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopSuspensionStatePort;
import com.tastyhouse.application.shop.port.out.write.ShopTemporaryClosureStatePort;
import com.tastyhouse.application.shop.port.out.write.StationRepository;
import com.tastyhouse.application.shop.port.out.write.TagStatePort;
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
import com.tastyhouse.application.shop.store.ProhibitedWordRepository;
import com.tastyhouse.application.shop.store.ProhibitedWordStore;
import com.tastyhouse.application.shop.store.ShopBookmarkRepository;
import com.tastyhouse.application.shop.store.ShopBookmarkStore;
import com.tastyhouse.application.shop.store.ShopCeoAssignmentHistoryRepository;
import com.tastyhouse.application.shop.store.ShopCeoAssignmentHistoryStore;
import com.tastyhouse.application.shop.store.ShopChangeHistoryRepository;
import com.tastyhouse.application.shop.store.ShopChangeHistoryStore;
import com.tastyhouse.application.shop.store.ShopChoiceRepository;
import com.tastyhouse.application.shop.store.ShopChoiceStore;
import com.tastyhouse.application.shop.store.ShopContentBoardRepository;
import com.tastyhouse.application.shop.store.ShopContentBoardStore;
import com.tastyhouse.application.shop.store.ShopConvenienceInfoRepository;
import com.tastyhouse.application.shop.store.ShopConvenienceInfoStore;
import com.tastyhouse.application.shop.store.ShopDeliveryAreaAdjustmentRequestRepository;
import com.tastyhouse.application.shop.store.ShopDeliveryAreaAdjustmentRequestStore;
import com.tastyhouse.application.shop.store.ShopDeliveryAreaPolygonRepository;
import com.tastyhouse.application.shop.store.ShopDeliveryAreaPolygonStore;
import com.tastyhouse.application.shop.store.ShopDeliveryAreaRepository;
import com.tastyhouse.application.shop.store.ShopDeliveryAreaStore;
import com.tastyhouse.application.shop.store.ShopDeliveryTipRegionLookup;
import com.tastyhouse.application.shop.store.ShopDeliveryTipRepository;
import com.tastyhouse.application.shop.store.ShopDeliveryTipStore;
import com.tastyhouse.application.shop.store.ShopDetailRepository;
import com.tastyhouse.application.shop.store.ShopDetailStore;
import com.tastyhouse.application.shop.store.ShopHygieneBadgeRepository;
import com.tastyhouse.application.shop.store.ShopHygieneBadgeStore;
import com.tastyhouse.application.shop.store.ShopImageChangeRequestRepository;
import com.tastyhouse.application.shop.store.ShopImageChangeRequestStore;
import com.tastyhouse.application.shop.store.ShopMenuCollectionImageRepository;
import com.tastyhouse.application.shop.store.ShopMenuCollectionImageStore;
import com.tastyhouse.application.shop.store.ShopNoticeImageRepository;
import com.tastyhouse.application.shop.store.ShopNoticeImageStore;
import com.tastyhouse.application.shop.store.ShopNoticeRepository;
import com.tastyhouse.application.shop.store.ShopNoticeStore;
import com.tastyhouse.application.shop.store.ShopOrderNoticeRepository;
import com.tastyhouse.application.shop.store.ShopOrderNoticeStore;
import com.tastyhouse.application.shop.store.ShopOriginInfoRepository;
import com.tastyhouse.application.shop.store.ShopOriginInfoStore;
import com.tastyhouse.application.shop.store.ShopPhoneNumberRepository;
import com.tastyhouse.application.shop.store.ShopPhoneNumberStore;
import com.tastyhouse.application.shop.store.ShopRepository;
import com.tastyhouse.application.shop.store.ShopRequestCommentRepository;
import com.tastyhouse.application.shop.store.ShopRequestCommentStore;
import com.tastyhouse.application.shop.store.ShopRequestIndexRepository;
import com.tastyhouse.application.shop.store.ShopRequestIndexStore;
import com.tastyhouse.application.shop.store.ShopRiderGuideRepository;
import com.tastyhouse.application.shop.store.ShopRiderGuideStore;
import com.tastyhouse.application.shop.store.ShopStore;
import com.tastyhouse.application.shop.store.ShopSuspensionRepository;
import com.tastyhouse.application.shop.store.ShopSuspensionStore;
import com.tastyhouse.application.shop.store.ShopTemporaryClosureRepository;
import com.tastyhouse.application.shop.store.ShopTemporaryClosureStore;
import com.tastyhouse.application.shop.store.TagRepository;
import com.tastyhouse.application.shop.store.TagStore;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.DeliveryTipDistanceUnit;
import com.tastyhouse.domain.shop.model.DeliveryTipPolicy;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopNextOpenTimeCalculator;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusCalculator;

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
    public ProhibitedWordRepository prohibitedWordRepository(ProhibitedWordStatePort prohibitedWordStatePort) {
        return new ProhibitedWordStore(prohibitedWordStatePort);
    }

    @Bean
    public ShopBookmarkRepository shopBookmarkRepository(ShopBookmarkStatePort shopBookmarkStatePort) {
        return new ShopBookmarkStore(shopBookmarkStatePort);
    }

    @Bean
    public ShopCeoAssignmentHistoryRepository shopCeoAssignmentHistoryRepository(ShopCeoAssignmentHistoryStatePort shopCeoAssignmentHistoryStatePort) {
        return new ShopCeoAssignmentHistoryStore(shopCeoAssignmentHistoryStatePort);
    }

    @Bean
    public ShopChangeHistoryRepository shopChangeHistoryRepository(ShopChangeHistoryStatePort shopChangeHistoryStatePort) {
        return new ShopChangeHistoryStore(shopChangeHistoryStatePort);
    }

    @Bean
    public ShopChoiceRepository shopChoiceRepository(ShopChoiceStatePort shopChoiceStatePort) {
        return new ShopChoiceStore(shopChoiceStatePort);
    }

    @Bean
    public ShopContentBoardRepository shopContentBoardRepository(ShopContentBoardStatePort shopContentBoardStatePort) {
        return new ShopContentBoardStore(shopContentBoardStatePort);
    }

    @Bean
    public ShopConvenienceInfoRepository shopConvenienceInfoRepository(ShopConvenienceInfoStatePort shopConvenienceInfoStatePort) {
        return new ShopConvenienceInfoStore(shopConvenienceInfoStatePort);
    }

    @Bean
    public ShopHygieneBadgeRepository shopHygieneBadgeRepository(ShopHygieneBadgeStatePort shopHygieneBadgeStatePort) {
        return new ShopHygieneBadgeStore(shopHygieneBadgeStatePort);
    }

    @Bean
    public ShopOriginInfoRepository shopOriginInfoRepository(ShopOriginInfoStatePort shopOriginInfoStatePort) {
        return new ShopOriginInfoStore(shopOriginInfoStatePort);
    }

    @Bean
    public ShopPhoneNumberRepository shopPhoneNumberRepository(ShopPhoneNumberStatePort shopPhoneNumberStatePort) {
        return new ShopPhoneNumberStore(shopPhoneNumberStatePort);
    }

    @Bean
    public TagRepository tagRepository(TagStatePort tagStatePort) {
        return new TagStore(tagStatePort);
    }

    @Bean
    public ShopRequestCommentRepository shopRequestCommentRepository(ShopRequestCommentStatePort shopRequestCommentStatePort) {
        return new ShopRequestCommentStore(shopRequestCommentStatePort);
    }

    @Bean
    public ShopSuspensionRepository shopSuspensionRepository(ShopSuspensionStatePort shopSuspensionStatePort) {
        return new ShopSuspensionStore(shopSuspensionStatePort);
    }

    @Bean
    public ShopTemporaryClosureRepository shopTemporaryClosureRepository(ShopTemporaryClosureStatePort shopTemporaryClosureStatePort) {
        return new ShopTemporaryClosureStore(shopTemporaryClosureStatePort);
    }

    @Bean
    public ShopNoticeRepository shopNoticeRepository(ShopNoticeStatePort shopNoticeStatePort) {
        return new ShopNoticeStore(shopNoticeStatePort);
    }

    @Bean
    public ShopNoticeImageRepository shopNoticeImageRepository(ShopNoticeImageStatePort shopNoticeImageStatePort) {
        return new ShopNoticeImageStore(shopNoticeImageStatePort);
    }

    @Bean
    public ShopOrderNoticeRepository shopOrderNoticeRepository(ShopOrderNoticeStatePort shopOrderNoticeStatePort) {
        return new ShopOrderNoticeStore(shopOrderNoticeStatePort);
    }

    @Bean
    public ShopRequestIndexRepository shopRequestIndexRepository(ShopRequestIndexStatePort shopRequestIndexStatePort) {
        return new ShopRequestIndexStore(shopRequestIndexStatePort);
    }

    @Bean
    public ShopImageChangeRequestRepository shopImageChangeRequestRepository(ShopImageChangeRequestStatePort shopImageChangeRequestStatePort) {
        return new ShopImageChangeRequestStore(shopImageChangeRequestStatePort);
    }

    @Bean
    public ShopMenuCollectionImageRepository shopMenuCollectionImageRepository(ShopMenuCollectionImageStatePort shopMenuCollectionImageStatePort) {
        return new ShopMenuCollectionImageStore(shopMenuCollectionImageStatePort);
    }

    @Bean
    public ShopRiderGuideRepository shopRiderGuideRepository(ShopRiderGuideStatePort shopRiderGuideStatePort) {
        return new ShopRiderGuideStore(shopRiderGuideStatePort);
    }

    @Bean
    public ShopDetailRepository shopDetailRepository(ShopDetailStatePort shopDetailStatePort) {
        return new ShopDetailStore(shopDetailStatePort);
    }

    @Bean
    public ShopRepository shopRepository(ShopStatePort shopStatePort) {
        return new ShopStore(shopStatePort);
    }

    @Bean
    public ShopDeliveryAreaRepository shopDeliveryAreaRepository(ShopDeliveryAreaStatePort shopDeliveryAreaStatePort) {
        return new ShopDeliveryAreaStore(shopDeliveryAreaStatePort);
    }

    @Bean
    public ShopDeliveryAreaPolygonRepository shopDeliveryAreaPolygonRepository(ShopDeliveryAreaPolygonStatePort shopDeliveryAreaPolygonStatePort) {
        return new ShopDeliveryAreaPolygonStore(shopDeliveryAreaPolygonStatePort);
    }

    @Bean
    public ShopDeliveryAreaAdjustmentRequestRepository shopDeliveryAreaAdjustmentRequestRepository(ShopDeliveryAreaAdjustmentRequestStatePort shopDeliveryAreaAdjustmentRequestStatePort) {
        return new ShopDeliveryAreaAdjustmentRequestStore(shopDeliveryAreaAdjustmentRequestStatePort);
    }

    @Bean
    public ShopDeliveryTipStore shopDeliveryTipStore(ShopDeliveryTipStatePort shopDeliveryTipStatePort) {
        return new ShopDeliveryTipStore(shopDeliveryTipStatePort);
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
                ErrorCode.DELIVERY_TIP_DISTANCE_UNIT_UNKNOWN.getDefaultMessage() + ": " + code)
        );
    }
}
