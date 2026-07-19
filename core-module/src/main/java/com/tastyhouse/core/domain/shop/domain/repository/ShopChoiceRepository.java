package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopChoice;
import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopChoiceRepository {

    PageResult<EditorChoiceResult> findEditorChoice(PageQuery pageQuery);

    Optional<ShopChoice> findById(Long id);

    ShopChoice save(ShopChoice shopChoice);

    void deleteById(Long id);
}
