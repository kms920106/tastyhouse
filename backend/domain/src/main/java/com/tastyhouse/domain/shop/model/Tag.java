package com.tastyhouse.domain.shop.model;

/**
 * 태그 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code TagJpaEntity} + {@code TagMapper}가 담당한다.
 */
public class Tag {

    private final Long id;
    private final String tagName;

    private Tag(Long id, String tagName) {
        this.id = id;
        this.tagName = tagName;
    }

    public static Tag of(String tagName) {
        return new Tag(null, tagName);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static Tag reconstitute(Long id, String tagName) {
        return new Tag(id, tagName);
    }

    public Long getId() {
        return this.id;
    }

    public String getTagName() {
        return this.tagName;
    }
}
