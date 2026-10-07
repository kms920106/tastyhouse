package com.tastyhouse.application.shop.port.in;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ShopNoticeOwnerCreateUseCase {

    Long createNotice(ShopNoticeCreateCommand command, List<MultipartFile> files);
}
