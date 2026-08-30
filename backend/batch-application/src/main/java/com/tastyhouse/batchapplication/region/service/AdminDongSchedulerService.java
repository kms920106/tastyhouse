package com.tastyhouse.batchapplication.region.service;

import com.tastyhouse.batchapplication.region.port.in.SynchronizeAdminDongsUseCase;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;
import com.tastyhouse.external.region.AdminDongBoundaryClient;
import com.tastyhouse.external.region.AdminDongBoundaryResult;

/**
 * 행정동 마스터 동기화 배치 application 서비스.
 *
 * <p>외부 원천(통계청 SGIS 파생 행정동 경계)을 읽어 {@code ADMIN_DONG}을 최신 상태로 맞춘다. 행정동은
 * 통폐합·신설이 연 몇 회 발생하는데, 이 마스터가 낡으면 개편된 지역의 배달지역 설정과 주소 매칭이
 * 조용히 어긋난다.
 *
 * <p><b>다운로드를 트랜잭션 밖에서 수행한다.</b> 30MB대 파일을 받는 동안 DB 커넥션을 붙잡고 있으면
 * 커넥션 풀이 그만큼 오래 묶인다. 네트워크가 끝난 뒤 저장만 한 트랜잭션으로 처리한다.
 */
@Service
public class AdminDongSchedulerService implements SynchronizeAdminDongsUseCase {

    private static final Logger log = LoggerFactory.getLogger(AdminDongSchedulerService.class);

    private final AdminDongBoundaryClient adminDongBoundaryClient;
    private final AdminDongSyncExecutor adminDongSyncExecutor;

    public AdminDongSchedulerService(
        AdminDongBoundaryClient adminDongBoundaryClient,
        AdminDongSyncExecutor adminDongSyncExecutor
    ) {
        this.adminDongBoundaryClient = adminDongBoundaryClient;
        this.adminDongSyncExecutor = adminDongSyncExecutor;
    }

    /**
     * 행정동 마스터를 원천과 동기화한다.
     *
     * <p>결과 요약은 반환하지 않고 로그로만 남긴다 — 호출자(스케줄러·수동 러너)는 트리거일 뿐 결과로
     * 분기하지 않으므로, 반환값을 두면 아무도 읽지 않는 계약만 남는다.
     */
    @Override
    public void synchronizeAdminDongs() {
        // 네트워크 구간 — 트랜잭션 밖이다.
        List<AdminDongBoundaryResult> sourceRows = adminDongBoundaryClient.fetchAll();

        List<AdminDong> adminDongs = sourceRows.stream()
            .map(AdminDongSchedulerService::toAdminDong)
            .toList();

        // 저장 구간 — 별도 빈이 트랜잭션 경계를 갖는다(self-invocation 회피).
        AdminDongSyncResult result = adminDongSyncExecutor.synchronizeInTx(adminDongs);
        log.info("행정동 마스터 동기화 결과: 신규 {}건, 갱신 {}건, 폐지 {}건 (반영 총 {}건)",
            result.inserted(), result.updated(), result.deactivated(), result.appliedCount());
    }

    /** 원천 행을 도메인 모델로 승격한다. 원천에 있는 동은 전부 사용 중({@code active})으로 본다. */
    private static AdminDong toAdminDong(AdminDongBoundaryResult source) {
        return AdminDong.of(
            source.code(),
            source.sidoName(),
            source.sigunguName(),
            source.dongName(),
            true,
            source.center(),
            source.boundary()
        );
    }
}
