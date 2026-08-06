-- ============================================================
-- 가게배달 배달팁 설정 — 스키마 마이그레이션
-- create.sql과 동일 내용을 ALTER TABLE / CREATE TABLE 형태로 담는다.
-- ddl-auto: validate 이므로 엔티티와 정확히 일치해야 부팅된다.
-- ============================================================

-- ------------------------------------------------------------
-- 1) 선행 기반 (행정동·배달가능지역·공휴일·회원 배달주소록)
-- ------------------------------------------------------------

CREATE TABLE ADMIN_DONG
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,              -- 행정동 ID (PK)
    code         VARCHAR(10) NOT NULL,                           -- 행정동 코드(10자리)
    sido_name    VARCHAR(50) NOT NULL,                           -- 시/도
    sigungu_name VARCHAR(50) NOT NULL,                           -- 시/군/구
    dong_name    VARCHAR(50) NOT NULL,                           -- 행정동
    is_active    TINYINT(1)  NOT NULL DEFAULT 1,                 -- 사용 여부
    UNIQUE KEY uk_admin_dong_code (code),
    INDEX idx_admin_dong_name (sido_name, sigungu_name, dong_name)
);

CREATE TABLE SHOP_DELIVERY_AREA
(
    id            BIGINT   AUTO_INCREMENT PRIMARY KEY,           -- 배달가능지역 ID (PK)
    shop_id       BIGINT   NOT NULL,                             -- 장소 ID (SHOP.id 참조)
    admin_dong_id BIGINT   NOT NULL,                             -- 행정동 ID (ADMIN_DONG.id 참조)
    created_at    DATETIME NOT NULL,
    updated_at    DATETIME NOT NULL,
    INDEX idx_shop_delivery_area_shop_id (shop_id),
    UNIQUE KEY uk_shop_delivery_area (shop_id, admin_dong_id)
);

-- 일요일 자체는 담지 않는다 — "일요일은 공휴일 배달팁 대상 아님"과
-- "법정공휴일∩일요일이면 공휴일 배달팁 부과"를 코드 분기 없이 동시에 만족시킨다.
CREATE TABLE PUBLIC_HOLIDAY
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 공휴일 ID (PK)
    holiday_date  DATE        NOT NULL,                          -- 공휴일 날짜
    name          VARCHAR(50) NOT NULL,                          -- 공휴일 명칭
    is_substitute TINYINT(1)  NOT NULL DEFAULT 0,                -- 대체공휴일 여부
    UNIQUE KEY uk_public_holiday_date (holiday_date)
);

CREATE TABLE MEMBER_DELIVERY_ADDRESS
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,            -- 배달 주소 ID (PK)
    member_id      BIGINT        NOT NULL,                       -- 회원 ID (MEMBER.id 참조)
    alias          VARCHAR(50),                                  -- 주소 별칭 (집/회사 등)
    road_address   VARCHAR(500)  NOT NULL,                       -- 도로명 주소
    lot_address    VARCHAR(500),                                 -- 지번 주소
    detail_address VARCHAR(200),                                 -- 상세 주소
    admin_dong_id  BIGINT,                                       -- 행정동 ID (매칭 실패 시 NULL)
    latitude       DECIMAL(9, 6) NOT NULL,                       -- 위도
    longitude      DECIMAL(9, 6) NOT NULL,                       -- 경도
    is_default     TINYINT(1)    NOT NULL DEFAULT 0,             -- 기본 배송지 여부
    created_at     DATETIME      NOT NULL,
    updated_at     DATETIME      NOT NULL,
    INDEX idx_member_delivery_address_member_id (member_id)
);

-- ------------------------------------------------------------
-- 2) 배달팁 본체 (5테이블)
-- ------------------------------------------------------------

CREATE TABLE SHOP_DELIVERY_TIP_SETTING
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 배달팁 설정 ID (PK)
    shop_id              BIGINT      NOT NULL,                   -- 장소 ID (SHOP.id 참조)
    extra_tip_type       VARCHAR(20) NOT NULL DEFAULT 'NONE',    -- 추가 배달팁 방식 (NONE, DISTANCE, REGION)
    base_distance_meters INT,                                    -- 기본배달거리(m). 1000/1500/2000/2500/3000
    surcharge_unit       VARCHAR(20),                            -- 할증 단위 (PER_100M, PER_500M)
    surcharge_amount     INT,                                    -- 단위당 할증액(원)
    created_at           DATETIME    NOT NULL,
    updated_at           DATETIME    NOT NULL,
    UNIQUE KEY uk_shop_delivery_tip_setting_shop_id (shop_id)
);

