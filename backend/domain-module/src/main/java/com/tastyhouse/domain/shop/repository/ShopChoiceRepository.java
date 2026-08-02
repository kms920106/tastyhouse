package com.tastyhouse.domain.shop.repository;

import java.util.Optional;

import com.tastyhouse.domain.shop.model.ShopChoice;

/**
 * 에디터 추천(가게 선정) write 포트.
 *
 * <p>목록 페이징 조회({@code findEditorChoice})는 infrastructure-module의
 * {@code infrastructure/shop/query/ShopChoiceQueryDao}로 이관했다(공통 지침 패턴 4).
 */
public interface ShopChoiceRepository {

    Optional<ShopChoice> findById(Long id);

    ShopChoice save(ShopChoice shopChoice);

    void deleteById(Long id);
}
