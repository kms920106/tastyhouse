package com.tastyhouse.application.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductImageLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductRepresentativeRequestSavePort;
import com.tastyhouse.application.product.port.out.write.ProductSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

@Service
public class ProductRepresentativeApprovalService {

    private static final long MAX_REPRESENTATIVE_COUNT = 6L;

    private final ProductRepresentativeRequestLoadPort requestLoadPort;
    private final ProductRepresentativeRequestSavePort requestSavePort;
    private final ProductLoadPort productLoadPort;
    private final ProductSavePort productSavePort;
    private final ProductImageLoadPort productImageLoadPort;

    public ProductRepresentativeApprovalService(
        ProductRepresentativeRequestLoadPort requestLoadPort,
        ProductRepresentativeRequestSavePort requestSavePort,
        ProductLoadPort productLoadPort,
        ProductSavePort productSavePort,
        ProductImageLoadPort productImageLoadPort
    ) {
        this.requestLoadPort = requestLoadPort;
        this.requestSavePort = requestSavePort;
        this.productLoadPort = productLoadPort;
        this.productSavePort = productSavePort;
        this.productImageLoadPort = productImageLoadPort;
    }

    public List<Long> requestRepresentative(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        if (distinctIds.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

        List<Product> targets = new ArrayList<>();
        for (ProductId productId : distinctIds) {
            Product product = loadOwnedProduct(shopId, productId);
            if (product.isRepresentative()) {
                continue;
            }
            if (requestLoadPort.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
                continue;
            }
            requireHasImage(productId);
            targets.add(product);
        }

        validateLimit(shopId, targets.size());

        List<Long> requestIds = new ArrayList<>();
        for (Product target : targets) {
            ProductRepresentativeRequest saved = requestSavePort.save(
                ProductRepresentativeRequest.of(target.getProductId(), shopId));
            requestIds.add(saved.getId());
        }
        return requestIds;
    }

    public void approve(ProductRepresentativeRequestId requestId) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        Product product = loadProduct(request.getProductId());

        if (!product.isRepresentative()) {
            validateApprovableLimit(request.getShopId());
        }
        requireHasImage(request.getProductId());

        request.approve();
        requestSavePort.save(request);

        product.changeRepresentative(true);
        productSavePort.save(product);
    }

    public void reject(ProductRepresentativeRequestId requestId, String rejectReason) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestSavePort.save(request);
    }

    public void cancel(ProductRepresentativeRequestId requestId) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        request.cancel();
        requestSavePort.save(request);
    }

    public void clearRepresentative(ShopId shopId, ProductId productId) {
        Product product = loadOwnedProduct(shopId, productId);
        if (!product.isRepresentative()) {
            return;
        }

        if (productLoadPort.countVisibleRepresentativeByShopId(shopId) <= 1L) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE);
        }

        product.changeRepresentative(false);
        productSavePort.save(product);
    }

    private void validateLimit(ShopId shopId, int additional) {
        if (additional <= 0) {
            return;
        }
        long current = productLoadPort.countRepresentativeByShopId(shopId);
        long pending = requestLoadPort.countByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
        if (current + pending + additional > MAX_REPRESENTATIVE_COUNT) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
        }
    }

    private void validateApprovableLimit(ShopId shopId) {
        if (productLoadPort.countRepresentativeByShopId(shopId) + 1 > MAX_REPRESENTATIVE_COUNT) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
        }
    }

    private void requireHasImage(ProductId productId) {
        if (productImageLoadPort.findRepresentativeImageFileId(productId) == null) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_REPRESENTATIVE_IMAGE_REQUIRED);
        }
    }

    private ProductRepresentativeRequest loadRequest(ProductRepresentativeRequestId requestId) {
        return requestLoadPort.findById(requestId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_REPRESENTATIVE_REQUEST_NOT_FOUND));
    }

    private Product loadProduct(ProductId productId) {
        return productLoadPort.findById(productId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
    }

    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productLoadPort.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    private List<ProductId> distinct(List<ProductId> productIds) {
        return productIds == null
            ? List.of()
            : productIds.stream().filter(Objects::nonNull).distinct().toList();
    }
}
