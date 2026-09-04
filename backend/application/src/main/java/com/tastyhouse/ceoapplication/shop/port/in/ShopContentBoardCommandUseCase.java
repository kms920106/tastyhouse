package com.tastyhouse.ceoapplication.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

/**
 * 점주 가게 콘텐츠보드 쓰기 인바운드 포트.
 *
 * <p>이미지/GIF 파일은 Command 필드가 아니라 별도 파라미터로 받는다.
 */
public interface ShopContentBoardCommandUseCase {

    Long createContentBoard(ShopContentBoardCreateCommand command, MultipartFile file);

    void updateContentBoard(ShopContentBoardUpdateCommand command, MultipartFile file);

    void deleteContentBoard(ShopContentBoardDeleteCommand command);
}
