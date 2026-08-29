package com.tastyhouse.adminapi.shop.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.ShopRequestCommentResult;
import com.tastyhouse.application.shop.port.out.ShopRequestQueryPort;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.ShopRequestCommentResponse;
import com.tastyhouse.adminapi.shop.application.port.in.ShopRequestCommentQueryUseCase;

/**
 * 담당자용 요청건 문의 스레드 조회 서비스(CQRS query 측).
 *
 * <p>가게 제약이 없다 — 관리자는 모든 가게의 요청을 처리하므로 소유권 검증 대상이 아니다.
 *
 * <p>스레드를 읽기 전에 요청 존재를 먼저 확인한다. 댓글 조회만 하면 <b>없는 요청에도 빈 배열 200</b>이
 * 내려가 담당자가 "문의가 없는 요청"과 "잘못된 requestId"를 구분할 수 없다.
 */
@Service
@Transactional(readOnly = true)
public class ShopRequestCommentQueryService implements ShopRequestCommentQueryUseCase {

    private final ShopRequestQueryPort shopRequestQueryPort;

    public ShopRequestCommentQueryService(ShopRequestQueryPort shopRequestQueryPort) {
        this.shopRequestQueryPort = shopRequestQueryPort;
    }

    /**
     * 요청건 문의 스레드를 작성순으로 조회한다.
     */
    @Override
    public List<ShopRequestCommentResponse> getComments(Long requestId) {
        shopRequestQueryPort.findRequestDetail(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_REQUEST_NOT_FOUND));

        return shopRequestQueryPort.findComments(requestId).stream()
            .map(this::toCommentResponse)
            .toList();
    }

    private ShopRequestCommentResponse toCommentResponse(ShopRequestCommentResult result) {
        return ShopRequestCommentResponse.from(
            result.commentId(),
            result.authorType().name(),
            result.authorType().getDescription(),
            result.content(),
            result.createdAt()
        );
    }
}
