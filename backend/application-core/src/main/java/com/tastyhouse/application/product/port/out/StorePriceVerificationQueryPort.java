package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface StorePriceVerificationQueryPort {

    PageResult<StorePriceVerificationListItemResult> findVerificationPage(String status, PageQuery pageQuery);

    Optional<StorePriceVerificationListItemResult> findVerificationById(Long verificationId);

    List<StorePriceVerificationItemResult> findVerificationItems(Long verificationId);
}
