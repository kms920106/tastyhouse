package com.tastyhouse.application.product.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductImagePersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;

public class ProductImageApprovalService {

    private final ProductImageChangeRequestPersistencePort requestPersistencePort;
    private final ProductImagePersistencePort productImagePersistencePort;
    private final ProductPersistencePort productPersistencePort;

    public ProductImageApprovalService(
        ProductImageChangeRequestPersistencePort requestPersistencePort,
        ProductImagePersistencePort productImagePersistencePort,
        ProductPersistencePort productPersistencePort
    ) {
        this.requestPersistencePort = requestPersistencePort;
        this.productImagePersistencePort = productImagePersistencePort;
        this.productPersistencePort = productPersistencePort;
    }

    public Long requestImageChange(ProductId productId, UploadedFileId imageFileId) {
        requireProductExists(productId);
        if (requestPersistencePort.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ProductImageChangeRequest saved =
            requestPersistencePort.save(ProductImageChangeRequest.of(productId, imageFileId));
        return saved.getId();
    }

    public void approve(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.approve();
        requestPersistencePort.save(request);

        int nextSort = productImagePersistencePort.findAllByProductId(request.getProductId()).size();
        productImagePersistencePort.save(
            ProductImage.of(request.getProductId(), request.getImageFileId(), nextSort, true));
    }

    public void reject(ProductImageChangeRequestId requestId, String rejectReason) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestPersistencePort.save(request);
    }

    public void cancel(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.cancel();
        requestPersistencePort.save(request);
    }

    public void reorderImages(ProductId productId, List<Long> orderedImageIds) {
        List<ProductImage> current = productImagePersistencePort.findAllByProductId(productId);
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
            productImagePersistencePort.save(rebuildWithSort(image, index));
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
        return requestPersistencePort.findById(requestId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_NOT_FOUND));
    }

    private void requireProductExists(ProductId productId) {
        if (productPersistencePort.findById(productId).isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
