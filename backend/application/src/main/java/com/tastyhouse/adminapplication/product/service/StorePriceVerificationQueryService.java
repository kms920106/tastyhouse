package com.tastyhouse.adminapplication.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationListItemResult;
import com.tastyhouse.application.product.port.out.StorePriceVerificationQueryPort;
import com.tastyhouse.adminapplication.product.port.in.StorePriceVerificationQueryUseCase;

/**
 * 매장 가격 인증 요청 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 요청을 상태로 필터해 조회한다 — 관리자는 모든 가게의 요청을 본다.
 *
 * <p>가격표 이미지 URL은 infra query DAO가 조인·변환으로 완성하므로 여기서 파일을 재조회하지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class StorePriceVerificationQueryService implements StorePriceVerificationQueryUseCase {

    private final StorePriceVerificationQueryPort storePriceVerificationQueryPort;

    public StorePriceVerificationQueryService(StorePriceVerificationQueryPort storePriceVerificationQueryPort) {
        this.storePriceVerificationQueryPort = storePriceVerificationQueryPort;
    }

    @Override
    public PageResult<StorePriceVerificationListItemResult> getVerifications(
        String status,
        int page,
        int size
    ) {
        StorePriceVerificationStatus verificationStatus = promoteStatus(status);

        return storePriceVerificationQueryPort.findVerificationPage(verificationStatus, PageQuery.of(page, size));
    }

    /**
     * 인증 요청 상세의 헤더 — 대상 항목은 {@link #getVerificationItems(Long)}가 <b>별도 쿼리로</b>
     * 조회한다. 한 쿼리로 조인하면 요청 1건이 항목 수만큼 부풀고, 항목 0건인 요청이 조인에서 탈락한다.
     */
    @Override
    public StorePriceVerificationListItemResult getVerification(Long verificationId) {
        return storePriceVerificationQueryPort.findVerificationById(verificationId)
            .orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.SHOP_STORE_PRICE_VERIFICATION_NOT_FOUND));
    }

    @Override
    public List<StorePriceVerificationItemResult> getVerificationItems(Long verificationId) {
        return storePriceVerificationQueryPort.findVerificationItems(verificationId);
    }

    /**
     * HTTP 경계에서 {@code String}으로 받은 상태를 도메인 enum으로 승격한다. 미지정(null)은 전체
     * 조회를 뜻하므로 그대로 통과시킨다 — 승격은 application 계층의 책임이라 챕터 06에서도 남는다
     * (컨트롤러는 domain-free다).
     */
    private StorePriceVerificationStatus promoteStatus(String status) {
        return status == null ? null : StorePriceVerificationStatus.from(status);
    }
}
