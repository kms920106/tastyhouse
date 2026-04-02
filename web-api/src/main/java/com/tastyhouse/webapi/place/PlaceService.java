package com.tastyhouse.webapi.place;

import com.tastyhouse.core.entity.place.*;
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
import com.tastyhouse.webapi.common.PageRequest;
import com.tastyhouse.webapi.place.request.LatestPlaceFilterRequest;
import com.tastyhouse.webapi.place.response.*;
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

    @Transactional(readOnly = true)
    public List<PlaceMapMarkerResponse> searchMapMarkers(Double latitude, Double longitude) {
        return placeCoreService.findNearbyPlaces(latitude, longitude).stream()
                .map(place -> new PlaceMapMarkerResponse(
                        place.getId(), place.getLatitude(), place.getLongitude(), place.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<BestPlaceListItem> searchBestPlaces(PageRequest pageRequest) {
        org.springframework.data.domain.Page<BestPlaceItemDto> page =
            placeCoreService.findBestPlaces(pageRequest.page(), pageRequest.size());

        List<BestPlaceListItem> bestPlaceListItems = page.getContent().stream().map(this::convertToBestPlaceListItem).toList();

        return new PageResult<>(bestPlaceListItems, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Transactional(readOnly = true)
    public PageResult<LatestPlaceListItem> searchLatestPlaces(PageRequest pageRequest, LatestPlaceFilterRequest filterRequest) {
        org.springframework.data.domain.Page<LatestPlaceItemDto> page = placeCoreService.findLatestPlaces(
                pageRequest.page(),
                pageRequest.size(),
                filterRequest.stationId(),
                filterRequest.foodTypes(),
                filterRequest.amenities()
        );

        List<LatestPlaceListItem> latestPlaceListItems = page.getContent().stream().map(this::convertToLatestPlaceListItem).toList();

        return new PageResult<>(latestPlaceListItems, page.getTotalElements(), page.getTotalPages(), page.getNumber(), page.getSize());
    }

    @Transactional(readOnly = true)
    public List<EditorChoiceResponse> searchEditorChoices() {
        return placeCoreService.findEditorChoices().stream().map(this::convertToEditorChoiceResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EditorChoiceResponse> searchEditorChoices(PageRequest pageRequest) {
        return placeCoreService.findEditorChoices(pageRequest.page(), pageRequest.size())
            .getContent().stream().map(this::convertToEditorChoiceResponse).toList();
    }

    private EditorChoiceResponse convertToEditorChoiceResponse(EditorChoiceDto dto) {
        List<EditorChoiceProductItem> productItems = dto.products() != null
            ? dto.products().stream().map(this::convertToEditorChoiceProductItem).toList()
            : new ArrayList<>();

        return new EditorChoiceResponse(dto.id(), dto.name(), dto.placeImageUrl(), dto.title(), dto.content(), productItems);
    }

    private BestPlaceListItem convertToBestPlaceListItem(BestPlaceItemDto dto) {
        return new BestPlaceListItem(dto.id(), dto.name(), dto.stationName(), dto.rating(), dto.imageUrl(), dto.foodTypes());
    }

    private LatestPlaceListItem convertToLatestPlaceListItem(LatestPlaceItemDto dto) {
        return new LatestPlaceListItem(
            dto.id(), dto.name(), dto.stationName(), dto.rating(), dto.imageUrl(),
            dto.createdAt(), dto.reviewCount(), dto.bookmarkCount(), dto.foodTypes()
        );
    }

    private EditorChoiceProductItem convertToEditorChoiceProductItem(ProductSimpleDto dto) {
        return new EditorChoiceProductItem(
            dto.id(), dto.placeName(), dto.name(), dto.imageUrl(),
            dto.originalPrice(), dto.discountPrice(), dto.discountRate()
        );
    }

    @Transactional(readOnly = true)
    public List<StationListItem> searchAllStations() {
        List<PlaceStation> stations = placeCoreService.findAllStations();
        return stations.stream().map(this::convertToStationListItem).toList();
    }

    @Transactional(readOnly = true)
    public List<FoodTypeListItem> searchAllFoodTypes() {
        List<PlaceFoodTypeCategory> categories = placeCoreService.findAllFoodTypeCategories();
        return categories.stream()
                .map(this::convertToFoodTypeListItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AmenityListItem> searchAllAmenities() {
        List<PlaceAmenityCategory> categories = placeCoreService.findAllAmenityCategories();
        return categories.stream()
                .map(this::convertToAmenityListItem)
                .toList();
    }

    private StationListItem convertToStationListItem(PlaceStation station) {
        return new StationListItem(station.getId(), station.getStationName());
    }

    private FoodTypeListItem convertToFoodTypeListItem(PlaceFoodTypeCategory category) {
        return new FoodTypeListItem(category.getFoodType().name(), category.getDisplayName(), category.getImageUrl());
    }

    private AmenityListItem convertToAmenityListItem(PlaceAmenityCategory category) {
        return new AmenityListItem(
            category.getAmenity().name(), category.getDisplayName(),
            category.getImageUrlOn(), category.getImageUrlOff()
        );
    }

    @Transactional(readOnly = true)
    public PlaceSummaryResponse getPlaceSummary(Long placeId) {
        Place place = placeCoreService.findPlaceById(placeId);

        return new PlaceSummaryResponse(
            place.getId(), place.getName(), place.getRoadAddress(), place.getLotAddress(), place.getRating()
        );
    }

    @Transactional(readOnly = true)
    public PlaceInfoResponse getPlaceInfo(Long placeId) {
        Place place = placeCoreService.findPlaceById(placeId);
        PlaceStation station = placeCoreService.findStationById(place.getStationId());
        List<PlaceBusinessHour> businessHours = placeCoreService.findPlaceBusinessHours(placeId);
        List<PlaceBreakTime> breakTimes = placeCoreService.findPlaceBreakTimes(placeId);
        List<PlaceClosedDay> closedDays = placeCoreService.findPlaceClosedDays(placeId);
        List<PlaceAmenity> placeAmenities = placeCoreService.findPlaceAmenities(placeId);

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

        return new PlaceInfoResponse(
            place.getId(), place.getLatitude(), place.getLongitude(),
            station.getStationName(), place.getPhoneNumber(),
            closedDayItems, businessHourItems, breakTimeItems, amenityItems,
            ownerMessage, ownerMessageCreatedAt
        );
    }

    @Transactional(readOnly = true)
    public List<PlaceBannerResponse> getPlaceBanners(Long placeId) {
        List<PlaceBannerImage> banners = placeCoreService.findPlaceBannerImages(placeId);
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
                    List<Product> categoryProducts = productsByCategory.getOrDefault(category.getId(), new ArrayList<Product>());
                    List<PlaceMenuResponse> menuResponses = categoryProducts.stream()
                            .map(this::convertToPlaceMenuResponse)
                            .toList();
                    return new PlaceMenuCategoryResponse(category.getName(), menuResponses);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlacePhotoCategoryResponse> getPlacePhotos(Long placeId) {
        List<PlacePhotoCategory> categories = placeCoreService.findAllPlacePhotoCategories();
        List<PlacePhotoCategoryImage> images = placeCoreService.findAllPlacePhotoCategoryImages();

        // 카테고리 ID별로 이미지 그룹화
        Map<Long, List<PlacePhotoCategoryImage>> imagesByCategory = images.stream()
                .filter(image -> image.getPlacePhotoCategoryId() != null)
                .collect(Collectors.groupingBy(PlacePhotoCategoryImage::getPlacePhotoCategoryId));

        // 카테고리 순서대로 응답 생성
        return categories.stream()
                .map(category -> {
                    List<PlacePhotoCategoryImage> categoryImages = imagesByCategory.getOrDefault(category.getId(), new ArrayList<PlacePhotoCategoryImage>());
                    List<String> imageUrls = categoryImages.stream()
                            .map(PlacePhotoCategoryImage::getImageUrl)
                            .toList();
                    return new PlacePhotoCategoryResponse(category.getName(), imageUrls);
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

        PlaceReviewsByRatingResponse response = new PlaceReviewsByRatingResponse(
            reviewsByRating, allReviews, result.getTotalReviewCount()
        );

        return new PlaceReviewsByRatingWithPagination(response, result.getTotalElements());
    }

    private PlaceReviewListItem convertToPlaceReviewListItem(LatestReviewListItemDto dto) {
        return new PlaceReviewListItem(
            dto.id(), dto.imageUrls(), dto.totalRating(), dto.content(),
            dto.memberNickname(), dto.memberProfileImageUrl(), dto.createdAt(),
            dto.productId(), dto.productName()
        );
    }

    @Transactional(readOnly = true)
    public PlaceReviewStatisticsResponse getPlaceReviewStatistics(Long placeId) {
        PlaceReviewStatisticsDto statistics = reviewCoreService.findPlaceReviewStatistics(placeId);

        Place place = placeCoreService.findPlaceById(placeId);

        return new PlaceReviewStatisticsResponse(
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

        return new PlaceInfoResponse.BusinessHourItem(
            businessHour.getDayType().name(),
            businessHour.getDayType().getDescription(),
            businessHour.getOpenTime() != null ? businessHour.getOpenTime().format(formatter) : null,
            businessHour.getCloseTime() != null ? businessHour.getCloseTime().format(formatter) : null,
            businessHour.getIsClosed()
        );
    }

    private PlaceInfoResponse.BreakTimeItem convertToBreakTimeItem(PlaceBreakTime breakTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return new PlaceInfoResponse.BreakTimeItem(
            breakTime.getDayType().name(),
            breakTime.getDayType().getDescription(),
            breakTime.getStartTime() != null ? breakTime.getStartTime().format(formatter) : null,
            breakTime.getEndTime() != null ? breakTime.getEndTime().format(formatter) : null
        );
    }

    private PlaceInfoResponse.ClosedDayItem convertToClosedDayItem(PlaceClosedDay closedDay) {
        return new PlaceInfoResponse.ClosedDayItem(
            closedDay.getClosedDayType().name(),
            closedDay.getClosedDayType().getDescription()
        );
    }

    private PlaceInfoResponse.AmenityItem convertToAmenityItem(PlaceAmenity placeAmenity) {
        PlaceAmenityCategory category = placeCoreService.findPlaceAmenityCategoryById(placeAmenity.getPlaceAmenityCategoryId());
        return new PlaceInfoResponse.AmenityItem(
            category.getAmenity().name(), category.getDisplayName(), category.getImageUrlOn()
        );
    }

    private PlaceBannerResponse convertToPlaceBannerResponse(PlaceBannerImage image) {
        return new PlaceBannerResponse(image.getId(), image.getImageUrl(), image.getSort());
    }

    private PlaceMenuResponse convertToPlaceMenuResponse(Product product) {
        String imageUrl = getFirstImageUrl(product.getId());

        return new PlaceMenuResponse(
            product.getId(), product.getName(), imageUrl,
            product.getOriginalPrice(), product.getDiscountPrice(), product.getDiscountRate(),
            product.getRating(), product.getReviewCount(), product.getIsRepresentative(), product.getSpiciness()
        );
    }

    private String getFirstImageUrl(Long productId) {
        return productCoreService.getFirstImageUrl(productId);
    }

    @Transactional(readOnly = true)
    public PlaceBookmarkResponse isBookmarked(Long placeId, Long memberId) {
        boolean isBookmarked = placeCoreService.isBookmarked(placeId, memberId);
        return new PlaceBookmarkResponse(isBookmarked);
    }

    @Transactional
    public boolean toggleBookmark(Long placeId, Long memberId) {
        return placeCoreService.toggleBookmark(placeId, memberId);
    }

    @Transactional(readOnly = true)
    public PlaceOwnerMessageHistoryResponse getPlaceOwnerMessageHistory(Long placeId) {
        placeCoreService.findPlaceById(placeId);

        return placeCoreService.findLatestOwnerMessage(placeId)
                .map(history -> new PlaceOwnerMessageHistoryResponse(history.getMessage(), history.getCreatedAt()))
                .orElse(new PlaceOwnerMessageHistoryResponse(null, null));
    }

    @Transactional(readOnly = true)
    public PlaceNameResponse getPlaceName(Long placeId) {
        Place place = placeCoreService.findPlaceById(placeId);
        return new PlaceNameResponse(place.getId(), place.getName());
    }

    @Transactional(readOnly = true)
    public PlaceOrderMethodResponse getPlaceOrderMethods(Long placeId) {
        placeCoreService.findPlaceById(placeId); // Ensure place exists
        List<PlaceOrderMethod> placeOrderMethods = placeCoreService.findPlaceOrderMethods(placeId);

        List<PlaceOrderMethodResponse.OrderMethodItem> orderMethodItems = placeOrderMethods.stream()
                .map(pom -> new PlaceOrderMethodResponse.OrderMethodItem(
                        pom.getOrderMethod().name(), pom.getOrderMethod().getDisplayName()))
                .toList();

        return new PlaceOrderMethodResponse(placeId, orderMethodItems);
    }
}
