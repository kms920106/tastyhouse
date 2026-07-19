package com.tastyhouse.core.domain.shop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "SHOP_CHOICE")
public class ShopChoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 선택지 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // 선택지 상세 내용

    private ShopChoice(Long shopId, String title, String content) {
        this.shopId = shopId;
        this.title = title;
        this.content = content;
    }

    public static ShopChoice of(Long shopId, String title, String content) {
        return new ShopChoice(shopId, title, content);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
