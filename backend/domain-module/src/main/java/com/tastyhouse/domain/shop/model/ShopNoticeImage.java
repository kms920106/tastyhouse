package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;

/**
 * 점주 공지 첨부 이미지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopNoticeImageJpaEntity} + {@code ShopNoticeImageMapper}가 담당한다. update 경로가 없는
 * 불변 애그리거트이므로 전 필드가 {@code final}이고 감사 시각을 소비하지 않는다 — 수정은 부분 갱신이
 * 아니라 replace-all(전량 삭제 후 재삽입)로 처리한다.
 */
public class ShopNoticeImage {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final Long shopNoticeId;
    private final UploadedFileId imageFileId;
    private final int sortOrder;

    private ShopNoticeImage(Long id, Long shopNoticeId, UploadedFileId imageFileId, int sortOrder) {
        this.id = id;
        this.shopNoticeId = shopNoticeId;
        this.imageFileId = imageFileId;
        this.sortOrder = sortOrder;
    }

    /**
     * 신규 공지 이미지를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static ShopNoticeImage of(Long shopNoticeId, UploadedFileId imageFileId, int sortOrder) {
        return new ShopNoticeImage(null, shopNoticeId, imageFileId, sortOrder);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopNoticeImage reconstitute(Long id, Long shopNoticeId, UploadedFileId imageFileId, int sortOrder) {
        return new ShopNoticeImage(id, shopNoticeId, imageFileId, sortOrder);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopNoticeId() {
        return this.shopNoticeId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public int getSortOrder() {
        return this.sortOrder;
    }
}
