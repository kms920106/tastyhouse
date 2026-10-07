package com.tastyhouse.application.shop.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ShopMenuCollectionImageOwnerCreateUseCase {

    Long registerMenuCollectionImage(ShopMenuCollectionImageCreateCommand command, MultipartFile file);
}
