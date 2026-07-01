package com.tastyhouse.core.domain.shop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(name = "SHOP_OWNER_MESSAGE_HISTORY")
public class ShopOwnerMessageHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // 사장님 한마디 메시지 내용

    protected ShopOwnerMessageHistory() {
    }
}
