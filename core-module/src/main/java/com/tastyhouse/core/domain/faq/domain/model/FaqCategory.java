package com.tastyhouse.core.domain.faq.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(name = "FAQ_CATEGORY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FaqCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible = true;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    private FaqCategory(String name, Integer sort, boolean visible) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
    }

    public static FaqCategory of(String name, Integer sort, boolean visible) {
        return new FaqCategory(name, sort, visible);
    }

    public FaqCategoryId getFaqCategoryId() {
        return FaqCategoryId.of(this.id);
    }

    public void update(String name, Integer sort, boolean visible) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }
}
