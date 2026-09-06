package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.shared.model.DayType;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.OrderUnavailableReason;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipBreakdown;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipContext;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusResult;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.domain.shop.vo.ShopId;

import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.review.port.out.ReviewsByRatingResult;
import com.tastyhouse.application.review.port.out.ShopReviewStatisticsResult;
import com.tastyhouse.application.shop.port.out.BestShopItemResult;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;
import com.tastyhouse.application.shop.port.out.LatestShopItemResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopAmenityWithCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopBannerImageResult;
import com.tastyhouse.application.shop.port.out.ShopBreakTimeResult;
import com.tastyhouse.application.shop.port.out.ShopBusinessHourResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceQueryPort;
import com.tastyhouse.application.shop.port.out.ShopClosedDayResult;
import com.tastyhouse.application.shop.port.out.ShopConvenienceInfoResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRangeResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipRegionResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipTierResult;
import com.tastyhouse.application.shop.port.out.ShopFoodTypeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopMapMarkerResult;
import com.tastyhouse.application.shop.port.out.ShopNoticeQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopVisibleDetailResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;
import com.tastyhouse.application.shop.port.out.StationResult;
import com.tastyhouse.application.product.service.ProductQueryService;
import com.tastyhouse.application.review.service.ReviewQueryService;
import com.tastyhouse.application.shop.port.out.ScheduledOrderSlotItemResult;
import com.tastyhouse.application.shop.port.out.ScheduledOrderSlotsViewResult;
import com.tastyhouse.application.shop.port.out.ShopBestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipBreakdownItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipScheduleItemResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipViewResult;
import com.tastyhouse.application.shop.port.out.ShopDetailViewResult;
import com.tastyhouse.application.shop.port.out.ShopInfoViewResult;
import com.tastyhouse.application.shop.port.out.ShopLatestListItemViewResult;
import com.tastyhouse.application.shop.port.out.ShopOrderMethodItemResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryViewResult;
import com.tastyhouse.application.shop.port.out.ShopProductCategoryViewResult;
import com.tastyhouse.application.shop.port.out.ShopReviewStatisticsViewResult;
import com.tastyhouse.application.shop.port.in.ShopDetailQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderInfoQueryUseCase;
import com.tastyhouse.application.shop.port.in.ShopSearchQueryUseCase;

@Service
@WebApp
@Transactional(readOnly = true)
public class ShopQueryService implements ShopSearchQueryUseCase, ShopDetailQueryUseCase, ShopOrderInfoQueryUseCase {

    private static final String UNCATEGORIZED_CATEGORY_NAME = "미분류";

    private final ShopRepository shopRepository;
    private final MemberDeliveryAddressRepository memberDeliveryAddressRepository;
    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;
    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopQueryPort shopQueryPort;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopNoticeQueryPort shopNoticeQueryPort;
    private final ShopSearchQueryPort shopSearchQueryPort;
    private final ShopChoiceQueryPort shopChoiceQueryPort;
    private final ShopDeliveryTipQueryPort shopDeliveryTipQueryPort;
    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ScheduledOrderSlotService scheduledOrderSlotService;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ProductQueryService productQueryService;
    private final ReviewQueryService reviewQueryService;

    public ShopQueryService(
        ShopRepository shopRepository,
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort,
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopQueryPort shopQueryPort,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopNoticeQueryPort shopNoticeQueryPort,
        ShopSearchQueryPort shopSearchQueryPort,
        ShopChoiceQueryPort shopChoiceQueryPort,
        ShopDeliveryTipQueryPort shopDeliveryTipQueryPort,
        ShopOperatingStatusService shopOperatingStatusService,
        ScheduledOrderSlotService scheduledOrderSlotService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        PublicHolidayCalendar publicHolidayCalendar,
        ProductQueryService productQueryService,
        ReviewQueryService reviewQueryService
    ) {
        this.shopRepository = shopRepository;
        this.memberDeliveryAddressRepository = memberDeliveryAddressRepository;
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopQueryPort = shopQueryPort;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopNoticeQueryPort = shopNoticeQueryPort;
        this.shopSearchQueryPort = shopSearchQueryPort;
        this.shopChoiceQueryPort = shopChoiceQueryPort;
        this.shopDeliveryTipQueryPort = shopDeliveryTipQueryPort;
        this.shopOperatingStatusService = shopOperatingStatusService;
        this.scheduledOrderSlotService = scheduledOrderSlotService;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.publicHolidayCalendar = publicHolidayCalendar;
        this.productQueryService = productQueryService;
        this.reviewQueryService = reviewQueryService;
    }

