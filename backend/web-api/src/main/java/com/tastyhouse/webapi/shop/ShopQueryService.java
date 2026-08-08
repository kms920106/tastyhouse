package com.tastyhouse.webapi.shop;

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

import com.tastyhouse.domain.holiday.service.PublicHolidayCalendar;
import com.tastyhouse.domain.member.model.MemberDeliveryAddress;
import com.tastyhouse.domain.member.repository.MemberDeliveryAddressRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.DayType;
import com.tastyhouse.domain.shop.model.DeliveryTipExtraType;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ScheduledOrderPolicy;
import com.tastyhouse.domain.shop.model.ScheduledOrderSlot;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ScheduledOrderSlotService;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipBreakdown;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipCalculator;
import com.tastyhouse.domain.shop.service.ShopDeliveryTipContext;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.geo.GeoDistance;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.query.ProductSimpleResult;
import com.tastyhouse.infrastructure.product.query.ShopProductItemResult;
import com.tastyhouse.infrastructure.review.query.LatestReviewListItemResult;
import com.tastyhouse.infrastructure.review.query.ReviewsByRatingResult;
import com.tastyhouse.infrastructure.review.query.ShopReviewStatisticsResult;
import com.tastyhouse.infrastructure.shop.query.BestShopItemResult;
import com.tastyhouse.infrastructure.shop.query.EditorChoiceResult;
import com.tastyhouse.infrastructure.shop.query.LatestShopItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopAmenityCategoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopAmenityWithCategoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopBannerImageResult;
import com.tastyhouse.infrastructure.shop.query.ShopBreakTimeResult;
import com.tastyhouse.infrastructure.shop.query.ShopBusinessHourResult;
import com.tastyhouse.infrastructure.shop.query.ShopChoiceQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopClosedDayResult;
import com.tastyhouse.infrastructure.shop.query.ShopConvenienceInfoResult;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryTipQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryTipRangeResult;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryTipRegionResult;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryTipScheduleResult;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryTipSettingResult;
import com.tastyhouse.infrastructure.shop.query.ShopDeliveryTipTierResult;
import com.tastyhouse.infrastructure.shop.query.ShopFoodTypeCategoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopImageUrlsResult;
import com.tastyhouse.infrastructure.shop.query.ShopMapMarkerResult;
import com.tastyhouse.infrastructure.shop.query.ShopOrderMethodResult;
import com.tastyhouse.infrastructure.shop.query.ShopPhoneNumberResult;
import com.tastyhouse.infrastructure.shop.query.ShopPhotoCategoryImageResult;
import com.tastyhouse.infrastructure.shop.query.ShopPhotoCategoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopSearchQueryDao;
import com.tastyhouse.webapi.product.ProductQueryService;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.review.ReviewQueryService;
import com.tastyhouse.webapi.shop.response.ScheduledOrderSlotItemResponse;
import com.tastyhouse.webapi.shop.response.ScheduledOrderSlotsResponse;
import com.tastyhouse.webapi.shop.response.ShopAmenityItem;
import com.tastyhouse.webapi.shop.response.ShopAmenityListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBannerResponse;
import com.tastyhouse.webapi.shop.response.ShopBestListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBookmarkResponse;
import com.tastyhouse.webapi.shop.response.ShopBreakTimeItem;
import com.tastyhouse.webapi.shop.response.ShopBusinessHourItem;
import com.tastyhouse.webapi.shop.response.ShopClosedDayItem;
import com.tastyhouse.webapi.shop.response.ShopDeliveryTipBreakdownItem;
import com.tastyhouse.webapi.shop.response.ShopDeliveryTipDistanceItem;
import com.tastyhouse.webapi.shop.response.ShopDeliveryTipRegionItem;
import com.tastyhouse.webapi.shop.response.ShopDeliveryTipResponse;
import com.tastyhouse.webapi.shop.response.ShopDeliveryTipScheduleItem;
import com.tastyhouse.webapi.shop.response.ShopDeliveryTipTierItem;
import com.tastyhouse.webapi.shop.response.ShopDetailResponse;
import com.tastyhouse.webapi.shop.response.ShopEditorChoiceProductItem;
import com.tastyhouse.webapi.shop.response.ShopEditorChoiceResponse;
import com.tastyhouse.webapi.shop.response.ShopFoodTypeListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopInfoResponse;
import com.tastyhouse.webapi.shop.response.ShopLatestListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopMapMarkerResponse;
import com.tastyhouse.webapi.shop.response.ShopOrderMethodItem;
import com.tastyhouse.webapi.shop.response.ShopOrderMethodResponse;
import com.tastyhouse.webapi.shop.response.ShopPhoneNumberItem;
import com.tastyhouse.webapi.shop.response.ShopPhotoCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopProductCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewStatisticsResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingPageResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingResponse;
import com.tastyhouse.webapi.shop.response.ShopStationListItemResponse;

