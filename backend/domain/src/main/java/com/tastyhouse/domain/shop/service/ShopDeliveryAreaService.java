package com.tastyhouse.domain.shop.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

public class ShopDeliveryAreaService {
    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryAreaService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopDeliveryTipRegionLookup = shopDeliveryTipRegionLookup;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public Long addArea(ShopId shopId, AdminDongId adminDongId, ShopChangeActor actor) {
        if (!adminDongRepository.existsById(adminDongId)) {
            throw new ResourceNotFoundException(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        if (shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_DUPLICATED);
        }

        ShopDeliveryAreaPolicy.validateTotalCount((int) shopDeliveryAreaRepository.countByShopId(shopId) + 1);

        ShopDeliveryArea saved = shopDeliveryAreaRepository.save(ShopDeliveryArea.of(shopId, adminDongId));

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.CREATE,
            actor,
            null,
            describeArea(adminDongId)
        );
        return saved.getId();
    }

    public BulkResult addAreas(ShopId shopId, Collection<AdminDongId> adminDongIds, ShopChangeActor actor) {
        String previousValue = describeAreas(shopDeliveryAreaRepository.findAdminDongIdsByShopId(shopId));

        BulkResult result = addAreasWithoutHistory(shopId, adminDongIds);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeAreas(shopDeliveryAreaRepository.findAdminDongIdsByShopId(shopId))
        );
        return result;
    }

    BulkResult addAreasWithoutHistory(ShopId shopId, Collection<AdminDongId> adminDongIds) {
        Set<AdminDongId> requested = new LinkedHashSet<>(adminDongIds);
        if (requested.isEmpty()) {
            long total = shopDeliveryAreaRepository.countByShopId(shopId);
            return new BulkResult(0, 0, 0, (int) total);
        }

        Set<AdminDongId> existingDongs = adminDongRepository.filterExistingIds(requested);
        if (existingDongs.size() != requested.size()) {
            throw new ResourceNotFoundException(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        Set<AdminDongId> alreadyRegistered = shopDeliveryAreaRepository.findAdminDongIdsByShopId(shopId);
        List<ShopDeliveryArea> toAdd = requested.stream()
            .filter(adminDongId -> !alreadyRegistered.contains(adminDongId))
            .map(adminDongId -> ShopDeliveryArea.of(shopId, adminDongId, DeliveryAreaSource.MANUAL))
            .toList();

        int totalAfterApply = alreadyRegistered.size() + toAdd.size();
        ShopDeliveryAreaPolicy.validateTotalCount(totalAfterApply);

        if (!toAdd.isEmpty()) {
            shopDeliveryAreaRepository.saveAll(toAdd);
        }

        return new BulkResult(requested.size(), toAdd.size(), requested.size() - toAdd.size(), totalAfterApply);
    }

    public BulkResult removeAreas(
        ShopId shopId,
        Collection<AdminDongId> adminDongIds,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById,
        ShopChangeActor actor
    ) {
        Set<AdminDongId> requested = new LinkedHashSet<>(adminDongIds);
        Set<AdminDongId> registered = shopDeliveryAreaRepository.findAdminDongIdsByShopId(shopId);

        Set<AdminDongId> targets = requested.stream()
            .filter(registered::contains)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        validateNotReferencedByRegionTip(shopId, targets, adminDongNamesById);

        String previousValue = describeAreas(registered);

        List<ShopDeliveryArea> removable = shopDeliveryAreaRepository.findByShopId(shopId).stream()
            .filter(area -> targets.contains(area.getAdminDongId()))
            .toList();
        removable.forEach(area -> shopDeliveryAreaRepository.deleteById(area.getId()));

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeAreas(shopDeliveryAreaRepository.findAdminDongIdsByShopId(shopId))
        );

        return new BulkResult(requested.size(), 0, requested.size() - removable.size(), registered.size() - removable.size());
    }

    void validateNotReferencedByRegionTip(
        ShopId shopId,
        Collection<AdminDongId> targets,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById
    ) {
        if (targets.isEmpty()) {
            return;
        }

        Set<AdminDongId> referenced = shopDeliveryTipRegionLookup.findRegionTipAdminDongIds(shopId);
        List<AdminDongId> blocked = targets.stream()
            .filter(referenced::contains)
            .toList();
        if (blocked.isEmpty()) {
            return;
        }

        List<String> blockedNames = adminDongNamesById == null ? List.of() : adminDongNamesById.apply(blocked);
        String message = ErrorCode.SHOP_DELIVERY_AREA_IN_USE.getDefaultMessage();
        if (!blockedNames.isEmpty()) {
            message = message + ": " + String.join(", ", blockedNames);
        }
        throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_IN_USE, message);
    }

    public record BulkResult(
        int requestedCount,
        int addedCount,
        int skippedCount,
        int totalCount
    ) {
    }

    public void removeArea(Long deliveryAreaId, ShopChangeActor actor) {
        ShopDeliveryArea deliveryArea = shopDeliveryAreaRepository.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));

        boolean referencedByRegionTip = shopDeliveryTipRegionLookup.existsRegionTipByShopIdAndAdminDongId(
            deliveryArea.getShopId(),
            deliveryArea.getAdminDongId()
        );
        if (referencedByRegionTip) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_IN_USE);
        }

        String previousValue = describeArea(deliveryArea.getAdminDongId());

        shopDeliveryAreaRepository.deleteById(deliveryAreaId);

        shopChangeHistoryRecorder.record(
            deliveryArea.getShopId(),
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.DELETE,
            actor,
            previousValue,
            null
        );
    }

    private String describeArea(AdminDongId adminDongId) {
        return adminDongRepository.findById(adminDongId)
            .map(AdminDong::fullName)
            .orElseGet(() -> "행정동 " + adminDongId.value());
    }

    private String describeAreas(Collection<AdminDongId> adminDongIds) {
        Map<Long, String> namesById = adminDongRepository.findAllByIds(adminDongIds).stream()
            .collect(Collectors.toMap(AdminDong::getId, AdminDong::fullName, (first, second) -> first));

        return ShopChangeValueFormatter.snapshot(
            adminDongIds.stream()
                .map(adminDongId -> namesById.getOrDefault(adminDongId.value(), "행정동 " + adminDongId.value()))
                .toList()
        );
    }
}
