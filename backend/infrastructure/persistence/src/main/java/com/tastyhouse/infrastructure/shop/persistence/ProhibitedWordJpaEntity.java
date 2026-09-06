package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PROHIBITED_WORD")
public class ProhibitedWordJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "reason")
    private String reason;

    protected ProhibitedWordJpaEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public String getWord() {
        return this.word;
    }

    public String getReason() {
        return this.reason;
    }
}
