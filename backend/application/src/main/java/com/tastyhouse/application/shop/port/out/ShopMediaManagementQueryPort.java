package com.tastyhouse.application.shop.port.out;

import java.util.List;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopMediaManagementQueryPort {

    PageResult<ShopContentBoardResult> findContentBoardPage(Long shopId, Boolean hidden, String contentType, PageQuery pageQuery);

    PageResult<ShopImageChangeRequestResult> findImageChangeRequestPage(String status, String imageType, PageQuery pageQuery);

    PageResult<ShopMenuCollectionImageRequestResult> findMenuCollectionImageRequestPage(String status, PageQuery pageQuery);

    List<ShopPhotoCategoryImageManagementResult> findPhotoCategoryImages(Long shopPhotoCategoryId);

    List<ShopBannerImageResult> findBannerImages(Long shopId);

    List<ShopPhotoCategoryResult> findPhotoCategories(Long shopId);
}
