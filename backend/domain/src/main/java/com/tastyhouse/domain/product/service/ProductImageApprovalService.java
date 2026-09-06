package com.tastyhouse.domain.product.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.repository.ProductImageChangeRequestRepository;
import com.tastyhouse.domain.product.repository.ProductImageRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;

public class ProductImageApprovalService {
    private final ProductImageChangeRequestRepository requestRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    public ProductImageApprovalService(
        ProductImageChangeRequestRepository requestRepository,
        ProductImageRepository productImageRepository,
        ProductRepository productRepository
    ) {
        this.requestRepository = requestRepository;
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    public Long requestImageChange(ProductId productId, UploadedFileId imageFileId) {
        requireProductExists(productId);
        if (requestRepository.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ProductImageChangeRequest saved =
            requestRepository.save(ProductImageChangeRequest.of(productId, imageFileId));
        return saved.getId();
    }

    public void approve(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.approve();
        requestRepository.save(request);

        int nextSort = productImageRepository.findAllByProductId(request.getProductId()).size();
        productImageRepository.save(
            ProductImage.of(request.getProductId(), request.getImageFileId(), nextSort, true));
    }

    public void reject(ProductImageChangeRequestId requestId, String rejectReason) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestRepository.save(request);
    }

    public void cancel(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.cancel();
        requestRepository.save(request);
    }

    public void reorderImages(ProductId productId, List<Long> orderedImageIds) {
        List<ProductImage> current = productImageRepository.findAllByProductId(productId);
        Set<Long> currentIds = current.stream().map(ProductImage::getId).collect(Collectors.toSet());
        List<Long> requested = orderedImageIds == null ? List.of()
            : orderedImageIds.stream().filter(Objects::nonNull).distinct().toList();

        if (currentIds.size() != requested.size() || !currentIds.containsAll(requested)) {
            throw new BusinessException(ErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            Long imageId = requested.get(index);
            ProductImage image = current.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_IMAGE_NOT_FOUND));
            productImageRepository.save(rebuildWithSort(image, index));
        }
    }

    private ProductImage rebuildWithSort(ProductImage image, int sort) {
        return ProductImage.reconstitute(
            image.getId(),
            image.getProductId(),
            image.getImageFileId(),
            sort,
            image.isVisible()
        );
    }

    private ProductImageChangeRequest loadRequest(ProductImageChangeRequestId requestId) {
        return requestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_NOT_FOUND));
    }

    private void requireProductExists(ProductId productId) {
        if (productRepository.findById(productId).isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
