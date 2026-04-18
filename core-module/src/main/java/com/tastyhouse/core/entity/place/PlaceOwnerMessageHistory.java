package com.tastyhouse.core.entity.place;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "PLACE_OWNER_MESSAGE_HISTORY")
public class PlaceOwnerMessageHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message; // 사장님 한마디

    protected PlaceOwnerMessageHistory() {
    }

    public PlaceOwnerMessageHistory(Long placeId, String message) {
        this.placeId = placeId;
        this.message = message;
    }
}
