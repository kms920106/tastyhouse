package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductPrice;
import com.tastyhouse.domain.product.repository.ProductPriceRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 메뉴 가격 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당하고, 여기에 남은 조회는
 * 가격 교체·인증 반영이라는 불변식 판정에 필요한 것들이다.
 */
@Repository
public class ProductPriceRepositoryImpl implements ProductPriceRepository {

    private final ProductPriceJpaRepository productPriceJpaRepository;

    public ProductPriceRepositoryImpl(ProductPriceJpaRepository productPriceJpaRepository) {
        this.productPriceJpaRepository = productPriceJpaRepository;
    }

    @Override
    public ProductPrice save(ProductPrice productPrice) {
        if (productPrice.getId() == null) {
            ProductPriceJpaEntity saved = productPriceJpaRepository
                .save(ProductPriceMapper.toEntity(productPrice));
            return ProductPriceMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ProductPriceJpaEntity entity = productPriceJpaRepository.findById(productPrice.getId())
            .orElseThrow(() -> new IllegalStateException(
                "존재하지 않는 메뉴 가격입니다: " + productPrice.getId()));
        ProductPriceMapper.applyChanges(entity, productPrice);
        return ProductPriceMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductPrice> findById(ProductPriceId id) {
        return productPriceJpaRepository.findById(id.value())
            .map(ProductPriceMapper::toDomain);
    }

    @Override
    public List<ProductPrice> findAllByProductId(ProductId productId) {
        return productPriceJpaRepository.findAllByProductIdOrderBySortAsc(productId.value()).stream()
            .map(ProductPriceMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductPrice> findAllByShopId(ShopId shopId) {
        return productPriceJpaRepository.findAllByShopId(shopId.value()).stream()
            .map(ProductPriceMapper::toDomain)
            .toList();
    }

    @Override
    public void deleteAllByIdIn(List<ProductPriceId> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        productPriceJpaRepository.deleteAllByIdInBatch(ids.stream().map(ProductPriceId::value).toList());
    }
}
