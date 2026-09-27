package com.tastyhouse.application.region.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.region.port.in.SynchronizeAdminDongsUseCase;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.region.port.out.AdminDongBoundaryFetchResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryPort;
import com.tastyhouse.application.region.port.out.AdminDongBoundarySource;
import com.tastyhouse.application.region.port.out.BoundaryCoordinate;
import com.tastyhouse.application.region.port.out.BoundaryRing;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shared.geo.InteriorPoint;

@Service
@BatchApp
public class AdminDongSchedulerService implements SynchronizeAdminDongsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AdminDongSchedulerService.class);

    private final AdminDongBoundaryPort adminDongBoundaryPort;
    private final AdminDongSyncExecutor adminDongSyncExecutor;

    public AdminDongSchedulerService(
        AdminDongBoundaryPort adminDongBoundaryPort,
        AdminDongSyncExecutor adminDongSyncExecutor
    ) {
        this.adminDongBoundaryPort = adminDongBoundaryPort;
        this.adminDongSyncExecutor = adminDongSyncExecutor;
    }

    @Override
    public void synchronizeAdminDongs() {

        AdminDongBoundaryFetchResult fetchResult = adminDongBoundaryPort.fetchAll();
        if (fetchResult.failed()) {
            throw new BusinessException(ErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        }

        List<AdminDong> adminDongs = toAdminDongs(fetchResult.sources());

        AdminDongSyncResult result = adminDongSyncExecutor.synchronizeInTx(adminDongs);
        log.info("행정동 마스터 동기화 결과: 신규 {}건, 갱신 {}건, 폐지 {}건 (반영 총 {}건)",
            result.inserted(), result.updated(), result.deactivated(), result.appliedCount());
    }

    private static List<AdminDong> toAdminDongs(List<AdminDongBoundarySource> sources) {
        List<AdminDong> adminDongs = new ArrayList<>(sources.size());
        int skipped = 0;
        for (AdminDongBoundarySource source : sources) {
            List<GeoRing> boundary = toGeoRings(source.boundary());
            GeoPoint center = InteriorPoint.of(boundary);
            if (center == null) {
                log.warn("행정동 대표점을 계산하지 못해 건너뜁니다: code={}, name={}", source.code(), source.dongName());
                skipped++;
                continue;
            }
            adminDongs.add(AdminDong.of(
                source.code(),
                source.sidoName(),
                source.sigunguName(),
                source.dongName(),
                true,
                center,
                boundary
            ));
        }

        if (adminDongs.isEmpty()) {
            log.error("행정동 경계 원천에서 대표점을 계산한 행이 없습니다");
            throw new BusinessException(ErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        }

        log.info("행정동 대표점 계산 완료: {}건 (대표점 계산 실패로 제외 {}건)", adminDongs.size(), skipped);
        return adminDongs;
    }

    private static List<GeoRing> toGeoRings(List<BoundaryRing> rings) {
        List<GeoRing> geoRings = new ArrayList<>(rings.size());
        for (BoundaryRing ring : rings) {
            List<GeoPoint> points = new ArrayList<>(ring.coordinates().size());
            for (BoundaryCoordinate coordinate : ring.coordinates()) {
                points.add(GeoPoint.of(coordinate.latitude(), coordinate.longitude()));
            }

            try {
                geoRings.add(GeoRing.of(points));
            } catch (IllegalArgumentException e) {
                log.debug("행정동 경계의 퇴화 링을 건너뜁니다: 정점 {}개", points.size());
            }
        }
        return geoRings;
    }
}
