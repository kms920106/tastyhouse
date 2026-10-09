package com.tastyhouse.application.shop.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopChangeActionType;
import com.tastyhouse.domain.shop.model.ShopChangeActor;
import com.tastyhouse.domain.shop.model.ShopChangeType;
import com.tastyhouse.domain.shop.model.ShopChangeValueFormatter;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.model.ShopDeliveryAreaPolicy;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.region.port.out.write.AdminDongLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryAreaSavePort;
import com.tastyhouse.application.shop.port.out.write.ShopDeliveryTipRegionLoadPort;

@Service
public class ShopDeliveryAreaService {

    private final ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort;
    private final ShopDeliveryAreaSavePort shopDeliveryAreaSavePort;
    private final AdminDongLoadPort adminDongLoadPort;
    private final ShopDeliveryTipRegionLoadPort shopDeliveryTipRegionLoadPort;
    private final ShopChangeHistoryRecorder shopChangeHistoryRecorder;

    public ShopDeliveryAreaService(
        ShopDeliveryAreaLoadPort shopDeliveryAreaLoadPort,
        ShopDeliveryAreaSavePort shopDeliveryAreaSavePort,
        AdminDongLoadPort adminDongLoadPort,
        ShopDeliveryTipRegionLoadPort shopDeliveryTipRegionLoadPort,
        ShopChangeHistoryRecorder shopChangeHistoryRecorder
    ) {
        this.shopDeliveryAreaLoadPort = shopDeliveryAreaLoadPort;
        this.shopDeliveryAreaSavePort = shopDeliveryAreaSavePort;
        this.adminDongLoadPort = adminDongLoadPort;
        this.shopDeliveryTipRegionLoadPort = shopDeliveryTipRegionLoadPort;
        this.shopChangeHistoryRecorder = shopChangeHistoryRecorder;
    }

    public Long addArea(ShopId shopId, AdminDongId adminDongId, ShopChangeActor actor) {
        if (!adminDongLoadPort.existsActiveById(adminDongId)) {
            throw new ResourceNotFoundException(CeoErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        if (shopDeliveryAreaLoadPort.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new ApplicationException(CeoErrorCode.SHOP_DELIVERY_AREA_DUPLICATED);
        }

        ShopDeliveryAreaPolicy.validateTotalCount((int) shopDeliveryAreaLoadPort.countByShopId(shopId) + 1);

        ShopDeliveryArea saved = shopDeliveryAreaSavePort.save(ShopDeliveryArea.of(shopId, adminDongId));

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
        String previousValue = describeAreas(shopDeliveryAreaLoadPort.findAdminDongIdsByShopId(shopId));

        BulkResult result = addAreasWithoutHistory(shopId, adminDongIds);

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeAreas(shopDeliveryAreaLoadPort.findAdminDongIdsByShopId(shopId))
        );
        return result;
    }

    BulkResult addAreasWithoutHistory(ShopId shopId, Collection<AdminDongId> adminDongIds) {
        Set<AdminDongId> requested = new LinkedHashSet<>(adminDongIds);
        if (requested.isEmpty()) {
            long total = shopDeliveryAreaLoadPort.countByShopId(shopId);
            return new BulkResult(0, 0, 0, (int) total);
        }

        Set<AdminDongId> existingDongs = adminDongLoadPort.filterActiveIds(requested);
        if (existingDongs.size() != requested.size()) {
            throw new ResourceNotFoundException(CeoErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        Set<AdminDongId> alreadyRegistered = shopDeliveryAreaLoadPort.findAdminDongIdsByShopId(shopId);
        List<ShopDeliveryArea> toAdd = requested.stream()
            .filter(adminDongId -> !alreadyRegistered.contains(adminDongId))
            .map(adminDongId -> ShopDeliveryArea.of(shopId, adminDongId, DeliveryAreaSource.MANUAL))
            .toList();

        int totalAfterApply = alreadyRegistered.size() + toAdd.size();
        ShopDeliveryAreaPolicy.validateTotalCount(totalAfterApply);

        if (!toAdd.isEmpty()) {
            shopDeliveryAreaSavePort.saveAll(toAdd);
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
        Set<AdminDongId> registered = shopDeliveryAreaLoadPort.findAdminDongIdsByShopId(shopId);

        Set<AdminDongId> targets = requested.stream()
            .filter(registered::contains)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        validateNotReferencedByRegionTip(shopId, targets, adminDongNamesById);

        String previousValue = describeAreas(registered);

        List<ShopDeliveryArea> removable = shopDeliveryAreaLoadPort.findByShopId(shopId).stream()
            .filter(area -> targets.contains(area.getAdminDongId()))
            .toList();
        removable.forEach(area -> shopDeliveryAreaSavePort.deleteById(area.getId()));

        shopChangeHistoryRecorder.record(
            shopId,
            ShopChangeType.DELIVERY_AREA,
            ShopChangeActionType.UPDATE,
            actor,
            previousValue,
            describeAreas(shopDeliveryAreaLoadPort.findAdminDongIdsByShopId(shopId))
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

        Set<AdminDongId> referenced = shopDeliveryTipRegionLoadPort.findRegionTipAdminDongIds(shopId);
        List<AdminDongId> blocked = targets.stream()
            .filter(referenced::contains)
            .toList();
        if (blocked.isEmpty()) {
            return;
        }

        List<String> blockedNames = adminDongNamesById == null ? List.of() : adminDongNamesById.apply(blocked);
        String message = CeoErrorCode.SHOP_DELIVERY_AREA_IN_USE.getDefaultMessage();
        if (!blockedNames.isEmpty()) {
            message = message + ": " + String.join(", ", blockedNames);
        }
        throw new ApplicationException(CeoErrorCode.SHOP_DELIVERY_AREA_IN_USE, message);
    }

    public record BulkResult(
        int requestedCount,
        int addedCount,
        int skippedCount,
        int totalCount
    ) {
    }

    public void removeArea(Long deliveryAreaId, ShopChangeActor actor) {
        ShopDeliveryArea deliveryArea = shopDeliveryAreaLoadPort.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));

        boolean referencedByRegionTip = shopDeliveryTipRegionLoadPort.existsRegionTipByShopIdAndAdminDongId(
            deliveryArea.getShopId(),
            deliveryArea.getAdminDongId()
        );
        if (referencedByRegionTip) {
            throw new ApplicationException(CeoErrorCode.SHOP_DELIVERY_AREA_IN_USE);
        }

        String previousValue = describeArea(deliveryArea.getAdminDongId());

        shopDeliveryAreaSavePort.deleteById(deliveryAreaId);

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
        return adminDongLoadPort.findById(adminDongId)
            .map(AdminDong::fullName)
            .orElseGet(() -> "행정동 " + adminDongId.value());
    }

    private String describeAreas(Collection<AdminDongId> adminDongIds) {
        Map<Long, String> namesById = adminDongLoadPort.findAllActiveByIds(adminDongIds).stream()
            .collect(Collectors.toMap(AdminDong::getId, AdminDong::fullName, (first, second) -> first));

        return ShopChangeValueFormatter.snapshot(
            adminDongIds.stream()
                .map(adminDongId -> namesById.getOrDefault(adminDongId.value(), "행정동 " + adminDongId.value()))
                .toList()
        );
    }
}
