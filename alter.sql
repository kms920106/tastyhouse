-- rank 도메인 하드 삭제 -> 소프트 삭제 전환: RANK_PERIOD/RANK_PRIZE에 is_deleted 컬럼 추가
ALTER TABLE RANK_PERIOD ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)' AFTER is_visible;
ALTER TABLE RANK_PRIZE ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)' AFTER image_file_id;
