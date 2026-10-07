package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopContentBoardOwnerCreateUseCase {

    Long createContentBoard(ShopContentBoardCreateCommand command, MultipartFile file);
}
