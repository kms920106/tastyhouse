package com.tastyhouse.application.region.service;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shared.geo.InteriorPoint;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryFetchResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryPort;
import com.tastyhouse.application.region.port.out.AdminDongBoundarySource;
import com.tastyhouse.application.region.port.out.BoundaryCoordinate;
import com.tastyhouse.application.region.port.out.BoundaryRing;
import com.tastyhouse.application.region.port.out.write.AdminDongSyncResult;
import com.tastyhouse.application.shared.exception.BatchErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminDongSchedulerServiceTest {

    private static final BoundaryRing SQUARE = new BoundaryRing(List.of(
        new BoundaryCoordinate(37.50, 127.00),
        new BoundaryCoordinate(37.50, 127.02),
        new BoundaryCoordinate(37.52, 127.02),
        new BoundaryCoordinate(37.52, 127.00),
        new BoundaryCoordinate(37.50, 127.00)
    ));
    private static final BoundaryRing DEGENERATE = new BoundaryRing(List.of(
        new BoundaryCoordinate(37.50, 127.00),
        new BoundaryCoordinate(37.51, 127.01)
    ));

    private AdminDongBoundaryPort boundaryPort;
    private AdminDongSyncExecutor syncExecutor;
    private AdminDongSchedulerService service;

    @BeforeEach
    void setUp() {
        boundaryPort = mock(AdminDongBoundaryPort.class);
        syncExecutor = mock(AdminDongSyncExecutor.class);
        when(syncExecutor.synchronizeInTx(any())).thenReturn(AdminDongSyncResult.of(1, 0, 0));
        service = new AdminDongSchedulerService(boundaryPort, syncExecutor);
    }

    @Test
    @DisplayName("원천 조회 실패 결과는 ADMIN_DONG_BOUNDARY_FETCH_FAILED로 번역되고 동기화하지 않는다")
    void fetchFailureIsTranslated() {
        when(boundaryPort.fetchAll()).thenReturn(AdminDongBoundaryFetchResult.fetchFailed());

        assertThatThrownBy(() -> service.synchronizeAdminDongs())
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", BatchErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        verify(syncExecutor, never()).synchronizeInTx(any());
    }

    @Test
    @DisplayName("모든 행의 대표점을 계산하지 못하면 ADMIN_DONG_BOUNDARY_FETCH_FAILED로 실패한다")
    void allRowsWithoutCenterFail() {
        when(boundaryPort.fetchAll()).thenReturn(AdminDongBoundaryFetchResult.fetched(List.of(
            source("1111051500", List.of(DEGENERATE))
        )));

        assertThatThrownBy(() -> service.synchronizeAdminDongs())
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", BatchErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        verify(syncExecutor, never()).synchronizeInTx(any());
    }

    @Test
    @DisplayName("좌표로 경계 링과 대표점을 만들고, 퇴화 링은 버리며 대표점이 없는 행은 건너뛴다")
    void buildsRingsAndCenterFromCoordinates() {
        when(boundaryPort.fetchAll()).thenReturn(AdminDongBoundaryFetchResult.fetched(List.of(
            source("1111051500", List.of(SQUARE, DEGENERATE)),
            source("1111053000", List.of(DEGENERATE))
        )));

        service.synchronizeAdminDongs();

        ArgumentCaptor<List<AdminDong>> captor = ArgumentCaptor.captor();
        verify(syncExecutor).synchronizeInTx(captor.capture());
        List<AdminDong> synced = captor.getValue();
        assertThat(synced).hasSize(1);

        List<GeoRing> expectedRings = List.of(GeoRing.of(List.of(
            GeoPoint.of(BigDecimal.valueOf(37.50), BigDecimal.valueOf(127.00)),
            GeoPoint.of(BigDecimal.valueOf(37.50), BigDecimal.valueOf(127.02)),
            GeoPoint.of(BigDecimal.valueOf(37.52), BigDecimal.valueOf(127.02)),
            GeoPoint.of(BigDecimal.valueOf(37.52), BigDecimal.valueOf(127.00))
        )));
        assertThat(synced.getFirst().getBoundary()).isEqualTo(expectedRings);
        assertThat(synced.getFirst().getCenter()).isEqualTo(InteriorPoint.of(expectedRings));
    }

    private static AdminDongBoundarySource source(String code, List<BoundaryRing> rings) {
        return new AdminDongBoundarySource(code, "서울", "종로구", "청운효자동", rings);
    }
}
