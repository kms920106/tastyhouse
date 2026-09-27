package com.tastyhouse.application.product.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface StorePriceVerificationQueryPort {

    PageResult<StorePriceVerificationListItemResult> findVerificationPage(StorePriceVerificationStatus status, PageQuery pageQuery);

    Optional<StorePriceVerificationListItemResult> findVerificationById(Long verificationId);

    List<StorePriceVerificationItemResult> findVerificationItems(Long verificationId);
}
