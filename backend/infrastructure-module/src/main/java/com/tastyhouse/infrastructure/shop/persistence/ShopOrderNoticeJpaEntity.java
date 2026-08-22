package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 주문안내 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ShopOrderNotice}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사
 * 필드)만 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code ShopOrderNoticeMapper}가
 * 수행한다.
 *
 * <p><b>{@code shop_id}에 유니크 제약이 있다.</b> "가게당 주문안내 1건"은 애플리케이션 검증만으로
 * 지키면 동시 요청 두 건이 각각 "기존 행 없음"을 읽고 둘 다 insert하는 경합이 남는다. 유니크 인덱스가
 * 그 창을 닫아, 도메인 서비스의 선행 조회는 "정상 수정 경로로 유도하는 편의"만 담당하면 된다.
 * {@code ShopNotice}가 부분 유니크 인덱스로 불변식을 강제할 수 없었던 것과 대비되는데, 여기서는
 * 조건 없는 컬럼 단위 유니크라 MySQL로도 표현된다.
 */
@Entity
@Table(
    name = "SHOP_ORDER_NOTICE",
    uniqueConstraints = @UniqueConstraint(name = "uk_shop_order_notice_shop_id", columnNames = "shop_id")
)
public class ShopOrderNoticeJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Column(name = "hidden_reason", length = 500)
    private String hiddenReason;

    protected ShopOrderNoticeJpaEntity() {
    }

    private ShopOrderNoticeJpaEntity(Long shopId, String content, boolean hidden, String hiddenReason) {
        this.shopId = shopId;
        this.content = content;
        this.hidden = hidden;
        this.hiddenReason = hiddenReason;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code ShopOrderNoticeMapper#toEntity}에서만 호출한다.
     */
    static ShopOrderNoticeJpaEntity create(Long shopId, String content, boolean hidden, String hiddenReason) {
        return new ShopOrderNoticeJpaEntity(shopId, content, hidden, hiddenReason);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체).
     * 감사 필드·식별자·shopId는 건드리지 않는다.
     */
    void applyChanges(String content, boolean hidden, String hiddenReason) {
        this.content = content;
        this.hidden = hidden;
        this.hiddenReason = hiddenReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public String getHiddenReason() {
        return this.hiddenReason;
    }
}
