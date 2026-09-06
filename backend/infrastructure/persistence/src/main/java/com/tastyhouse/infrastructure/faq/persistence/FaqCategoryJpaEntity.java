package com.tastyhouse.infrastructure.faq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "FAQ_CATEGORY")
public class FaqCategoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    protected FaqCategoryJpaEntity() {
    }

    private FaqCategoryJpaEntity(String name, Integer sort, boolean visible, boolean deleted) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    static FaqCategoryJpaEntity create(String name, Integer sort, boolean visible, boolean deleted) {
        return new FaqCategoryJpaEntity(name, sort, visible, deleted);
    }

    void applyChanges(String name, Integer sort, boolean visible, boolean deleted) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
