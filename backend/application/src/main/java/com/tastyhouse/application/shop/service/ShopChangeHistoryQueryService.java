package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopChangeHistoryQueryUseCase;
import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryQueryPort;
import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopChangeHistorySearchCondition;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopChangeCategoryResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopChangeHistoryQueryService implements ShopChangeHistoryQueryUseCase {

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

        ShopChangeCategory categoryFilter = category == null ? null : ShopChangeCategory.from(category);
        ShopChangeType changeTypeFilter = changeType == null ? null : ShopChangeType.from(changeType);

        ShopChangeHistorySearchCondition condition = new ShopChangeHistorySearchCondition(
            shopId,
            categoryFilter,
            changeTypeFilter,
            targetDate,
            retentionFrom
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return shopChangeHistoryQueryPort.findChangeHistoryPage(condition, pageQuery);
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
        List<ShopChangeType> changeTypes = Arrays.stream(ShopChangeType.values())
            .filter(changeType -> changeType.getCategory() == category)
            .toList();
        return new ShopChangeCategoryResult(category, changeTypes);
    }
}
