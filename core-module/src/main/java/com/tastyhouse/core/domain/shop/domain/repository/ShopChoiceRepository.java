package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopChoiceRepository {

    PageResult<EditorChoiceResult> findEditorChoice(PageQuery pageQuery);
}
