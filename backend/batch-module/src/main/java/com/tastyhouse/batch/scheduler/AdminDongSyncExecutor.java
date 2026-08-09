package com.tastyhouse.batch.scheduler;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.region.model.AdminDong;
import com.tastyhouse.domain.region.repository.AdminDongRepository;
import com.tastyhouse.domain.region.repository.AdminDongSyncResult;

/**
 * 행정동 마스터 저장 구간의 트랜잭션 경계.
 *
 * <p><b>별도 빈으로 분리한 이유</b>: 같은 클래스의 메서드를 자기 자신이 호출하면 Spring 프록시를 거치지
 * 않아 {@code @Transactional}이 적용되지 않는다(self-invocation). 다운로드는 트랜잭션 밖에서, 저장은
 * 트랜잭션 안에서 수행하려면 두 구간이 서로 다른 빈에 있어야 한다 — {@code ReservationBookingExecutor}
 * 선례와 같은 형태다.
 */
@Component
public class AdminDongSyncExecutor {

    private final AdminDongRepository adminDongRepository;

    public AdminDongSyncExecutor(AdminDongRepository adminDongRepository) {
        this.adminDongRepository = adminDongRepository;
    }

    /** 실패하면 마스터 변경이 통째로 롤백되어 이전 상태가 유지된다. */
    @Transactional
    public AdminDongSyncResult synchronizeInTx(List<AdminDong> adminDongs) {
        return adminDongRepository.synchronize(adminDongs);
    }
}
