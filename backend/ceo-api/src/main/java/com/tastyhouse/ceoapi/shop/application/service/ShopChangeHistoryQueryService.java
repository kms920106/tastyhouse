package com.tastyhouse.ceoapi.shop.application.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopChangeCategory;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.infrastructure.shop.query.ShopChangeHistoryQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopChangeHistoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopChangeHistorySearchCondition;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopChangeCategoryResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopChangeHistoryListItemResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopChangeTypeResponse;

/**
 * 점주용 가게 변경이력 조회 서비스(CQRS query 측).
 *
 * <p>조회 기간 6개월 제한을 <b>여기 한 곳에서</b> 강제한다.
 * <ul>
 *   <li>domain-module이 아닌 이유: 6개월은 도메인 불변식이 아니라 조회 화면 정책이다. 6개월 지난 변경도
 *       유효하게 일어난 사실이므로 기록·저장은 제한하지 않는다.</li>
 *   <li>Bean Validation만으로 불가능한 이유: {@code @PastOrPresent}는 상한만 막고, "오늘 기준 -6개월"이라는
 *       상대 하한은 어노테이션으로 표현할 수 없다.</li>
 *   <li>DAO 단독이 아닌 이유: DAO에서 조용히 잘라내면 사용자에게 "왜 비었는지"가 보이지 않는다. 여기서
 *       명시적으로 400을 던진다.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ShopChangeHistoryQueryService {

    /** 변경이력 조회 가능 기간(개월). */
    private static final int RETENTION_MONTHS = 6;

    private final ShopChangeHistoryQueryDao shopChangeHistoryQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopChangeHistoryQueryService(
        ShopChangeHistoryQueryDao shopChangeHistoryQueryDao,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopChangeHistoryQueryDao = shopChangeHistoryQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    /**
     * 내 가게 변경이력 목록을 최신순으로 페이징 조회한다.
     *
     * <p>소유권 검증을 가장 먼저 수행한다 — 생략하면 남의 가게 변경이력이 통째로 새는 IDOR가 된다.
     */
    public PaginationResponse<ShopChangeHistoryListItemResponse> getChangeHistories(
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

        PageResult<ShopChangeHistoryListItemResponse> pageResult =
            shopChangeHistoryQueryDao.findChangeHistoryPage(condition, pageQuery)
                .map(this::toListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 필터 드롭다운용 대분류·중분류 카탈로그. 가게에 종속되지 않는 정적 목록이라 소유권 검증이 없다.
     */
    public List<ShopChangeCategoryResponse> getChangeHistoryTypes() {
        return Arrays.stream(ShopChangeCategory.values())
            .map(this::toCategoryResponse)
            .toList();
    }

    /**
     * 조회 날짜를 확정한다. 미지정이면 오늘이고, 미래이거나 보관 기간을 벗어나면 400으로 거부한다.
     */
    private LocalDate resolveChangedDate(LocalDate changedDate, LocalDate today) {
        if (changedDate == null) {
            return today;
        }
        if (changedDate.isAfter(today) || changedDate.isBefore(today.minusMonths(RETENTION_MONTHS))) {
            throw new BusinessException(ErrorCode.SHOP_CHANGE_HISTORY_DATE_OUT_OF_RANGE);
        }
        return changedDate;
    }

    private ShopChangeCategoryResponse toCategoryResponse(ShopChangeCategory category) {
        List<ShopChangeTypeResponse> changeTypes = Arrays.stream(ShopChangeType.values())
            .filter(changeType -> changeType.getCategory() == category)
            .map(this::toChangeTypeResponse)
            .toList();
        return ShopChangeCategoryResponse.from(
            category.name(),
            category.getDescription(),
            changeTypes
        );
    }

    private ShopChangeTypeResponse toChangeTypeResponse(ShopChangeType changeType) {
        return ShopChangeTypeResponse.from(
            changeType.name(),
            changeType.getDescription()
        );
    }

    private ShopChangeHistoryListItemResponse toListItemResponse(ShopChangeHistoryResult result) {
        return ShopChangeHistoryListItemResponse.from(
            result.id(),
            result.category().name(),
            result.category().getDescription(),
            result.changeType().name(),
            result.changeType().getDescription(),
            result.actionType().name(),
            result.actionType().getDescription(),
            result.previousValue(),
            result.newValue(),
            result.changedAt()
        );
    }
}
