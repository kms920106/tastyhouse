package com.tastyhouse.webapi.shop;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewsByRatingResult;
import com.tastyhouse.core.domain.review.application.dto.result.ShopReviewStatisticsResult;
import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.core.domain.shop.application.dto.result.BestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceDto;
import com.tastyhouse.core.domain.shop.application.dto.result.LatestShopItemDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopAmenityWithCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopBannerImageDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopFoodTypeCategoryDto;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopPhotoCategoryImageDto;
import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopBreakTime;
import com.tastyhouse.core.domain.shop.domain.model.ShopBusinessHour;
import com.tastyhouse.core.domain.shop.domain.model.ShopClosedDay;
import com.tastyhouse.core.domain.shop.domain.model.ShopOrderMethod;
import com.tastyhouse.core.domain.shop.domain.model.ShopPhotoCategory;
import com.tastyhouse.core.domain.shop.domain.model.Station;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.shop.request.LatestShopFilterRequest;
import com.tastyhouse.webapi.shop.response.AmenityListItemResponse;
import com.tastyhouse.webapi.shop.response.BestShopListItemResponse;
import com.tastyhouse.webapi.shop.response.EditorChoiceProductItem;
import com.tastyhouse.webapi.shop.response.EditorChoiceResponse;
import com.tastyhouse.webapi.shop.response.FoodTypeListItemResponse;
import com.tastyhouse.webapi.shop.response.LatestShopListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopBannerResponse;
import com.tastyhouse.webapi.shop.response.ShopBookmarkResponse;
import com.tastyhouse.webapi.shop.response.ShopDetailResponse;
import com.tastyhouse.webapi.shop.response.ShopInfoResponse;
import com.tastyhouse.webapi.shop.response.ShopMapMarkerResponse;
import com.tastyhouse.webapi.shop.response.ShopOrderMethodResponse;
import com.tastyhouse.webapi.shop.response.ShopPhotoCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopProductCategoryResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewListItemResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewStatisticsResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingResponse;
import com.tastyhouse.webapi.shop.response.ShopReviewsByRatingWithPagination;
import com.tastyhouse.webapi.shop.response.StationListItemResponse;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopQueryService shopQueryService;
    private final ShopCommandService shopCommandService;
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
    public PageResult<BestShopListItemResponse> searchBestShops(int page, int size) {
        return shopQueryService.findBestShops(page, size).map(this::convertToBestShopListItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResult<LatestShopListItemResponse> searchLatestShops(LatestShopFilterRequest filterRequest, int page, int size) {
        return shopQueryService.findLatestShops(filterRequest.stationId(), filterRequest.foodTypes(), filterRequest.amenities(), page, size).map(this::convertToLatestShopListItemResponse);
    }

    @Transactional(readOnly = true)
    public List<EditorChoiceResponse> searchEditorChoices(int page, int size) {
        return shopQueryService.findEditorChoices(page, size).content().stream().map(this::convertToEditorChoiceResponse).toList();
    }

    private EditorChoiceResponse convertToEditorChoiceResponse(EditorChoiceDto dto) {
        List<EditorChoiceProductItem> productItems = dto.products() != null
            ? dto.products().stream().map(this::convertToEditorChoiceProductItem).toList()
            : new ArrayList<>();

        return EditorChoiceResponse.from(
            dto.id(),
            dto.name(),
            fileService.getUrlByPath(dto.shopImageUrl()),
            dto.title(),
            dto.content(),
            productItems
        );
    }

    private BestShopListItemResponse convertToBestShopListItemResponse(BestShopItemDto dto) {
        return BestShopListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.foodTypes()
        );
    }

    private LatestShopListItemResponse convertToLatestShopListItemResponse(LatestShopItemDto dto) {
        return LatestShopListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.createdAt(),
            dto.reviewCount(),
            dto.bookmarkCount(),
            dto.foodTypes()
        );
    }

    private EditorChoiceProductItem convertToEditorChoiceProductItem(ProductSimpleResult dto) {
        return EditorChoiceProductItem.from(
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
    public List<StationListItemResponse> searchAllStations() {
        List<Station> stations = shopQueryService.findAllStations();
        return stations.stream().map(this::convertToStationListItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodTypeListItemResponse> searchAllFoodTypes() {
        List<ShopFoodTypeCategoryDto> categories = shopQueryService.findAllFoodTypeCategories();
        return categories.stream()
                .map(this::convertToFoodTypeListItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AmenityListItemResponse> searchAllAmenities() {
        List<ShopAmenityCategoryDto> categories = shopQueryService.findAllAmenityCategories();
        return categories.stream()
                .map(this::convertToAmenityListItemResponse)
                .toList();
    }

    private StationListItemResponse convertToStationListItemResponse(Station station) {
        return StationListItemResponse.from(
            station.getId(),
            station.getStationName()
        );
    }

    private FoodTypeListItemResponse convertToFoodTypeListItemResponse(ShopFoodTypeCategoryDto category) {
        return FoodTypeListItemResponse.from(
            category.foodType().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    private AmenityListItemResponse convertToAmenityListItemResponse(ShopAmenityCategoryDto category) {
        return AmenityListItemResponse.from(
            category.amenity().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    @Transactional(readOnly = true)
    public ShopDetailResponse getShopDetail(Long shopId) {
        Shop shop = shopQueryService.findShopById(ShopId.of(shopId));
        return ShopDetailResponse.from(shop);
    }

    @Transactional(readOnly = true)
    public ShopInfoResponse getShopInfo(Long shopId) {
        shopQueryService.findShopById(ShopId.of(shopId));
        List<ShopBusinessHour> businessHours = shopQueryService.findShopBusinessHours(shopId);
        List<ShopBreakTime> breakTimes = shopQueryService.findShopBreakTimes(shopId);
        List<ShopClosedDay> closedDays = shopQueryService.findShopClosedDays(shopId);
        List<ShopAmenityWithCategoryDto> shopAmenities = shopQueryService.findShopAmenitiesWithCategory(shopId);

        List<ShopInfoResponse.BusinessHourItem> businessHourItems = businessHours.stream()
                .map(this::convertToBusinessHourItem)
                .toList();

        List<ShopInfoResponse.BreakTimeItem> breakTimeItems = breakTimes.stream()
                .map(this::convertToBreakTimeItem)
                .toList();

        List<ShopInfoResponse.ClosedDayItem> closedDayItems = closedDays.stream()
                .map(this::convertToClosedDayItem)
                .toList();

        List<ShopInfoResponse.AmenityItem> amenityItems = shopAmenities.stream()
                .map(this::convertToAmenityItem)
                .toList();

        String ownerMessage = null;
        java.time.LocalDateTime ownerMessageCreatedAt = null;
        var ownerMessageHistory = shopQueryService.findLatestOwnerMessage(shopId);
        if (ownerMessageHistory.isPresent()) {
            ownerMessage = ownerMessageHistory.get().getMessage();
            ownerMessageCreatedAt = ownerMessageHistory.get().getCreatedAt();
        }

        return ShopInfoResponse.from(
            closedDayItems,
            businessHourItems,
            breakTimeItems,
            amenityItems,
            ownerMessage,
            ownerMessageCreatedAt
        );
    }

    @Transactional(readOnly = true)
    public List<ShopBannerResponse> getShopBanners(Long shopId) {
        List<ShopBannerImageDto> banners = shopQueryService.findShopBannerImages(shopId);
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
        List<ShopPhotoCategoryImageDto> images = shopQueryService.findAllShopPhotoCategoryImages();

        Map<Long, List<ShopPhotoCategoryImageDto>> imagesByCategory = images.stream()
                .filter(image -> image.shopPhotoCategoryId() != null)
                .collect(Collectors.groupingBy(ShopPhotoCategoryImageDto::shopPhotoCategoryId));

        return categories.stream()
                .map(category -> {
                    List<ShopPhotoCategoryImageDto> categoryImages = imagesByCategory.getOrDefault(category.getId(), new ArrayList<>());
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
    public ShopReviewsByRatingWithPagination getShopReviewsByRatingWithPagination(Long shopId, int page, int size, Boolean hasImage) {
        ReviewsByRatingResult result = reviewQueryService.findShopReviewsByRating(shopId, page, size, hasImage);

        Map<Integer, List<ShopReviewListItemResponse>> reviewsByRating = result.getReviewsByRating().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToShopReviewListItemResponse)
                                .toList()
                ));

        List<ShopReviewListItemResponse> allReviews = result.getAllReviews().stream()
                .map(this::convertToShopReviewListItemResponse)
                .toList();

        ShopReviewsByRatingResponse response = ShopReviewsByRatingResponse.from(
            reviewsByRating, allReviews,
            result.getTotalReviewCount()
        );

        return new ShopReviewsByRatingWithPagination(response, result.getTotalElements());
    }

    private ShopReviewListItemResponse convertToShopReviewListItemResponse(LatestReviewListItemResult dto) {
        List<String> imageUrls = dto.imageUrls().stream().map(fileService::getUrlByPath).toList();

        return ShopReviewListItemResponse.from(
            dto.id(),
            imageUrls,
            dto.totalRating(),
            dto.content(),
            dto.memberId(),
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

        Shop shop = shopQueryService.findShopById(ShopId.of(shopId));

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

    private ShopInfoResponse.BusinessHourItem convertToBusinessHourItem(ShopBusinessHour businessHour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return ShopInfoResponse.BusinessHourItem.from(
            businessHour.getDayType().name(),
            businessHour.getDayType().getDescription(),
            businessHour.getOpenTime() != null ? businessHour.getOpenTime().format(formatter) : null,
            businessHour.getCloseTime() != null ? businessHour.getCloseTime().format(formatter) : null,
            businessHour.getIsClosed()
        );
    }

    private ShopInfoResponse.BreakTimeItem convertToBreakTimeItem(ShopBreakTime breakTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return ShopInfoResponse.BreakTimeItem.from(
            breakTime.getDayType().name(),
            breakTime.getDayType().getDescription(),
            breakTime.getStartTime() != null ? breakTime.getStartTime().format(formatter) : null,
            breakTime.getEndTime() != null ? breakTime.getEndTime().format(formatter) : null
        );
    }

    private ShopInfoResponse.ClosedDayItem convertToClosedDayItem(ShopClosedDay closedDay) {
        return ShopInfoResponse.ClosedDayItem.from(
            closedDay.getClosedDayType().name(),
            closedDay.getClosedDayType().getDescription()
        );
    }

    private ShopInfoResponse.AmenityItem convertToAmenityItem(ShopAmenityWithCategoryDto dto) {
        return ShopInfoResponse.AmenityItem.from(
            dto.amenity().name(),
            dto.displayName(),
            fileService.getUrlByPath(dto.activeFilePath())
        );
    }

    private ShopBannerResponse convertToShopBannerResponse(ShopBannerImageDto image) {
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
        boolean isBookmarked = shopQueryService.isBookmarked(shopId, memberId);
        return ShopBookmarkResponse.from(isBookmarked);
    }

    @Transactional
    public boolean toggleBookmark(Long shopId, Long memberId) {
        return shopCommandService.toggleBookmark(shopId, memberId);
    }

    @Transactional(readOnly = true)
    public ShopOrderMethodResponse getShopOrderMethods(Long shopId) {
        shopQueryService.findShopById(ShopId.of(shopId));
        List<ShopOrderMethod> shopOrderMethods = shopQueryService.findShopOrderMethods(shopId);

        List<ShopOrderMethodResponse.OrderMethodItem> orderMethodItems =
            shopOrderMethods.stream()
                .map(som -> ShopOrderMethodResponse.OrderMethodItem.from(
                    som.getOrderMethod().name(),
                    som.getOrderMethod().getDisplayName()))
                .toList();

        return ShopOrderMethodResponse.from(orderMethodItems);
    }
}
