package com.tastyhouse.batch.region.application.port.in;

/**
 * region 배치 잡의 인바운드 포트.
 *
 * <p>{@code @Scheduled} 트리거(adapter/in/scheduler)가 이 인터페이스만 주입하고, 구현은
 * {@code AdminDongSchedulerService}가 맡는다 — 트리거가 구체 서비스를 알지 않아야 잡 본문의 교체·테스트 대역
 * 주입이 가능하다(api 모듈의 컨트롤러 → UseCase 관계와 같은 형태).
 *
 * <p><b>Command record가 없다</b>: 배치 잡은 스케줄이 유일한 입력이라 경계에서 받을 값이 없다.
 * 파라미터 없는 연산에 빈 Command를 만드는 것은 형식만 맞추는 껍데기이므로 두지 않는다(스펙 §5).
 */
public interface SynchronizeAdminDongsUseCase {

    void synchronizeAdminDongs();
}
