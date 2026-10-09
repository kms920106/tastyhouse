package com.tastyhouse.application.product.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.model.ProductImageChangeRequest;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductImageChangeRequestId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductImageChangeRequestSavePort;
import com.tastyhouse.application.product.port.out.write.ProductImageLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductImageSavePort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

@Service
public class ProductImageApprovalService {

    private final ProductImageChangeRequestLoadPort requestLoadPort;
    private final ProductImageChangeRequestSavePort requestSavePort;
    private final ProductImageLoadPort productImageLoadPort;
    private final ProductImageSavePort productImageSavePort;
    private final ProductLoadPort productLoadPort;

    public ProductImageApprovalService(
        ProductImageChangeRequestLoadPort requestLoadPort,
        ProductImageChangeRequestSavePort requestSavePort,
        ProductImageLoadPort productImageLoadPort,
        ProductImageSavePort productImageSavePort,
        ProductLoadPort productLoadPort
    ) {
        this.requestLoadPort = requestLoadPort;
        this.requestSavePort = requestSavePort;
        this.productImageLoadPort = productImageLoadPort;
        this.productImageSavePort = productImageSavePort;
        this.productLoadPort = productLoadPort;
    }

    public Long requestImageChange(ProductId productId, UploadedFileId imageFileId) {
        requireProductExists(productId);
        if (requestLoadPort.existsByProductIdAndStatus(productId, ApprovalStatus.PENDING)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ProductImageChangeRequest saved =
            requestSavePort.save(ProductImageChangeRequest.of(productId, imageFileId));
        return saved.getId();
    }

    public void approve(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.approve();
        requestSavePort.save(request);

        int nextSort = productImageLoadPort.findAllByProductId(request.getProductId()).size();
        productImageSavePort.save(
            ProductImage.of(request.getProductId(), request.getImageFileId(), nextSort, true));
    }

    public void reject(ProductImageChangeRequestId requestId, String rejectReason) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.reject(rejectReason);
        requestSavePort.save(request);
    }

    public void cancel(ProductImageChangeRequestId requestId) {
        ProductImageChangeRequest request = loadRequest(requestId);
        request.cancel();
        requestSavePort.save(request);
    }

    public void reorderImages(ProductId productId, List<Long> orderedImageIds) {
        List<ProductImage> current = productImageLoadPort.findAllByProductId(productId);
        Set<Long> currentIds = current.stream().map(ProductImage::getId).collect(Collectors.toSet());
        List<Long> requested = orderedImageIds == null ? List.of()
            : orderedImageIds.stream().filter(Objects::nonNull).distinct().toList();

        if (currentIds.size() != requested.size() || !currentIds.containsAll(requested)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            Long imageId = requested.get(index);
            ProductImage image = current.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_IMAGE_NOT_FOUND));
            productImageSavePort.save(rebuildWithSort(image, index));
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
        return requestLoadPort.findById(requestId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.PRODUCT_IMAGE_CHANGE_REQUEST_NOT_FOUND));
    }

    private void requireProductExists(ProductId productId) {
        if (productLoadPort.findById(productId).isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_NOT_FOUND);
        }
    }
}
