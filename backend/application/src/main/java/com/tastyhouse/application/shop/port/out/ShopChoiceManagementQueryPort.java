package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopChoiceManagementQueryPort {

    PageResult<EditorChoiceResult> findEditorChoices(PageQuery pageQuery);

    List<StationResult> findAllStations();

    Optional<ShopChoiceDetailResult> findShopChoiceById(Long id);

    List<TagResult> findAllTags();
}
