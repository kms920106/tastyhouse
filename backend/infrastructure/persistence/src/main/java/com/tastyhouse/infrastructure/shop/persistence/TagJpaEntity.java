package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "TAG")
public class TagJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    protected TagJpaEntity() {
    }

    private TagJpaEntity(String tagName) {
        this.tagName = tagName;
    }

    static TagJpaEntity create(String tagName) {
        return new TagJpaEntity(tagName);
    }

    public Long getId() {
        return this.id;
    }

    public String getTagName() {
        return this.tagName;
    }
}
