package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductRepresentativeRequest;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductRepresentativeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductRepresentativeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductRepresentativeApprovalService {
    private static final long MAX_REPRESENTATIVE_COUNT = 6L;

    private final ProductRepresentativeRequestRepository requestRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    public ProductRepresentativeApprovalService(
        ProductRepresentativeRequestRepository requestRepository,
        ProductRepository productRepository,
        ProductImageRepository productImageRepository
    ) {
        this.requestRepository = requestRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    public List<Long> requestRepresentative(ShopId shopId, List<ProductId> productIds) {
        List<ProductId> distinctIds = distinct(productIds);
        if (distinctIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }

        List<Product> targets = new ArrayList<>();
        for (ProductId productId : distinctIds) {
            Product product = loadOwnedProduct(shopId, productId);
            if (product.isRepresentative()) {
                continue;
            }
            if (requestRepository.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
                continue;
            }
            requireHasImage(productId);
            targets.add(product);
        }

        validateLimit(shopId, targets.size());

        List<Long> requestIds = new ArrayList<>();
        for (Product target : targets) {
            ProductRepresentativeRequest saved = requestRepository.save(
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
        requestRepository.save(request);

        product.changeRepresentative(true);
        productRepository.save(product);
    }

    public void reject(ProductRepresentativeRequestId requestId, String rejectReason) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestRepository.save(request);
    }

    public void cancel(ProductRepresentativeRequestId requestId) {
        ProductRepresentativeRequest request = loadRequest(requestId);
        request.cancel();
        requestRepository.save(request);
    }

    public void clearRepresentative(ShopId shopId, ProductId productId) {
        Product product = loadOwnedProduct(shopId, productId);
        if (!product.isRepresentative()) {
            return;
        }

        if (productRepository.countVisibleRepresentativeByShopId(shopId) <= 1L) {
            throw new BusinessException(ErrorCode.PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE);
        }

        product.changeRepresentative(false);
        productRepository.save(product);
    }

    private void validateLimit(ShopId shopId, int additional) {
        if (additional <= 0) {
            return;
        }
        long current = productRepository.countRepresentativeByShopId(shopId);
        long pending = requestRepository.countByShopIdAndStatus(shopId, ApprovalStatus.PENDING);
        if (current + pending + additional > MAX_REPRESENTATIVE_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
        }
    }

    private void validateApprovableLimit(ShopId shopId) {
        if (productRepository.countRepresentativeByShopId(shopId) + 1 > MAX_REPRESENTATIVE_COUNT) {
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_LIMIT_EXCEEDED);
        }
    }

    private void requireHasImage(ProductId productId) {
        if (productImageRepository.findRepresentativeImageFileId(productId) == null) {
            throw new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_IMAGE_REQUIRED);
        }
    }

    private ProductRepresentativeRequest loadRequest(ProductRepresentativeRequestId requestId) {
        return requestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_REPRESENTATIVE_REQUEST_NOT_FOUND));
    }

    private Product loadProduct(ProductId productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Product loadOwnedProduct(ShopId shopId, ProductId productId) {
        List<Product> found = productRepository.findAllByShopIdAndIdIn(shopId, List.of(productId));
        if (found.isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return found.getFirst();
    }

    private List<ProductId> distinct(List<ProductId> productIds) {
        return productIds == null
            ? List.of()
            : productIds.stream().filter(Objects::nonNull).distinct().toList();
    }
}
