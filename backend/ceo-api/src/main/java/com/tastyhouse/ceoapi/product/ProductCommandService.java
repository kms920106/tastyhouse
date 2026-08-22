package com.tastyhouse.ceoapi.product;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductAvailabilityChangeResult;
import com.tastyhouse.domain.product.service.ProductAvailabilityFailure;
import com.tastyhouse.domain.product.service.ProductDeletionService;
import com.tastyhouse.domain.product.service.ProductRegistrationService;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.ceoapi.product.response.ProductAvailabilityChangeResponse;
import com.tastyhouse.ceoapi.product.response.ProductAvailabilityFailureResponse;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴 CRUD 서비스(CQRS command 측).
 *
 * <p>이 서비스는 트랜잭션 경계·소유권 검증·입력 검수(금칙어·메뉴명)·VO 승격·응답 조립만 담당하고,
 * 등록/삭제의 불변식은 도메인 서비스({@link ProductRegistrationService} · {@link ProductDeletionService})가
 * 소유한다.
 *
 * <p><b>메뉴명 검증을 도메인이 아니라 여기서 하는 이유</b>는 {@link ProductNameValidator} Javadoc 참고 —
 * 특수문자 화이트리스트는 점주 입력 경로 한정 정책이라 애그리거트 불변식이 아니다.
 */
@Service
@Transactional
public class ProductCommandService {

    /** 등록 직후의 노출 상태 — 숨김은 별도 경로(품절·숨김 관리)가 담당하므로 등록은 항상 노출로 만든다. */
    private static final boolean DEFAULT_VISIBLE = true;

    private final ProductRegistrationService productRegistrationService;
    private final ProductDeletionService productDeletionService;
    private final ProductRepository productRepository;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ProductNameValidator productNameValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductCommandService(
        ProductRegistrationService productRegistrationService,
        ProductDeletionService productDeletionService,
        ProductRepository productRepository,
        ProhibitedWordValidator prohibitedWordValidator,
        ProductNameValidator productNameValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productRegistrationService = productRegistrationService;
        this.productDeletionService = productDeletionService;
        this.productRepository = productRepository;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.productNameValidator = productNameValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 메뉴를 등록하고 생성된 id를 반환한다.
     *
     * <p>정렬값은 클라이언트에서 받지 않고 서버가 그룹의 현재 메뉴 수로 채운다 — 등록 직후에는 목록
     * 맨 뒤에 붙고, 위치 조정은 순서 변경 API(§4)가 담당한다.
     */
    public Long createProduct(
        Long ceoId,
        Long shopId,
        Long productCategoryId,
        String name,
        String composition,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        Boolean singleServing,
        Integer spiciness,
        Boolean representative,
        Boolean ratingExcluded
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateTexts(name, composition, description);
        productNameValidator.validateForCreate(shopId, name);

        ProductCategoryId categoryId = toProductCategoryId(productCategoryId);
        Product created = productRegistrationService.createProduct(
            ShopId.of(shopId),
            categoryId,
            name,
            description,
            originalPrice,
            discountPrice,
            null,
            null,
            null,
            Boolean.TRUE.equals(representative),
            spiciness,
            false,
            DEFAULT_VISIBLE,
            nextSort(shopId, categoryId),
            Boolean.TRUE.equals(ratingExcluded),
            composition,
            Boolean.TRUE.equals(singleServing)
        );
        return created.getId();
    }

    /**
     * 메뉴 기본 정보를 변경한다. 이미지·채식은 승인 워크플로를 거치므로 이 경로로 바꾸지 않는다.
     *
     * <p>그룹을 옮기면 {@code sort}가 도착 그룹 기준으로 다시 매겨져야 하므로
     * {@link Product#relocate}로 두 값을 함께 반영한다.
     */
    public void updateProduct(
        Long ceoId,
        Long productId,
        Long shopId,
        Long productCategoryId,
        String name,
        String composition,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        Boolean singleServing,
        Integer spiciness,
        Boolean representative,
        Boolean ratingExcluded,
        String weightText
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        validateTexts(name, composition, description);
        productNameValidator.validateForUpdate(shopId, productId, name);

        Product product = loadOwnedProduct(shopId, productId);
        ProductCategoryId categoryId = toProductCategoryId(productCategoryId);
        boolean categoryChanged = !isSameCategory(product.getProductCategoryId(), categoryId);

        product.changeDetails(
            categoryId,
            name,
            composition,
            description,
            originalPrice,
            discountPrice,
            null,
            Boolean.TRUE.equals(singleServing),
            spiciness,
            Boolean.TRUE.equals(representative),
            Boolean.TRUE.equals(ratingExcluded),
            weightText
        );
        if (categoryChanged) {
            product.relocate(categoryId, nextSort(shopId, categoryId));
        }
        productRepository.save(product);
    }

    /**
     * 메뉴를 일괄 소프트 삭제한다. 부분 실패는 예외가 아니라 {@code failed}로 반환된다.
     */
    public ProductAvailabilityChangeResponse deleteProducts(Long ceoId, Long shopId, List<Long> productIds) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeResponse(productDeletionService.deleteProducts(ShopId.of(shopId), toProductIds(productIds)));
    }

