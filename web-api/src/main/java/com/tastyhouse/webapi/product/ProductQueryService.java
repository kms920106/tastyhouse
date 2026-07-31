package com.tastyhouse.webapi.product;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.product.query.OptionGroupResult;
import com.tastyhouse.infrastructure.product.query.ProductBatchItem;
import com.tastyhouse.infrastructure.product.query.ProductBatchResult;
import com.tastyhouse.infrastructure.product.query.ProductCategoryResult;
import com.tastyhouse.infrastructure.product.query.ProductDetailResult;
import com.tastyhouse.infrastructure.product.query.ProductOptionsResult;
import com.tastyhouse.infrastructure.product.query.ProductQueryDao;
import com.tastyhouse.infrastructure.product.query.SearchProductItemResult;
import com.tastyhouse.infrastructure.product.query.ShopProductItemResult;
import com.tastyhouse.infrastructure.product.query.TodayDiscountProductResult;
import com.tastyhouse.infrastructure.review.query.LatestReviewListItemResult;
import com.tastyhouse.infrastructure.review.query.ProductReviewStatisticsResult;
import com.tastyhouse.infrastructure.review.query.ReviewsByRatingResult;
import com.tastyhouse.webapi.common.PaginationResponse;
import com.tastyhouse.webapi.file.FileService;
import com.tastyhouse.webapi.product.request.ProductBatchRequest;
import com.tastyhouse.webapi.product.response.ProductBatchOptionResponse;
import com.tastyhouse.webapi.product.response.ProductBatchResponse;
import com.tastyhouse.webapi.product.response.ProductDetailResponse;
import com.tastyhouse.webapi.product.response.ProductImagesResponse;
import com.tastyhouse.webapi.product.response.ProductOptionGroupResponse;
import com.tastyhouse.webapi.product.response.ProductOptionGroupsResponse;
import com.tastyhouse.webapi.product.response.ProductOptionResponse;
import com.tastyhouse.webapi.product.response.ProductResponse;
import com.tastyhouse.webapi.product.response.ProductReviewCountResponse;
import com.tastyhouse.webapi.product.response.ProductReviewListItemResponse;
import com.tastyhouse.webapi.product.response.ProductReviewStatisticsResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingPageResponse;
import com.tastyhouse.webapi.product.response.ProductReviewsByRatingResponse;
import com.tastyhouse.webapi.product.response.ProductTodayDiscountListItemResponse;
import com.tastyhouse.webapi.review.ReviewQueryService;

