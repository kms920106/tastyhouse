package com.tastyhouse.application.shop.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.service.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolicy;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.region.port.out.write.AdminDongPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaPersistencePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionLookupPort;

public class ShopDeliveryAreaService {
    private final ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort;
    private final AdminDongPersistencePort adminDongPersistencePort;
    private final ShopDeliveryTipRegionLookupPort shopDeliveryTipRegionLookupPort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryAreaService(
        ShopDeliveryAreaPersistencePort shopDeliveryAreaPersistencePort,
        AdminDongPersistencePort adminDongPersistencePort,
        ShopDeliveryTipRegionLookupPort shopDeliveryTipRegionLookupPort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryAreaPersistencePort = shopDeliveryAreaPersistencePort;
        this.adminDongPersistencePort = adminDongPersistencePort;
        this.shopDeliveryTipRegionLookupPort = shopDeliveryTipRegionLookupPort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public Long addArea(ShopId shopId, AdminDongId adminDongId, ShopChangeActor actor) {
        if (!adminDongPersistencePort.existsById(adminDongId)) {
            throw new ResourceNotFoundException(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        if (shopDeliveryAreaPersistencePort.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_DUPLICATED);
        }

        ShopDeliveryAreaPolicy.validateTotalCount((int) shopDeliveryAreaPersistencePort.countByShopId(shopId) + 1);

        ShopDeliveryArea saved = shopDeliveryAreaPersistencePort.save(ShopDeliveryArea.of(shopId, adminDongId));

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
        String previousValue = describeAreas(shopDeliveryAreaPersistencePort.findAdminDongIdsByShopId(shopId));

        BulkResult result = addAreasWithoutHistory(shopId, adminDongIds);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeAreas(shopDeliveryAreaPersistencePort.findAdminDongIdsByShopId(shopId))
        );
        return result;
    }

    BulkResult addAreasWithoutHistory(ShopId shopId, Collection<AdminDongId> adminDongIds) {
        Set<AdminDongId> requested = new LinkedHashSet<>(adminDongIds);
        if (requested.isEmpty()) {
            long total = shopDeliveryAreaPersistencePort.countByShopId(shopId);
            return new BulkResult(0, 0, 0, (int) total);
        }

        Set<AdminDongId> existingDongs = adminDongPersistencePort.filterExistingIds(requested);
        if (existingDongs.size() != requested.size()) {
            throw new ResourceNotFoundException(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        Set<AdminDongId> alreadyRegistered = shopDeliveryAreaPersistencePort.findAdminDongIdsByShopId(shopId);
        List<ShopDeliveryArea> toAdd = requested.stream()
            .filter(adminDongId -> !alreadyRegistered.contains(adminDongId))
            .map(adminDongId -> ShopDeliveryArea.of(shopId, adminDongId, DeliveryAreaSource.MANUAL))
            .toList();

        int totalAfterApply = alreadyRegistered.size() + toAdd.size();
        ShopDeliveryAreaPolicy.validateTotalCount(totalAfterApply);

        if (!toAdd.isEmpty()) {
            shopDeliveryAreaPersistencePort.saveAll(toAdd);
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
        Set<AdminDongId> registered = shopDeliveryAreaPersistencePort.findAdminDongIdsByShopId(shopId);

        Set<AdminDongId> targets = requested.stream()
            .filter(registered::contains)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        validateNotReferencedByRegionTip(shopId, targets, adminDongNamesById);

        String previousValue = describeAreas(registered);

        List<ShopDeliveryArea> removable = shopDeliveryAreaPersistencePort.findByShopId(shopId).stream()
            .filter(area -> targets.contains(area.getAdminDongId()))
            .toList();
        removable.forEach(area -> shopDeliveryAreaPersistencePort.deleteById(area.getId()));

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeAreas(shopDeliveryAreaPersistencePort.findAdminDongIdsByShopId(shopId))
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

        Set<AdminDongId> referenced = shopDeliveryTipRegionLookupPort.findRegionTipAdminDongIds(shopId);
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
        ShopDeliveryArea deliveryArea = shopDeliveryAreaPersistencePort.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));

        boolean referencedByRegionTip = shopDeliveryTipRegionLookupPort.existsRegionTipByShopIdAndAdminDongId(
            deliveryArea.getShopId(),
            deliveryArea.getAdminDongId()
        );
        if (referencedByRegionTip) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_IN_USE);
        }

        String previousValue = describeArea(deliveryArea.getAdminDongId());

        shopDeliveryAreaPersistencePort.deleteById(deliveryAreaId);

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
        return adminDongPersistencePort.findById(adminDongId)
            .map(AdminDong::fullName)
            .orElseGet(() -> "행정동 " + adminDongId.value());
    }

    private String describeAreas(Collection<AdminDongId> adminDongIds) {
        Map<Long, String> namesById = adminDongPersistencePort.findAllByIds(adminDongIds).stream()
            .collect(Collectors.toMap(AdminDong::getId, AdminDong::fullName, (first, second) -> first));

        return ShopChangeValueFormatter.snapshot(
            adminDongIds.stream()
                .map(adminDongId -> namesById.getOrDefault(adminDongId.value(), "행정동 " + adminDongId.value()))
                .toList()
        );
    }
}
