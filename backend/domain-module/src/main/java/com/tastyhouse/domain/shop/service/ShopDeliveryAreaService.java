package com.tastyhouse.domain.shop.service;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.vo.AdminDongId;
import com.tastyhouse.domain.shop.model.DeliveryAreaSource;
import com.tastyhouse.domain.shop.model.ShopDeliveryArea;
import com.tastyhouse.domain.shop.repository.ShopDeliveryAreaRepository;
import com.tastyhouse.domain.shop.repository.ShopDeliveryTipRegionLookup;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 가게 배달가능지역 등록·삭제(도메인 서비스).
 *
 * <p>{@code ShopDeliveryArea}는 행 하나만으로는 판정할 수 없는 규칙 두 가지를 갖는다 —
 * (1) 등록 대상 행정동이 마스터에 실재해야 하고(다른 애그리거트 타입인 {@code AdminDong}의 존재 확인),
 * (2) 같은 가게에 같은 행정동을 두 번 등록할 수 없다(집합 차원). 삭제도 마찬가지로 지역별 배달팁이
 * 그 행정동을 참조하고 있으면 막아야 한다. 이 규칙들이 호출 경로마다 갈리지 않도록 도메인 계층에 둔다.
 *
 * <p>{@code @Service}/{@code @Transactional} 없는 순수 POJO이며, 빈 등록은 infrastructure-module의
 * {@code DomainServiceConfig}가 담당한다. 점주 소유권 검증은 ceo-api 계층
 * ({@code ShopOwnershipValidator})의 책임이라 여기서는 다루지 않는다.
 */
public class ShopDeliveryAreaService {

    private final ShopDeliveryAreaRepository shopDeliveryAreaRepository;
    private final AdminDongRepository adminDongRepository;
    private final ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup;

    public ShopDeliveryAreaService(
        ShopDeliveryAreaRepository shopDeliveryAreaRepository,
        AdminDongRepository adminDongRepository,
        ShopDeliveryTipRegionLookup shopDeliveryTipRegionLookup
    ) {
        this.shopDeliveryAreaRepository = shopDeliveryAreaRepository;
        this.adminDongRepository = adminDongRepository;
        this.shopDeliveryTipRegionLookup = shopDeliveryTipRegionLookup;
    }

