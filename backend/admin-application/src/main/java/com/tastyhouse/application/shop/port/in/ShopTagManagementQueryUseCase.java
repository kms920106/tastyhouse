package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.TagResult;

public interface ShopTagManagementQueryUseCase {

    List<TagResult> getTags();
}
