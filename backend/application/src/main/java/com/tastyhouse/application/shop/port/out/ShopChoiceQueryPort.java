package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ShopChoiceQueryPort {

    PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery);

    List<StationResult> findAllStations();
}
