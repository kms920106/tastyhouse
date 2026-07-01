package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceDto;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopChoiceRepository {

    PageResult<EditorChoiceDto> findEditorChoice(PageQuery pageQuery);
}
