package com.tastyhouse.webapplication.shop.service;

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

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ProductSimpleResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;
import com.tastyhouse.application.review.port.out.LatestReviewListItemResult;
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
import com.tastyhouse.application.shop.port.out.ShopOrderMethodResult;
import com.tastyhouse.application.shop.port.out.ShopPhoneNumberResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryImageResult;
import com.tastyhouse.application.shop.port.out.ShopPhotoCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;
import com.tastyhouse.application.shop.port.out.ShopVisibleDetailResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;
import com.tastyhouse.webapplication.product.response.ProductSummaryResponse;
import com.tastyhouse.webapplication.product.service.ProductQueryService;
import com.tastyhouse.webapplication.review.service.ReviewQueryService;
import com.tastyhouse.webapplication.shop.response.ScheduledOrderSlotItemResponse;
import com.tastyhouse.webapplication.shop.response.ScheduledOrderSlotsResponse;
import com.tastyhouse.webapplication.shop.response.ShopAmenityItem;
import com.tastyhouse.webapplication.shop.response.ShopAmenityListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopBannerResponse;
import com.tastyhouse.webapplication.shop.response.ShopBestListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopBookmarkResponse;
import com.tastyhouse.webapplication.shop.response.ShopBreakTimeItem;
import com.tastyhouse.webapplication.shop.response.ShopBusinessHourItem;
import com.tastyhouse.webapplication.shop.response.ShopClosedDayItem;
import com.tastyhouse.webapplication.shop.response.ShopDeliveryTipBreakdownItem;
import com.tastyhouse.webapplication.shop.response.ShopDeliveryTipDistanceItem;
import com.tastyhouse.webapplication.shop.response.ShopDeliveryTipRegionItem;
import com.tastyhouse.webapplication.shop.response.ShopDeliveryTipResponse;
import com.tastyhouse.webapplication.shop.response.ShopDeliveryTipScheduleItem;
import com.tastyhouse.webapplication.shop.response.ShopDeliveryTipTierItem;
import com.tastyhouse.webapplication.shop.response.ShopDetailResponse;
import com.tastyhouse.webapplication.shop.response.ShopEditorChoiceProductItem;
import com.tastyhouse.webapplication.shop.response.ShopEditorChoiceResponse;
import com.tastyhouse.webapplication.shop.response.ShopFoodTypeListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopInfoResponse;
import com.tastyhouse.webapplication.shop.response.ShopLatestListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopMapMarkerResponse;
import com.tastyhouse.webapplication.shop.response.ShopNoticeResponse;
import com.tastyhouse.webapplication.shop.response.ShopOrderMethodItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopOrderMethodResponse;
import com.tastyhouse.webapplication.shop.response.ShopPhoneNumberItem;
import com.tastyhouse.webapplication.shop.response.ShopPhotoCategoryResponse;
import com.tastyhouse.webapplication.shop.response.ShopPopularProductResponse;
import com.tastyhouse.webapplication.shop.response.ShopProductCategoryResponse;
import com.tastyhouse.webapplication.shop.response.ShopReviewListItemResponse;
import com.tastyhouse.webapplication.shop.response.ShopReviewStatisticsResponse;
import com.tastyhouse.webapplication.shop.response.ShopReviewsByRatingPageResponse;
import com.tastyhouse.webapplication.shop.response.ShopReviewsByRatingResponse;
import com.tastyhouse.webapplication.shop.response.ShopStationListItemResponse;
import com.tastyhouse.webapplication.shop.port.in.ShopDetailQueryUseCase;
import com.tastyhouse.webapplication.shop.port.in.ShopOrderInfoQueryUseCase;
import com.tastyhouse.webapplication.shop.port.in.ShopSearchQueryUseCase;

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
public class ShopQueryService implements ShopSearchQueryUseCase, ShopDetailQueryUseCase, ShopOrderInfoQueryUseCase {

    /** productCategoryId가 null인(미분류) 메뉴 묶음에 붙이는 표시용 카테고리명. */
    private static final String UNCATEGORIZED_CATEGORY_NAME = "미분류";

    private final ShopRepository shopRepository;
    private final MemberDeliveryAddressRepository memberDeliveryAddressRepository;
    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;
    private final ShopDeliveryTipRepository shopDeliveryTipRepository;
    private final ShopQueryPort shopQueryPort;
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
    public List<ShopMapMarkerResponse> searchMapMarkers(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude);
        BigDecimal lon = BigDecimal.valueOf(longitude);
        return shopSearchQueryPort.findNearbyShops(lat, lon).stream()
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