/**
 * 회원용 가게 조회 서비스(CQRS query 측).
 *
 * <p>표현 목적 조회는 전부 infra query DAO에서 Result를 받아 Response로 조립한다. 노출 가게 단건과
 * 북마크 여부만 도메인 write 포트를 쓴다 — 전자는 폐업·노출정지 가게 차단이라는 도메인 판정이고,
 * 후자는 존재 검증이다.
 *
 * <p>실시간 영업 상태는 여섯 애그리거트를 함께 읽어 판정해야 하므로 도메인 서비스
 * {@link ShopOperatingStatusService}에 위임한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopQueryService {

    private final ShopRepository shopRepository;
    private final ShopBookmarkRepository shopBookmarkRepository;
    private final MemberDeliveryAddressRepository memberDeliveryAddressRepository;
    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopQueryDao shopQueryDao;
    private final ShopSearchQueryDao shopSearchQueryDao;
    private final ShopChoiceQueryDao shopChoiceQueryDao;
    private final ShopDeliveryTipQueryDao shopDeliveryTipQueryDao;
    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ScheduledOrderSlotService scheduledOrderSlotService;
    private final ShopDeliveryTipCalculator shopDeliveryTipCalculator;
    private final PublicHolidayCalendar publicHolidayCalendar;
    private final ProductQueryService productQueryService;
    private final ReviewQueryService reviewQueryService;

    public ShopQueryService(
        ShopRepository shopRepository,
        ShopBookmarkRepository shopBookmarkRepository,
        MemberDeliveryAddressRepository memberDeliveryAddressRepository,
        ShopDeliveryTipRepository shopDeliveryTipRepository,
        ShopQueryDao shopQueryDao,
        ShopSearchQueryDao shopSearchQueryDao,
        ShopChoiceQueryDao shopChoiceQueryDao,
        ShopDeliveryTipQueryDao shopDeliveryTipQueryDao,
        ShopOperatingStatusService shopOperatingStatusService,
        ScheduledOrderSlotService scheduledOrderSlotService,
        ShopDeliveryTipCalculator shopDeliveryTipCalculator,
        PublicHolidayCalendar publicHolidayCalendar,
        ProductQueryService productQueryService,
        ReviewQueryService reviewQueryService
    ) {
        this.shopRepository = shopRepository;
        this.shopBookmarkRepository = shopBookmarkRepository;
        this.memberDeliveryAddressRepository = memberDeliveryAddressRepository;
        this.shopDeliveryTipRepository = shopDeliveryTipRepository;
        this.shopQueryDao = shopQueryDao;
        this.shopSearchQueryDao = shopSearchQueryDao;
        this.shopChoiceQueryDao = shopChoiceQueryDao;
        this.shopDeliveryTipQueryDao = shopDeliveryTipQueryDao;
        this.shopOperatingStatusService = shopOperatingStatusService;
        this.scheduledOrderSlotService = scheduledOrderSlotService;
        this.shopDeliveryTipCalculator = shopDeliveryTipCalculator;
        this.publicHolidayCalendar = publicHolidayCalendar;
        this.productQueryService = productQueryService;
        this.reviewQueryService = reviewQueryService;
    }

    public List<ShopMapMarkerResponse> searchMapMarkers(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return shopSearchQueryDao.findNearbyShops(lat, lon).stream()
            .map(this::toShopMapMarkerResponse)
            .toList();
    }

    private ShopMapMarkerResponse toShopMapMarkerResponse(ShopMapMarkerResult dto) {
        return ShopMapMarkerResponse.from(
            dto.id(),
            dto.latitude(),
            dto.longitude(),
            dto.name()
        );
    }

    public PageResult<ShopBestListItemResponse> searchBestShops(int page, int size) {
        PageResult<BestShopItemResult> result = shopSearchQueryDao.findBestShops(PageQuery.of(page, size));
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(BestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToBestShopListItemResponse(dto, statusMap));
    }

    public PageResult<ShopLatestListItemResponse> searchLatestShops(
        Long stationId,
        List<String> foodTypes,
        List<String> amenities,
        int page,
        int size
    ) {
        List<FoodType> foodTypeFilters = foodTypes == null ? null : foodTypes.stream().map(FoodType::from).toList();
        List<Amenity> amenityFilters = amenities == null ? null : amenities.stream().map(Amenity::from).toList();
        PageResult<LatestShopItemResult> result =
            shopSearchQueryDao.findLatestShops(stationId, foodTypeFilters, amenityFilters, PageQuery.of(page, size));
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(LatestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToLatestShopListItemResponse(dto, statusMap));
    }

    private Map<Long, ShopOperatingStatus> resolveOperatingStatuses(List<Long> shopIds) {
        return shopOperatingStatusService.findOperatingStatuses(shopIds, LocalDateTime.now());
    }

    private String operatingStatusName(Map<Long, ShopOperatingStatus> statusMap, Long shopId) {
        ShopOperatingStatus status = statusMap.get(shopId);
        return status == null ? null : status.name();
    }

    public List<ShopEditorChoiceResponse> searchEditorChoices(int page, int size) {
        return shopChoiceQueryDao.findEditorChoices(PageQuery.of(page, size)).content().stream()
            .map(this::convertToEditorChoiceResponse)
            .toList();
    }

    private ShopEditorChoiceResponse convertToEditorChoiceResponse(EditorChoiceResult dto) {
        List<ShopEditorChoiceProductItem> productItems = dto.products() != null
            ? dto.products().stream().map(this::convertToEditorChoiceProductItem).toList()
            : new ArrayList<>();

        return ShopEditorChoiceResponse.from(
            dto.id(),
            dto.name(),
            dto.shopImageUrl(),
            dto.title(),
            dto.content(),
            productItems
        );
    }

    private ShopBestListItemResponse convertToBestShopListItemResponse(BestShopItemResult dto, Map<Long, ShopOperatingStatus> statusMap) {
        return ShopBestListItemResponse.from(
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

    private ShopLatestListItemResponse convertToLatestShopListItemResponse(LatestShopItemResult dto, Map<Long, ShopOperatingStatus> statusMap) {
        return ShopLatestListItemResponse.from(
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

    private ShopEditorChoiceProductItem convertToEditorChoiceProductItem(ProductSimpleResult dto) {
        return ShopEditorChoiceProductItem.from(
            dto.id(),
            dto.shopName(),
            dto.name(),
            dto.imageUrl(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate()
        );
    }

    public List<ShopStationListItemResponse> searchAllStations() {
        return shopChoiceQueryDao.findAllStations().stream()
            .map(station -> ShopStationListItemResponse.from(station.id(), station.stationName()))
            .toList();
    }

    public List<ShopFoodTypeListItemResponse> searchAllFoodTypes() {
        return shopQueryDao.findVisibleFoodTypeCategories().stream()
            .map(this::convertToFoodTypeListItemResponse)
            .toList();
    }

    public List<ShopAmenityListItemResponse> searchAllAmenities() {
        return shopQueryDao.findVisibleAmenityCategories().stream()
            .map(this::convertToAmenityListItemResponse)
            .toList();
    }

    private ShopFoodTypeListItemResponse convertToFoodTypeListItemResponse(ShopFoodTypeCategoryResult category) {
        return ShopFoodTypeListItemResponse.from(
            category.foodType().name(),
            category.displayName(),
            category.activeIconUrl(),
            category.inactiveIconUrl()
        );
    }

    private ShopAmenityListItemResponse convertToAmenityListItemResponse(ShopAmenityCategoryResult category) {
        return ShopAmenityListItemResponse.from(
            category.amenity().name(),
            category.displayName(),
            category.activeIconUrl(),
            category.inactiveIconUrl()
        );
    }

    public ShopDetailResponse getShopDetail(Long shopId) {
        Shop shop = findVisibleShop(shopId);

        List<ShopPhoneNumberItem> phoneNumbers = shopQueryDao.findPhoneNumbers(shopId).stream()
            .map(this::convertToShopPhoneNumberItem)
            .toList();

        String trademarkImageUrl = shopQueryDao.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::trademarkImageUrl)
            .orElse(null);

        String operatingStatus = shopOperatingStatusService
            .findOperatingStatus(shopId, LocalDateTime.now())
            .name();

        // 배달팁·공휴일은 시각 의존 값이라 이 응답에는 캐시를 두지 않는다(최소주문금액 최신화를 위해
        // 가게 상세 캐시를 제거한 선례와 같은 이유) — 범위 값 자체는 시각에 의존하지 않지만, 점주가
        // 설정을 바꾼 직후 상세 화면이 옛 금액을 보여주면 팝업의 확정 금액과 어긋난다.
        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryDao.findTipRange(shopId);

        return ShopDetailResponse.of(
            shop.getId(),
            shop.getName(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getRating(),
            shop.getRoadAddress(),
            shop.getLotAddress(),
            shop.getPhoneNumber(),
            phoneNumbers,
            trademarkImageUrl,
            operatingStatus,
            shop.getMinOrderAmount(),
            tipRange.minDeliveryTip(),
            tipRange.maxDeliveryTip(),
            shop.isScheduledOrderEnabled()
        );
    }

    /**
     * 예약 가능한 수령시간 슬롯 목록 조회.
     *
     * <p><b>캐시를 두지 않는다</b> — 시각 의존 응답이라 30분만 지나도 첫 슬롯이 달라진다.
     *
     * <p>예약할 수 없는 상태(미운영·미지원 주문방식·영업 종료 등)는 예외가 아니라
     * {@code available:false} + 빈 목록으로 내려간다. 그래도 리드타임·슬롯 단위·범위 여부는 함께 주어
     * 프론트가 안내 문구를 띄울 때 상수를 복제하지 않게 한다.
     *
     * <p>가게가 없으면 도메인 서비스가 {@code SHOP_NOT_FOUND}(404)를 던진다.
     */
    public ScheduledOrderSlotsResponse getScheduledOrderSlots(Long shopId, String orderMethod) {
        OrderMethod method = OrderMethod.from(orderMethod);
        List<ScheduledOrderSlot> slots = scheduledOrderSlotService.findAvailableSlots(
            ShopId.of(shopId), method, LocalDateTime.now()
        );

        // 미지원 주문방식(TABLE·RESERVATION)은 계산기가 빈 목록을 주므로 리드타임을 물을 수 없다.
        // 이때는 안내할 리드타임 자체가 없으므로 0으로 내린다.
        int leadTimeMinutes = ScheduledOrderPolicy.supports(method)
            ? ScheduledOrderPolicy.leadTimeMinutes(method)
            : 0;

        return ScheduledOrderSlotsResponse.from(
            !slots.isEmpty(),
            leadTimeMinutes,
            ScheduledOrderPolicy.SLOT_UNIT_MINUTES,
            ScheduledOrderPolicy.isRangeSlot(method),
            slots.stream().map(slot -> toScheduledOrderSlotItemResponse(slot, method)).toList()
        );
    }

    /**
     * 슬롯 하나를 표시 문구까지 완성해 응답으로 조립한다.
     *
     * <p>배달은 범위({@code "오후 6:00~오후 6:30"}), 포장은 단일 시각({@code "오후 6:00"})으로 표기한다 —
     * 프론트가 이 분기를 복제하지 않도록 서버가 문구를 완성한다({@code ShopDeliveryTipResponse#breakdown} 선례).
     * 날짜 구분은 오늘 기준 상대 표기이며, 자정 넘김 영업·24시간 가게에서 "내일"이 나온다.
     */
    private ScheduledOrderSlotItemResponse toScheduledOrderSlotItemResponse(
        ScheduledOrderSlot slot,
        OrderMethod orderMethod
    ) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN);
        String startLabel = slot.startAt().format(formatter);
        String label = ScheduledOrderPolicy.isRangeSlot(orderMethod)
            ? startLabel + "~" + slot.endAt().format(formatter)
            : startLabel;

        return ScheduledOrderSlotItemResponse.from(
            slot.startAt(),
            slot.endAt(),
            label,
            toDayLabel(slot.startAt())
        );
    }

    /** 오늘 기준 날짜 구분 문구. 하루를 넘어가는 슬롯은 24시간 가게에서만 나오므로 "내일"까지만 표기한다. */
    private String toDayLabel(LocalDateTime slotStartAt) {
        return slotStartAt.toLocalDate().isEqual(LocalDate.now()) ? "오늘" : "내일";
    }

    /**
     * 배달팁 팝업·재견적 조회.
     *
     * <p>{@code memberId}·{@code deliveryAddressId}·{@code orderAmount}가 모두 있으면 <b>확정 모드</b>다 —
     * 주소를 로드해 소유권을 검증하고, 가게 좌표와의 직선거리·공휴일 여부를 해석해
     * {@link ShopDeliveryTipCalculator}에 넘긴다. 하나라도 없으면 <b>범위 모드</b>로 떨어뜨려
     * {@code deliveryTip}을 {@code null}로 둔다(비로그인 사용자도 팝업을 열 수 있어야 한다).
     *
     * <p>확정 계산 입력은 write 포트({@code ShopDeliveryTipRepository})에서 도메인 모델로 읽는다 —
     * 계산기가 도메인 모델을 받으므로 표현용 Result를 도메인으로 되돌리는 변환을 두지 않기 위함이다.
     * 화면 표기용 목록(지역 이름 조립 등)만 infra query DAO에서 받는다.
     */
    public ShopDeliveryTipResponse getShopDeliveryTip(
        Long shopId,
        Long memberId,
        Long deliveryAddressId,
        Integer orderAmount,
        String orderMethod
    ) {
        Shop shop = findVisibleShop(shopId);

        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryDao.findTipRange(shopId);
        ShopDeliveryTipSettingResult setting = shopDeliveryTipQueryDao.findSetting(shopId).orElse(null);

        // 구간 Result는 응답 매핑과 breakdown 문구(적용 구간의 하한 금액) 양쪽이 쓰므로 지역 변수로 잡는다.
        List<ShopDeliveryTipTierResult> tierResults = shopDeliveryTipQueryDao.findTiers(shopId);
        List<ShopDeliveryTipTierItem> tiers = tierResults.stream()
            .map(this::toShopDeliveryTipTierItem)
            .toList();
        List<ShopDeliveryTipRegionItem> regions = shopDeliveryTipQueryDao.findRegionTips(shopId).stream()
            .map(this::toShopDeliveryTipRegionItem)
            .toList();
        List<ShopDeliveryTipScheduleItem> schedules = shopDeliveryTipQueryDao.findScheduleTips(shopId).stream()
            .map(this::toShopDeliveryTipScheduleItem)
            .toList();

        ShopDeliveryTipBreakdown breakdown = calculateDeliveryTip(
            shop, memberId, deliveryAddressId, orderAmount, orderMethod
        );

        return ShopDeliveryTipResponse.of(
            breakdown == null ? null : breakdown.totalTipAmount(),
            tipRange.minDeliveryTip(),
            tipRange.maxDeliveryTip(),
            toShopDeliveryTipBreakdownItems(breakdown, orderAmount, setting, tierResults),
            tiers,
            setting == null ? DeliveryTipExtraType.NONE.name() : setting.extraTipType(),
            toShopDeliveryTipDistanceItem(setting),
            regions,
            schedules,
            shopDeliveryTipQueryDao.findHolidayTipAmount(shopId)
        );
    }

    /**
     * 확정 배달팁을 산출한다 — 확정에 필요한 입력이 하나라도 없으면 {@code null}을 돌려 범위 모드로
     * 떨어뜨린다. 주소가 본인 것이 아니면 남의 주소로 배달팁을 떠보는 경로가 되므로 거절한다.
     */
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

    /**
     * 가게~배달지 직선거리(m). 어느 한쪽 좌표라도 없으면 {@code null}을 돌려 거리별 할증을 0으로 둔다 —
     * 좌표 없는 주소는 저장 단계에서 막지만, 불변식 도입 이전 데이터가 남아 있을 수 있다.
     */
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

    /**
     * 항목별 금액을 사람이 읽는 근거 문구와 함께 나열한다. <b>금액이 0인 항목은 넣지 않는다</b> —
     * "지역별 추가 0원" 같은 줄은 근거가 아니라 잡음이다.
     *
     * <p>문구를 서버가 만드는 것은 어떤 설정이 그 금액을 만들었는지(어느 구간·어느 기본배달거리)가
     * 계산 결과와 함께여야 의미를 갖기 때문이다. 문구 안의 숫자는 <b>천 단위 콤마까지 서버가 넣는다</b> —
     * 이 값은 응답의 금액 필드가 아니라 이미 완성된 문장의 일부라, 프론트가 문자열을 뜯어 다시 포맷할
     * 수 없기 때문이다(금액 필드 자체의 표기 포맷은 그대로 프론트 담당이다).
     */
    private List<ShopDeliveryTipBreakdownItem> toShopDeliveryTipBreakdownItems(
        ShopDeliveryTipBreakdown breakdown,
        Integer orderAmount,
        ShopDeliveryTipSettingResult setting,
        List<ShopDeliveryTipTierResult> tiers
    ) {
        if (breakdown == null) {
            return List.of();
        }

        List<ShopDeliveryTipBreakdownItem> items = new ArrayList<>();
        addBreakdownItem(items, baseTipLabel(orderAmount, tiers), breakdown.baseTipAmount());
        addBreakdownItem(items, distanceTipLabel(setting), breakdown.distanceTipAmount());
        addBreakdownItem(items, "지역별 추가", breakdown.regionTipAmount());
        addBreakdownItem(items, "시간대 할증", breakdown.scheduleTipAmount());
        addBreakdownItem(items, "공휴일 할증", breakdown.holidayTipAmount());
        return List.copyOf(items);
    }

    private void addBreakdownItem(List<ShopDeliveryTipBreakdownItem> items, String label, int amount) {
        if (amount > 0) {
            items.add(ShopDeliveryTipBreakdownItem.from(label, amount));
        }
    }

    /**
     * 구간별 항목 문구 — <b>실제로 적용된 구간의 하한 금액</b>을 담는다("주문금액 15,000원 이상").
     *
     * <p>손님이 입력한 주문금액이 아니라 적용 구간의 경계를 쓰는 이유는, 근거로서 의미 있는 값이
     * "얼마를 주문했는가"가 아니라 "어느 구간에 걸렸는가"이기 때문이다. 구간 선택 규칙은
     * {@code ShopDeliveryTipCalculator}와 같아야 하므로 동일하게 <b>조건을 만족하는 구간 중 하한이
     * 가장 큰 것</b>을 고르고, 최저 구간에도 미달하면 최저 구간을 고른다.
     */
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

    /**
     * 거리별 항목 문구 — 기본배달거리를 넘겨야 붙는 할증이므로 그 거리를 문구에 담는다("3km 초과 거리 할증").
     *
     * <p>기본배달거리 허용값이 1 / 1.5 / 2 / 2.5 / 3km라 km 환산 시 소수점 한 자리가 필요하며,
     * 정수로 떨어지면 소수점을 붙이지 않는다(1500m → "1.5km", 3000m → "3km").
     */
    private String distanceTipLabel(ShopDeliveryTipSettingResult setting) {
        if (setting == null || setting.baseDistanceMeters() == null) {
            return "거리 할증";
        }
        return formatKilometers(setting.baseDistanceMeters()) + "km 초과 거리 할증";
    }

    /** 문구에 들어갈 금액에 천 단위 콤마를 넣는다. */
    private String formatAmount(int amount) {
        return String.format(Locale.KOREA, "%,d", amount);
    }

    /** 미터를 km 문구로 바꾼다 — 정수로 떨어지면 소수점을 생략한다(3000 → "3", 1500 → "1.5"). */
    private String formatKilometers(int meters) {
        return meters % 1000 == 0
            ? String.valueOf(meters / 1000)
            : String.format(Locale.KOREA, "%.1f", meters / 1000.0);
    }

    private ShopDeliveryTipTierItem toShopDeliveryTipTierItem(ShopDeliveryTipTierResult dto) {
        return ShopDeliveryTipTierItem.from(
            dto.minOrderAmount(),
            dto.tipAmount()
        );
    }

    private ShopDeliveryTipRegionItem toShopDeliveryTipRegionItem(ShopDeliveryTipRegionResult dto) {
        return ShopDeliveryTipRegionItem.from(
            dto.regionName(),
            dto.tipAmount()
        );
    }

    private ShopDeliveryTipScheduleItem toShopDeliveryTipScheduleItem(ShopDeliveryTipScheduleResult dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        DayType dayType = DayType.from(dto.dayType());

        return ShopDeliveryTipScheduleItem.from(
            dayType.name(),
            dayType.getDescription(),
            dto.startTime() != null ? dto.startTime().format(formatter) : null,
            dto.endTime() != null ? dto.endTime().format(formatter) : null,
            dto.tipAmount()
        );
    }

    /**
     * 거리별 설정 3필드를 응답으로 조립한다 — 거리별을 쓰지 않는 가게는 {@code null}을 반환해
     * 응답의 {@code distance} 필드를 비운다(거리별↔지역별 상호 배타).
     */
    private ShopDeliveryTipDistanceItem toShopDeliveryTipDistanceItem(ShopDeliveryTipSettingResult dto) {
        if (dto == null
            || !DeliveryTipExtraType.DISTANCE.name().equals(dto.extraTipType())
            || dto.baseDistanceMeters() == null
            || dto.surchargeUnit() == null
            || dto.surchargeAmount() == null) {
            return null;
        }
        return ShopDeliveryTipDistanceItem.from(
            dto.baseDistanceMeters(),
            dto.surchargeUnit(),
            dto.surchargeAmount()
        );
    }

    private ShopPhoneNumberItem convertToShopPhoneNumberItem(ShopPhoneNumberResult dto) {
        return ShopPhoneNumberItem.from(
            dto.phoneNumber(),
            dto.primary(),
            dto.virtual()
        );
    }

    public ShopInfoResponse getShopInfo(Long shopId) {
        findVisibleShop(shopId);
        List<ShopBusinessHourResult> businessHours = shopQueryDao.findBusinessHours(shopId);
        List<ShopBreakTimeResult> breakTimes = shopQueryDao.findBreakTimes(shopId);
        List<ShopClosedDayResult> closedDays = shopQueryDao.findClosedDays(shopId);
        List<ShopAmenityWithCategoryResult> shopAmenities = shopQueryDao.findAmenitiesWithCategory(shopId);

        List<ShopBusinessHourItem> businessHourItems = businessHours.stream()
            .map(this::convertToBusinessHourItem)
            .toList();

        List<ShopBreakTimeItem> breakTimeItems = breakTimes.stream()
            .map(this::convertToBreakTimeItem)
            .toList();

        List<ShopClosedDayItem> closedDayItems = closedDays.stream()
            .map(this::convertToClosedDayItem)
            .toList();

        List<ShopAmenityItem> amenityItems = shopAmenities.stream()
            .map(this::convertToAmenityItem)
            .toList();

        String ownerMessage = null;
        LocalDateTime ownerMessageCreatedAt = null;
        var ownerMessageHistory = shopQueryDao.findLatestOwnerMessage(shopId);
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
        var convenienceInfo = shopQueryDao.findConvenienceInfo(shopId);
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

        return ShopInfoResponse.from(
            closedDayItems,
            businessHourItems,
            breakTimeItems,
            amenityItems,
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

    public List<ShopBannerResponse> getShopBanners(Long shopId) {
        return shopQueryDao.findBannerImages(shopId).stream()
            .map(this::convertToShopBannerResponse)
            .toList();
    }

    public List<ShopProductCategoryResponse> getShopProducts(Long shopId) {
        Map<Long, List<ShopProductItemResult>> productsByCategory =
            productQueryService.findShopProducts(shopId).stream()
                .filter(product -> product.productCategoryId() != null)
                .collect(Collectors.groupingBy(ShopProductItemResult::productCategoryId));

        return productQueryService.findShopProductCategories(shopId).stream()
            .map(category -> {
                List<ProductSummaryResponse> menuResponses = productsByCategory
                    .getOrDefault(category.id(), new ArrayList<>())
                    .stream()
                    .map(this::convertToShopMenuResponse)
                    .toList();
                return ShopProductCategoryResponse.from(
                    category.name(),
                    menuResponses
                );
            })
            .toList();
    }

    public List<ShopPhotoCategoryResponse> getShopPhotos(Long shopId) {
        List<ShopPhotoCategoryResult> categories = shopQueryDao.findPhotoCategories(shopId);
        List<ShopPhotoCategoryImageResult> images = shopQueryDao.findAllPhotoCategoryImages();

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
                return ShopPhotoCategoryResponse.from(
                    category.name(),
                    imageUrls
                );
            })
            .toList();
    }

    public ShopReviewsByRatingPageResponse getShopReviewsByRatingWithPagination(Long shopId, int page, int size, Boolean hasImage) {
        ReviewsByRatingResult result = reviewQueryService.findShopReviewsByRating(shopId, page, size, hasImage);

        Map<Integer, List<ShopReviewListItemResponse>> reviewsByRating = result.reviewsByRating().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(this::convertToShopReviewListItemResponse)
                    .toList()
            ));

        List<ShopReviewListItemResponse> allReviews = result.allReviews().stream()
            .map(this::convertToShopReviewListItemResponse)
            .toList();

        ShopReviewsByRatingResponse response = ShopReviewsByRatingResponse.from(
            reviewsByRating, allReviews,
            result.totalReviewCount()
        );

        return new ShopReviewsByRatingPageResponse(response, result.totalElements());
    }

    private ShopReviewListItemResponse convertToShopReviewListItemResponse(LatestReviewListItemResult dto) {
        return ShopReviewListItemResponse.from(
            dto.id(),
            dto.imageUrls(),
            dto.totalRating(),
            dto.content(),
            dto.memberId(),
            dto.memberNickname(),
            dto.memberProfileImageUrl(),
            dto.createdAt(),
            dto.productId(),
            dto.productName()
        );
    }

    public ShopReviewStatisticsResponse getShopReviewStatistics(Long shopId) {
        ShopReviewStatisticsResult statistics = reviewQueryService.findShopReviewStatistics(shopId);

        Shop shop = findVisibleShop(shopId);

        return ShopReviewStatisticsResponse.from(
            shop.getRating(),
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

    private ShopBusinessHourItem convertToBusinessHourItem(ShopBusinessHourResult businessHour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return ShopBusinessHourItem.from(
            businessHour.dayType().name(),
            businessHour.dayType().getDescription(),
            businessHour.openTime() != null ? businessHour.openTime().format(formatter) : null,
            businessHour.closeTime() != null ? businessHour.closeTime().format(formatter) : null,
            Boolean.TRUE.equals(businessHour.closed()),
            Boolean.TRUE.equals(businessHour.allDay())
        );
    }

    private ShopBreakTimeItem convertToBreakTimeItem(ShopBreakTimeResult breakTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return ShopBreakTimeItem.from(
            breakTime.dayType().name(),
            breakTime.dayType().getDescription(),
            breakTime.startTime() != null ? breakTime.startTime().format(formatter) : null,
            breakTime.endTime() != null ? breakTime.endTime().format(formatter) : null
        );
    }

    private ShopClosedDayItem convertToClosedDayItem(ShopClosedDayResult closedDay) {
        return ShopClosedDayItem.from(
            closedDay.closedDayType().name(),
            closedDay.closedDayType().getDescription()
        );
    }

    private ShopAmenityItem convertToAmenityItem(ShopAmenityWithCategoryResult dto) {
        return ShopAmenityItem.from(
            dto.amenity().name(),
            dto.displayName(),
            dto.activeIconUrl()
        );
    }

    private ShopBannerResponse convertToShopBannerResponse(ShopBannerImageResult image) {
        return ShopBannerResponse.from(
            image.id(),
            image.imageUrl(),
            image.sort()
        );
    }

    private ProductSummaryResponse convertToShopMenuResponse(ShopProductItemResult product) {
        return ProductSummaryResponse.from(
            product.id(),
            product.name(),
            product.imageUrl(),
            product.originalPrice(),
            product.discountPrice(),
            product.discountRate(),
            product.rating(),
            product.reviewCount(),
            product.representative(),
            product.spiciness()
        );
    }

    public ShopBookmarkResponse isBookmarked(Long shopId, Long memberId) {
        boolean isBookmarked = shopBookmarkRepository.existsByShopIdAndMemberId(shopId, MemberId.of(memberId));
        return ShopBookmarkResponse.from(isBookmarked);
    }

    public ShopOrderMethodResponse getShopOrderMethods(Long shopId) {
        findVisibleShop(shopId);
        List<ShopOrderMethodResult> shopOrderMethods = shopQueryDao.findOrderMethods(shopId);

        List<ShopOrderMethodItem> orderMethodItems =
            shopOrderMethods.stream()
                .map(som -> ShopOrderMethodItem.from(
                    som.orderMethod().name(),
                    som.orderMethod().getDisplayName()))
                .toList();

        return ShopOrderMethodResponse.from(orderMethodItems);
    }

    /**
     * 회원 노출용 가게 단건. 폐업·노출정지 가게는 조회되지 않아 딥링크 진입이 차단된다.
     */
    private Shop findVisibleShop(Long shopId) {
        return shopRepository.findVisibleById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

}
