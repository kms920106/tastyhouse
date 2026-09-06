package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.web.multipart.MultipartFile;

@CeoApp
public interface ShopContentBoardOwnerCommandUseCase {

    Long createContentBoard(ShopContentBoardCreateCommand command, MultipartFile file);

    void updateContentBoard(ShopContentBoardUpdateCommand command, MultipartFile file);

    void deleteContentBoard(ShopContentBoardOwnerDeleteCommand command);
}