/**
 * 회원용 상품 조회 서비스. infrastructure의 read 어댑터 {@link ProductQueryDao}만 주입하고, 조회 결과를
 * Response로 조립한다(private 매퍼). web-api에는 상품 command 경로가 없어 QueryService만 둔다.
 *
 * <p>리뷰 통계·목록은 review 도메인 소관이라 {@link ReviewQueryService}에 위임한다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductQueryDao productQueryDao;
    private final ReviewQueryService reviewQueryService;
    private final FileService fileService;

    public PaginationResponse<ProductTodayDiscountListItemResponse> searchTodayDiscountProducts(int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<ProductTodayDiscountListItemResponse> pageResult =
            productQueryDao.findTodayDiscountProducts(pageQuery)
                .map(this::toTodayDiscountProductListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private ProductTodayDiscountListItemResponse toTodayDiscountProductListItemResponse(TodayDiscountProductResult dto) {
        return ProductTodayDiscountListItemResponse.from(
            dto.id(),
            dto.shopName(),
            dto.name(),
            fileService.getUrlByPath(dto.imageUrl()),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate()
        );
    }

    public ProductDetailResponse findProductById(Long productId) {
        ProductDetailResult dto = loadProductDetail(productId);
        return ProductDetailResponse.from(
            dto.id(),
            dto.name(),
            dto.description(),
            dto.originalPrice(),
            dto.discountPrice(),
            dto.discountRate(),
            dto.soldOut()
        );
    }

    public ProductReviewCountResponse findProductReviewCount(Long productId) {
        loadProductDetail(productId);
        ProductReviewStatisticsResult statistics = reviewQueryService.findProductReviewStatistics(productId);
        Long total = statistics.totalReviewCount();
        return ProductReviewCountResponse.from(total != null ? total.intValue() : 0);
    }

    public ProductOptionGroupsResponse findProductOptions(Long productId) {
        loadProductDetail(productId);
        ProductOptionsResult result = productQueryDao.findProductOptions(productId);
        return ProductOptionGroupsResponse.from(toOptionGroupResponses(result));
    }

    /**
     * 상품 배치 조회. (상품ID, 옵션ID) 조합 목록을 받아 상품 단위로 그룹핑하여 반환합니다.
     * 판매 종료/미존재 상품은 제외하지 않고 available=false 로 남기며, 옵션은 조회에 성공한 것만 포함됩니다.
     */
    public ProductBatchResponse findProductsBatch(ProductBatchRequest request) {
        List<ProductBatchItem> items = request.items().stream()
            .map(item -> ProductBatchItem.of(item.productId(), item.optionId()))
            .toList();

        List<ProductResponse> products = productQueryDao.findProductsBatch(items).stream()
            .map(this::toProductBatchResponse)
            .toList();

        return ProductBatchResponse.from(products);
    }

    private ProductResponse toProductBatchResponse(ProductBatchResult result) {
        List<ProductBatchOptionResponse> options = result.options().stream()
            .map(option -> ProductBatchOptionResponse.from(
                option.id(),
                option.name(),
                option.price()
            ))
            .toList();
        return ProductResponse.from(
            result.id(),
            result.available(),
            result.name(),
            fileService.getUrlByPath(result.imageFilePath()),
            result.originalPrice(),
            result.discountPrice(),
            options
        );
    }

    private List<ProductOptionGroupResponse> toOptionGroupResponses(ProductOptionsResult result) {
        return result.optionGroups().stream()
            .map(this::toOptionGroupResponse)
            .toList();
    }

    private ProductOptionGroupResponse toOptionGroupResponse(OptionGroupResult group) {
        List<ProductOptionResponse> options = group.options().stream()
            .map(option -> ProductOptionResponse.from(
                option.id(),
                option.name(),
                option.additionalPrice(),
                option.soldOut()
            ))
            .toList();
        return ProductOptionGroupResponse.from(
            group.id(),
            group.name(),
            group.description(),
            group.required(),
            group.multipleSelect(),
            group.minSelect(),
            group.maxSelect(),
            group.common(),
            options
        );
    }

    public ProductImagesResponse findProductImages(Long productId) {
        loadProductDetail(productId);
        List<String> imageUrls = productQueryDao.findProductImagePaths(productId).stream()
            .map(fileService::getUrlByPath)
            .toList();
        return ProductImagesResponse.from(imageUrls);
    }

    public ProductReviewsByRatingPageResponse getProductReviewsByRatingWithPagination(
        Long productId,
        int page,
        int size,
        Boolean hasImage
    ) {
        ReviewsByRatingResult result = reviewQueryService.findProductReviewsByRating(productId, page, size, hasImage);

        Map<Integer, List<ProductReviewListItemResponse>> reviewsByRating = result.reviewsByRating().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream()
                    .map(this::toProductReviewListItemResponse)
                    .toList()
            ));

        List<ProductReviewListItemResponse> allReviews = result.allReviews().stream()
            .map(this::toProductReviewListItemResponse)
            .toList();

        ProductReviewsByRatingResponse response = ProductReviewsByRatingResponse.from(
            reviewsByRating,
            allReviews,
            result.totalReviewCount()
        );

        return new ProductReviewsByRatingPageResponse(response, result.totalElements());
    }

    private ProductReviewListItemResponse toProductReviewListItemResponse(LatestReviewListItemResult dto) {
        return ProductReviewListItemResponse.from(
            dto.id(),
            dto.imageUrls().stream().map(fileService::getUrlByPath).toList(),
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

    public ProductReviewStatisticsResponse getProductReviewStatistics(Long productId) {
        ProductReviewStatisticsResult statistics = reviewQueryService.findProductReviewStatistics(productId);
        ProductDetailResult product = loadProductDetail(productId);

        return ProductReviewStatisticsResponse.from(
            product.rating(),
            statistics.totalReviewCount(),
            statistics.averageTasteRating(),
            statistics.averageAmountRating(),
            statistics.averagePriceRating()
        );
    }

    /**
     * 상품 검색(통합검색) — search 도메인이 메뉴 검색 결과를 조립할 때 사용한다.
     */
    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return productQueryDao.searchByKeyword(keyword, pageQuery);
    }

    /**
     * 가게 상세 화면의 상품 목록 — shop 도메인이 카테고리별로 그룹핑해 조립한다.
     */
    public List<ShopProductItemResult> findShopProducts(Long shopId) {
        return productQueryDao.findShopProducts(shopId);
    }

    /**
     * 가게의 노출 상품 카테고리 목록 — shop 도메인이 카테고리별 상품 묶음을 조립할 때 사용한다.
     */
    public List<ProductCategoryResult> findShopProductCategories(Long shopId) {
        return productQueryDao.findProductCategories(shopId);
    }

    /**
     * 상품 대표 이미지 파일 경로. 없으면 null.
     */
    public String findFirstImageFilePath(Long productId) {
        return productQueryDao.findProductImagePaths(productId).stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * 다른 도메인(review 등)이 상품 정보를 곁들여 조립할 때 쓰는 단건 조회. 없으면 빈 Optional.
     */
    public Optional<ProductDetailResult> findProductDetail(Long productId) {
        return productQueryDao.findProductDetailById(productId);
    }

    private ProductDetailResult loadProductDetail(Long productId) {
        return productQueryDao.findProductDetailById(productId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
