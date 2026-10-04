package com.tastyhouse.application.product.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestPersistencePort;

@Service
public class ProductVegetarianApprovalService {

    private static final Set<String> DISALLOWED_SHOP_CATEGORIES = Set.of(
        "돈까스/회/일식", "고기/구이", "찜/탕/찌개", "족발/보쌈", "피자", "치킨", "중식", "야식"
    );

    private final ProductVegetarianRequestPersistencePort requestPersistencePort;
    private final ProductPersistencePort productPersistencePort;

    public ProductVegetarianApprovalService(
        ProductVegetarianRequestPersistencePort requestPersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        this.requestPersistencePort = requestPersistencePort;
        this.productPersistencePort = productPersistencePort;
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

        if (requestPersistencePort.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_ALREADY_PENDING);
        }

        ProductVegetarianRequest saved = requestPersistencePort.save(
            ProductVegetarianRequest.of(productId, vegetarianType, ingredients, description));
        return saved.getId();
    }

    public void approve(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.approve();
        requestPersistencePort.save(request);

        Product product = loadProduct(request.getProductId());
        product.applyVegetarianType(request.getVegetarianType());
        productPersistencePort.save(product);
    }

    public void reject(ProductVegetarianRequestId requestId, String rejectReason) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestPersistencePort.save(request);
    }

    public void cancel(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.cancel();
        requestPersistencePort.save(request);
    }

    public void clearVegetarian(ProductId productId) {
        Product product = loadProduct(productId);
        product.applyVegetarianType(null);
        productPersistencePort.save(product);
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
        return requestPersistencePort.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_VEGETARIAN_REQUEST_NOT_FOUND));
    }

    private Product loadProduct(ProductId productId) {
        return productPersistencePort.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
