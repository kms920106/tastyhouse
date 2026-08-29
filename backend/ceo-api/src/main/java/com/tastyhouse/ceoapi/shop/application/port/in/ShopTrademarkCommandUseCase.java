package com.tastyhouse.ceoapi.shop.application.port.in;

import org.springframework.web.multipart.MultipartFile;

/**
 * 점주 가게 상표·대표이미지 변경 요청 쓰기 인바운드 포트.
 *
 * <p>이미지 파일은 Command 필드가 아니라 별도 파라미터로 받는다.
 */
public interface ShopTrademarkCommandUseCase {

    Long requestTrademarkChange(ShopTrademarkChangeRequestCommand command, MultipartFile file);

    Long requestThumbnailChange(ShopThumbnailChangeRequestCommand command, MultipartFile file);
}
