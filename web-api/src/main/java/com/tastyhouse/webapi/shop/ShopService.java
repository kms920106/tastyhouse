package com.tastyhouse.webapi.shop;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.shop.domain.model.Amenity;
import com.tastyhouse.core.domain.shop.domain.model.FoodType;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOperatingStatus;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.core.domain.shop.application.ShopConvenienceInfoQueryService;
import com.tastyhouse.core.domain.shop.application.ShopOperatingStatusQueryService;
import com.tastyhouse.core.domain.shop.application.ShopPhoneNumberQueryService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceResult;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopConvenienceInfoResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhoneNumberResult;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageResult;
import com.tastyhouse.infrastructure.review.query.LatestReviewListItemResult;
import com.tastyhouse.infrastructure.review.query.ReviewsByRatingResult;
import com.tastyhouse.infrastructure.review.query.ShopReviewStatisticsResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.webapi.file.FileService;
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

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopQueryService shopQueryService;
    private final ShopCommandService shopCommandService;
    private final ShopPhoneNumberQueryService shopPhoneNumberQueryService;
    private final ShopConvenienceInfoQueryService shopConvenienceInfoQueryService;
    private final ShopOperatingStatusQueryService shopOperatingStatusQueryService;
    private final ProductQueryService productQueryService;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<ShopMapMarkerResponse> searchMapMarkers(Double latitude, Double longitude) {
        return shopQueryService.findNearbyShops(latitude, longitude).stream()
                .map(shop -> ShopMapMarkerResponse.from(
                    shop.getId(),
                    shop.getLatitude(),
                    shop.getLongitude(),
                    shop.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<ShopBestListItemResponse> searchBestShops(int page, int size) {
        PageResult<BestShopItemResult> result = shopQueryService.findBestShops(page, size);
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(BestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToBestShopListItemResponse(dto, statusMap));
    }

    @Transactional(readOnly = true)
    public PageResult<ShopLatestListItemResponse> searchLatestShops(Long stationId, List<String> foodTypes, List<String> amenities, int page, int size) {
        List<FoodType> foodTypeFilters = foodTypes == null ? null : foodTypes.stream().map(FoodType::from).toList();
        List<Amenity> amenityFilters = amenities == null ? null : amenities.stream().map(Amenity::from).toList();
        PageResult<LatestShopItemResult> result = shopQueryService.findLatestShops(stationId, foodTypeFilters, amenityFilters, page, size);
        Map<Long, ShopOperatingStatus> statusMap = resolveOperatingStatuses(
            result.content().stream().map(LatestShopItemResult::id).toList()
        );
        return result.map(dto -> convertToLatestShopListItemResponse(dto, statusMap));
    }

    private Map<Long, ShopOperatingStatus> resolveOperatingStatuses(List<Long> shopIds) {
        return shopOperatingStatusQueryService.findOperatingStatuses(shopIds, LocalDateTime.now());
    }

    private String operatingStatusName(Map<Long, ShopOperatingStatus> statusMap, Long shopId) {
        ShopOperatingStatus status = statusMap.get(shopId);
        return status == null ? null : status.name();
    }

    @Transactional(readOnly = true)
    public List<ShopEditorChoiceResponse> searchEditorChoices(int page, int size) {
        return shopQueryService.findEditorChoices(page, size).content().stream().map(this::convertToEditorChoiceResponse).toList();
    }

    private ShopEditorChoiceResponse convertToEditorChoiceResponse(EditorChoiceResult dto) {
        List<ShopEditorChoiceProductItem> productItems = dto.products() != null
            ? dto.products().stream().map(this::convertToEditorChoiceProductItem).toList()
            : new ArrayList<>();

        return ShopEditorChoiceResponse.from(
            dto.id(),
            dto.name(),
            fileService.getUrlByPath(dto.shopImageUrl()),
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
            fileService.getUrlByPath(dto.imageUrl()),
            dto.foodTypes().stream().map(Enum::name).toList(),
            operatingStatusName(statusMap, dto.id())
        );
    }

    private ShopLatestListItemResponse convertToLatestShopListItemResponse(LatestShopItemResult dto, Map<Long, ShopOperatingStatus> statusMap) {
        return ShopLatestListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.createdAt(),
            dto.reviewCount(),
            dto.bookmarkCount(),
            dto.foodTypes().stream().map(Enum::name).toList(),
            operatingStatusName(statusMap, dto.id())
        );
    }

    private ShopEditorChoiceProductItem convertToEditorChoiceProductItem(ProductSimpleResult dto) {
        return ShopEditorChoiceProductItem.from(
            dto.id(),
            dto.shopName(),
            dto.name(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate()
        );
    }

    @Transactional(readOnly = true)
    public List<ShopStationListItemResponse> searchAllStations() {
        List<Station> stations = shopQueryService.findAllStations();
        return stations.stream().map(this::convertToStationListItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ShopFoodTypeListItemResponse> searchAllFoodTypes() {
        List<ShopFoodTypeCategoryResult> categories = shopQueryService.findAllFoodTypeCategories();
        return categories.stream()
                .map(this::convertToFoodTypeListItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShopAmenityListItemResponse> searchAllAmenities() {
        List<ShopAmenityCategoryResult> categories = shopQueryService.findAllAmenityCategories();
        return categories.stream()
                .map(this::convertToAmenityListItemResponse)
                .toList();
    }

    private ShopStationListItemResponse convertToStationListItemResponse(Station station) {
        return ShopStationListItemResponse.from(
            station.getId(),
            station.getStationName()
        );
    }

    private ShopFoodTypeListItemResponse convertToFoodTypeListItemResponse(ShopFoodTypeCategoryResult category) {
        return ShopFoodTypeListItemResponse.from(
            category.foodType().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    private ShopAmenityListItemResponse convertToAmenityListItemResponse(ShopAmenityCategoryResult category) {
        return ShopAmenityListItemResponse.from(
            category.amenity().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    @Transactional(readOnly = true)
    public ShopDetailResponse getShopDetail(Long shopId) {
        Shop shop = shopQueryService.findVisibleShopById(ShopId.of(shopId));

        List<ShopPhoneNumberItem> phoneNumbers = shopPhoneNumberQueryService.findPhoneNumbers(shopId).stream()
            .map(this::convertToShopPhoneNumberItem)
            .toList();

        String trademarkImageUrl = fileService.getUrlByPath(
            shopQueryService.findThumbnailFilePath(shop.getTrademarkImageFileId()).orElse(null)
        );

        String operatingStatus = shopOperatingStatusQueryService
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
            operatingStatus
        );
    }

    private ShopPhoneNumberItem convertToShopPhoneNumberItem(ShopPhoneNumberResult dto) {
        return ShopPhoneNumberItem.from(
            dto.phoneNumber(),
            dto.primary(),
            dto.virtual()
        );
    }

    @Transactional(readOnly = true)
    public ShopInfoResponse getShopInfo(Long shopId) {
        shopQueryService.findVisibleShopById(ShopId.of(shopId));
        List<ShopBusinessHour> businessHours = shopQueryService.findShopBusinessHours(shopId);
        List<ShopBreakTime> breakTimes = shopQueryService.findShopBreakTimes(shopId);
        List<ShopClosedDay> closedDays = shopQueryService.findShopClosedDays(shopId);
        List<ShopAmenityWithCategoryResult> shopAmenities = shopQueryService.findShopAmenitiesWithCategory(shopId);

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
        var ownerMessageHistory = shopQueryService.findLatestOwnerMessage(shopId);
        if (ownerMessageHistory.isPresent()) {
            ownerMessage = ownerMessageHistory.get().getMessage();
            ownerMessageCreatedAt = ownerMessageHistory.get().getCreatedAt();
        }

        Boolean parkingAvailable = null;
        Boolean parkingPaid = null;
        Boolean valetAvailable = null;
        Boolean valetPaid = null;
        String directionsGuide = null;
        BigDecimal displayLatitude = null;
        BigDecimal displayLongitude = null;
        var convenienceInfo = shopConvenienceInfoQueryService.findConvenienceInfo(shopId);
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

    @Transactional(readOnly = true)
    public List<ShopBannerResponse> getShopBanners(Long shopId) {
        List<ShopBannerImageResult> banners = shopQueryService.findShopBannerImages(shopId);
        return banners.stream()
                .map(this::convertToShopBannerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShopProductCategoryResponse> getShopProducts(Long shopId) {
        List<ProductCategory> categories = productQueryService.findProductCategoriesByShopId(shopId);
        List<Product> products = productQueryService.findActiveProductsByShopId(shopId);

        Map<Long, List<Product>> productsByCategory = products.stream()
                .filter(product -> product.getProductCategoryId() != null)
                .collect(Collectors.groupingBy(Product::getProductCategoryId));

        return categories.stream()
                .map(category -> {
                    List<Product> categoryProducts = productsByCategory.getOrDefault(category.getId(), new ArrayList<>());
                    List<ProductSummaryResponse> menuResponses = categoryProducts.stream()
                            .map(this::convertToShopMenuResponse)
                            .toList();
                    return ShopProductCategoryResponse.from(
                        category.getName(),
                        menuResponses
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShopPhotoCategoryResponse> getShopPhotos(Long shopId) {
        List<ShopPhotoCategory> categories = shopQueryService.findShopPhotoCategoriesByShopId(shopId);
        List<ShopPhotoCategoryImageResult> images = shopQueryService.findAllShopPhotoCategoryImages();

        Map<Long, List<ShopPhotoCategoryImageResult>> imagesByCategory = images.stream()
                .filter(image -> image.shopPhotoCategoryId() != null)
                .collect(Collectors.groupingBy(ShopPhotoCategoryImageResult::shopPhotoCategoryId));

        return categories.stream()
                .map(category -> {
                    List<ShopPhotoCategoryImageResult> categoryImages = imagesByCategory.getOrDefault(category.getId(), new ArrayList<>());
                    List<String> imageUrls = categoryImages.stream()
                            .map(image -> fileService.getUrlByPath(image.filePath()))
                            .toList();
                    return ShopPhotoCategoryResponse.from(
                        category.getName(),
                        imageUrls
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
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
        List<String> imageUrls = dto.imageUrls().stream().map(fileService::getUrlByPath).toList();

        return ShopReviewListItemResponse.from(
            dto.id(),
            imageUrls,
            dto.totalRating(),
            dto.content(),
            dto.memberId().value(),
            dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(),
            dto.productId(),
            dto.productName()
        );
    }

    @Transactional(readOnly = true)
    public ShopReviewStatisticsResponse getShopReviewStatistics(Long shopId) {
        ShopReviewStatisticsResult statistics = reviewQueryService.findShopReviewStatistics(shopId);

        Shop shop = shopQueryService.findVisibleShopById(ShopId.of(shopId));

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

    private ShopBusinessHourItem convertToBusinessHourItem(ShopBusinessHour businessHour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return ShopBusinessHourItem.from(
            businessHour.getDayType().name(),
            businessHour.getDayType().getDescription(),
            businessHour.getOpenTime() != null ? businessHour.getOpenTime().format(formatter) : null,
            businessHour.getCloseTime() != null ? businessHour.getCloseTime().format(formatter) : null,
            Boolean.TRUE.equals(businessHour.getIsClosed()),
            Boolean.TRUE.equals(businessHour.getIs24Hours())
        );
    }

    private ShopBreakTimeItem convertToBreakTimeItem(ShopBreakTime breakTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return ShopBreakTimeItem.from(
            breakTime.getDayType().name(),
            breakTime.getDayType().getDescription(),
            breakTime.getStartTime() != null ? breakTime.getStartTime().format(formatter) : null,
            breakTime.getEndTime() != null ? breakTime.getEndTime().format(formatter) : null
        );
    }

    private ShopClosedDayItem convertToClosedDayItem(ShopClosedDay closedDay) {
        return ShopClosedDayItem.from(
            closedDay.getClosedDayType().name(),
            closedDay.getClosedDayType().getDescription()
        );
    }

    private ShopAmenityItem convertToAmenityItem(ShopAmenityWithCategoryResult dto) {
        return ShopAmenityItem.from(
            dto.amenity().name(),
            dto.displayName(),
            fileService.getUrlByPath(dto.activeFilePath())
        );
    }

    private ShopBannerResponse convertToShopBannerResponse(ShopBannerImageResult image) {
        return ShopBannerResponse.from(
            image.id(),
            fileService.getUrlByPath(image.filePath()),
            image.sort()
        );
    }

    private ProductSummaryResponse convertToShopMenuResponse(Product product) {
        String imageUrl = getFirstImageUrl(product.getId());

        return ProductSummaryResponse.from(
            product.getId(),
            product.getName(),
            fileService.getUrlByPath(imageUrl),
            product.getOriginalPrice(),
            product.getDiscountPrice(),
            product.getDiscountRate(),
            product.getRating(),
            product.getReviewCount(),
            product.isRepresentative(),
            product.getSpiciness()
        );
    }

    private String getFirstImageUrl(Long productId) {
        return productQueryService.getFirstImageFilePath(productId);
    }

    @Transactional(readOnly = true)
    public ShopBookmarkResponse isBookmarked(Long shopId, Long memberId) {
        boolean isBookmarked = shopQueryService.isBookmarked(shopId, MemberId.of(memberId));
        return ShopBookmarkResponse.from(isBookmarked);
    }

    @Transactional
    public boolean toggleBookmark(Long shopId, Long memberId) {
        MemberId targetMemberId = MemberId.of(memberId);
        return shopCommandService.toggleBookmark(shopId, targetMemberId);
    }

    @Transactional(readOnly = true)
    public ShopOrderMethodResponse getShopOrderMethods(Long shopId) {
        shopQueryService.findVisibleShopById(ShopId.of(shopId));
        List<ShopOrderMethod> shopOrderMethods = shopQueryService.findShopOrderMethods(shopId);

        List<ShopOrderMethodItem> orderMethodItems =
            shopOrderMethods.stream()
                .map(som -> ShopOrderMethodItem.from(
                    som.getOrderMethod().name(),
                    som.getOrderMethod().getDisplayName()))
                .toList();

        return ShopOrderMethodResponse.from(orderMethodItems);
    }
}
