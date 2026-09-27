package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ShopContentBoardOwnerCommandUseCase {

    Long createContentBoard(ShopContentBoardCreateCommand command, MultipartFile file);

    void updateContentBoard(ShopContentBoardUpdateCommand command, MultipartFile file);

    void deleteContentBoard(ShopContentBoardOwnerDeleteCommand command);
}
