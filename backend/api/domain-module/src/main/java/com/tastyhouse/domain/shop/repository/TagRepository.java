package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.Tag;

/**
 * 태그 write 포트.
 *
 * <p>목록 조회({@code findAllTags})는 infrastructure-module의
 * {@code infrastructure/shop/query/ShopChoiceQueryDao}로 이관했다(공통 지침 패턴 4).
 * {@code findTagNamesByIds}는 review 도메인이 자기 {@code ReviewQueryDao}에 같은 조회를 갖게 되어
 * 이 포트에서는 소비자가 사라졌으므로 제거했다.
 *
 * <p>{@link #findByTagName(String)}은 리뷰 등록 시 태그를 이름으로 찾아 없으면 생성하는
 * {@code ReviewLifecycleService}(도메인 서비스)가 트랜잭션 안에서 소비하므로 write 포트에 남는다.
 */
public interface TagRepository {

    /**
     * 태그 이름으로 단건 조회. 리뷰 등록 시 기존 태그 재사용 판정(도메인 서비스)에 쓰인다.
     */
    Optional<Tag> findByTagName(String tagName);

    Tag save(Tag tag);

    void deleteById(Long id);
}
