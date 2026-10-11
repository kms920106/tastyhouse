package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.shop.model.ShopContentBoard;

public interface ShopContentBoardSavePort {

    ShopContentBoard save(ShopContentBoard shopContentBoard);

    void deleteById(Long id);
}
