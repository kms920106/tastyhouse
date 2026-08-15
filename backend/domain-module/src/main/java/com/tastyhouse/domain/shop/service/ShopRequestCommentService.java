package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthor;
import com.tastyhouse.domain.shop.repository.ShopRequestCommentRepository;

/**
 * 요청건 문의 스레드 작성을 소유하는 도메인 서비스.
 *
 * <p>담는 불변식은 "댓글은 실재하는 요청에만 달린다"이다 — 그 확인을 위해 인덱스 행을 로드해야 하므로
 * {@link ShopRequestIndexRecorder}를 경유한다. 점주 경로는 스레드가 <b>경로의 가게에 속하는지</b>까지
 * 재검증하고, 불일치는 403이 아니라 404로 응답한다(다른 가게 요청의 존재를 흘리지 않는다). 관리자 경로는
 * 가게 제약이 없다.
 *
 * <p><b>상태 제약을 두지 않는다.</b> 반려·취소·승인 이후에도 작성할 수 있어야 한다 — 반려 사유 문의가 이
 * 기능의 주요 사용례다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code ShopDomainConfig}가 담당한다.
 */
public class ShopRequestCommentService {

    private final ShopRequestCommentRepository shopRequestCommentRepository;
    private final ShopRequestIndexRecorder shopRequestIndexRecorder;

    public ShopRequestCommentService(
        ShopRequestCommentRepository shopRequestCommentRepository,
        ShopRequestIndexRecorder shopRequestIndexRecorder
    ) {
        this.shopRequestCommentRepository = shopRequestCommentRepository;
        this.shopRequestIndexRecorder = shopRequestIndexRecorder;
    }

    /**
     * 점주가 자기 가게 요청에 문의를 남긴다.
     *
     * @return 생성된 댓글 식별자
     */
    public Long addCommentByCeo(Long requestId, Long shopId, Long ceoId, String content) {
        shopRequestIndexRecorder.getRequestOfShop(requestId, shopId);
        return save(requestId, ShopRequestCommentAuthor.ceo(ceoId), content);
    }

    /**
     * 담당자가 요청에 답변을 남긴다. 가게 제약이 없다.
     *
     * @return 생성된 댓글 식별자
     */
    public Long addCommentByAdmin(Long requestId, Long adminId, String content) {
        shopRequestIndexRecorder.getRequest(requestId);
        return save(requestId, ShopRequestCommentAuthor.admin(adminId), content);
    }

    private Long save(Long requestId, ShopRequestCommentAuthor author, String content) {
        ShopRequestComment saved = shopRequestCommentRepository.save(
            ShopRequestComment.of(requestId, author, content)
        );
        return saved.getId();
    }
}
