package com.tastyhouse.ceoapi.product;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.service.ProductVegetarianApprovalService;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.ceoapi.shop.ShopFoodTypeCategoryReader;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;

/**
 * 점주용 메뉴 채식 설정 서비스(CQRS command 측).
 *
 * <p>승인 워크플로와 카테고리 거절 규칙은 도메인 서비스 {@link ProductVegetarianApprovalService}가
 * 소유하고, 이 서비스는 트랜잭션 경계·소유권 검증·경계 타입 승격(String → {@link VegetarianType})과
 * <b>두 컨텍스트의 조립</b>만 담당한다.
 *
 * <p>조립 지점이 여기인 이유: 판정 근거인 가게 카테고리 이름은 shop 컨텍스트의 값이므로 product
 * 도메인 서비스가 직접 읽으면 경계 위반이다. 그래서 {@link ShopFoodTypeCategoryReader}로 읽어
 * 도메인에 넘긴다.
 */
@Service
@Transactional
public class ProductVegetarianCommandService {

    private final ProductVegetarianApprovalService productVegetarianApprovalService;
    private final ProductRepository productRepository;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ShopFoodTypeCategoryReader shopFoodTypeCategoryReader;

    public ProductVegetarianCommandService(
        ProductVegetarianApprovalService productVegetarianApprovalService,
        ProductRepository productRepository,
        ShopOwnershipValidator shopOwnershipValidator,
        ShopFoodTypeCategoryReader shopFoodTypeCategoryReader
    ) {
        this.productVegetarianApprovalService = productVegetarianApprovalService;
        this.productRepository = productRepository;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.shopFoodTypeCategoryReader = shopFoodTypeCategoryReader;
    }

    /**
     * 채식 설정을 신청한다. 점주가 직접 켤 수는 없다 — 관리자가 재료를 보고 판정해야 반영된다.
     *
     * @return 생성된 검수 요청 식별자
     */
    public Long requestVegetarian(
        Long ceoId,
        Long shopId,
        Long productId,
        String vegetarianType,
        String ingredients,
        String description
    ) {
        requireOwnedProduct(ceoId, shopId, productId);
        Set<String> shopCategoryNames = shopFoodTypeCategoryReader.readCategoryNames(shopId);

        return productVegetarianApprovalService.requestVegetarian(
            ProductId.of(productId),
            VegetarianType.from(vegetarianType),
            ingredients,
            description,
            shopCategoryNames
        );
    }

    /**
     * 채식 설정을 해제한다. <b>승인을 거치지 않는다</b> — 잘못된 채식 표기를 점주가 즉시 내릴 수
     * 있어야 하고, 해제 방향에는 오표기 위험이 없다.
     */
    public void clearVegetarian(Long ceoId, Long shopId, Long productId) {
        requireOwnedProduct(ceoId, shopId, productId);
        productVegetarianApprovalService.clearVegetarian(ProductId.of(productId));
    }

    /**
     * 로그인 점주가 대상 가게의 소유자이고 그 메뉴가 정말 그 가게 것인지 확인한다.
     *
     * <p>가게 소유권만 확인하면 다른 가게의 메뉴 id를 넣은 요청이 통과하므로 두 축을 함께 검증한다.
     * "메뉴 없음"과 "남의 가게 메뉴"를 같은 {@code PRODUCT_NOT_FOUND}(404)로 합쳐 존재 여부가 새지
     * 않게 한다.
     */
    private void requireOwnedProduct(Long ceoId, Long shopId, Long productId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<Product> found = productRepository.findAllByShopIdAndIdIn(
            ShopId.of(shopId), List.of(ProductId.of(productId)));
        if (found.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
