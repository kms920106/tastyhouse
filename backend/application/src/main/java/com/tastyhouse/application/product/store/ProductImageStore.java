package com.tastyhouse.application.product.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.product.port.out.write.ProductImageStatePort;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.product.model.ProductImage;
import com.tastyhouse.domain.product.vo.ProductId;

public class ProductImageStore implements ProductImageRepository {
    private final ProductImageStatePort productImageStatePort;

    public ProductImageStore(ProductImageStatePort productImageStatePort) {
        this.productImageStatePort = productImageStatePort;
    }

    @Override
    public UploadedFileId findRepresentativeImageFileId(ProductId productId) {
        Long imageFileId = productImageStatePort.findRepresentativeImageFileId(productId.value());
        return imageFileId == null ? null : UploadedFileId.of(imageFileId);
    }

    @Override
    public ProductImage save(ProductImage productImage) {
        return ProductImageStateMapper.toDomain(productImageStatePort.save(ProductImageStateMapper.toState(productImage)));
    }

    @Override
    public Optional<ProductImage> findById(Long id) {
        return productImageStatePort.findById(id).map(ProductImageStateMapper::toDomain);
    }

    @Override
    public List<ProductImage> findAllByProductId(ProductId productId) {
        return productImageStatePort.findAllByProductId(productId.value()).stream()
            .map(ProductImageStateMapper::toDomain)
            .toList();
    }

    @Override
    public void delete(ProductImage productImage) {
        productImageStatePort.deleteById(productImage.getId());
    }
}
