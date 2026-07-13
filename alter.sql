-- =====================================================================
-- 버그 제보(BUG_REPORT) 운영 관리 컬럼 추가 마이그레이션
-- 기존 운영 DB에 적용. create.sql은 신규 스키마 기준이므로 이 파일은 기존 테이블 변경 전용.
-- 순서 중요: status 는 NULL 허용으로 추가 → 기존 row 백필 → NOT NULL 로 전환.
-- =====================================================================

-- 1) status: 우선 NULL 허용으로 추가 (기존 row 가 NULL 위반 없이 생성되도록)
ALTER TABLE BUG_REPORT
    ADD COLUMN status VARCHAR(20) NULL COMMENT '처리 상태 (RECEIVED, IN_PROGRESS, RESOLVED, REJECTED, ON_HOLD)' AFTER content;

-- 2) 기존 row 백필: 모든 미처리 제보를 접수(RECEIVED) 상태로
UPDATE BUG_REPORT
SET status = 'RECEIVED'
WHERE status IS NULL;

-- 3) status 를 NOT NULL + 기본값 RECEIVED 로 전환
ALTER TABLE BUG_REPORT
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'RECEIVED' COMMENT '처리 상태 (RECEIVED, IN_PROGRESS, RESOLVED, REJECTED, ON_HOLD)';

-- 4) 나머지 운영 컬럼 추가 (모두 NULL 허용 → 백필 불필요)
ALTER TABLE BUG_REPORT
    ADD COLUMN category          VARCHAR(20)  NULL COMMENT '분류 (PAYMENT, LOGIN, ORDER, RESERVATION, UI, PERFORMANCE, ETC / 미분류 시 NULL)' AFTER status,
    ADD COLUMN priority          VARCHAR(20)  NULL COMMENT '우선순위 (LOW, MEDIUM, HIGH, CRITICAL / 미지정 시 NULL)'                          AFTER category,
    ADD COLUMN assignee_admin_id BIGINT       NULL COMMENT '담당 관리자 ID (ADMIN.id 참조 / 미배정 시 NULL)'                                 AFTER priority,
    ADD COLUMN admin_answer      TEXT         NULL COMMENT '처리 결과/반려 사유 (미처리 시 NULL)'                                            AFTER assignee_admin_id,
    ADD COLUMN resolved_at       DATETIME     NULL COMMENT '처리 완료 일시 (RESOLVED/REJECTED 시 기록)'                                       AFTER admin_answer,
    ADD COLUMN app_version       VARCHAR(30)  NULL COMMENT '앱 버전 (제보자 입력, 선택)'                                                     AFTER resolved_at,
    ADD COLUMN platform          VARCHAR(20)  NULL COMMENT '플랫폼 (IOS, ANDROID / 선택)'                                                    AFTER app_version,
    ADD COLUMN os_version        VARCHAR(30)  NULL COMMENT 'OS 버전 (제보자 입력, 선택)'                                                     AFTER platform;

-- 5) 처리 상태별 조회 인덱스 추가
ALTER TABLE BUG_REPORT
    ADD INDEX idx_bug_report_status (status);
