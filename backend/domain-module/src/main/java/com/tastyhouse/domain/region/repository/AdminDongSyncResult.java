package com.tastyhouse.domain.region.repository;

/**
 * 행정동 마스터 동기화 결과 요약.
 *
 * <p>총 건수 하나만 돌려주면 "3,558건 동기화됨"까지만 알 수 있어, 행정구역 개편으로 동이 실제로
 * 바뀐 것인지 원천이 그대로인지 구분되지 않는다. 신규·갱신·폐지를 나눠 담아 배치 로그만으로
 * 무엇이 달라졌는지 읽히게 한다.
 *
 * @param inserted    새로 추가된 행정동 수
 * @param updated     기존 행을 제자리에서 갱신한 수({@code id} 보존)
 * @param deactivated 원천에서 사라져 {@code is_active = 0}으로 내린 수
 */
public record AdminDongSyncResult(
    int inserted,
    int updated,
    int deactivated
) {

    public static AdminDongSyncResult of(int inserted, int updated, int deactivated) {
        return new AdminDongSyncResult(inserted, updated, deactivated);
    }

    /** 원천에서 읽어 마스터에 반영된 총 건수(신규 + 갱신). */
    public int appliedCount() {
        return this.inserted + this.updated;
    }
}
