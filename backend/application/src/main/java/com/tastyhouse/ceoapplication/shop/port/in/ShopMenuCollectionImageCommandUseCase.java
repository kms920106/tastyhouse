package com.tastyhouse.ceoapplication.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

/**
 * 점주 가게 메뉴모음컷 쓰기 인바운드 포트.
 *
 * <p>등록 이미지 파일은 Command 필드가 아니라 별도 파라미터로 받는다.
 */
public interface ShopMenuCollectionImageCommandUseCase {

    Long registerMenuCollectionImage(ShopMenuCollectionImageCreateCommand command, MultipartFile file);

    void reorderMenuCollectionImages(ShopMenuCollectionImageReorderCommand command);

    void deleteMenuCollectionImage(ShopMenuCollectionImageDeleteCommand command);
}
