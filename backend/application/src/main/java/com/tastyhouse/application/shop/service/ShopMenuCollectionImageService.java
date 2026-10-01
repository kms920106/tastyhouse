package com.tastyhouse.application.shop.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImagePersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;

public class ShopMenuCollectionImageService {

    private static final int MAX_IMAGE_COUNT = 6;

    private final ShopMenuCollectionImagePersistencePort imagePersistencePort;
    private final ShopPersistencePort shopPersistencePort;

    public ShopMenuCollectionImageService(
        ShopMenuCollectionImagePersistencePort imagePersistencePort,
        ShopPersistencePort shopPersistencePort
    ) {
        this.imagePersistencePort = imagePersistencePort;
        this.shopPersistencePort = shopPersistencePort;
    }

    public Long register(ShopId shopId, UploadedFileId imageFileId) {
        requireShopExists(shopId);

        List<ShopMenuCollectionImage> current = imagePersistencePort.findAllByShopId(shopId);
        if (current.size() >= MAX_IMAGE_COUNT) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LIMIT_EXCEEDED);
        }

        ShopMenuCollectionImage saved =
            imagePersistencePort.save(ShopMenuCollectionImage.of(shopId, imageFileId, current.size()));
        return saved.getId();
    }

    public void approve(ShopMenuCollectionImageId imageId) {
        ShopMenuCollectionImage image = loadImage(imageId);
        image.approve();
        imagePersistencePort.save(image);
    }

    public void reject(ShopMenuCollectionImageId imageId, String rejectReason) {
        ShopMenuCollectionImage image = loadImage(imageId);
        image.reject(rejectReason);
        imagePersistencePort.save(image);
    }

    public void reorder(ShopId shopId, List<Long> orderedImageIds) {
        List<ShopMenuCollectionImage> current = imagePersistencePort.findAllByShopId(shopId);
        Set<Long> currentIds = current.stream()
            .map(ShopMenuCollectionImage::getId)
            .collect(Collectors.toSet());
        List<Long> requested = orderedImageIds == null ? List.of()
            : orderedImageIds.stream().filter(Objects::nonNull).distinct().toList();

        if (currentIds.size() != requested.size() || !currentIds.containsAll(requested)) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            Long imageId = requested.get(index);
            ShopMenuCollectionImage image = current.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));
            image.changeSort(index);
            imagePersistencePort.save(image);
        }
    }

    public void delete(ShopId shopId, ShopMenuCollectionImageId imageId) {
        List<ShopMenuCollectionImage> current = imagePersistencePort.findAllByShopId(shopId);
        ShopMenuCollectionImage target = current.stream()
            .filter(candidate -> candidate.getId().equals(imageId.value()))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));

        if (target.getStatus() == ApprovalStatus.APPROVED && countApproved(current) <= 1) {
            throw new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_LAST_CANNOT_DELETE);
        }
        imagePersistencePort.delete(target);

        renumberSort(current.stream().filter(candidate -> !candidate.getId().equals(imageId.value())).toList());
    }

    private long countApproved(List<ShopMenuCollectionImage> images) {
        return images.stream().filter(image -> image.getStatus() == ApprovalStatus.APPROVED).count();
    }

    private void renumberSort(List<ShopMenuCollectionImage> remaining) {
        for (int index = 0; index < remaining.size(); index++) {
            ShopMenuCollectionImage image = remaining.get(index);
            if (image.getSort() != index) {
                image.changeSort(index);
                imagePersistencePort.save(image);
            }
        }
    }

    private ShopMenuCollectionImage loadImage(ShopMenuCollectionImageId imageId) {
        return imagePersistencePort.findById(imageId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));
    }

    private void requireShopExists(ShopId shopId) {
        if (shopPersistencePort.findById(shopId).isEmpty()) {
            throw new BusinessException(ErrorCode.SHOP_NOT_FOUND);
        }
    }
}
