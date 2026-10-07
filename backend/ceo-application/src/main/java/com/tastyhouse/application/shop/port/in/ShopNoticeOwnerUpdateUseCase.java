package com.tastyhouse.application.shop.port.in;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ShopNoticeOwnerUpdateUseCase {

    void updateNotice(ShopNoticeUpdateCommand command, List<MultipartFile> files);
}
