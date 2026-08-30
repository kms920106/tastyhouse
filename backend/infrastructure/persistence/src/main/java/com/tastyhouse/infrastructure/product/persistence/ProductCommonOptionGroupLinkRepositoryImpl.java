package com.tastyhouse.infrastructure.product.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.product.model.ProductCommonOptionGroupLink;
import com.tastyhouse.domain.product.repository.ProductCommonOptionGroupLinkRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

/**
 * 메뉴 ↔ 공통 옵션그룹 연결 write 어댑터. 표현 목적 조회는 {@code ProductQueryDao}가 담당한다.
 */
@Repository
public class ProductCommonOptionGroupLinkRepositoryImpl implements ProductCommonOptionGroupLinkRepository {

    private final ProductCommonOptionGroupLinkJpaRepository productCommonOptionGroupLinkJpaRepository;

    public ProductCommonOptionGroupLinkRepositoryImpl(
        ProductCommonOptionGroupLinkJpaRepository productCommonOptionGroupLinkJpaRepository
    ) {
        this.productCommonOptionGroupLinkJpaRepository = productCommonOptionGroupLinkJpaRepository;
    }

    @Override
    public ProductCommonOptionGroupLink save(ProductCommonOptionGroupLink link) {
        if (link.getId() == null) {
            ProductCommonOptionGroupLinkJpaEntity saved =
                productCommonOptionGroupLinkJpaRepository.save(ProductCommonOptionGroupLinkMapper.toEntity(link));
            return ProductCommonOptionGroupLinkMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회한 뒤 변경 필드만 복사해 dirty checking으로 flush.
        // detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        ProductCommonOptionGroupLinkJpaEntity entity = productCommonOptionGroupLinkJpaRepository.findById(link.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 옵션그룹 연결입니다: " + link.getId()));
        ProductCommonOptionGroupLinkMapper.applyChanges(entity, link);
        return ProductCommonOptionGroupLinkMapper.toDomain(entity);
    }

    @Override
    public Optional<ProductCommonOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    ) {
        return productCommonOptionGroupLinkJpaRepository
            .findByProductIdAndOptionGroupId(productId.value(), optionGroupId.value())
            .map(ProductCommonOptionGroupLinkMapper::toDomain);
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByProductId(ProductId productId) {
        return productCommonOptionGroupLinkJpaRepository.findAllByProductIdOrderBySortAsc(productId.value()).stream()
            .map(ProductCommonOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return productCommonOptionGroupLinkJpaRepository.findAllByOptionGroupId(optionGroupId.value()).stream()
            .map(ProductCommonOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public List<ProductCommonOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
        if (optionGroupIds.isEmpty()) {
            return List.of();
        }

        List<Long> rawIds = optionGroupIds.stream().map(ProductOptionGroupId::value).toList();
        return productCommonOptionGroupLinkJpaRepository.findAllByOptionGroupIdIn(rawIds).stream()
            .map(ProductCommonOptionGroupLinkMapper::toDomain)
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
        return productCommonOptionGroupLinkJpaRepository
            .existsByProductIdAndOptionGroupId(productId.value(), optionGroupId.value());
    }

    @Override
    public void delete(ProductCommonOptionGroupLink link) {
        productCommonOptionGroupLinkJpaRepository.deleteById(link.getId());
    }
}
