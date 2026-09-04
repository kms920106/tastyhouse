package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopRequestCommentCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopRequestCommentManagementCreateCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopRequestCommentService;

/**
 * 담당자용 요청건 답변 작성 서비스(CQRS command 측).
 *
 * <p>요청 존재 확인은 도메인 서비스가 담당하고, 이 서비스는 트랜잭션 경계만 책임진다. 가게 제약이 없어
 * 소유권 검증도 없다.
 */
@Service
@AdminApp
@Transactional
public class ShopRequestCommentCommandService implements ShopRequestCommentCommandUseCase {

    private final ShopRequestCommentService shopRequestCommentService;

    public ShopRequestCommentCommandService(ShopRequestCommentService shopRequestCommentService) {
        this.shopRequestCommentService = shopRequestCommentService;
    }

    /**
     * 담당자가 요청건에 답변을 남긴다.
     *
     * @param adminId 작성자 관리자 ID({@code CustomUserDetails#getPrincipalId()})
     * @return 생성된 댓글 식별자
     */
    @Override
    public Long addComment(ShopRequestCommentManagementCreateCommand command) {
        Long requestId = command.requestId();
        Long adminId = command.adminId();
        String content = command.content();

        return shopRequestCommentService.addCommentByAdmin(requestId, adminId, content);
    }
}
