package com.tastyhouse.application.shop.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.application.shared.port.out.CodeLabelResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopChangeHistoryQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopChangeCategoryResult;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryQueryPort;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopChangeHistorySearchCondition;

@Service
@Transactional(readOnly = true)
class ShopChangeHistoryQueryService implements ShopChangeHistoryQueryUseCase {

    private static final int RETENTION_MONTHS = 6;

    private final ShopChangeHistoryQueryPort shopChangeHistoryQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopChangeHistoryQueryService(
        ShopChangeHistoryQueryPort shopChangeHistoryQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopChangeHistoryQueryPort = shopChangeHistoryQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ShopChangeHistoryResult> getChangeHistories(
        Long ceoId,
        Long shopId,
        String category,
        String changeType,
        LocalDate changedDate,
        int page,
        int size
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        LocalDate today = LocalDate.now();
        LocalDate targetDate = resolveChangedDate(changedDate, today);
        LocalDate retentionFrom = today.minusMonths(RETENTION_MONTHS);

        String categoryFilter = category == null ? null : ShopChangeCategory.from(category).name();
        String changeTypeFilter = changeType == null ? null : ShopChangeType.from(changeType).name();

        ShopChangeHistorySearchCondition condition = new ShopChangeHistorySearchCondition(
            shopId,
            categoryFilter,
            changeTypeFilter,
            targetDate,
            retentionFrom
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return shopChangeHistoryQueryPort.findChangeHistoryPage(condition, pageQuery)
            .map(history -> history.withDescriptions(
                history.category() == null ? null : ShopChangeCategory.valueOf(history.category()).getDescription(),
                history.changeType() == null ? null : ShopChangeType.valueOf(history.changeType()).getDescription(),
                history.actionType() == null ? null : ShopChangeActionType.valueOf(history.actionType()).getDescription()
            ));
    }

    @Override
    public List<ShopChangeCategoryResult> getChangeHistoryTypes() {
        return Arrays.stream(ShopChangeCategory.values())
            .map(this::toCategoryResult)
            .toList();
    }

    private LocalDate resolveChangedDate(LocalDate changedDate, LocalDate today) {
        if (changedDate == null) {
            return today;
        }
        if (changedDate.isAfter(today) || changedDate.isBefore(today.minusMonths(RETENTION_MONTHS))) {
            throw new BusinessException(ErrorCode.SHOP_CHANGE_HISTORY_DATE_OUT_OF_RANGE);
        }
        return changedDate;
    }

    private ShopChangeCategoryResult toCategoryResult(ShopChangeCategory category) {
        List<CodeLabelResult> changeTypes = Arrays.stream(ShopChangeType.values())
            .filter(changeType -> changeType.getCategory() == category)
            .map(changeType -> new CodeLabelResult(changeType.name(), changeType.getDescription()))
            .toList();
        return new ShopChangeCategoryResult(category.name(), category.getDescription(), changeTypes);
    }
}
