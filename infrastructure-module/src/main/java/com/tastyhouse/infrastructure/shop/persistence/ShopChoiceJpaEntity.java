package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 에디터 초이스 JPA 영속 모델. 순수 도메인 모델 {@code ShopChoice}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_CHOICE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopChoiceJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 선택지 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 선택지 상세 내용

    private ShopChoiceJpaEntity(ShopId shopId, String title, String content) {
        this.shopId = shopId;
        this.title = title;
        this.content = content;
    }

    static ShopChoiceJpaEntity create(ShopId shopId, String title, String content) {
        return new ShopChoiceJpaEntity(shopId, title, content);
    }

    void applyChanges(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
