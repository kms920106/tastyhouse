package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 메뉴 ↔ 일반 옵션그룹 연결 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductOptionGroupLinkRepositoryImpl implements ProductOptionGroupLinkRepository {

    private final ProductOptionGroupLinkJpaRepository productOptionGroupLinkJpaRepository;

    public ProductOptionGroupLinkRepositoryImpl(
        ProductOptionGroupLinkJpaRepository productOptionGroupLinkJpaRepository
    ) {
        this.productOptionGroupLinkJpaRepository = productOptionGroupLinkJpaRepository;
    }

    @Override
    public ProductOptionGroupLink save(ProductOptionGroupLink link) {
        if (link.getId() == null) {
            ProductOptionGroupLinkJpaEntity saved =
                productOptionGroupLinkJpaRepository.save(ProductOptionGroupLinkMapper.toEntity(link));
            return ProductOptionGroupLinkMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush.
        // detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ProductOptionGroupLinkJpaEntity entity = productOptionGroupLinkJpaRepository.findById(link.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 옵션그룹 연결입니다: " + link.getId()));
        ProductOptionGroupLinkMapper.applyChanges(entity, link);
        return ProductOptionGroupLinkMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    ) {
        return productOptionGroupLinkJpaRepository
            .findByProductIdAndOptionGroupId(productId.value(), optionGroupId.value())
            .map(ProductOptionGroupLinkMapper::toDomain);
    }

    @Override
    public List<ProductOptionGroupLink> findAllByProductId(ProductId productId) {
        return productOptionGroupLinkJpaRepository.findAllByProductIdOrderBySortAsc(productId.value()).stream()
            .map(ProductOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productOptionGroupLinkJpaRepository.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }

        List<Long> rawIds = optionGroupIds.stream().map(ProductOptionGroupId::value).toList();
        return productOptionGroupLinkJpaRepository.findAllByOptionGroupIdIn(rawIds).stream()
            .map(ProductOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
        return productOptionGroupLinkJpaRepository
            .existsByProductIdAndOptionGroupId(productId.value(), optionGroupId.value());
    }

    @Override
    public void delete(ProductOptionGroupLink link) {
        productOptionGroupLinkJpaRepository.deleteById(link.getId());
    }
}
