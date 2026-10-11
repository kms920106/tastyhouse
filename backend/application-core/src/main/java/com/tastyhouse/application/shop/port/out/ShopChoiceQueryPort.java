package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopChoiceQueryPort {

    PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery, int productLimit);

    List<StationResult> findAllStations();
}