    /**
     * 가게에 배달가능지역(행정동)을 추가한다.
     *
     * @return 생성된 배달가능지역의 식별자
     */
    public Long addArea(ShopId shopId, AdminDongId adminDongId) {
        if (!adminDongRepository.existsById(adminDongId)) {
            throw new ResourceNotFoundException(ErrorCode.ADMIN_DONG_NOT_FOUND);
        }

        if (shopDeliveryAreaRepository.existsByShopIdAndAdminDongId(shopId, adminDongId)) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_DUPLICATED);
        }

        ShopDeliveryAreaPolicy.validateTotalCount((int) shopDeliveryAreaRepository.countByShopId(shopId) + 1);

        ShopDeliveryArea saved = shopDeliveryAreaRepository.save(ShopDeliveryArea.of(shopId, adminDongId));
        return saved.getId();
    }

    /**
     * 여러 행정동을 한 번에 배달가능지역으로 추가한다(출처 {@code MANUAL}).
     *
     * <p><b>이미 등록된 동은 실패가 아니라 건너뛴다.</b> 단건 추가가 중복을 409로 막는 것과 다른데, 일괄
     * 경로에서 중복 하나로 전체를 실패시키면 "반경으로 추가한 뒤 반경을 넓혀 다시 추가"가 항상 실패하기
     * 때문이다. 겹치는 동이 생기는 것은 이 기능의 정상적인 사용 방식이다.
     *
     * <p><b>반면 존재하지 않는 행정동이 섞이면 전체를 404로 막는다.</b> 중복은 "이미 원하는 상태"라 넘어가도
     * 점주 의도가 보존되지만, 없는 식별자는 잘못된 입력이라 조용히 넘기면 점주는 등록됐다고 믿는다.
     *
     * @return 요청·추가·건너뜀·반영 후 총계
     */
    public BulkResult addAreas(ShopId shopId, Collection<AdminDongId> adminDongIds) {
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

    /**
     * 여러 행정동을 배달가능지역에서 한 번에 제거한다.
     *
     * <p><b>지역별 배달팁이 참조하는 동이 하나라도 있으면 한 건도 지우지 않고 409로 막는다.</b> 단건 삭제와
     * 같은 불변식이지만 판정 방식이 다르다 — 건별로 확인하며 지우면 앞쪽 몇 건만 지워진 뒤 막히는 부분
     * 삭제가 되고, 점주는 무엇이 지워지고 무엇이 남았는지 알 수 없다. 그래서 참조 집합을 <b>먼저 한 번에</b>
     * 읽어 교집합을 확인한 뒤에야 삭제를 시작한다.
     *
     * <p>등록돼 있지 않은 동이 요청에 섞이는 것은 막지 않는다 — 이미 원하는 상태(없음)이므로 그대로 둔다.
     *
     * @param adminDongNamesById 차단 메시지에 이름을 나열하기 위한 표시명 조회 함수
     */
    public BulkResult removeAreas(
        ShopId shopId,
        Collection<AdminDongId> adminDongIds,
        Function<Collection<AdminDongId>, List<String>> adminDongNamesById
    ) {
        Set<AdminDongId> requested = new LinkedHashSet<>(adminDongIds);
        Set<AdminDongId> registered = shopDeliveryAreaRepository.findAdminDongIdsByShopId(shopId);

        Set<AdminDongId> targets = requested.stream()
            .filter(registered::contains)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        validateNotReferencedByRegionTip(shopId, targets, adminDongNamesById);

        List<ShopDeliveryArea> removable = shopDeliveryAreaRepository.findByShopId(shopId).stream()
            .filter(area -> targets.contains(area.getAdminDongId()))
            .toList();
        removable.forEach(area -> shopDeliveryAreaRepository.deleteById(area.getId()));

        return new BulkResult(requested.size(), 0, requested.size() - removable.size(), registered.size() - removable.size());
    }

    /**
     * 닫으려는 행정동 집합이 지역별 배달팁에 참조되고 있지 않은지 검증한다. 참조가 있으면 막힌 동 이름을
     * 메시지에 나열해 점주가 무엇을 먼저 정리해야 하는지 알 수 있게 한다.
     */
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

    /**
     * 일괄 처리 결과.
     *
     * @param requestedCount 중복 제거 후 요청 개수
     * @param addedCount     실제로 새로 등록된 개수
     * @param skippedCount   이미 등록돼 있거나 대상이 아니어서 건너뛴 개수
     * @param totalCount     반영 후 이 가게의 총 배달가능지역 개수
     */
    public record BulkResult(
        int requestedCount,
        int addedCount,
        int skippedCount,
        int totalCount
    ) {
    }

    /**
     * 배달가능지역을 삭제한다.
     *
     * <p>그 행정동을 대상으로 하는 지역별 배달팁이 남아 있으면 {@code SHOP_DELIVERY_AREA_IN_USE}(409)로
     * 차단한다 — 지역별 팁이 배달불가 지역을 가리키는 상태를 만들지 않기 위해서다. 점주는 지역별 배달팁을
     * 먼저 정리한 뒤 배달가능지역을 지워야 한다.
     */
    public void removeArea(Long deliveryAreaId) {
        ShopDeliveryArea deliveryArea = shopDeliveryAreaRepository.findById(deliveryAreaId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_DELIVERY_AREA_NOT_FOUND));

        boolean referencedByRegionTip = shopDeliveryTipRegionLookup.existsRegionTipByShopIdAndAdminDongId(
            deliveryArea.getShopId(),
            deliveryArea.getAdminDongId()
        );
        if (referencedByRegionTip) {
            throw new BusinessException(ErrorCode.SHOP_DELIVERY_AREA_IN_USE);
        }

        shopDeliveryAreaRepository.deleteById(deliveryAreaId);
    }
}
