package com.tastyhouse.webapi.place;

import com.tastyhouse.core.entity.place.Place;
import com.tastyhouse.core.entity.place.PlaceBreakTime;
import com.tastyhouse.core.entity.place.PlaceBusinessHour;
import com.tastyhouse.core.entity.place.PlaceClosedDay;
import com.tastyhouse.core.entity.place.PlaceOrderMethod;
import com.tastyhouse.core.entity.place.PlacePhotoCategory;
import com.tastyhouse.core.entity.place.PlaceStation;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceAmenityWithCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlaceBannerImageDto;
import com.tastyhouse.core.entity.place.dto.PlaceFoodTypeCategoryDto;
import com.tastyhouse.core.entity.place.dto.PlacePhotoCategoryImageDto;
import com.tastyhouse.core.entity.place.dto.BestPlaceItemDto;
import com.tastyhouse.core.entity.place.dto.EditorChoiceDto;
import com.tastyhouse.core.entity.place.dto.LatestPlaceItemDto;
import com.tastyhouse.core.entity.product.Product;
import com.tastyhouse.core.entity.product.ProductCategory;
import com.tastyhouse.core.entity.product.dto.ProductSimpleDto;
import com.tastyhouse.core.entity.review.dto.LatestReviewListItemDto;
import com.tastyhouse.core.entity.review.dto.PlaceReviewStatisticsDto;
import com.tastyhouse.core.common.PageResult;
import com.tastyhouse.core.common.ReviewsByRatingResult;
import com.tastyhouse.core.service.PlaceCoreService;
import com.tastyhouse.core.service.ProductCoreService;
import com.tastyhouse.core.service.ReviewCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.place.request.LatestPlaceFilterRequest;
import com.tastyhouse.webapi.place.response.AmenityListItem;
import com.tastyhouse.webapi.place.response.BestPlaceListItem;
import com.tastyhouse.webapi.place.response.EditorChoiceProductItem;
import com.tastyhouse.webapi.place.response.EditorChoiceResponse;
import com.tastyhouse.webapi.place.response.FoodTypeListItem;
import com.tastyhouse.webapi.place.response.LatestPlaceListItem;
import com.tastyhouse.webapi.place.response.PlaceBannerResponse;
import com.tastyhouse.webapi.place.response.PlaceBookmarkResponse;
import com.tastyhouse.webapi.place.response.PlaceInfoResponse;
import com.tastyhouse.webapi.place.response.PlaceMapMarkerResponse;
import com.tastyhouse.webapi.place.response.PlaceMenuCategoryResponse;
import com.tastyhouse.webapi.place.response.PlaceMenuResponse;
import com.tastyhouse.webapi.place.response.PlaceNameResponse;
import com.tastyhouse.webapi.place.response.PlaceOrderMethodResponse;
import com.tastyhouse.webapi.place.response.PlacePhotoCategoryResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewListItem;
import com.tastyhouse.webapi.place.response.PlaceReviewStatisticsResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewsByRatingResponse;
import com.tastyhouse.webapi.place.response.PlaceReviewsByRatingWithPagination;
import com.tastyhouse.webapi.place.response.PlaceSummaryResponse;
import com.tastyhouse.webapi.place.response.StationListItem;
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

    private final PlaceCoreService placeCoreService;
    private final ProductCoreService productCoreService;
    private final ReviewCoreService reviewCoreService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public List<PlaceMapMarkerResponse> searchMapMarkers(Double latitude, Double longitude) {
        return placeCoreService.findNearbyPlaces(latitude, longitude).stream()
                .map(place -> PlaceMapMarkerResponse.from(
                    place.getId(),
                    place.getLatitude(),
                    place.getLongitude(),
                    place.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<BestPlaceListItem> searchBestPlaces(int page, int size) {
        return PageResult.from(placeCoreService.findBestPlaces(page, size)).map(this::convertToBestPlaceListItem);
    }

    @Transactional(readOnly = true)
    public PageResult<LatestPlaceListItem> searchLatestPlaces(LatestPlaceFilterRequest filterRequest, int page, int size) {
        return PageResult.from(placeCoreService.findLatestPlaces(filterRequest.stationId(), filterRequest.foodTypes(), filterRequest.amenities(), page, size)).map(this::convertToLatestPlaceListItem);
    }

    @Transactional(readOnly = true)
    public List<EditorChoiceResponse> searchEditorChoices(int page, int size) {
        return placeCoreService.findEditorChoices(page, size).getContent().stream().map(this::convertToEditorChoiceResponse).toList();
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

    private BestPlaceListItem convertToBestPlaceListItem(BestPlaceItemDto dto) {
        return BestPlaceListItem.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.rating(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.foodTypes()
        );
    }

    private LatestPlaceListItem convertToLatestPlaceListItem(LatestPlaceItemDto dto) {
        return LatestPlaceListItem.from(
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

    private EditorChoiceProductItem convertToEditorChoiceProductItem(ProductSimpleDto dto) {
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
    public List<StationListItem> searchAllStations() {
        List<PlaceStation> stations = placeCoreService.findAllStations();
        return stations.stream().map(this::convertToStationListItem).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodTypeListItem> searchAllFoodTypes() {
        List<PlaceFoodTypeCategoryDto> categories = placeCoreService.findAllFoodTypeCategories();
        return categories.stream()
                .map(this::convertToFoodTypeListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AmenityListItem> searchAllAmenities() {
        List<PlaceAmenityCategoryDto> categories = placeCoreService.findAllAmenityCategories();
        return categories.stream()
                .map(this::convertToAmenityListItem)
                .toList();
    }

    private StationListItem convertToStationListItem(PlaceStation station) {
        return StationListItem.from(
            station.getId(),
            station.getStationName()
        );
    }

    private FoodTypeListItem convertToFoodTypeListItem(PlaceFoodTypeCategoryDto category) {
        return FoodTypeListItem.from(
            category.foodType().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    private AmenityListItem convertToAmenityListItem(PlaceAmenityCategoryDto category) {
        return AmenityListItem.from(
            category.amenity().name(),
            category.displayName(),
            fileService.getUrlByPath(category.activeFilePath()),
            fileService.getUrlByPath(category.inactiveFilePath())
        );
    }

    @Transactional(readOnly = true)
    public PlaceSummaryResponse getPlaceSummary(Long placeId) {
        Place place = placeCoreService.findPlaceById(placeId);

        return PlaceSummaryResponse.from(
            place.getId(),
            place.getName(),
            place.getRoadAddress(),
            place.getLotAddress(),
            place.getRating()
        );
    }

    @Transactional(readOnly = true)
    public PlaceInfoResponse getPlaceInfo(Long placeId) {
        Place place = placeCoreService.findPlaceById(placeId);
        PlaceStation station = placeCoreService.findStationById(place.getStationId());
        List<PlaceBusinessHour> businessHours = placeCoreService.findPlaceBusinessHours(placeId);
        List<PlaceBreakTime> breakTimes = placeCoreService.findPlaceBreakTimes(placeId);
        List<PlaceClosedDay> closedDays = placeCoreService.findPlaceClosedDays(placeId);
        List<PlaceAmenityWithCategoryDto> placeAmenities = placeCoreService.findPlaceAmenitiesWithCategory(placeId);

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

        // 사장님 한마디 히스토리 조회
        String ownerMessage = null;
        java.time.LocalDateTime ownerMessageCreatedAt = null;
        var ownerMessageHistory = placeCoreService.findLatestOwnerMessage(placeId);
        if (ownerMessageHistory.isPresent()) {
            ownerMessage = ownerMessageHistory.get().getMessage();
            ownerMessageCreatedAt = ownerMessageHistory.get().getCreatedAt();
        }

        return PlaceInfoResponse.from(
            place.getId(),
            place.getLatitude(),
            place.getLongitude(),
            station.getStationName(),
            place.getPhoneNumber(),
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
        List<PlaceBannerImageDto> banners = placeCoreService.findPlaceBannerImages(placeId);
        return banners.stream()
                .map(this::convertToPlaceBannerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlaceMenuCategoryResponse> getPlaceMenus(Long placeId) {
        List<ProductCategory> categories = productCoreService.findProductCategoriesByPlaceId(placeId);
        List<Product> products = productCoreService.findProductsByPlaceId(placeId);

        // 카테고리 ID별로 Product 그룹화
        Map<Long, List<Product>> productsByCategory = products.stream()
                .filter(product -> product.getProductCategoryId() != null)
                .collect(Collectors.groupingBy(Product::getProductCategoryId));

        // 카테고리 순서대로 응답 생성
        return categories.stream()
                .map(category -> {
                    List<Product> categoryProducts = productsByCategory.getOrDefault(category.getId(), new ArrayList<>());
                    List<PlaceMenuResponse> menuResponses = categoryProducts.stream()
                            .map(this::convertToPlaceMenuResponse)
                            .toList();
                    return PlaceMenuCategoryResponse.from(
                        category.getName(),
                        menuResponses
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlacePhotoCategoryResponse> getPlacePhotos(Long placeId) {
        List<PlacePhotoCategory> categories = placeCoreService.findPlacePhotoCategoriesByPlaceId(placeId);
        List<PlacePhotoCategoryImageDto> images = placeCoreService.findAllPlacePhotoCategoryImages();

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
        ReviewsByRatingResult result = reviewCoreService.findPlaceReviewsByRating(placeId, page, size);

        Map<Integer, List<PlaceReviewListItem>> reviewsByRating = result.getReviewsByRating().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry<Integer, List<LatestReviewListItemDto>>::getKey,
                        entry -> entry.getValue().stream()
                                .map(this::convertToPlaceReviewListItem)
                                .toList()
                ));

        List<PlaceReviewListItem> allReviews = result.getAllReviews().stream()
                .map(this::convertToPlaceReviewListItem)
                .toList();

        PlaceReviewsByRatingResponse response = PlaceReviewsByRatingResponse.from(
            reviewsByRating, allReviews,
            result.getTotalReviewCount()
        );

        return new PlaceReviewsByRatingWithPagination(response, result.getTotalElements());
    }

    private PlaceReviewListItem convertToPlaceReviewListItem(LatestReviewListItemDto dto) {
        List<String> imageUrls = dto.imageUrls().stream().map(fileService::getUrlByPath).toList();

        return PlaceReviewListItem.from(
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
        PlaceReviewStatisticsDto statistics = reviewCoreService.findPlaceReviewStatistics(placeId);

        Place place = placeCoreService.findPlaceById(placeId);

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

    private PlaceMenuResponse convertToPlaceMenuResponse(Product product) {
        String imageUrl = getFirstImageUrl(product.getId());

        return PlaceMenuResponse.from(
            product.getId(),
            product.getName(),
            imageUrl,
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
        return productCoreService.getFirstImageFilePath(productId);
    }

    @Transactional(readOnly = true)
    public PlaceBookmarkResponse isBookmarked(Long placeId, Long memberId) {
        boolean isBookmarked = placeCoreService.isBookmarked(placeId, memberId);
        return PlaceBookmarkResponse.from(isBookmarked);
    }

    @Transactional
    public boolean toggleBookmark(Long placeId, Long memberId) {
        return placeCoreService.toggleBookmark(placeId, memberId);
    }

    @Transactional(readOnly = true)
    public PlaceNameResponse getPlaceName(Long placeId) {
        Place place = placeCoreService.findPlaceById(placeId);
        return PlaceNameResponse.from(
            place.getId(),
            place.getName()
        );
    }

    @Transactional(readOnly = true)
    public PlaceOrderMethodResponse getPlaceOrderMethods(Long placeId) {
        placeCoreService.findPlaceById(placeId); // Ensure place exists
        List<PlaceOrderMethod> placeOrderMethods = placeCoreService.findPlaceOrderMethods(placeId);

        List<PlaceOrderMethodResponse.OrderMethodItem> orderMethodItems = placeOrderMethods.stream()
                .map(pom -> PlaceOrderMethodResponse.OrderMethodItem.from(
                    pom.getOrderMethod().name(),
                    pom.getOrderMethod().getDisplayName()))
                .toList();

        return PlaceOrderMethodResponse.from(placeId, orderMethodItems);
    }
}
