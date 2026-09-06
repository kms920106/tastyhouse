package com.tastyhouse.domain.product.service;

import java.util.Set;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.repository.ProductVegetarianRequestRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public class ProductVegetarianApprovalService {
    private static final Set<String> DISALLOWED_SHOP_CATEGORIES = Set.of(
        "돈까스/회/일식", "고기/구이", "찜/탕/찌개", "족발/보쌈", "피자", "치킨", "중식", "야식"
    );

    private final ProductVegetarianRequestRepository requestRepository;
    private final ProductRepository productRepository;

    public ProductVegetarianApprovalService(
        ProductVegetarianRequestRepository requestRepository,
        ProductRepository productRepository
    ) {
        this.requestRepository = requestRepository;
        this.productRepository = productRepository;
    }

    public Long requestVegetarian(
        ProductId productId,
        VegetarianType vegetarianType,
        String ingredients,
        String description,
        Set<String> shopCategoryNames
    ) {
        loadProduct(productId);
        validateShopCategoryAllowed(shopCategoryNames);

        if (requestRepository.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_ALREADY_PENDING);
        }

        ProductVegetarianRequest saved = requestRepository.save(
            ProductVegetarianRequest.of(productId, vegetarianType, ingredients, description));
        return saved.getId();
    }

    public void approve(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.approve();
        requestRepository.save(request);

        Product product = loadProduct(request.getProductId());
        product.applyVegetarianType(request.getVegetarianType());
        productRepository.save(product);
    }

    public void reject(ProductVegetarianRequestId requestId, String rejectReason) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestRepository.save(request);
    }

    public void cancel(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.cancel();
        requestRepository.save(request);
    }

    public void clearVegetarian(ProductId productId) {
        Product product = loadProduct(productId);
        product.applyVegetarianType(null);
        productRepository.save(product);
    }

    public boolean isShopCategoryAllowed(Set<String> shopCategoryNames) {
        if (shopCategoryNames == null || shopCategoryNames.isEmpty()) {
            return true;
        }
        return shopCategoryNames.stream().noneMatch(DISALLOWED_SHOP_CATEGORIES::contains);
    }

    private void validateShopCategoryAllowed(Set<String> shopCategoryNames) {
        if (!isShopCategoryAllowed(shopCategoryNames)) {
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_CATEGORY_NOT_ALLOWED);
        }
    }

    private ProductVegetarianRequest loadRequest(ProductVegetarianRequestId requestId) {
        return requestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_NOT_FOUND));
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
