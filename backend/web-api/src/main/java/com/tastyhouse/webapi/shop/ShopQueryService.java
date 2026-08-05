package com.tastyhouse.webapi.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.Amenity;
import com.tastyhouse.domain.shop.model.FoodType;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.model.ShopOperatingStatus;
import com.tastyhouse.domain.shop.repository.ShopBookmarkRepository;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.service.ShopOperatingStatusService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
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
import com.tastyhouse.webapi.shop.response.ShopAmenityItem;
import com.tastyhouse.webapi.shop.response.ShopAmenityListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBannerResponse;
import com.tastyhouse.webapi.shop.response.ShopBestListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBookmarkResponse;
import com.tastyhouse.webapi.shop.response.ShopBreakTimeItem;
import com.tastyhouse.webapi.shop.response.ShopBusinessHourItem;
import com.tastyhouse.webapi.shop.response.ShopClosedDayItem;
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
    private final ShopQueryDao shopQueryDao;
    private final ShopSearchQueryDao shopSearchQueryDao;
    private final ShopChoiceQueryDao shopChoiceQueryDao;
    private final ShopOperatingStatusService shopOperatingStatusService;
    private final ProductQueryService productQueryService;
    private final ReviewQueryService reviewQueryService;

    public ShopQueryService(
        ShopRepository shopRepository,
        ShopBookmarkRepository shopBookmarkRepository,
        ShopQueryDao shopQueryDao,
        ShopSearchQueryDao shopSearchQueryDao,
        ShopChoiceQueryDao shopChoiceQueryDao,
        ShopOperatingStatusService shopOperatingStatusService,
        ProductQueryService productQueryService,
        ReviewQueryService reviewQueryService
    ) {
        this.shopRepository = shopRepository;
        this.shopBookmarkRepository = shopBookmarkRepository;
        this.shopQueryDao = shopQueryDao;
        this.shopSearchQueryDao = shopSearchQueryDao;
        this.shopChoiceQueryDao = shopChoiceQueryDao;
        this.shopOperatingStatusService = shopOperatingStatusService;
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
            dto.minOrderAmount()
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
            dto.minOrderAmount()
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
            shop.getMinOrderAmount()
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
            dto.memberId().value(),
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
