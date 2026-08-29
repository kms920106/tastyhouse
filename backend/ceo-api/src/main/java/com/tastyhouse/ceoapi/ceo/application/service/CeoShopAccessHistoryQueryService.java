package com.tastyhouse.ceoapi.ceo.application.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.ceo.adapter.in.web.response.CeoShopAccessHistoryListItemResponse;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.domain.shop.model.ShopCeoAssignmentActionType;
import com.tastyhouse.infrastructure.shop.query.ShopCeoAssignmentHistoryQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopCeoAssignmentHistoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopCeoAssignmentHistorySearchCondition;

/**
 * 점주 본인 시스템 접근권한 이력 조회 서비스(CQRS query 측).
 *
 * <p><b>인가는 "토큰의 {@code ceoId}로만 필터한다"는 것 자체다.</b> {@code shopId} 파라미터에 소유권
 * 검증을 걸지 않는 이유는 내 {@code ceo_id} 이력만 조회하기 때문이다 — 남의 가게 id를 넣으면 빈 목록이
 * 되고, 그래서 가게 존재 여부가 새지 않는다.
 *
 * <p>조회 기간 {@value #RETENTION_YEARS}년 제한을 여기 한 곳에서 강제한다(판정 위치의 근거는
 * {@link CeoLoginHistoryQueryService}와 같다).
 */
@Service
@Transactional(readOnly = true)
public class CeoShopAccessHistoryQueryService {

    /** 접근권한 이력 조회 가능 기간(년). */
    private static final int RETENTION_YEARS = 5;

    /** 시작일 미지정 시 종료일로부터 거슬러 올라가는 기본 조회 폭(년). */
    private static final int DEFAULT_RANGE_YEARS = 1;

    private final ShopCeoAssignmentHistoryQueryDao shopCeoAssignmentHistoryQueryDao;

    public CeoShopAccessHistoryQueryService(
        ShopCeoAssignmentHistoryQueryDao shopCeoAssignmentHistoryQueryDao
    ) {
        this.shopCeoAssignmentHistoryQueryDao = shopCeoAssignmentHistoryQueryDao;
    }

    /**
     * 내 시스템 접근권한 이력 목록을 최신순으로 페이징 조회한다.
     */
    public PaginationResponse<CeoShopAccessHistoryListItemResponse> getShopAccessHistories(
        Long ceoId,
        String actionType,
        Long shopId,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedEndDate = endDate == null ? today : endDate;
        LocalDate resolvedStartDate = startDate == null
            ? resolvedEndDate.minusYears(DEFAULT_RANGE_YEARS)
            : startDate;
        validateDateRange(resolvedStartDate, resolvedEndDate, today);

        ShopCeoAssignmentActionType actionTypeFilter = actionType == null
            ? null
            : ShopCeoAssignmentActionType.from(actionType);

        ShopCeoAssignmentHistorySearchCondition condition = ShopCeoAssignmentHistorySearchCondition.of(
            ceoId,
            shopId,
            actionTypeFilter,
            resolvedStartDate,
            resolvedEndDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<CeoShopAccessHistoryListItemResponse> pageResult =
            shopCeoAssignmentHistoryQueryDao.findShopAccessHistoryPage(condition, pageQuery)
                .map(this::toListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    /**
     * 조회 기간을 검증한다. 시작일이 종료일보다 늦거나, 기간이 미래이거나 보관 기간을 벗어나면 400으로
     * 거부한다.
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
        if (endDate.isAfter(today) || startDate.isBefore(today.minusYears(RETENTION_YEARS))) {
            throw new BusinessException(ErrorCode.CEO_SHOP_ACCESS_HISTORY_DATE_OUT_OF_RANGE);
        }
    }

    private CeoShopAccessHistoryListItemResponse toListItemResponse(
        ShopCeoAssignmentHistoryResult result
    ) {
        return CeoShopAccessHistoryListItemResponse.from(
            result.id(),
            result.shopId(),
            result.shopName(),
            result.actionType().name(),
            result.actionType().getDescription(),
            result.occurredAt()
        );
    }
}
