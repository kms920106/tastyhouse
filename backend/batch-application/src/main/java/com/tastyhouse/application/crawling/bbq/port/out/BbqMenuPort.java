package com.tastyhouse.application.crawling.bbq.port.out;

import java.util.List;

public interface BbqMenuPort {

    List<BbqProductCategoryResponse> fetchMenuCategories();

    List<BbqProductResponse> fetchMenusByCategoryId(Long categoryId);

    BbqProductResponse fetchMenuDetail(Long menuId);

    List<BbqProductSubOptionResponse> fetchMenuSubOptions(Long menuId);
}
