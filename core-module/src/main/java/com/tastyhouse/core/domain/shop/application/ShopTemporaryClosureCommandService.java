package com.tastyhouse.core.domain.shop.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.ShopTemporaryClosure;
import com.tastyhouse.core.domain.shop.domain.repository.ShopTemporaryClosureRepository;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopTemporaryClosureCreateCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopTemporaryClosureCommandService {

    private static final long MAX_ACCUMULATED_CLOSURE_DAYS = 30;

    private final ShopTemporaryClosureRepository shopTemporaryClosureRepository;

    public Long createTemporaryClosure(ShopTemporaryClosureCreateCommand command) {
        ShopTemporaryClosure shopTemporaryClosure = ShopTemporaryClosure.of(
            command.shopId(),
            command.startDate(),
            command.endDate()
        );

        long accumulatedDays = shopTemporaryClosureRepository.findByShopId(command.shopId())
            .stream()
            .mapToLong(ShopTemporaryClosure::days)
            .sum();

        if (accumulatedDays + shopTemporaryClosure.days() > MAX_ACCUMULATED_CLOSURE_DAYS) {
            throw new BusinessException(ErrorCode.SHOP_TEMPORARY_CLOSURE_LIMIT_EXCEEDED);
        }

        ShopTemporaryClosure saved = shopTemporaryClosureRepository.save(shopTemporaryClosure);
        return saved.getId();
    }

    public void deleteTemporaryClosure(Long id) {
        shopTemporaryClosureRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_TEMPORARY_CLOSURE_NOT_FOUND));

        shopTemporaryClosureRepository.deleteById(id);
    }
}