    @Override
    public List<ShopMapMarkerResult> searchMapMarkers(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return shopSearchQueryPort.findNearbyShops(lat, lon);
    }

    private Long resolveDeliveryAdminDongId(Long memberId) {
        if (memberId == null) {
            return null;
        }

        return memberDeliveryAddressQueryPort.findDefaultAdminDongId(MemberId.of(memberId)).orElse(null);
    }

    @Override
    public PageResult<ShopBestListItemViewResult> searchBestShops(Long memberId, int page, int size) {
        PageResult<BestShopItemResult> result =
            shopSearchQueryPort.findBestShops(resolveDeliveryAdminDongId(memberId), PageQuery.of(page, size));
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(BestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToBestShopListItemResult(dto, statusMap));
    }

    @Override
    public PageResult<ShopLatestListItemViewResult> searchLatestShops(
        Long stationId,
        List<String> foodTypes,
        List<String> amenities,
        Long memberId,
        int page,
        int size
    ) {
        List<FoodType> foodTypeFilters = foodTypes == null ? null : foodTypes.stream().map(FoodType::from).toList();
        List<Amenity> amenityFilters = amenities == null ? null : amenities.stream().map(Amenity::from).toList();
        PageResult<LatestShopItemResult> result = shopSearchQueryPort.findLatestShops(
            stationId,
            foodTypeFilters,
            amenityFilters,
            resolveDeliveryAdminDongId(memberId),
            PageQuery.of(page, size)
        );
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(LatestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToLatestShopListItemResult(dto, statusMap));
    }

    private Map<Long, ShopOperatingStatus> resolveOperatingStatuses(List<Long> shopIds) {
        return shopOperatingStatusService.findOperatingStatuses(shopIds, LocalDateTime.now());
    }

    private String operatingStatusName(Map<Long, ShopOperatingStatus> statusMap, Long shopId) {
        ShopOperatingStatus status = statusMap.get(shopId);
        return status == null ? null : status.name();
    }

    @Override
    public List<EditorChoiceResult> searchEditorChoices(int page, int size) {
        return shopChoiceQueryPort.findEditorChoices(PageQuery.of(page, size)).content();
    }

    private ShopBestListItemViewResult convertToBestShopListItemResult(BestShopItemResult dto, Map<Long, ShopOperatingStatus> statusMap) {
        return new ShopBestListItemViewResult(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl(),
            dto.foodTypes().stream().map(Enum::name).toList(),
            operatingStatusName(statusMap, dto.id()),
            dto.minOrderAmount(),
            dto.minDeliveryTip(),
            dto.maxDeliveryTip()
        );
    }

    private ShopLatestListItemViewResult convertToLatestShopListItemResult(LatestShopItemResult dto, Map<Long, ShopOperatingStatus> statusMap) {
        return new ShopLatestListItemViewResult(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            dto.imageUrl(),
            dto.createdAt(),
            dto.reviewCount(),
            dto.bookmarkCount(),
            dto.foodTypes().stream().map(Enum::name).toList(),
            operatingStatusName(statusMap, dto.id()),
            dto.minOrderAmount(),
            dto.minDeliveryTip(),
            dto.maxDeliveryTip()
        );
    }

    @Override
    public List<StationResult> searchAllStations() {
        return shopChoiceQueryPort.findAllStations();
    }

    @Override
    public List<ShopFoodTypeCategoryResult> searchAllFoodTypes() {
        return shopQueryPort.findVisibleFoodTypeCategories();
    }

    @Override
    public List<ShopAmenityCategoryResult> searchAllAmenities() {
        return shopQueryPort.findVisibleAmenityCategories();
    }

    @Override
    public ShopDetailViewResult getShopDetail(Long shopId) {
        ShopVisibleDetailResult shop = findVisibleShop(shopId);

        List<ShopPhoneNumberResult> phoneNumbers = shopBasicInfoQueryPort.findPhoneNumbers(shopId);

        String trademarkImageUrl = shopBasicInfoQueryPort.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::trademarkImageUrl)
            .orElse(null);

