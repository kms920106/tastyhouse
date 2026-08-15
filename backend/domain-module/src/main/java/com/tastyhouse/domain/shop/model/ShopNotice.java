package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 점주 공지(사장님 공지) 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopNoticeJpaEntity} + {@code ShopNoticeMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code ShopNoticeRepository#save}를 호출해야 한다.
 *
 * <p>상태 필드가 둘이다. {@code exposed}는 점주가 제어하는 "앱에 반영" 토글로 가게당 최대 1건만
 * {@code true}일 수 있는 <b>집합 불변식</b>이라 단일 애그리거트 연산이 아니며, 그 불변식은
 * {@code ShopNoticeExposureService}가 소유한다. {@code hidden}은 관리자가 규정 위반 시 내리는 사후
 * 게시중단으로 {@code SHOP_CONTENT_BOARD.hidden}과 동형이다. 사용자 노출 조건은
 * {@code exposed = true AND hidden = false}이다.
 */
public class ShopNotice {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private String content;
    private boolean exposed; // 앱 노출 (가게당 최대 1건)
    private boolean hidden; // 관리자 게시중단
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private ShopNotice(
        Long id,
        ShopId shopId,
        String content,
        boolean exposed,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.content = content;
        this.exposed = exposed;
        this.hidden = hidden;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 공지를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static ShopNotice of(ShopId shopId, String content) {
        return new ShopNotice(null, shopId, content, false, false, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static ShopNotice reconstitute(
        Long id,
        ShopId shopId,
        String content,
        boolean exposed,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopNotice(id, shopId, content, exposed, hidden, createdAt, updatedAt);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    /**
     * 앱에 노출한다. 가게당 1건 불변식은 이 애그리거트가 아니라
     * {@code ShopNoticeExposureService}가 지킨다.
     */
    public void expose() {
        this.exposed = true;
    }

    public void unexpose() {
        this.exposed = false;
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isExposed() {
        return this.exposed;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
