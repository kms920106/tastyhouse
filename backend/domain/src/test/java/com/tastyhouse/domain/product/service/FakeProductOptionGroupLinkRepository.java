package com.tastyhouse.domain.product.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import com.tastyhouse.domain.product.model.ProductOptionGroupLink;
import com.tastyhouse.domain.product.repository.ProductOptionGroupLinkRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;

class FakeProductOptionGroupLinkRepository implements ProductOptionGroupLinkRepository {
    private final List<ProductOptionGroupLink> links = new ArrayList<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    @Override
    public ProductOptionGroupLink save(ProductOptionGroupLink link) {
        if (link.getId() != null) {
            return link;
        }
        ProductOptionGroupLink persisted = ProductOptionGroupLink.reconstitute(
            sequence.getAndIncrement(),
            link.getProductId(),
            link.getOptionGroupId(),
            link.getSort()
        );
        links.add(persisted);
        return persisted;
    }

    @Override
    public Optional<ProductOptionGroupLink> findByProductIdAndOptionGroupId(
        ProductId productId,
        ProductOptionGroupId optionGroupId
    ) {
        return links.stream()
            .filter(link -> link.getProductId().equals(productId))
            .filter(link -> link.getOptionGroupId().equals(optionGroupId))
            .findFirst();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByProductId(ProductId productId) {
        return links.stream()
            .filter(link -> link.getProductId().equals(productId))
            .sorted(Comparator.comparing(ProductOptionGroupLink::getSort,
                Comparator.nullsLast(Comparator.naturalOrder())))
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupId(ProductOptionGroupId optionGroupId) {
        return links.stream()
            .filter(link -> link.getOptionGroupId().equals(optionGroupId))
            .toList();
    }

    @Override
    public List<ProductOptionGroupLink> findAllByOptionGroupIdIn(List<ProductOptionGroupId> optionGroupIds) {
        return links.stream()
            .filter(link -> optionGroupIds.contains(link.getOptionGroupId()))
            .toList();
    }

    @Override
    public boolean existsByProductIdAndOptionGroupId(ProductId productId, ProductOptionGroupId optionGroupId) {
        return findByProductIdAndOptionGroupId(productId, optionGroupId).isPresent();
    }

    @Override
    public void delete(ProductOptionGroupLink link) {
        links.removeIf(existing -> existing.getId().equals(link.getId()));
    }

    void seed(Long productId, Long optionGroupId, int sort) {
        save(ProductOptionGroupLink.of(ProductId.of(productId), ProductOptionGroupId.of(optionGroupId), sort));
    }
}
