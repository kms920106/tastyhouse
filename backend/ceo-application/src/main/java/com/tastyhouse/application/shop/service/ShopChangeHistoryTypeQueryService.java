package com.tastyhouse.application.shop.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.application.shared.port.out.CodeLabelResult;
import com.tastyhouse.application.shop.port.in.ShopChangeHistoryTypeQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopChangeCategoryResult;

@Service
@Transactional(readOnly = true)
class ShopChangeHistoryTypeQueryService implements ShopChangeHistoryTypeQueryUseCase {

    @Override
    public List<ShopChangeCategoryResult> getChangeHistoryTypes() {
        return Arrays.stream(ShopChangeCategory.values())
            .map(this::toCategoryResult)
            .toList();
    }

    private ShopChangeCategoryResult toCategoryResult(ShopChangeCategory category) {
        List<CodeLabelResult> changeTypes = Arrays.stream(ShopChangeType.values())
            .filter(changeType -> changeType.getCategory() == category)
            .map(changeType -> new CodeLabelResult(changeType.name(), changeType.getDescription()))
            .toList();
        return new ShopChangeCategoryResult(category.name(), category.getDescription(), changeTypes);
    }
}