    // ── 검수 ────────────────────────────────────────────────────────────────────────

    private void validateTexts(String name, String composition, String description) {
        prohibitedWordValidator.validate(name);
        prohibitedWordValidator.validate(composition);
        prohibitedWordValidator.validate(description);
    }

    /**
     * 대상 메뉴를 로드하면서 소유 가게까지 대조한다 — 가게 소유권만 검증하면 남의 가게 메뉴 id를 실어
     * 보내는 경로가 열린다. 미존재와 타 가게 소유는 같은 코드로 묶는다.
     */
    private Product loadOwnedProduct(Long shopId, Long productId) {
        Product product = productRepository.findById(ProductId.of(productId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));
        if (!product.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    // ── 변환 ────────────────────────────────────────────────────────────────────────

    /** 그룹의 현재 메뉴 수를 다음 정렬값으로 쓴다(0-based라 곧 맨 뒤 인덱스다). */
    private Integer nextSort(Long shopId, ProductCategoryId productCategoryId) {
        return productRepository.findAllByShopIdAndCategoryId(ShopId.of(shopId), productCategoryId).size();
    }

    private boolean isSameCategory(ProductCategoryId current, ProductCategoryId requested) {
        if (current == null || requested == null) {
            return current == requested;
        }
        return current.equals(requested);
    }

    private ProductCategoryId toProductCategoryId(Long productCategoryId) {
        return productCategoryId != null ? ProductCategoryId.of(productCategoryId) : null;
    }

    /**
     * 메뉴 id를 VO로 승격한다. 빈 목록은 {@code PRODUCT_AVAILABILITY_TARGET_EMPTY}(400)로 거부한다 —
     * Bean Validation이 먼저 걸러 주지만 그 경로는 스펙이 약속한 {@code code}를 전달하지 않는다.
     */
    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        return productIds.stream().map(ProductId::of).toList();
    }

    /**
     * 도메인 결과를 응답으로 옮긴다 — 컨트롤러는 {@code com.tastyhouse.domain..}를 import하지 않으므로
     * (ArchUnit {@code LayerRulesTest}) 도메인 타입은 이 서비스 경계에서 멈춘다.
     */
    private ProductAvailabilityChangeResponse toChangeResponse(ProductAvailabilityChangeResult result) {
        List<ProductAvailabilityFailureResponse> failed = result.failed().stream()
            .map(this::toFailureResponse)
            .toList();

        return ProductAvailabilityChangeResponse.from(
            result.succeeded(),
            failed
        );
    }

    private ProductAvailabilityFailureResponse toFailureResponse(ProductAvailabilityFailure failure) {
        return ProductAvailabilityFailureResponse.from(
            failure.id(),
            failure.name(),
            failure.errorCode().getCode(),
            failure.errorCode().getDefaultMessage()
        );
    }
}
