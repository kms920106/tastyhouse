package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopDeliveryAreaRadiusQueryUseCase;
import com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolicy;
import com.tastyhouse.application.region.port.out.AdminDongCandidateResult;
import com.tastyhouse.application.region.port.out.AdminDongQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaQueryPort;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaCandidateView;
import com.tastyhouse.application.shop.port.out.ShopDeliveryAreaRadiusPreviewResult;
import com.tastyhouse.application.shop.port.out.ShopLocationResult;
import com.tastyhouse.domain.shared.geo.GeoCircle;
import com.tastyhouse.domain.shared.geo.GeoPoint;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopDeliveryAreaRadiusQueryService implements ShopDeliveryAreaRadiusQueryUseCase {

    private final AdminDongQueryPort adminDongQueryPort;
    private final ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort;

    public ShopDeliveryAreaRadiusQueryService(
        AdminDongQueryPort adminDongQueryPort,
        ShopDeliveryAreaQueryPort shopDeliveryAreaQueryPort
    ) {
        this.adminDongQueryPort = adminDongQueryPort;
        this.shopDeliveryAreaQueryPort = shopDeliveryAreaQueryPort;
    }

    @Override
    public ShopDeliveryAreaRadiusPreviewResult previewRadius(Long ceoId, Long shopId, int radiusMeters) {
        ShopDeliveryAreaPolicy.validateRadius(radiusMeters);

        ShopLocationResult shopLocation = shopDeliveryAreaQueryPort.findShopLocation(ceoId, shopId);
        GeoPoint center = GeoPoint.of(shopLocation.latitude(), shopLocation.longitude());

        var circle = GeoCircle.approximate(center, radiusMeters, ShopDeliveryAreaPolicy.CIRCLE_SEGMENTS);
        var candidateBox = circle.boundingBox();

        List<AdminDongCandidateResult> candidates = adminDongQueryPort.findCandidatesWithinBoundingBox(
            candidateBox.minLatitude(),
            candidateBox.maxLatitude(),
            candidateBox.minLongitude(),
            candidateBox.maxLongitude()
        );

        Set<Long> registered = shopDeliveryAreaQueryPort.findAdminDongIds(shopId);

        List<ShopDeliveryAreaCandidateView> withinRadius = candidates.stream()
            .filter(candidate -> hasCenter(candidate) && isWithinRadius(center, candidate, radiusMeters))
            .map(candidate -> toCandidateView(candidate, registered))
            .toList();

        int unresolvedCount = (int) candidates.stream().filter(candidate -> !hasCenter(candidate)).count();

        return new ShopDeliveryAreaRadiusPreviewResult(
            shopLocation.latitude(),
            shopLocation.longitude(),
            radiusMeters,
            ShopDeliveryAreaPolicy.MAX_DELIVERY_RADIUS_METERS,
            ShopDeliveryAreaPolicy.DEFAULT_EXPOSURE_RADIUS_METERS,
            ShopDeliveryAreaGeoMapper.toPointViews(circle),
            withinRadius,
            withinRadius.size(),
            unresolvedCount
        );
    }

    private static boolean hasCenter(AdminDongCandidateResult candidate) {
        return candidate.centerLatitude() != null && candidate.centerLongitude() != null;
    }

    private static boolean isWithinRadius(GeoPoint center, AdminDongCandidateResult candidate, int radiusMeters) {
        GeoPoint candidateCenter = GeoPoint.of(candidate.centerLatitude(), candidate.centerLongitude());
        return center.distanceMetersTo(candidateCenter) <= radiusMeters;
    }

    private ShopDeliveryAreaCandidateView toCandidateView(AdminDongCandidateResult dto, Set<Long> registered) {
        return new ShopDeliveryAreaCandidateView(
            dto.adminDongId(),
            dto.regionName(),
            dto.centerLatitude(),
            dto.centerLongitude(),
            registered.contains(dto.adminDongId())
        );
    }

}
