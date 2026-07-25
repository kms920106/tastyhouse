package com.tastyhouse.core.domain.shop.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.Shop;
import com.tastyhouse.core.domain.shop.domain.model.ShopImageChangeRequest;
import com.tastyhouse.core.domain.shop.domain.model.ShopImageType;
import com.tastyhouse.core.domain.shop.domain.repository.ShopImageChangeRequestRepository;
import com.tastyhouse.core.domain.shop.domain.repository.ShopRepository;
import com.tastyhouse.core.domain.shop.domain.vo.ShopId;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopImageChangeRequestCreateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.model.ApprovalStatus;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopImageChangeCommandService {

    private final ShopImageChangeRequestRepository shopImageChangeRequestRepository;
    private final ShopRepository shopRepository;

    public Long requestImageChange(ShopImageChangeRequestCreateCommand command) {
        if (shopImageChangeRequestRepository.existsByShopIdAndImageTypeAndStatus(
            command.shopId(), command.imageType(), ApprovalStatus.PENDING)) {
            throw new BusinessException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_ALREADY_PENDING);
        }

        ShopImageChangeRequest saved = shopImageChangeRequestRepository.save(
            ShopImageChangeRequest.of(command.shopId(), command.imageType(), command.imageFileId())
        );
        return saved.getId();
    }

    public void approveImageChange(Long id) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.approve();
        shopImageChangeRequestRepository.save(shopImageChangeRequest);

        ShopId shopId = ShopId.of(shopImageChangeRequest.getShopId());
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (shopImageChangeRequest.getImageType() == ShopImageType.TRADEMARK) {
            shop.changeTrademarkImage(shopImageChangeRequest.getImageFileId());
        } else {
            shop.changeThumbnailImage(shopImageChangeRequest.getImageFileId());
        }
        shopRepository.save(shop);
    }

    public void rejectImageChange(Long id, String reason) {
        ShopImageChangeRequest shopImageChangeRequest = shopImageChangeRequestRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_IMAGE_CHANGE_REQUEST_NOT_FOUND));
        shopImageChangeRequest.reject(reason);
        shopImageChangeRequestRepository.save(shopImageChangeRequest);
    }
}
