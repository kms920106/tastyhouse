package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopContentBoardOwnerUpdateUseCase {

    void updateContentBoard(ShopContentBoardUpdateCommand command, MultipartFile file);
}