CREATE TABLE SHOP_DELIVERY_TIP_TIER
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,          -- 구간 ID (PK)
    shop_id          BIGINT   NOT NULL,                          -- 장소 ID (SHOP.id 참조)
    tier_order       INT      NOT NULL,                          -- 구간 순서 (0=기본, 1~2=추가)
    min_order_amount INT      NOT NULL,                          -- 구간 하한 주문금액 (상품 할인 후 기준)
    tip_amount       INT      NOT NULL,                          -- 배달팁 (0 이상 5,000 미만)
    created_at       DATETIME NOT NULL,
    updated_at       DATETIME NOT NULL,
    INDEX idx_shop_delivery_tip_tier_shop_id (shop_id),
    UNIQUE KEY uk_shop_delivery_tip_tier (shop_id, tier_order)
);

CREATE TABLE SHOP_DELIVERY_TIP_REGION
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 지역별 배달팁 ID (PK)
    shop_id       BIGINT   NOT NULL,                             -- 장소 ID (SHOP.id 참조)
    admin_dong_id BIGINT   NOT NULL,                             -- 행정동 ID (ADMIN_DONG.id 참조)
    tip_amount    INT      NOT NULL,                             -- 추가 배달팁 (0~10,000)
    created_at    DATETIME NOT NULL,
    updated_at    DATETIME NOT NULL,
    INDEX idx_shop_delivery_tip_region_shop_id (shop_id),
    UNIQUE KEY uk_shop_delivery_tip_region (shop_id, admin_dong_id)
);

CREATE TABLE SHOP_DELIVERY_TIP_SCHEDULE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 시간별 배달팁 ID (PK)
    shop_id    BIGINT      NOT NULL,                             -- 장소 ID (SHOP.id 참조)
    day_type   VARCHAR(20) NOT NULL,                             -- 요일 구분 (DAILY, WEEKDAY, WEEKEND, MONDAY~SUNDAY / HOLIDAY 금지)
    start_time TIME        NOT NULL,                             -- 시작 시각
    end_time   TIME        NOT NULL,                             -- 종료 시각 (시작보다 이르면 자정 넘김)
    tip_amount INT         NOT NULL,                             -- 추가 배달팁 (0~10,000)
    created_at DATETIME    NOT NULL,
    updated_at DATETIME    NOT NULL,
    INDEX idx_shop_delivery_tip_schedule_shop_id (shop_id)
);

CREATE TABLE SHOP_DELIVERY_TIP_HOLIDAY
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 공휴일 배달팁 ID (PK)
    shop_id    BIGINT   NOT NULL,                                -- 장소 ID (SHOP.id 참조)
    tip_amount INT      NOT NULL,                                -- 추가 배달팁 (0~10,000)
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_shop_delivery_tip_holiday_shop_id (shop_id)
);

-- ------------------------------------------------------------
-- 3) ORDERS 배달팁·배달지 스냅샷 8컬럼 추가
--    기존 행은 delivery_tip_amount = 0 이므로
--    final_amount = 상품 금액 - 총 할인 + 0 이 그대로 성립한다(무손상).
-- ------------------------------------------------------------

ALTER TABLE ORDERS
    ADD COLUMN delivery_tip_amount      INT           NOT NULL DEFAULT 0 AFTER total_discount_amount,
    ADD COLUMN delivery_road_address    VARCHAR(500)  NULL     AFTER final_amount,
    ADD COLUMN delivery_lot_address     VARCHAR(500)  NULL     AFTER delivery_road_address,
    ADD COLUMN delivery_detail_address  VARCHAR(200)  NULL     AFTER delivery_lot_address,
    ADD COLUMN delivery_admin_dong_id   BIGINT        NULL     AFTER delivery_detail_address,
    ADD COLUMN delivery_latitude        DECIMAL(9, 6) NULL     AFTER delivery_admin_dong_id,
    ADD COLUMN delivery_longitude       DECIMAL(9, 6) NULL     AFTER delivery_latitude,
    ADD COLUMN delivery_distance_meters INT           NULL     AFTER delivery_longitude;
