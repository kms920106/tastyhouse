package com.tastyhouse.core.domain.shop.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.shop.domain.model.ShopHygieneBadge;
import com.tastyhouse.core.domain.shop.domain.repository.ShopHygieneBadgeRepository;
import com.tastyhouse.core.domain.shop.application.dto.command.ShopHygieneBadgeCreateCommand;
import com.tastyhouse.core.domain.shop.application.dto.result.ShopHygieneBadgeResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

/**
 * 가게 위생 인증 뱃지 등록/삭제 커맨드 서비스. admin 전용이며 수정(update)은 지원하지 않는다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ShopHygieneBadgeCommandService {

    private final ShopHygieneBadgeRepository shopHygieneBadgeRepository;

    public ShopHygieneBadgeResult create(ShopHygieneBadgeCreateCommand command) {
        ShopHygieneBadge saved = shopHygieneBadgeRepository.save(
            ShopHygieneBadge.of(command.shopId(), command.badgeType(), command.certifiedDate(), command.lastInspectionMonth())
        );
        return ShopHygieneBadgeResult.from(saved);
    }

    public void delete(Long id) {
        shopHygieneBadgeRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SHOP_HYGIENE_BADGE_NOT_FOUND));
        shopHygieneBadgeRepository.deleteById(id);
    }
}