    /**
     * 회원 기본 배송지의 행정동 — 목록 노출 시 배달 불가 가게를 걸러내는 기준.
     *
     * <p>비로그인이거나, 배송지를 등록하지 않았거나, 등록한 배송지가 아직 행정동에 매칭되지 않았으면
     * {@code null}이다. 그 경우 필터를 걸지 않는다 — 좁힐 근거가 없는데 감추면 노출만 줄어든다.
     */
    private Long resolveDeliveryAdminDongId(Long memberId) {
        if (memberId == null) {
            return null;
        }

        // write 포트가 아니라 read 어댑터를 쓴다 — 표현 목적 조회이므로 CQRS read 측이 맞다.
        return memberDeliveryAddressQueryPort.findDefaultAdminDongId(MemberId.of(memberId)).orElse(null);
    }

    @Override
    public PaginationResponse<ShopBestListItemResponse> searchBestShops(Long memberId, int page, int size) {
        PageResult<BestShopItemResult> result =
            shopSearchQueryPort.findBestShops(resolveDeliveryAdminDongId(memberId), PageQuery.of(page, size));
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(BestShopItemResult::id).toList()
        );
        return PaginationResponse.from(result.map(dto -> convertToBestShopListItemResponse(dto, statusMap)));
    }

    @Override
    public PaginationResponse<ShopLatestListItemResponse> searchLatestShops(
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
        return PaginationResponse.from(result.map(dto -> convertToLatestShopListItemResponse(dto, statusMap)));
    }

    private Map<Long, ShopOperatingStatus> resolveOperatingStatuses(List<Long> shopIds) {
        return shopOperatingStatusService.findOperatingStatuses(shopIds, LocalDateTime.now());
    }

    private String operatingStatusName(Map<Long, ShopOperatingStatus> statusMap, Long shopId) {
        ShopOperatingStatus status = statusMap.get(shopId);
        return status == null ? null : status.name();
    }

    @Override
    public List<ShopEditorChoiceResponse> searchEditorChoices(int page, int size) {
        return shopChoiceQueryPort.findEditorChoices(PageQuery.of(page, size)).content().stream()
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

    @Override
    public List<ShopStationListItemResponse> searchAllStations() {
        return shopChoiceQueryPort.findAllStations().stream()
            .map(station -> ShopStationListItemResponse.from(station.id(), station.stationName()))
            .toList();
    }

    @Override
    public List<ShopFoodTypeListItemResponse> searchAllFoodTypes() {
        return shopQueryPort.findVisibleFoodTypeCategories().stream()
            .map(this::convertToFoodTypeListItemResponse)
            .toList();
    }

    @Override
    public List<ShopAmenityListItemResponse> searchAllAmenities() {
        return shopQueryPort.findVisibleAmenityCategories().stream()
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

    @Override
    public ShopDetailResponse getShopDetail(Long shopId) {
        ShopVisibleDetailResult shop = findVisibleShop(shopId);

        List<ShopPhoneNumberItem> phoneNumbers = shopQueryPort.findPhoneNumbers(shopId).stream()
            .map(this::convertToShopPhoneNumberItem)
            .toList();

        String trademarkImageUrl = shopQueryPort.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::trademarkImageUrl)
            .orElse(null);

        ShopOperatingStatusResult operatingStatus =
            shopOperatingStatusService.findOrderAvailability(shopId, LocalDateTime.now());
        OrderUnavailableReason unavailableReason = operatingStatus.unavailableReason();

        // 배달팁·공휴일은 시각 의존 값이라 이 응답에는 캐시를 두지 않는다(최소주문금액 최신화를 위해
        // 가게 상세 캐시를 제거한 선례와 같은 이유) — 범위 값 자체는 시각에 의존하지 않지만, 점주가
        // 설정을 바꾼 직후 상세 화면이 옛 금액을 보여주면 팝업의 확정 금액과 어긋난다.
        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryPort.findTipRange(shopId);

        return ShopDetailResponse.of(
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
    @Override
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
    @Override
    public ShopDeliveryTipResponse getShopDeliveryTip(
        Long shopId,
        Long memberId,
        Long deliveryAddressId,
        Integer orderAmount,
        String orderMethod
    ) {
        Shop shop = findVisibleShopAggregate(shopId);

        ShopDeliveryTipRangeResult tipRange = shopDeliveryTipQueryPort.findTipRange(shopId);
        ShopDeliveryTipSettingResult setting = shopDeliveryTipQueryPort.findSetting(shopId).orElse(null);

        // 구간 Result는 응답 매핑과 breakdown 문구(적용 구간의 하한 금액) 양쪽이 쓰므로 지역 변수로 잡는다.
        List<ShopDeliveryTipTierResult> tierResults = shopDeliveryTipQueryPort.findTiers(shopId);
        List<ShopDeliveryTipTierItem> tiers = tierResults.stream()
            .map(this::toShopDeliveryTipTierItem)
            .toList();
        List<ShopDeliveryTipRegionItem> regions = shopDeliveryTipQueryPort.findRegionTips(shopId).stream()
            .map(this::toShopDeliveryTipRegionItem)
            .toList();
        List<ShopDeliveryTipScheduleItem> schedules = shopDeliveryTipQueryPort.findScheduleTips(shopId).stream()
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
            shopDeliveryTipQueryPort.findHolidayTipAmount(shopId)
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

    @Override
    public ShopInfoResponse getShopInfo(Long shopId) {
        findVisibleShop(shopId);
        List<ShopBusinessHourResult> businessHours = shopQueryPort.findBusinessHours(shopId);
        List<ShopBreakTimeResult> breakTimes = shopQueryPort.findBreakTimes(shopId);
        List<ShopClosedDayResult> closedDays = shopQueryPort.findClosedDays(shopId);
        List<ShopAmenityWithCategoryResult> shopAmenities = shopQueryPort.findAmenitiesWithCategory(shopId);

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
        var ownerMessageHistory = shopQueryPort.findLatestOwnerMessage(shopId);
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
        var convenienceInfo = shopQueryPort.findConvenienceInfo(shopId);
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

    /**
     * 가게 상세에 노출되는 점주 공지 — {@code exposed = true AND hidden = false} 최대 1건.
     *
     * <p>공지가 없는 것은 에러가 아니라 {@code null}이다. 대부분의 가게에 공지가 없으므로 404를 쓰면
     * 프론트가 정상 상태를 에러로 처리하게 된다. 가게 자체가 없으면 {@code SHOP_NOT_FOUND}(404)다.
     */
    @Override
    public ShopNoticeResponse getShopNotice(Long shopId) {
        findVisibleShop(shopId);
        return shopNoticeQueryPort.findExposedNotice(shopId)
            .map(dto -> ShopNoticeResponse.of(dto.id(), dto.content(), dto.imageUrls(), dto.createdAt()))
            .orElse(null);
    }

    @Override
    public List<ShopBannerResponse> getShopBanners(Long shopId) {
        return shopQueryPort.findBannerImages(shopId).stream()
            .map(this::convertToShopBannerResponse)
            .toList();
    }

    /**
     * 카테고리가 없는(미분류) 메뉴도 손님 화면에 노출되도록, 노출 카테고리 묶음 뒤에
     * 미분류 묶음을 추가한다. 미분류 메뉴가 없으면 이 묶음 자체를 응답에 포함하지 않는다.
     */
    @Override
    public List<ShopProductCategoryResponse> getShopProducts(Long shopId) {
        List<ShopProductItemResult> shopProducts = productQueryService.findShopProducts(shopId);

        Map<Long, List<ShopProductItemResult>> productsByCategory = shopProducts.stream()
            .filter(product -> product.productCategoryId() != null)
            .collect(Collectors.groupingBy(ShopProductItemResult::productCategoryId));

        List<ShopProductItemResult> uncategorizedProducts = shopProducts.stream()
            .filter(product -> product.productCategoryId() == null)
            .toList();

        List<ShopProductCategoryResponse> categoryResponses = productQueryService.findShopProductCategories(shopId)
            .stream()
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
            .collect(Collectors.toCollection(ArrayList::new));

        if (!uncategorizedProducts.isEmpty()) {
            List<ProductSummaryResponse> uncategorizedMenuResponses = uncategorizedProducts.stream()
                .map(this::convertToShopMenuResponse)
                .toList();
            categoryResponses.add(ShopProductCategoryResponse.from(
                UNCATEGORIZED_CATEGORY_NAME,
                uncategorizedMenuResponses
            ));
        }

        return categoryResponses;
    }

    @Override
    public List<ShopPhotoCategoryResponse> getShopPhotos(Long shopId) {
        List<ShopPhotoCategoryResult> categories = shopQueryPort.findPhotoCategories(shopId);
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
                return ShopPhotoCategoryResponse.from(
                    category.name(),
                    imageUrls
                );
            })
            .toList();
    }

    @Override
    public ShopReviewsByRatingPageResponse getShopReviewsByRatingWithPagination(
        Long shopId,
        int page,
        int size,
        Boolean hasImage,
        String sortType
    ) {
        ReviewsByRatingResult result = reviewQueryService.findShopReviewsByRating(shopId, page, size, hasImage, sortType);

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
            dto.productName(),
            dto.ownerReplyContent(),
            dto.ownerReplyCreatedAt()
        );
    }

    @Override
    public ShopReviewStatisticsResponse getShopReviewStatistics(Long shopId) {
        ShopReviewStatisticsResult statistics = reviewQueryService.findShopReviewStatistics(shopId);

        ShopVisibleDetailResult shop = findVisibleShop(shopId);

        return ShopReviewStatisticsResponse.from(
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

    /**
     * 가게 상세 상단 "가장 인기 있는 메뉴" 그룹 — 최대 5건.
     *
     * <p>사장님 추천을 먼저 채우고 남는 자리를 최근 30일 완료 주문의 판매량 순으로 채운다. 그 조합
     * 규칙과 집계는 {@code ProductQueryDao#findPopularProducts}가 소유하고, 이 메서드는 응답 변환만
     * 담당한다.
     */
    @Override
    public List<ShopPopularProductResponse> getPopularProducts(Long shopId) {
        return productQueryService.findPopularProducts(shopId).stream()
            .map(this::convertToPopularProductResponse)
            .toList();
    }

    private ShopPopularProductResponse convertToPopularProductResponse(PopularProductItemResult product) {
        return ShopPopularProductResponse.from(
            product.id(),
            product.name(),
            product.imageUrl(),
            product.originalPrice(),
            product.discountPrice(),
            product.discountRate(),
            product.rating(),
            product.reviewCount(),
            product.representative(),
            product.spiciness(),
            product.salesQuantity()
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

    @Override
    public ShopBookmarkResponse isBookmarked(Long shopId, Long memberId) {
        boolean isBookmarked = shopQueryPort.existsBookmark(shopId, memberId);
        return ShopBookmarkResponse.from(isBookmarked);
    }

    @Override
    public ShopOrderMethodResponse getShopOrderMethods(Long shopId) {
        findVisibleShop(shopId);

        // 배정 목록은 표시 순서를 위해 query DAO에서, 주문가능 여부는 도메인 판정에서 얻는다.
        // 배정된 유형만 판정 대상이므로 두 목록의 키 집합이 같다.
        Map<OrderMethod, ShopOperatingStatusResult> availabilities =
            shopOperatingStatusService.findOrderMethodAvailabilities(shopId, LocalDateTime.now());

        List<ShopOrderMethodItemResponse> orderMethodItems = shopQueryPort.findOrderMethods(shopId).stream()
            .map(dto -> toShopOrderMethodItemResponse(dto, availabilities.get(dto.orderMethod())))
            .toList();

        return ShopOrderMethodResponse.from(orderMethodItems);
    }

    private ShopOrderMethodItemResponse toShopOrderMethodItemResponse(
        ShopOrderMethodResult dto,
        ShopOperatingStatusResult availability
    ) {
        OrderUnavailableReason reason = availability == null ? null : availability.unavailableReason();
        return ShopOrderMethodItemResponse.from(
            dto.orderMethod().name(),
            dto.orderMethod().getDisplayName(),
            availability != null && availability.isOpen(),
            reason == null ? null : reason.name(),
            reason == null ? null : reason.getDisplayName()
        );
    }

    /**
     * 회원 노출용 가게 단건. 폐업·노출정지 가게는 조회되지 않아 딥링크 진입이 차단된다.
     */
    private ShopVisibleDetailResult findVisibleShop(Long shopId) {
        return shopQueryPort.findVisibleDetailById(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    /**
     * 배달팁 계산용 가게 애그리거트 로드.
     *
     * <p>표현용 단건({@link #findVisibleShop})과 달리 도메인 서비스
     * ({@code ShopDeliveryTipCalculator})에 넘길 도메인 모델이 필요해 write 포트를 쓴다 — 표현 목적
     * 조회가 아니므로 읽기 포트로 이관하지 않는다(CQRS 규칙의 정당한 예외).
     */
    private Shop findVisibleShopAggregate(Long shopId) {
        return shopRepository.findVisibleById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

}
