package com.tastyhouse.application.product.service;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductVegetarianRequest;
import com.tastyhouse.domain.product.model.VegetarianType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductVegetarianRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductVegetarianRequestSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

@Service
public class ProductVegetarianApprovalService {

    private static final Set<String> DISALLOWED_SHOP_CATEGORIES = Set.of(
        "돈까스/회/일식", "고기/구이", "찜/탕/찌개", "족발/보쌈", "피자", "치킨", "중식", "야식"
    );

    private final ProductVegetarianRequestLoadPort requestLoadPort;
    private final ProductVegetarianRequestSavePort requestSavePort;
    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;

    public ProductVegetarianApprovalService(
        ProductVegetarianRequestLoadPort requestLoadPort,
        ProductVegetarianRequestSavePort requestSavePort,
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort
    ) {
        this.requestLoadPort = requestLoadPort;
        this.requestSavePort = requestSavePort;
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
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

        if (requestLoadPort.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_VEGETARIAN_REQUEST_ALREADY_PENDING);
        }

        ProductVegetarianRequest saved = requestSavePort.save(
            ProductVegetarianRequest.of(productId, vegetarianType, ingredients, description));
        return saved.getId();
    }

    public void approve(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.approve();
        requestSavePort.save(request);

        Product product = loadProduct(request.getProductId());
        product.applyVegetarianType(request.getVegetarianType());
        productSavePort.save(product);
    }

    public void reject(ProductVegetarianRequestId requestId, String rejectReason) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestSavePort.save(request);
    }

    public void cancel(ProductVegetarianRequestId requestId) {
        ProductVegetarianRequest request = loadRequest(requestId);
        request.cancel();
        requestSavePort.save(request);
    }

    public void clearVegetarian(ProductId productId) {
        Product product = loadProduct(productId);
        product.applyVegetarianType(null);
        productSavePort.save(product);
    }

    public boolean isShopCategoryAllowed(Set<String> shopCategoryNames) {
        if (shopCategoryNames == null || shopCategoryNames.isEmpty()) {
            return true;
        }
        return shopCategoryNames.stream().noneMatch(DISALLOWED_SHOP_CATEGORIES::contains);
    }

    private void validateShopCategoryAllowed(Set<String> shopCategoryNames) {
        if (!isShopCategoryAllowed(shopCategoryNames)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_VEGETARIAN_CATEGORY_NOT_ALLOWED);
        }
    }

    private ProductVegetarianRequest loadRequest(ProductVegetarianRequestId requestId) {
        return requestLoadPort.findById(requestId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_VEGETARIAN_REQUEST_NOT_FOUND));
    }

    private Product loadProduct(ProductId productId) {
        return productLoadPort.findById(productId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
    }
}
