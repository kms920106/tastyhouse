package com.tastyhouse.application.shop.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopMenuCollectionImage;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopMenuCollectionImageId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopMenuCollectionImageSavePort;

@Service
public class ShopMenuCollectionImageService {

    private static final int MAX_IMAGE_COUNT = 6;

    private final ShopMenuCollectionImageLoadPort imageLoadPort;
    private final ShopMenuCollectionImageSavePort imageSavePort;
    private final ShopLoadPort shopLoadPort;

    public ShopMenuCollectionImageService(
        ShopMenuCollectionImageLoadPort imageLoadPort,
        ShopMenuCollectionImageSavePort imageSavePort,
        ShopLoadPort shopLoadPort
    ) {
        this.imageLoadPort = imageLoadPort;
        this.imageSavePort = imageSavePort;
        this.shopLoadPort = shopLoadPort;
    }

    public Long register(ShopId shopId, UploadedFileId imageFileId) {
        requireShopExists(shopId);

        List<ShopMenuCollectionImage> current = imageLoadPort.findAllByShopId(shopId);
        if (current.size() >= MAX_IMAGE_COUNT) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_MENU_COLLECTION_IMAGE_LIMIT_EXCEEDED);
        }

        ShopMenuCollectionImage saved =
            imageSavePort.save(ShopMenuCollectionImage.of(shopId, imageFileId, current.size()));
        return saved.getId();
    }

    public void approve(ShopMenuCollectionImageId imageId) {
        ShopMenuCollectionImage image = loadImage(imageId);
        image.approve();
        imageSavePort.save(image);
    }

    public void reject(ShopMenuCollectionImageId imageId, String rejectReason) {
        ShopMenuCollectionImage image = loadImage(imageId);
        image.reject(rejectReason);
        imageSavePort.save(image);
    }

    public void reorder(ShopId shopId, List<Long> orderedImageIds) {
        List<ShopMenuCollectionImage> current = imageLoadPort.findAllByShopId(shopId);
        Set<Long> currentIds = current.stream()
            .map(ShopMenuCollectionImage::getId)
            .collect(Collectors.toSet());
        List<Long> requested = orderedImageIds == null ? List.of()
            : orderedImageIds.stream().filter(Objects::nonNull).distinct().toList();

        if (currentIds.size() != requested.size() || !currentIds.containsAll(requested)) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_MENU_COLLECTION_IMAGE_ORDER_TARGET_MISMATCH);
        }

        for (int index = 0; index < requested.size(); index++) {
            Long imageId = requested.get(index);
            ShopMenuCollectionImage image = current.stream()
                .filter(candidate -> candidate.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));
            image.changeSort(index);
            imageSavePort.save(image);
        }
    }

    public void delete(ShopId shopId, ShopMenuCollectionImageId imageId) {
        List<ShopMenuCollectionImage> current = imageLoadPort.findAllByShopId(shopId);
        ShopMenuCollectionImage target = current.stream()
            .filter(candidate -> candidate.getId().equals(imageId.value()))
            .findFirst()
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));

        if (target.getStatus() == ApprovalStatus.APPROVED && countApproved(current) <= 1) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_MENU_COLLECTION_IMAGE_LAST_CANNOT_DELETE);
        }
        imageSavePort.delete(target);

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
                imageSavePort.save(image);
            }
        }
    }

    private ShopMenuCollectionImage loadImage(ShopMenuCollectionImageId imageId) {
        return imageLoadPort.findById(imageId)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.SHOP_MENU_COLLECTION_IMAGE_NOT_FOUND));
    }

    private void requireShopExists(ShopId shopId) {
        if (shopLoadPort.findById(shopId).isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_NOT_FOUND);
        }
    }
}
