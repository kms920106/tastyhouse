package com.tastyhouse.core.domain.place.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "PLACE_BOOKMARK")
public class PlaceBookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Column(name = "member_id", nullable = false)
    private Long memberId; // 회원 ID (MEMBER.id 참조)

    protected PlaceBookmark() {
    }

    public PlaceBookmark(Long placeId, Long memberId) {
        this.placeId = placeId;
        this.memberId = memberId;
    }
}
