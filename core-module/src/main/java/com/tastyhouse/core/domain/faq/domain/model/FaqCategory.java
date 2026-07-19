package com.tastyhouse.core.domain.faq.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;

/**
 * FAQ 카테고리 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code FaqCategoryJpaEntity} + {@code FaqCategoryMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로
 * {@code FaqCategoryRepository#save}를 호출해야 한다.
 */
@Getter
public class FaqCategory {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private String name;
    private Integer sort;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private FaqCategory(
        Long id,
        String name,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 FAQ 카테고리를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
    public static FaqCategory of(String name, Integer sort, boolean visible) {
        return new FaqCategory(null, name, sort, visible, false, null, null);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static FaqCategory reconstitute(
        Long id,
        String name,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new FaqCategory(id, name, sort, visible, deleted, createdAt, updatedAt);
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