        ShopOperatingStatusResult operatingStatus =
            shopOperatingStatusService.findOrderAvailability(shopId, LocalDateTime.now());
        OrderUnavailableReason unavailableReason = operatingStatus.unavailableReason();

        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryPort.findTipRange(shopId);

        return new ShopDetailViewResult(
            shop.id(),
            shop.name(),
            shop.latitude(),
            shop.longitude(),
            shop.rating(),
            shop.roadAddress(),
            shop.lotAddress(),
            shop.phoneNumber(),
            phoneNumbers,
            trademarkImageUrl,
            operatingStatus.status().name(),
            unavailableReason == null ? null : unavailableReason.name(),
            unavailableReason == null ? null : unavailableReason.getDisplayName(),
            shop.minOrderAmount(),
            tipRange.minDeliveryTip(),
            tipRange.maxDeliveryTip(),
            shop.scheduledOrderEnabled()
        );
    }

    @Override
    public ScheduledOrderSlotsViewResult getScheduledOrderSlots(Long shopId, String orderMethod) {
        OrderMethod method = OrderMethod.from(orderMethod);
        List<ScheduledOrderSlot> slots = scheduledOrderSlotService.findAvailableSlots(
            ShopId.of(shopId), method, LocalDateTime.now()
        );

        int leadTimeMinutes = ScheduledOrderPolicy.supports(method)
            ? ScheduledOrderPolicy.leadTimeMinutes(method)
            : 0;

        return new ScheduledOrderSlotsViewResult(
            !slots.isEmpty(),
            leadTimeMinutes,
            ScheduledOrderPolicy.SLOT_UNIT_MINUTES,
            ScheduledOrderPolicy.isRangeSlot(method),
            slots.stream().map(slot -> toScheduledOrderSlotItemResult(slot, method)).toList()
        );
    }

    private ScheduledOrderSlotItemResult toScheduledOrderSlotItemResult(
        ScheduledOrderSlot slot,
        OrderMethod orderMethod
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
        String startLabel = slot.startAt().format(formatter);
        String label = ScheduledOrderPolicy.isRangeSlot(orderMethod)
            ? startLabel + "~" + slot.endAt().format(formatter)
            : startLabel;

        return new ScheduledOrderSlotItemResult(
            slot.startAt(),
            slot.endAt(),
            label,
            toDayLabel(slot.startAt())
        );
    }

    private String toDayLabel(LocalDateTime slotStartAt) {
        return slotStartAt.toLocalDate().isEqual(LocalDate.now()) ? "오늘" : "내일";
    }

    @Override
    public ShopDeliveryTipViewResult getShopDeliveryTip(
        Long shopId,
        Long memberId,
        Long deliveryAddressId,
        Integer orderAmount,
        String orderMethod
    ) {
        Shop shop = findVisibleShopAggregate(shopId);

        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryPort.findTipRange(shopId);
        ShopDeliveryTipSettingResult setting = shopDeliveryTipQueryPort.findSetting(shopId).orElse(null);

        List<ShopDeliveryTipTierResult> tiers = shopDeliveryTipQueryPort.findTiers(shopId);
        List<ShopDeliveryTipRegionResult> regions = shopDeliveryTipQueryPort.findRegionTips(shopId);
        List<ShopDeliveryTipScheduleItemResult> schedules = shopDeliveryTipQueryPort.findScheduleTips(shopId).stream()
            .map(this::toShopDeliveryTipScheduleItemResult)
            .toList();

        ShopDeliveryTipBreakdown breakdown = calculateDeliveryTip(
            shop, memberId, deliveryAddressId, orderAmount, orderMethod
        );

        return new ShopDeliveryTipViewResult(
            breakdown == null ? null : breakdown.totalTipAmount(),
            tipRange.minDeliveryTip(),
            tipRange.maxDeliveryTip(),
            toShopDeliveryTipBreakdownItems(breakdown, orderAmount, setting, tiers),
            tiers,
            setting == null ? DeliveryTipExtraType.NONE.name() : setting.extraTipType(),
            toDistanceSetting(setting),
            regions,
            schedules,
            shopDeliveryTipQueryPort.findHolidayTipAmount(shopId)
        );
    }

    private ShopDeliveryTipBreakdown calculateDeliveryTip(
        Shop shop,
        Long memberId,
        Long deliveryAddressId,
        Integer orderAmount,
        String orderMethod
    ) {
        if (memberId == null || deliveryAddressId == null || orderAmount == null) {
            return null;
        }

        MemberDeliveryAddress deliveryAddress = memberDeliveryAddressRepository.findById(deliveryAddressId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_DELIVERY_ADDRESS_NOT_FOUND));
        if (!deliveryAddress.isOwnedBy(MemberId.of(memberId))) {
            throw new BusinessException(ErrorCode.MEMBER_DELIVERY_ADDRESS_ACCESS_DENIED);
        }

        LocalDateTime now = LocalDateTime.now();
        ShopId typedShopId = ShopId.of(shop.getId());

        return shopDeliveryTipCalculator.calculate(ShopDeliveryTipContext.of(
            OrderMethod.from(orderMethod),
            orderAmount,
            deliveryDistanceMeters(shop, deliveryAddress),
            deliveryAddress.getAdminDongId(),
            now,
            publicHolidayCalendar.isPublicHoliday(now.toLocalDate()),
            shopDeliveryTipRepository.findSettingByShopId(typedShopId).orElse(null),
            shopDeliveryTipRepository.findTiersByShopId(typedShopId),
            shopDeliveryTipRepository.findRegionTipsByShopId(typedShopId),
            shopDeliveryTipRepository.findScheduleTipsByShopId(typedShopId),
            shopDeliveryTipRepository.findHolidayTipByShopId(typedShopId).orElse(null)
        ));
    }

    private Double deliveryDistanceMeters(Shop shop, MemberDeliveryAddress deliveryAddress) {
        if (shop.getLatitude() == null || shop.getLongitude() == null
            || deliveryAddress.getLatitude() == null || deliveryAddress.getLongitude() == null) {
            return null;
        }
        return GeoDistance.distanceMeters(
            shop.getLatitude(),
            shop.getLongitude(),
            deliveryAddress.getLatitude(),
            deliveryAddress.getLongitude()
        );
    }

    private List<ShopDeliveryTipBreakdownItemResult> toShopDeliveryTipBreakdownItems(
        ShopDeliveryTipBreakdown breakdown,
        Integer orderAmount,
        ShopDeliveryTipSettingResult setting,
        List<ShopDeliveryTipTierResult> tiers
    ) {
        if (breakdown == null) {
            return List.of();
        }

        List<ShopDeliveryTipBreakdownItemResult> items = new ArrayList<>();
        addBreakdownItem(items, baseTipLabel(orderAmount, tiers), breakdown.baseTipAmount());
        addBreakdownItem(items, distanceTipLabel(setting), breakdown.distanceTipAmount());
        addBreakdownItem(items, "지역별 추가", breakdown.regionTipAmount());
        addBreakdownItem(items, "시간대 할증", breakdown.scheduleTipAmount());
        addBreakdownItem(items, "공휴일 할증", breakdown.holidayTipAmount());
        return List.copyOf(items);
    }

    private void addBreakdownItem(List<ShopDeliveryTipBreakdownItemResult> items, String label, int amount) {
        if (amount > 0) {
            items.add(new ShopDeliveryTipBreakdownItemResult(label, amount));
        }
    }

    private String baseTipLabel(Integer orderAmount, List<ShopDeliveryTipTierResult> tiers) {
        if (orderAmount == null || tiers == null || tiers.isEmpty()) {
            return "기본 배달팁";
        }

        return tiers.stream()
            .filter(tier -> orderAmount >= tier.minOrderAmount())
            .max(Comparator.comparingInt(ShopDeliveryTipTierResult::minOrderAmount))
            .or(() -> tiers.stream().min(Comparator.comparingInt(ShopDeliveryTipTierResult::minOrderAmount)))
            .map(tier -> "주문금액 " + formatAmount(tier.minOrderAmount()) + "원 이상")
            .orElse("기본 배달팁");
    }

    private String distanceTipLabel(ShopDeliveryTipSettingResult setting) {
        if (setting == null || setting.baseDistanceMeters() == null) {
            return "거리 할증";
        }
        return formatKilometers(setting.baseDistanceMeters()) + "km 초과 거리 할증";
    }

    private String formatAmount(int amount) {
        return String.format(Locale.KOREA, "%,d", amount);
    }

    private String formatKilometers(int meters) {
        return meters % 1000 == 0
            ? String.valueOf(meters / 1000)
            : String.format(Locale.KOREA, "%.1f", meters / 1000.0);
    }

    private ShopDeliveryTipScheduleItemResult toShopDeliveryTipScheduleItemResult(ShopDeliveryTipScheduleResult dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        DayType dayType = DayType.from(dto.dayType());

        return new ShopDeliveryTipScheduleItemResult(
            dayType.name(),
            dayType.getDescription(),
            dto.startTime() != null ? dto.startTime().format(formatter) : null,
            dto.endTime() != null ? dto.endTime().format(formatter) : null,
            dto.tipAmount()
        );
    }

    private ShopDeliveryTipSettingResult toDistanceSetting(ShopDeliveryTipSettingResult dto) {
        if (dto == null
            || !DeliveryTipExtraType.DISTANCE.name().equals(dto.extraTipType())
            || dto.baseDistanceMeters() == null
            || dto.surchargeUnit() == null
            || dto.surchargeAmount() == null) {
            return null;
        }
        return dto;
    }

    @Override
    public ShopInfoViewResult getShopInfo(Long shopId) {
        findVisibleShop(shopId);
        List<ShopBusinessHourResult> businessHours = shopBasicInfoQueryPort.findBusinessHours(shopId);
        List<ShopBreakTimeResult> breakTimes = shopBasicInfoQueryPort.findBreakTimes(shopId);
        List<ShopClosedDayResult> closedDays = shopBasicInfoQueryPort.findClosedDays(shopId);
        List<ShopAmenityWithCategoryResult> shopAmenities = shopQueryPort.findAmenitiesWithCategory(shopId);

        String ownerMessage = null;
        LocalDateTime ownerMessageCreatedAt = null;
        var ownerMessageHistory = shopBasicInfoQueryPort.findLatestOwnerMessage(shopId);
        if (ownerMessageHistory.isPresent()) {
            ownerMessage = ownerMessageHistory.get().message();
            ownerMessageCreatedAt = ownerMessageHistory.get().createdAt();
        }

        Boolean parkingAvailable = null;
        Boolean parkingPaid = null;
        Boolean valetAvailable = null;
        Boolean valetPaid = null;
        String directionsGuide = null;
        BigDecimal displayLatitude = null;
        BigDecimal displayLongitude = null;
        var convenienceInfo = shopBasicInfoQueryPort.findConvenienceInfo(shopId);
        if (convenienceInfo.isPresent()) {
            ShopConvenienceInfoResult info = convenienceInfo.get();
            parkingAvailable = info.parkingAvailable();
            parkingPaid = info.parkingPaid();
            valetAvailable = info.valetAvailable();
            valetPaid = info.valetPaid();
            directionsGuide = info.directionsGuide();
            displayLatitude = info.displayLatitude();
            displayLongitude = info.displayLongitude();
        }

        return new ShopInfoViewResult(
            closedDays,
            businessHours,
            breakTimes,
            shopAmenities,
            ownerMessage,
            ownerMessageCreatedAt,
            parkingAvailable,
            parkingPaid,
            valetAvailable,
            valetPaid,
            directionsGuide,
            displayLatitude,
            displayLongitude
        );
    }

    @Override
    public ShopNoticeResult getShopNotice(Long shopId) {
        findVisibleShop(shopId);
        return shopNoticeQueryPort.findExposedNotice(shopId).orElse(null);
    }

    @Override
    public List<ShopBannerImageResult> getShopBanners(Long shopId) {
        return shopBasicInfoQueryPort.findBannerImages(shopId);
    }

    @Override
    public List<ShopProductCategoryViewResult> getShopProducts(Long shopId) {
        List<ShopProductItemResult> shopProducts = productQueryService.findShopProducts(shopId);

        Map<Long, List<ShopProductItemResult>> productsByCategory = shopProducts.stream()
            .filter(product -> product.productCategoryId() != null)
            .collect(Collectors.groupingBy(ShopProductItemResult::productCategoryId));

        List<ShopProductItemResult> uncategorizedProducts = shopProducts.stream()
            .filter(product -> product.productCategoryId() == null)
            .toList();

        List<ShopProductCategoryViewResult> categories = productQueryService.findShopProductCategories(shopId)
            .stream()
            .map(category -> new ShopProductCategoryViewResult(
                category.name(),
                productsByCategory.getOrDefault(category.id(), new ArrayList<>())
            ))
            .collect(Collectors.toCollection(ArrayList::new));

        if (!uncategorizedProducts.isEmpty()) {
            categories.add(new ShopProductCategoryViewResult(
                UNCATEGORIZED_CATEGORY_NAME,
                uncategorizedProducts
            ));
        }

        return categories;
    }

    @Override
    public List<ShopPhotoCategoryViewResult> getShopPhotos(Long shopId) {
        List<ShopPhotoCategoryResult> categories = shopBasicInfoQueryPort.findPhotoCategories(shopId);
        List<ShopPhotoCategoryImageResult> images = shopQueryPort.findAllPhotoCategoryImages();

        Map<Long, List<ShopPhotoCategoryImageResult>> imagesByCategory = images.stream()
            .filter(image -> image.shopPhotoCategoryId() != null)
            .collect(Collectors.groupingBy(ShopPhotoCategoryImageResult::shopPhotoCategoryId));

        return categories.stream()
            .map(category -> {
                List<ShopPhotoCategoryImageResult> categoryImages =
                    imagesByCategory.getOrDefault(category.id(), new ArrayList<>());
                List<String> imageUrls = categoryImages.stream()
                    .map(ShopPhotoCategoryImageResult::imageUrl)
                    .toList();
                return new ShopPhotoCategoryViewResult(
                    category.name(),
                    imageUrls
                );
            })
            .toList();
    }

    @Override
    public ReviewsByRatingResult getShopReviewsByRatingWithPagination(
        Long shopId,
        int page,
        int size,
        Boolean hasImage,
        String sortType
    ) {
        return reviewQueryService.findShopReviewsByRating(shopId, page, size, hasImage, sortType);
    }

    @Override
    public ShopReviewStatisticsViewResult getShopReviewStatistics(Long shopId) {
        ShopReviewStatisticsResult statistics = reviewQueryService.findShopReviewStatistics(shopId);

        ShopVisibleDetailResult shop = findVisibleShop(shopId);

        return new ShopReviewStatisticsViewResult(
            shop.rating(),
            statistics.totalReviewCount(),
            statistics.averageTasteRating(),
            statistics.averageAmountRating(),
            statistics.averagePriceRating(),
            statistics.averageAtmosphereRating(),
            statistics.averageKindnessRating(),
            statistics.averageHygieneRating(),
            statistics.willRevisitPercentage(),
            statistics.monthlyReviewCounts(),
            statistics.ratingCounts()
        );
    }

    @Override
    public List<PopularProductItemResult> getPopularProducts(Long shopId) {
        return productQueryService.findPopularProducts(shopId);
    }

    @Override
    public boolean isBookmarked(Long shopId, Long memberId) {
        return shopQueryPort.existsBookmark(shopId, memberId);
    }

    @Override
    public List<ShopOrderMethodItemResult> getShopOrderMethods(Long shopId) {
        findVisibleShop(shopId);

        Map<OrderMethod, ShopOperatingStatusResult> availabilities =
            shopOperatingStatusService.findOrderMethodAvailabilities(shopId, LocalDateTime.now());

        return shopBasicInfoQueryPort.findOrderMethods(shopId).stream()
            .map(dto -> toShopOrderMethodItemResult(dto, availabilities.get(dto.orderMethod())))
            .toList();
    }

    private ShopOrderMethodItemResult toShopOrderMethodItemResult(
        ShopOrderMethodResult dto,
        ShopOperatingStatusResult availability
    ) {
        OrderUnavailableReason reason = availability == null ? null : availability.unavailableReason();
        return new ShopOrderMethodItemResult(
            dto.orderMethod().name(),
            dto.orderMethod().getDisplayName(),
            availability != null && availability.isOpen(),
            reason == null ? null : reason.name(),
            reason == null ? null : reason.getDisplayName()
        );
    }

    private ShopVisibleDetailResult findVisibleShop(Long shopId) {
        return shopQueryPort.findVisibleDetailById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    private Shop findVisibleShopAggregate(Long shopId) {
        return shopRepository.findVisibleById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

}
