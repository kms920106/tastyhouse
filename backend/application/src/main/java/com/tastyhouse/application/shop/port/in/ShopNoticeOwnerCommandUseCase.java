package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@CeoApp
public interface ShopNoticeOwnerCommandUseCase {

    Long createNotice(ShopNoticeCreateCommand command, List<MultipartFile> files);

    void updateNotice(ShopNoticeUpdateCommand command, List<MultipartFile> files);

    void deleteNotice(ShopNoticeDeleteCommand command);

    void changeExposure(ShopNoticeExposureChangeCommand command);
}
