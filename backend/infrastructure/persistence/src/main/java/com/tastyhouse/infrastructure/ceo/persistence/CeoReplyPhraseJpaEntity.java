package com.tastyhouse.infrastructure.ceo.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "CEO_REPLY_PHRASE")
public class CeoReplyPhraseJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ceo_id", nullable = false)
    private Long ceoId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "sort", nullable = false)
    private int sort;

    protected CeoReplyPhraseJpaEntity() {
    }

    private CeoReplyPhraseJpaEntity(Long ceoId, String name, String content, int sort) {
        this.ceoId = ceoId;
        this.name = name;
        this.content = content;
        this.sort = sort;
    }

    static CeoReplyPhraseJpaEntity create(Long ceoId, String name, String content, int sort) {
        return new CeoReplyPhraseJpaEntity(ceoId, name, content, sort);
    }

    void applyChanges(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public Long getCeoId() {
        return this.ceoId;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public int getSort() {
        return this.sort;
    }
}
