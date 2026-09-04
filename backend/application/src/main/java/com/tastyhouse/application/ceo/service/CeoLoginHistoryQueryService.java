package com.tastyhouse.application.ceo.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.in.CeoLoginHistoryQueryUseCase;
import com.tastyhouse.domain.ceo.model.CeoLoginResult;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryQueryPort;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistoryResult;
import com.tastyhouse.application.ceo.port.out.CeoLoginHistorySearchCondition;

/**
 * 점주 본인 로그인 이력 조회 서비스(CQRS query 측).
 *
 * <p><b>인가는 "토큰의 {@code ceoId}로만 필터한다"는 것 자체다</b> — 계정 단위 이력이라 가게에 종속되지
 * 않으므로 {@code shopId}·소유권 검증이 없다.
 *
 * <p>조회 기간 {@value #RETENTION_DAYS}일 제한을 <b>여기 한 곳에서</b> 강제한다.
 * <ul>
 *   <li>domain-module이 아닌 이유: 90일은 도메인 불변식이 아니라 조회 화면 정책이다. 90일이 지난 행도
 *       <b>삭제하지 않고 계속 보관</b>하며(고객센터 요청 시 최대 2년 조회 지원이 원 요구사항이다),
 *       조회 화면만 제한한다.</li>
 *   <li>Bean Validation만으로 불가능한 이유: "오늘 기준 -90일"이라는 상대 하한은 어노테이션으로
 *       표현할 수 없다.</li>
 *   <li>DAO 단독이 아닌 이유: DAO에서 조용히 잘라내면 사용자에게 "왜 비었는지"가 보이지 않는다.</li>
 * </ul>
 */
@Service
@CeoApp
@Transactional(readOnly = true)
public class CeoLoginHistoryQueryService implements CeoLoginHistoryQueryUseCase {

    /** 로그인 이력 조회 가능 기간(일). */
    private static final int RETENTION_DAYS = 90;

    /** 시작일 미지정 시 종료일로부터 거슬러 올라가는 기본 조회 폭(일). */
    private static final int DEFAULT_RANGE_DAYS = 29;

    private final CeoLoginHistoryQueryPort ceoLoginHistoryQueryPort;

    public CeoLoginHistoryQueryService(CeoLoginHistoryQueryPort ceoLoginHistoryQueryPort) {
        this.ceoLoginHistoryQueryPort = ceoLoginHistoryQueryPort;
    }

    /**
     * 내 로그인 이력 목록을 최신순으로 페이징 조회한다.
     */
    @Override
    public PageResult<CeoLoginHistoryResult> getLoginHistories(
        Long ceoId,
        String result,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
    ) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedEndDate = endDate == null ? today : endDate;
        LocalDate resolvedStartDate = startDate == null
            ? resolvedEndDate.minusDays(DEFAULT_RANGE_DAYS)
            : startDate;
        validateDateRange(resolvedStartDate, resolvedEndDate, today);

        CeoLoginResult resultFilter = result == null ? null : CeoLoginResult.from(result);

        CeoLoginHistorySearchCondition condition = CeoLoginHistorySearchCondition.of(
            ceoId,
            resultFilter,
            resolvedStartDate,
            resolvedEndDate
        );
        PageQuery pageQuery = PageQuery.of(page, size);

        return ceoLoginHistoryQueryPort.findLoginHistoryPage(condition, pageQuery);
    }

    /**
     * 조회 기간을 검증한다. 시작일이 종료일보다 늦거나, 기간이 미래이거나 보관 기간을 벗어나면 400으로
     * 거부한다.
     *
     * <p>기본값으로 채워진 경우에도 동일하게 검증한다 — 사용자가 종료일만 90일 밖으로 지정하면
     * 시작일이 파생되어 함께 밖으로 나가므로, 그 조합도 거부되어야 한다.
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException(ErrorCode.SHOP_REQUEST_DATE_RANGE_INVALID);
        }
        if (endDate.isAfter(today) || startDate.isBefore(today.minusDays(RETENTION_DAYS))) {
            throw new BusinessException(ErrorCode.CEO_LOGIN_HISTORY_DATE_OUT_OF_RANGE);
        }
    }
}
