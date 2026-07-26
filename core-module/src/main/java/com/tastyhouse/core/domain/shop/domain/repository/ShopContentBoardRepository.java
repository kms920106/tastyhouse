package com.tastyhouse.core.domain.shop.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.shop.domain.model.ShopContentBoard;
import com.tastyhouse.core.domain.shop.domain.model.ShopContentType;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface ShopContentBoardRepository {

    ShopContentBoard save(ShopContentBoard shopContentBoard);

    List<ShopContentBoard> findByShopId(Long shopId);

    PageResult<ShopContentBoard> findAll(Long shopId, Boolean hidden, ShopContentType contentType, PageQuery pageQuery);

    Optional<ShopContentBoard> findById(Long id);

    void deleteById(Long id);

    long countByShopId(Long shopId);
}
