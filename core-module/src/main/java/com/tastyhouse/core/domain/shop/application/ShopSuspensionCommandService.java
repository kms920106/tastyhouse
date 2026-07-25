package com.tastyhouse.core.domain.shop.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.ShopSuspension;
import com.tastyhouse.core.domain.shop.domain.repository.ShopSuspensionRepository;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopSuspensionCreateCommand;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopSuspensionCommandService {

    private final ShopSuspensionRepository shopSuspensionRepository;

    public Long createSuspension(ShopSuspensionCreateCommand command) {
        ShopSuspension shopSuspension = ShopSuspension.of(
            command.shopId(),
            command.reason(),
            command.orderMethod(),
            command.startAt(),
            command.endAt()
        );

        ShopSuspension saved = shopSuspensionRepository.save(shopSuspension);
        return saved.getId();
    }

    public void releaseSuspension(Long id, LocalDateTime releasedAt) {
        ShopSuspension shopSuspension = shopSuspensionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_SUSPENSION_NOT_FOUND));

        shopSuspension.release(releasedAt);
        shopSuspensionRepository.save(shopSuspension);
    }
}
