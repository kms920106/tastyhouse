package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.EditorChoiceResult;

public interface ShopChoiceListManagementQueryUseCase {

    PageResult<EditorChoiceResult> getShopChoices(int page, int size);
}
