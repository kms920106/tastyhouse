package com.tastyhouse.ceoapplication.shop.port.in;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

/**
 * 점주 가게 공지 쓰기 인바운드 포트.
 *
 * <p>첨부 이미지는 Command 필드가 아니라 별도 파라미터로 받는다.
 */
public interface ShopNoticeCommandUseCase {

    Long createNotice(ShopNoticeCreateCommand command, List<MultipartFile> files);

    void updateNotice(ShopNoticeUpdateCommand command, List<MultipartFile> files);

    void deleteNotice(ShopNoticeDeleteCommand command);

    void changeExposure(ShopNoticeExposureChangeCommand command);
}
