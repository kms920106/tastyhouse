package com.tastyhouse.webapi.place;

import com.tastyhouse.core.domain.place.application.dto.result.BestPlaceItemDto;
import com.tastyhouse.core.domain.place.application.dto.result.EditorChoiceDto;
import com.tastyhouse.core.domain.place.application.dto.result.LatestPlaceItemDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceAmenityCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceAmenityWithCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceBannerImageDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlaceFoodTypeCategoryDto;
import com.tastyhouse.core.domain.place.application.dto.result.PlacePhotoCategoryImageDto;
import com.tastyhouse.core.domain.place.domain.model.Place;
import com.tastyhouse.core.domain.place.domain.model.PlaceBreakTime;
import com.tastyhouse.core.domain.place.domain.model.PlaceBusinessHour;
import com.tastyhouse.core.domain.place.domain.model.PlaceClosedDay;
import com.tastyhouse.core.domain.place.domain.model.PlaceOrderMethod;
import com.tastyhouse.core.domain.place.domain.model.PlacePhotoCategory;
import com.tastyhouse.core.domain.place.domain.model.PlaceStation;
import com.tastyhouse.core.domain.product.application.ProductQueryService;
import com.tastyhouse.core.domain.product.application.dto.result.ProductSimpleResult;
import com.tastyhouse.core.domain.product.domain.model.Product;
import com.tastyhouse.core.domain.product.domain.model.ProductCategory;
import com.tastyhouse.core.domain.review.application.ReviewQueryService;
import com.tastyhouse.core.domain.review.application.dto.result.LatestReviewListItemResult;
import com.tastyhouse.core.domain.review.application.dto.result.PlaceReviewStatisticsResult;
import com.tastyhouse.core.domain.review.application.dto.result.ReviewsByRatingResult;
import com.tastyhouse.webapi.common.PageResponse;
import com.tastyhouse.core.domain.place.application.PlaceCommandService;
import com.tastyhouse.core.domain.place.application.PlaceQueryService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.place.request.LatestPlaceFilterRequest;
import com.tastyhouse.webapi.place.response.AmenityListItemResponse;
import com.tastyhouse.webapi.place.response.BestPlaceListItemResponse;
import com.tastyhouse.webapi.place.response.EditorChoiceProductItem;
import com.tastyhouse.webapi.place.response.EditorChoiceResponse;
import com.tastyhouse.webapi.place.response.FoodTypeListItemResponse;
import com.tastyhouse.webapi.place.response.LatestPlaceListItemResponse;
import com.tastyhouse.webapi.place.response.PlaceBannerResponse;
import com.tastyhouse.webapi.place.response.PlaceBookmarkResponse;
import com.tastyhouse.webapi.place.response.PlaceInfoResponse;
import com.tastyhouse.webapi.place.response.PlaceMapMarkerResponse;
import com.tastyhouse.webapi.place.response.PlaceProductCategoryResponse;
import com.tastyhouse.webapi.product.response.ProductSummaryResponse;
import com.tastyhouse.webapi.place.response.PlaceOrderMethodResponse;
import com.tastyhouse.webapi.place.response.PlacePhotoCategoryResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewListItemResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewStatisticsResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewsByRatingResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewsByRatingWithPagination;
import com.tastyhouse.webapi.place.response.PlaceDetailResponse;
import com.tastyhouse.webapi.place.response.StationListItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    private final PlaceQueryService placeQueryService;
    private final PlaceCommandService placeCommandService;
    private final ProductQueryService productQueryService;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<PlaceMapMarkerResponse> searchMapMarkers(Double latitude, Double longitude) {
        return placeQueryService.findNearbyPlaces(latitude, longitude).stream()
                .map(place -> PlaceMapMarkerResponse.from(
                    place.getId(),
                    place.getLatitude(),
                    place.getLongitude(),
                    place.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<BestPlaceListItemResponse> searchBestPlaces(int page, int size) {
        return PageResponse.from(placeQueryService.findBestPlaces(page, size)).map(this::convertToBestPlaceListItemResponse);
    }

    @Transactional(readOnly = true)
    public PageResponse<LatestPlaceListItemResponse> searchLatestPlaces(LatestPlaceFilterRequest filterRequest, int page, int size) {
        return PageResponse.from(placeQueryService.findLatestPlaces(filterRequest.stationId(), filterRequest.foodTypes(), filterRequest.amenities(), page, size)).map(this::convertToLatestPlaceListItemResponse);
    }

    @Transactional(readOnly = true)
    public List<EditorChoiceResponse> searchEditorChoices(int page, int size) {
        return placeQueryService.findEditorChoices(page, size).getContent().stream().map(this::convertToEditorChoiceResponse).toList();
    }

    private EditorChoiceResponse convertToEditorChoiceResponse(EditorChoiceDto dto) {
        List<EditorChoiceProductItem> productItems = dto.products() != null
            ? dto.products().stream().map(this::convertToEditorChoiceProductItem).toList()
            : new ArrayList<>();

        return EditorChoiceResponse.from(
            dto.id(),
            dto.name(),
            fileService.getUrlByPath(dto.placeImageUrl()),
            dto.title(),
            dto.content(),
            productItems
        );
    }

    private BestPlaceListItemResponse convertToBestPlaceListItemResponse(BestPlaceItemDto dto) {
        return BestPlaceListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.foodTypes()
        );
    }

    private LatestPlaceListItemResponse convertToLatestPlaceListItemResponse(LatestPlaceItemDto dto) {
        return LatestPlaceListItemResponse.from(
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
            dto.placeName(),
            dto.name(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate()
        );
    }

    @Transactional(readOnly = true)
    public List<StationListItemResponse> searchAllStations() {
        List<PlaceStation> stations = placeQueryService.findAllStations();
        return stations.stream().map(this::convertToStationListItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodTypeListItemResponse> searchAllFoodTypes() {
        List<PlaceFoodTypeCategoryDto> categories = placeQueryService.findAllFoodTypeCategories();
        return categories.stream()
                .map(this::convertToFoodTypeListItemResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AmenityListItemResponse> searchAllAmenities() {
        List<PlaceAmenityCategoryDto> categories = placeQueryService.findAllAmenityCategories();
        return categories.stream()
                .map(this::convertToAmenityListItemResponse)
                .toList();
    }

    private StationListItemResponse convertToStationListItemResponse(PlaceStation station) {
        return StationListItemResponse.from(
            station.getId(),
            station.getStationName()
        );
    }

    private FoodTypeListItemResponse convertToFoodTypeListItemResponse(PlaceFoodTypeCategoryDto category) {
        return FoodTypeListItemResponse.from(
            category.foodType().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    private AmenityListItemResponse convertToAmenityListItemResponse(PlaceAmenityCategoryDto category) {
        return AmenityListItemResponse.from(
            category.amenity().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    @Transactional(readOnly = true)
    public PlaceDetailResponse getPlaceDetail(Long placeId) {
        Place place = placeQueryService.findPlaceById(placeId);
        return PlaceDetailResponse.from(place);
    }

    @Transactional(readOnly = true)
    public PlaceInfoResponse getPlaceInfo(Long placeId) {
        placeQueryService.findPlaceById(placeId);
        List<PlaceBusinessHour> businessHours = placeQueryService.findPlaceBusinessHours(placeId);
        List<PlaceBreakTime> breakTimes = placeQueryService.findPlaceBreakTimes(placeId);
        List<PlaceClosedDay> closedDays = placeQueryService.findPlaceClosedDays(placeId);
        List<PlaceAmenityWithCategoryDto> placeAmenities = placeQueryService.findPlaceAmenitiesWithCategory(placeId);

        List<PlaceInfoResponse.BusinessHourItem> businessHourItems = businessHours.stream()
                .map(this::convertToBusinessHourItem)
                .toList();

        List<PlaceInfoResponse.BreakTimeItem> breakTimeItems = breakTimes.stream()
                .map(this::convertToBreakTimeItem)
                .toList();

        List<PlaceInfoResponse.ClosedDayItem> closedDayItems = closedDays.stream()
                .map(this::convertToClosedDayItem)
                .toList();

        List<PlaceInfoResponse.AmenityItem> amenityItems = placeAmenities.stream()
                .map(this::convertToAmenityItem)
                .toList();

        String ownerMessage = null;
        java.time.LocalDateTime ownerMessageCreatedAt = null;
        var ownerMessageHistory = placeQueryService.findLatestOwnerMessage(placeId);
        if (ownerMessageHistory.isPresent()) {
            ownerMessage = ownerMessageHistory.get().getMessage();
            ownerMessageCreatedAt = ownerMessageHistory.get().getCreatedAt();
        }

        return PlaceInfoResponse.from(
            closedDayItems,
            businessHourItems,
            breakTimeItems,
            amenityItems,
            ownerMessage,
            ownerMessageCreatedAt
        );
    }

    @Transactional(readOnly = true)
    public List<PlaceBannerResponse> getPlaceBanners(Long placeId) {
        List<PlaceBannerImageDto> banners = placeQueryService.findPlaceBannerImages(placeId);
        return banners.stream()
                .map(this::convertToPlaceBannerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlaceProductCategoryResponse> getPlaceProducts(Long placeId) {
        List<ProductCategory> categories = productQueryService.findProductCategoriesByPlaceId(placeId);
        List<Product> products = productQueryService.findActiveProductsByPlaceId(placeId);

        // 카테고리 ID별로 Product 그룹화
        Map<Long, List<Product>> productsByCategory = products.stream()
                .filter(product -> product.getProductCategoryId() != null)
                .collect(Collectors.groupingBy(Product::getProductCategoryId));

        // 카테고리 순서대로 응답 생성
        return categories.stream()
                .map(category -> {
                    List<Product> categoryProducts = productsByCategory.getOrDefault(category.getId(), new ArrayList<>());
                    List<ProductSummaryResponse> menuResponses = categoryProducts.stream()
                            .map(this::convertToPlaceMenuResponse)
                            .toList();
                    return PlaceProductCategoryResponse.from(
                        category.getName(),
                        menuResponses
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlacePhotoCategoryResponse> getPlacePhotos(Long placeId) {
        List<PlacePhotoCategory> categories = placeQueryService.findPlacePhotoCategoriesByPlaceId(placeId);
        List<PlacePhotoCategoryImageDto> images = placeQueryService.findAllPlacePhotoCategoryImages();

        Map<Long, List<PlacePhotoCategoryImageDto>> imagesByCategory = images.stream()
                .filter(image -> image.placePhotoCategoryId() != null)
                .collect(Collectors.groupingBy(PlacePhotoCategoryImageDto::placePhotoCategoryId));

        return categories.stream()
                .map(category -> {
                    List<PlacePhotoCategoryImageDto> categoryImages = imagesByCategory.getOrDefault(category.getId(), new ArrayList<>());
                    List<String> imageUrls = categoryImages.stream()
                            .map(image -> fileService.getUrlByPath(image.filePath()))
                            .toList();
                    return PlacePhotoCategoryResponse.from(
                        category.getName(),
                        imageUrls
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public PlaceReviewsByRatingWithPagination getPlaceReviewsByRatingWithPagination(Long placeId, int page, int size) {
        ReviewsByRatingResult result = reviewQueryService.findPlaceReviewsByRating(placeId, page, size);

        Map<Integer, List<PlaceReviewListItemResponse>> reviewsByRating = result.getReviewsByRating().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToPlaceReviewListItemResponse)
                                .toList()
                ));

        List<PlaceReviewListItemResponse> allReviews = result.getAllReviews().stream()
                .map(this::convertToPlaceReviewListItemResponse)
                .toList();

        PlaceReviewsByRatingResponse response = PlaceReviewsByRatingResponse.from(
            reviewsByRating, allReviews,
            result.getTotalReviewCount()
        );

        return new PlaceReviewsByRatingWithPagination(response, result.getTotalElements());
    }

    private PlaceReviewListItemResponse convertToPlaceReviewListItemResponse(LatestReviewListItemResult dto) {
        List<String> imageUrls = dto.imageUrls().stream().map(fileService::getUrlByPath).toList();

        return PlaceReviewListItemResponse.from(
            dto.id(),
            imageUrls,
            dto.totalRating(),
            dto.content(),
            dto.memberNickname(),
            fileService.getUrlByPath(dto.memberProfileImageUrl()),
            dto.createdAt(),
            dto.productId(),
            dto.productName()
        );
    }

    @Transactional(readOnly = true)
    public PlaceReviewStatisticsResponse getPlaceReviewStatistics(Long placeId) {
        PlaceReviewStatisticsResult statistics = reviewQueryService.findPlaceReviewStatistics(placeId);

        Place place = placeQueryService.findPlaceById(placeId);

        return PlaceReviewStatisticsResponse.from(
            place.getRating(),
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

    private PlaceInfoResponse.BusinessHourItem convertToBusinessHourItem(PlaceBusinessHour businessHour) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return PlaceInfoResponse.BusinessHourItem.from(
            businessHour.getDayType().name(),
            businessHour.getDayType().getDescription(),
            businessHour.getOpenTime() != null ? businessHour.getOpenTime().format(formatter) : null,
            businessHour.getCloseTime() != null ? businessHour.getCloseTime().format(formatter) : null,
            businessHour.getIsClosed()
        );
    }

    private PlaceInfoResponse.BreakTimeItem convertToBreakTimeItem(PlaceBreakTime breakTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return PlaceInfoResponse.BreakTimeItem.from(
            breakTime.getDayType().name(),
            breakTime.getDayType().getDescription(),
            breakTime.getStartTime() != null ? breakTime.getStartTime().format(formatter) : null,
            breakTime.getEndTime() != null ? breakTime.getEndTime().format(formatter) : null
        );
    }

    private PlaceInfoResponse.ClosedDayItem convertToClosedDayItem(PlaceClosedDay closedDay) {
        return PlaceInfoResponse.ClosedDayItem.from(
            closedDay.getClosedDayType().name(),
            closedDay.getClosedDayType().getDescription()
        );
    }

    private PlaceInfoResponse.AmenityItem convertToAmenityItem(PlaceAmenityWithCategoryDto dto) {
        return PlaceInfoResponse.AmenityItem.from(
            dto.amenity().name(),
            dto.displayName(),
            fileService.getUrlByPath(dto.activeFilePath())
        );
    }

    private PlaceBannerResponse convertToPlaceBannerResponse(PlaceBannerImageDto image) {
        return PlaceBannerResponse.from(
            image.id(),
            fileService.getUrlByPath(image.filePath()),
            image.sort()
        );
    }

    private ProductSummaryResponse convertToPlaceMenuResponse(Product product) {
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
            product.getIsRepresentative(),
            product.getSpiciness()
        );
    }

    private String getFirstImageUrl(Long productId) {
        return productQueryService.getFirstImageFilePath(productId);
    }

    @Transactional(readOnly = true)
    public PlaceBookmarkResponse isBookmarked(Long placeId, Long memberId) {
        boolean isBookmarked = placeQueryService.isBookmarked(placeId, memberId);
        return PlaceBookmarkResponse.from(isBookmarked);
    }

    @Transactional
    public boolean toggleBookmark(Long placeId, Long memberId) {
        return placeCommandService.toggleBookmark(placeId, memberId);
    }

    @Transactional(readOnly = true)
    public PlaceOrderMethodResponse getPlaceOrderMethods(Long placeId) {
        placeQueryService.findPlaceById(placeId);
        List<PlaceOrderMethod> placeOrderMethods = placeQueryService.findPlaceOrderMethods(placeId);

        List<PlaceOrderMethodResponse.OrderMethodItem> orderMethodItems =
            placeOrderMethods.stream()
                .map(pom -> PlaceOrderMethodResponse.OrderMethodItem.from(
                    pom.getOrderMethod().name(),
                    pom.getOrderMethod().getDisplayName()))
                .toList();

        return PlaceOrderMethodResponse.from(orderMethodItems);
    }
}
