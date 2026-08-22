CREATE TABLE BANNER
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 배너 ID (PK)
    type             VARCHAR(20) NOT NULL,               -- 배너 유형 (MAIN, EVENT 등)
    title            VARCHAR(100),                       -- 배너 제목
    image_file_id    BIGINT      NOT NULL,               -- 이미지 파일 ID (UPLOADED_FILE.id 참조)
    link_url         VARCHAR(500),                       -- 클릭 시 이동 URL
    start_date       DATETIME,                           -- 노출 시작 일시
    end_date         DATETIME,                           -- 노출 종료 일시
    sort             INT         NOT NULL,               -- 정렬 순서
    is_visible       TINYINT(1)  NOT NULL DEFAULT 1,     -- 노출 여부 (1: 노출, 0: 숨김)
    is_deleted       TINYINT(1)  NOT NULL DEFAULT 0,     -- 삭제 여부 (1: 삭제됨, 0: 정상 / Soft Delete)
    created_at       DATETIME    NOT NULL,               -- 생성 일시
    updated_at       DATETIME    NOT NULL,               -- 수정 일시
    INDEX idx_banner_type (type),                        -- 인덱스: 유형별 조회
    INDEX idx_banner_type_active (type, is_visible, sort) -- 인덱스: 유형·노출·정렬 복합 조회
);

CREATE TABLE BUG_REPORT
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 버그 신고 ID (PK)
    member_id        BIGINT       NOT NULL,                     -- 신고한 회원 ID (MEMBER.id 참조)
    device           VARCHAR(100) NOT NULL,                     -- 기기 정보 (제보자 원문)
    title            VARCHAR(200) NOT NULL,                     -- 신고 제목
    content          TEXT         NOT NULL,                     -- 신고 내용
    status           VARCHAR(20)  NOT NULL DEFAULT 'RECEIVED',  -- 처리 상태 (RECEIVED, IN_PROGRESS, RESOLVED, REJECTED, ON_HOLD)
    category         VARCHAR(20),                               -- 분류 (PAYMENT, LOGIN, ORDER, RESERVATION, UI, PERFORMANCE, ETC / 미분류 시 NULL)
    priority         VARCHAR(20),                               -- 우선순위 (LOW, MEDIUM, HIGH, CRITICAL / 미지정 시 NULL)
    assignee_admin_id BIGINT,                                   -- 담당 관리자 ID (ADMIN.id 참조 / 미배정 시 NULL)
    admin_answer     TEXT,                                      -- 처리 결과/반려 사유 (미처리 시 NULL)
    resolved_at      DATETIME,                                  -- 처리 완료 일시 (RESOLVED/REJECTED 시 기록)
    app_version      VARCHAR(30),                               -- 앱 버전 (제보자 입력, 선택)
    platform         VARCHAR(20),                               -- 플랫폼 (IOS, ANDROID / 선택)
    os_version       VARCHAR(30),                               -- OS 버전 (제보자 입력, 선택)
    created_at       DATETIME     NOT NULL,                     -- 생성 일시
    updated_at       DATETIME     NOT NULL,                     -- 수정 일시
    INDEX idx_bug_report_member_id (member_id),                 -- 인덱스: 회원별 조회
    INDEX idx_bug_report_status (status)                        -- 인덱스: 처리 상태별 조회
);

CREATE TABLE BUG_REPORT_IMAGE
(
    id               BIGINT   AUTO_INCREMENT PRIMARY KEY,    -- 버그 신고 이미지 ID (PK)
    bug_report_id    BIGINT   NOT NULL,                      -- 버그 신고 ID (BUG_REPORT.id 참조)
    image_file_id    BIGINT   NOT NULL,                      -- 이미지 파일 ID (UPLOADED_FILE.id 참조)
    sort             INT      NOT NULL,                      -- 정렬 순서
    created_at       DATETIME NOT NULL,                      -- 생성 일시
    updated_at       DATETIME NOT NULL,                      -- 수정 일시
    INDEX idx_bug_report_image_bug_report_id (bug_report_id) -- 인덱스: 버그 신고별 조회
);

CREATE TABLE COUPON
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 쿠폰 ID (PK)
    name               VARCHAR(200) NOT NULL,                     -- 쿠폰 이름
    description        VARCHAR(500),                              -- 쿠폰 설명
    discount_type      VARCHAR(20)  NOT NULL DEFAULT 'AMOUNT',    -- 할인 유형 (AMOUNT: 금액, RATE: 비율)
    discount_amount    INT          NOT NULL,                     -- 할인 금액 또는 할인율
    max_discount_amount INT,                                      -- 최대 할인 금액 (비율 할인 시 상한)
    min_order_amount   INT          NOT NULL DEFAULT 0,           -- 최소 주문 금액
    max_discount_count INT,                                       -- 최대 발급 수량
    issue_start_at     DATETIME     NOT NULL,                     -- 발급 시작 일시
    issue_end_at       DATETIME     NOT NULL,                     -- 발급 종료 일시
    use_start_at       DATETIME     NOT NULL,                     -- 사용 가능 시작 일시
    use_end_at         DATETIME     NOT NULL,                     -- 사용 가능 종료 일시
    is_visible         TINYINT(1)   NOT NULL DEFAULT 1,           -- 노출 여부 (1: 노출, 0: 숨김)
    is_deleted         TINYINT(1)   NOT NULL DEFAULT 0,           -- 삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)
    created_at         DATETIME     NOT NULL,                     -- 생성 일시
    updated_at         DATETIME     NOT NULL,                     -- 수정 일시
    INDEX idx_coupon_active (is_deleted, is_visible),             -- 인덱스: 삭제·노출 여부 복합 조회
    INDEX idx_coupon_issue_period (issue_start_at, issue_end_at), -- 인덱스: 발급 기간 조회
    INDEX idx_coupon_use_period (use_start_at, use_end_at)        -- 인덱스: 사용 기간 조회
);

CREATE TABLE MEMBER_COUPON
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,              -- 회원 쿠폰 ID (PK)
    member_id  BIGINT      NOT NULL,                           -- 회원 ID (MEMBER.id 참조)
    coupon_id  BIGINT      NOT NULL,                           -- 쿠폰 ID (COUPON.id 참조)
    is_used    TINYINT(1)  NOT NULL DEFAULT 0,                 -- 사용 여부 (1: 사용, 0: 미사용)
    used_at    DATETIME,                                       -- 사용 일시
    expired_at DATETIME    NOT NULL,                           -- 만료 일시
    created_at DATETIME    NOT NULL,                           -- 생성 일시
    updated_at DATETIME    NOT NULL,                           -- 수정 일시
    UNIQUE KEY uk_member_coupon (member_id, coupon_id),        -- 유니크: 회원당 쿠폰 중복 발급 방지
    INDEX idx_member_coupon_member_id (member_id),             -- 인덱스: 회원별 조회
    INDEX idx_member_coupon_coupon_id (coupon_id),             -- 인덱스: 쿠폰별 조회
    INDEX idx_member_coupon_used (member_id, is_used)          -- 인덱스: 회원·사용여부 복합 조회
);

CREATE TABLE EVENT_WINNER
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 이벤트 당첨자 ID (PK)
    event_id     BIGINT      NOT NULL,                               -- 이벤트 ID (EVENT.id 참조)
    rank_no      INT         NOT NULL,                               -- 당첨 순위
    winner_name  VARCHAR(50) NOT NULL,                               -- 당첨자 이름
    phone_number VARCHAR(11) NOT NULL,                               -- 당첨자 휴대폰 번호
    announced_at DATETIME    NOT NULL,                               -- 당첨 발표 일시
    is_deleted   TINYINT(1)  NOT NULL DEFAULT 0,                     -- 삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)
    created_at   DATETIME    NOT NULL,                               -- 생성 일시
    updated_at   DATETIME    NOT NULL,                               -- 수정 일시
    INDEX idx_event_winner_event_id (event_id, is_deleted),          -- 인덱스: 이벤트·삭제여부 복합 조회
    INDEX idx_event_winner_announced_at (announced_at)               -- 인덱스: 발표 일시별 조회
);

CREATE TABLE EVENT
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 이벤트 ID (PK)
    name                    VARCHAR(200) NOT NULL,              -- 이벤트 이름
    description             VARCHAR(1000),                      -- 이벤트 설명
    subtitle                VARCHAR(200),                       -- 이벤트 부제목
    thumbnail_image_file_id BIGINT,                             -- 썸네일 이미지 파일 ID (UPLOADED_FILE.id 참조)
    banner_image_file_id    BIGINT,                             -- 배너 이미지 파일 ID (UPLOADED_FILE.id 참조)
    content_html            TEXT,                               -- 이벤트 상세 내용 (HTML)
    status              VARCHAR(20)  NOT NULL,                  -- 이벤트 상태 (SCHEDULED, ACTIVE, ENDED)
    start_at            DATETIME     NOT NULL,                  -- 이벤트 시작 일시
    end_at              DATETIME     NOT NULL,                  -- 이벤트 종료 일시
    is_deleted          TINYINT(1)   NOT NULL DEFAULT 0,        -- 삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)
    created_at          DATETIME     NOT NULL,                  -- 생성 일시
    updated_at          DATETIME     NOT NULL,                  -- 수정 일시
    INDEX idx_event_active (is_deleted, status),                -- 인덱스: 삭제·상태 여부 복합 조회
    INDEX idx_event_period (start_at, end_at)                   -- 인덱스: 기간별 조회
);

CREATE TABLE EVENT_ANNOUNCEMENT
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,                              -- 이벤트 공지 ID (PK)
    event_id     BIGINT       NOT NULL UNIQUE,                                   -- 이벤트 ID (EVENT.id 참조, 이벤트당 1개)
    name         VARCHAR(200) NOT NULL,                                          -- 공지 이름
    content      VARCHAR(1000) NOT NULL,                                         -- 공지 내용
    announced_at DATETIME     NOT NULL,                                          -- 공지 발표 일시
    created_at   DATETIME     NOT NULL,                                          -- 생성 일시
    updated_at   DATETIME     NOT NULL,                                          -- 수정 일시
    INDEX idx_event_announcement_event_id (event_id),                            -- 인덱스: 이벤트별 조회
    INDEX idx_event_announcement_announced_at (announced_at)                     -- 인덱스: 발표 일시별 조회
);

CREATE TABLE MEMBER_FOLLOW
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,                            -- 팔로우 ID (PK)
    follower_id  BIGINT   NOT NULL,                                            -- 팔로워 회원 ID (MEMBER.id 참조)
    following_id BIGINT   NOT NULL,                                            -- 팔로잉 회원 ID (MEMBER.id 참조)
    created_at   DATETIME NOT NULL,                                            -- 생성 일시
    updated_at   DATETIME NOT NULL,                                            -- 수정 일시
    UNIQUE KEY uk_member_follow_follower_following (follower_id, following_id), -- 유니크: 중복 팔로우 방지
    INDEX idx_member_follow_follower_id (follower_id),                         -- 인덱스: 팔로워별 조회
    INDEX idx_member_follow_following_id (following_id)                        -- 인덱스: 팔로잉별 조회
);

CREATE TABLE MEMBER
(
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY, -- 회원 ID (PK)
    username                  VARCHAR(50)  NOT NULL UNIQUE,      -- 사용자명 (로그인 ID)
    password                  VARCHAR(255),                      -- 비밀번호 (소셜 로그인 시 NULL)
    nickname                  VARCHAR(50)  NOT NULL,             -- 닉네임
    full_name                 VARCHAR(100) NOT NULL,             -- 실명
    birth_date                INT,                               -- 생년월일 (YYYYMMDD 형식)
    gender                    VARCHAR(10)  NOT NULL,             -- 성별 (MALE, FEMALE 등)
    phone_number              VARCHAR(11)  NOT NULL,             -- 휴대폰 번호
    member_grade              VARCHAR(20)  NOT NULL DEFAULT 'NEWCOMER', -- 회원 등급 (NEWCOMER, BRONZE 등)
    profile_image_file_id     BIGINT,                            -- 프로필 이미지 파일 ID (UPLOADED_FILE.id 참조)
    status_message            VARCHAR(200),                      -- 상태 메시지
    push_notification_enabled TINYINT(1)   NOT NULL DEFAULT 1,  -- 푸시 알림 수신 여부 (1: 허용)
    marketing_info_enabled    TINYINT(1)   NOT NULL DEFAULT 0,  -- 마케팅 정보 수신 여부 (1: 허용)
    event_info_enabled        TINYINT(1)   NOT NULL DEFAULT 0,  -- 이벤트 정보 수신 여부 (1: 허용)
    member_status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- 회원 상태 (ACTIVE, DELETED 등)
    created_at                DATETIME     NOT NULL,             -- 생성 일시
    updated_at                DATETIME     NOT NULL              -- 수정 일시
);

CREATE TABLE ADMIN
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 관리자 ID (PK)
    username   VARCHAR(50)  NOT NULL UNIQUE,      -- 관리자 아이디 (로그인 ID)
    password   VARCHAR(255) NOT NULL,             -- 비밀번호 (BCrypt 인코딩)
    name       VARCHAR(100) NOT NULL,             -- 관리자 이름
    role       VARCHAR(20)  NOT NULL,             -- 관리자 권한 (SUPER_ADMIN, ADMIN)
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- 계정 상태 (ACTIVE, INACTIVE)
    created_at DATETIME     NOT NULL,             -- 생성 일시
    updated_at DATETIME     NOT NULL              -- 수정 일시
);

CREATE TABLE CEO
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 점주 ID (PK)
    username   VARCHAR(50)  NOT NULL UNIQUE,      -- 점주 아이디 (로그인 ID)
    password   VARCHAR(255) NOT NULL,             -- 비밀번호 (BCrypt 인코딩)
    name       VARCHAR(100) NOT NULL,             -- 점주 이름
    business_registration_number VARCHAR(20),     -- 사업자등록번호
    phone_number VARCHAR(11),                     -- 점주 휴대폰번호
    email      VARCHAR(200),                      -- 점주 이메일
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE', -- 계정 상태 (ACTIVE, INACTIVE)
    created_at DATETIME     NOT NULL,             -- 생성 일시
    updated_at DATETIME     NOT NULL              -- 수정 일시
);

CREATE TABLE MEMBER_SOCIAL_ACCOUNT
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,                              -- 소셜 계정 ID (PK)
    member_id                   BIGINT       NOT NULL,                                           -- 회원 ID (MEMBER.id 참조)
    provider                    VARCHAR(20)  NOT NULL,                                           -- 소셜 제공자 (KAKAO, NAVER, GOOGLE 등)
    provider_id                 VARCHAR(100) NOT NULL,                                           -- 소셜 제공자 고유 ID
    provider_email              VARCHAR(200),                                                    -- 소셜 계정 이메일
    provider_nickname           VARCHAR(100),                                                    -- 소셜 계정 닉네임
    provider_profile_image_url  VARCHAR(500),                                                    -- 소셜 계정 프로필 이미지 URL
    last_login_at               DATETIME,                                                        -- 마지막 로그인 일시
    created_at                  DATETIME     NOT NULL,                                           -- 생성 일시
    updated_at                  DATETIME     NOT NULL,                                           -- 수정 일시
    UNIQUE KEY uk_member_social_account_provider_provider_id (provider, provider_id),            -- 유니크: 제공자·제공자ID 중복 방지
    INDEX idx_member_social_account_member_id (member_id),                                       -- 인덱스: 회원별 조회
    INDEX idx_member_social_account_provider_id (provider, provider_id)                          -- 인덱스: 소셜 제공자·ID 조회
);

CREATE TABLE MEMBER_REVIEW_RANK
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,                    -- 회원 리뷰 랭킹 ID (PK)
    member_id      BIGINT      NOT NULL,                                 -- 회원 ID (MEMBER.id 참조)
    review_count   INT         NOT NULL,                                 -- 리뷰 작성 수
    rank_no        INT         NOT NULL,                                 -- 순위
    rank_type      VARCHAR(20) NOT NULL,                                 -- 랭킹 유형 (WEEKLY, MONTHLY 등)
    base_date      DATE        NOT NULL,                                 -- 랭킹 기준 날짜
    last_review_at DATETIME,                                             -- 마지막 리뷰 작성 일시
    created_at     DATETIME    NOT NULL,                                 -- 생성 일시
    updated_at     DATETIME    NOT NULL,                                 -- 수정 일시
    UNIQUE KEY uk_member_rank (member_id, rank_type, base_date),         -- 유니크: 회원·유형·날짜 중복 방지
    INDEX idx_rank_query (rank_type, base_date, rank_no),                -- 인덱스: 유형·날짜·순위 복합 조회
    INDEX idx_member_rank (member_id, rank_type)                         -- 인덱스: 회원·유형 조회
);

CREATE TABLE PRODUCT
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 상품 ID (PK)
    shop_id            BIGINT        NOT NULL,                      -- 장소 ID (SHOP.id 참조)
    product_category_id BIGINT,                                      -- 상품 카테고리 ID (PRODUCT_CATEGORY.id 참조)
    name                VARCHAR(255)  NOT NULL,                      -- 상품명
    description         VARCHAR(1000),                               -- 상품 설명
    original_price      INT           NOT NULL,                      -- 정가
    discount_price      INT,                                         -- 할인가
    discount_rate       DECIMAL(19, 2),                              -- 할인율 (%)
    rating              DOUBLE,                                      -- 평균 평점
    review_count        INT           DEFAULT 0,                     -- 리뷰 수
    is_representative   TINYINT(1)    DEFAULT 0,                     -- 대표 상품 여부 (1: 대표)
    spiciness           INT,                                         -- 맵기 단계
    is_sold_out         TINYINT(1)    NOT NULL DEFAULT 0,            -- 품절 여부 (1: 품절)
    sold_out_until      DATETIME,                                    -- 품절 자동해제 시각 (NULL이면 수동 해제까지 유지)
    is_visible          TINYINT(1)    NOT NULL DEFAULT 1,            -- 노출 여부 (1: 노출)
    is_rating_excluded  TINYINT(1)    NOT NULL DEFAULT 0,            -- 메뉴 평가 제외 여부 (1: 제외 — 주류·사이드 등)
    is_deleted          TINYINT(1)    NOT NULL DEFAULT 0,            -- 삭제 여부 (1: 삭제 — Soft Delete)
    composition         VARCHAR(500),                                -- 메뉴구성 (메뉴판 목록에서 메뉴명 하단에 노출)
    single_serving      TINYINT(1)    NOT NULL DEFAULT 0,            -- 1인분 여부 (1: 1인분)
    exposure_start_date DATE,                                        -- 노출 시작일 (NULL이면 하한 없음)
    exposure_end_date   DATE,                                        -- 노출 종료일 (NULL이면 상한 없음, 당일 포함)
    vegetarian_type     VARCHAR(20),                                 -- 채식 단계 (VEGAN, LACTO, OVO, LACTO_OVO, PESCO / NULL이면 채식 아님). 관리자 승인 시에만 반영
    sort                INT           NOT NULL,                      -- 정렬 순서
    created_at          DATETIME      NOT NULL,                      -- 생성 일시
    updated_at          DATETIME      NOT NULL,                      -- 수정 일시
    INDEX idx_product_shop_id (shop_id),                           -- 인덱스: 장소별 조회
    INDEX idx_product_category (shop_id, is_deleted, product_category_id, sort), -- 인덱스: 장소·카테고리 복합 조회
    INDEX idx_product_representative (shop_id, is_representative),  -- 인덱스: 장소·대표상품 조회
    INDEX idx_product_active (shop_id, is_deleted, is_visible, sort), -- 인덱스: 장소·노출·정렬 복합 조회
    INDEX idx_product_sold_out_until (is_sold_out, sold_out_until)  -- 인덱스: 품절 자동해제 배치 스캔용
);

CREATE TABLE PRODUCT_BBQ
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,               -- BBQ 상품 연동 ID (PK)
    product_id      BIGINT   NOT NULL UNIQUE,                        -- 상품 ID (PRODUCT.id 참조)
    bbq_menu_id     BIGINT   NOT NULL,                               -- BBQ 메뉴 ID
    bbq_category_id BIGINT,                                          -- BBQ 카테고리 ID
    is_options_synced TINYINT(1) NOT NULL DEFAULT 0,                 -- 옵션 동기화 여부 (1: 동기화 완료)
    created_at      DATETIME NOT NULL,                               -- 생성 일시
    updated_at      DATETIME NOT NULL,                               -- 수정 일시
    INDEX idx_product_bbq_product_id (product_id),                   -- 인덱스: 상품별 조회
    INDEX idx_product_bbq_menu_id (bbq_menu_id),                     -- 인덱스: BBQ 메뉴별 조회
    INDEX idx_product_bbq_category_id (bbq_category_id)              -- 인덱스: BBQ 카테고리별 조회
);

CREATE TABLE PRODUCT_IMAGE
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 상품 이미지 ID (PK)
    product_id       BIGINT       NOT NULL,                            -- 상품 ID (PRODUCT.id 참조)
    sort             INT          NOT NULL,                            -- 정렬 순서
    is_visible       TINYINT(1)   NOT NULL DEFAULT 1,                  -- 노출 여부 (1: 노출)
    image_file_id    BIGINT       NOT NULL,                            -- 이미지 파일 ID (UPLOADED_FILE.id 참조)
    created_at       DATETIME     NOT NULL,                            -- 생성 일시
    updated_at       DATETIME     NOT NULL,                            -- 수정 일시
    INDEX idx_product_image_product_id (product_id),                   -- 인덱스: 상품별 조회
    INDEX idx_product_image_active (product_id, is_visible, sort)      -- 인덱스: 상품·노출·정렬 복합 조회
);

CREATE TABLE PRODUCT_CATEGORY
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,                      -- 상품 카테고리 ID (PK)
    shop_id      BIGINT       NOT NULL,                                  -- 장소 ID (SHOP.id 참조)
    name  VARCHAR(100) NOT NULL,                                          -- 카테고리 이름
    description   VARCHAR(500),                                            -- 메뉴그룹 설명
    sort          INT          NOT NULL,                                  -- 정렬 순서
    is_visible    TINYINT(1)   NOT NULL DEFAULT 1,                        -- 노출 여부 (1: 노출)
    created_at    DATETIME     NOT NULL,                                  -- 생성 일시
    updated_at    DATETIME     NOT NULL,                                  -- 수정 일시
    INDEX idx_product_category_shop_id (shop_id),                       -- 인덱스: 장소별 조회
    INDEX idx_product_category_active (shop_id, is_visible, sort)        -- 인덱스: 장소·노출·정렬 복합 조회
);

CREATE TABLE PRODUCT_COMMON_OPTION_GROUP
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,                                 -- 공통 옵션 그룹 ID (PK)
    product_id         BIGINT       NOT NULL,                                             -- 상품 ID (PRODUCT.id 참조)
    name               VARCHAR(100) NOT NULL,                                             -- 옵션 그룹 이름
    description        VARCHAR(500),                                                      -- 옵션 그룹 설명
    is_required        TINYINT(1)   NOT NULL DEFAULT 0,                                   -- 필수 선택 여부 (1: 필수)
    is_multiple_select TINYINT(1)   NOT NULL DEFAULT 0,                                   -- 다중 선택 여부 (1: 가능)
    min_select         INT,                                                                -- 최소 선택 수
    max_select         INT,                                                                -- 최대 선택 수
    sort               INT          NOT NULL,                                             -- 정렬 순서
    is_visible         TINYINT(1)   NOT NULL DEFAULT 1,                                   -- 노출 여부 (1: 노출)
    created_at         DATETIME     NOT NULL,                                             -- 생성 일시
    updated_at         DATETIME     NOT NULL,                                             -- 수정 일시
    INDEX idx_product_common_option_group_product_id (product_id),                        -- 인덱스: 상품별 조회
    INDEX idx_product_common_option_group_active (product_id, is_visible, sort)           -- 인덱스: 상품·노출·정렬 복합 조회
);

CREATE TABLE PRODUCT_COMMON_OPTION
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,                              -- 공통 옵션 ID (PK)
    option_group_id  BIGINT       NOT NULL,                                          -- 옵션 그룹 ID (PRODUCT_COMMON_OPTION_GROUP.id 참조)
    name             VARCHAR(100) NOT NULL,                                          -- 옵션 이름
    additional_price INT          NOT NULL DEFAULT 0,                                -- 추가 금액
    sort             INT          NOT NULL,                                          -- 정렬 순서
    is_sold_out      TINYINT(1)   NOT NULL DEFAULT 0,                                -- 품절 여부 (1: 품절)
    sold_out_until   DATETIME,                                                       -- 품절 자동해제 시각 (NULL이면 수동 해제까지 유지)
    is_visible       TINYINT(1)   NOT NULL DEFAULT 1,                                -- 노출 여부 (1: 노출)
    created_at       DATETIME     NOT NULL,                                          -- 생성 일시
    updated_at       DATETIME     NOT NULL,                                          -- 수정 일시
    INDEX idx_product_common_option_group_id (option_group_id),                      -- 인덱스: 옵션 그룹별 조회
    INDEX idx_product_common_option_active (option_group_id, is_visible, sort),      -- 인덱스: 옵션 그룹·노출·정렬 복합 조회
    INDEX idx_product_common_option_sold_out_until (is_sold_out, sold_out_until)     -- 인덱스: 품절 자동해제 배치 스캔용
);

CREATE TABLE PRODUCT_OPTION
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,                         -- 상품 옵션 ID (PK)
    option_group_id  BIGINT       NOT NULL,                                     -- 옵션 그룹 ID (PRODUCT_OPTION_GROUP.id 참조)
    name             VARCHAR(100) NOT NULL,                                     -- 옵션 이름
    additional_price INT          NOT NULL DEFAULT 0,                           -- 추가 금액 (보증금 옵션은 0)
    cup_count        INT,                                                        -- 일회용컵 제공 개수(1~10). 보증금 옵션그룹의 옵션만 값을 가짐. 보증금액 = cup_count * 300
    personal_cup_discount_amount INT,                                            -- 개인컵 사용 할인 금액(원). 개인컵 옵션이 아니면 NULL. 보증금이 아니라 상품 할인 축
    sort             INT          NOT NULL,                                     -- 정렬 순서
    is_sold_out      TINYINT(1)   NOT NULL DEFAULT 0,                           -- 품절 여부 (1: 품절)
    sold_out_until   DATETIME,                                                  -- 품절 자동해제 시각 (NULL이면 수동 해제까지 유지)
    is_visible       TINYINT(1)   NOT NULL DEFAULT 1,                           -- 노출 여부 (1: 노출)
    created_at       DATETIME     NOT NULL,                                     -- 생성 일시
    updated_at       DATETIME     NOT NULL,                                     -- 수정 일시
    INDEX idx_product_option_group_id (option_group_id),                        -- 인덱스: 옵션 그룹별 조회
    INDEX idx_product_option_active (option_group_id, is_visible, sort),        -- 인덱스: 옵션 그룹·노출·정렬 복합 조회
    INDEX idx_product_option_sold_out_until (is_sold_out, sold_out_until)       -- 인덱스: 품절 자동해제 배치 스캔용
);

CREATE TABLE PRODUCT_OPTION_GROUP
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,                             -- 상품 옵션 그룹 ID (PK)
    product_id         BIGINT       NOT NULL,                                         -- 상품 ID (PRODUCT.id 참조)
    name               VARCHAR(100) NOT NULL,                                         -- 옵션 그룹 이름
    description        VARCHAR(500),                                                  -- 옵션 그룹 설명
    is_required        TINYINT(1)   NOT NULL DEFAULT 0,                               -- 필수 선택 여부 (1: 필수)
    is_multiple_select TINYINT(1)   NOT NULL DEFAULT 0,                               -- 다중 선택 여부 (1: 가능)
    min_select         INT,                                                            -- 최소 선택 수
    max_select         INT,                                                            -- 최대 선택 수
    group_type         VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',                        -- 옵션그룹 유형 (NORMAL, CUP_DEPOSIT)
    sort               INT          NOT NULL,                                         -- 정렬 순서
    is_visible         TINYINT(1)   NOT NULL DEFAULT 1,                               -- 노출 여부 (1: 노출)
    created_at         DATETIME     NOT NULL,                                         -- 생성 일시
    updated_at         DATETIME     NOT NULL,                                         -- 수정 일시
    INDEX idx_product_option_group_product_id (product_id),                           -- 인덱스: 상품별 조회
    INDEX idx_product_option_group_active (product_id, is_visible, sort)              -- 인덱스: 상품·노출·정렬 복합 조회
);

CREATE TABLE PRODUCT_OPTION_GROUP_LINK
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,                              -- 링크 ID (PK)
    product_id      BIGINT   NOT NULL,                                              -- 상품 ID (PRODUCT.id 참조)
    option_group_id BIGINT   NOT NULL,                                              -- 옵션 그룹 ID (PRODUCT_OPTION_GROUP.id 참조)
    sort            INT      NOT NULL,                                              -- 이 메뉴에서의 옵션그룹 정렬 순서 (메뉴별 독립)
    created_at      DATETIME NOT NULL,                                              -- 생성 일시
    updated_at      DATETIME NOT NULL,                                              -- 수정 일시
    UNIQUE KEY uk_product_option_group_link (product_id, option_group_id),          -- 유니크: 같은 메뉴에 같은 그룹 중복 연결 방지
    INDEX idx_product_option_group_link_product (product_id, sort),                 -- 인덱스: 메뉴별 정렬 조회
    INDEX idx_product_option_group_link_group (option_group_id)                     -- 인덱스: 그룹 역조회(소유 메뉴 목록 — 소유권 검증)
);

-- 옵션그룹 합치기 이력 (append-only).
-- 합치기는 분리 불가이고, 흡수된 그룹은 링크가 기준 그룹으로 옮겨져 소유 가게 역조회가 영구히
-- 불가능해진다. 이 테이블이 없으면 "내 옵션그룹이 사라졌어요" 문의에 답할 근거가 0이다.
-- 되돌리기용이 아니라 감사·문의응대 전용. 선례: SHOP_CHANGE_HISTORY
CREATE TABLE PRODUCT_OPTION_GROUP_MERGE_HISTORY
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 이력 ID (PK)
    shop_id                BIGINT       NOT NULL,                       -- 가게 ID (SHOP.id 참조). 합치기 후 그룹→가게 역조회가 불가해 여기에 박제
    base_option_group_id   BIGINT       NOT NULL,                       -- 기준 옵션그룹 ID (살아남은 그룹, PRODUCT_OPTION_GROUP.id 참조)
    merged_option_group_id BIGINT       NOT NULL,                       -- 흡수된 옵션그룹 ID (감춰진 그룹, PRODUCT_OPTION_GROUP.id 참조)
    merged_group_name      VARCHAR(100) NOT NULL,                       -- 흡수 시점 옵션그룹명 (스냅샷). 이후 기준그룹만 수정되므로 이름이 유일한 식별 단서
    entry_type             VARCHAR(20)  NOT NULL,                       -- 진입 경로 (RECOMMENDED, MANUAL)
    actor_ceo_id           BIGINT       NOT NULL,                       -- 합치기를 수행한 점주 ID (CEO.id 참조)
    created_at             DATETIME     NOT NULL,                       -- 생성 일시 (= 합치기 시각)
    updated_at             DATETIME     NOT NULL,                       -- 수정 일시 (append-only 라 created_at 과 동일)
    INDEX idx_product_option_group_merge_history_shop (shop_id, created_at),      -- 인덱스: 가게별 이력 조회
    INDEX idx_product_option_group_merge_history_merged (merged_option_group_id), -- 인덱스: "이 그룹은 어디로 갔나" 역조회
    INDEX idx_product_option_group_merge_history_base (base_option_group_id, created_at) -- 인덱스: "이 그룹은 무엇을 흡수했나"
);

-- 옵션그룹 합치기 추천 제외 (append-only, 가게 단위).
-- 그룹 id 쌍이 아니라 '동일성 서명'을 저장한다 — 추천 기준은 쌍이 아니라 동치류이므로 쌍 저장은
-- O(n^2) 행이 필요하고 멤버 하나가 빠진 같은 묶음이 다시 추천된다.
-- 서명이 달라지면(옵션명·가격 수정) 다시 추천되는 것이 의도된 동작이다.
CREATE TABLE PRODUCT_OPTION_GROUP_MERGE_EXCLUSION
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 제외 ID (PK)
    shop_id         BIGINT   NOT NULL,                                  -- 가게 ID (SHOP.id 참조)
    group_signature CHAR(64) NOT NULL,                                  -- 동일성 서명 (SHA-256 hex: 그룹명|min|max|옵션개수|정렬된 옵션명:가격 목록)
    actor_ceo_id    BIGINT   NOT NULL,                                  -- 제외한 점주 ID (CEO.id 참조)
    created_at      DATETIME NOT NULL,                                  -- 생성 일시
    updated_at      DATETIME NOT NULL,                                  -- 수정 일시 (append-only 라 created_at 과 동일)
    UNIQUE KEY uk_product_option_group_merge_exclusion (shop_id, group_signature), -- 유니크: 같은 묶음 중복 제외 방지(멱등)
    INDEX idx_product_option_group_merge_exclusion_shop (shop_id)       -- 인덱스: 추천 조회 시 가게별 제외 목록 로드
);

CREATE TABLE PRODUCT_COMMON_OPTION_GROUP_LINK
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,                              -- 링크 ID (PK)
    product_id      BIGINT   NOT NULL,                                              -- 상품 ID (PRODUCT.id 참조)
    option_group_id BIGINT   NOT NULL,                                              -- 공통 옵션 그룹 ID (PRODUCT_COMMON_OPTION_GROUP.id 참조)
    sort            INT      NOT NULL,                                              -- 이 메뉴에서의 옵션그룹 정렬 순서 (메뉴별 독립)
    created_at      DATETIME NOT NULL,                                              -- 생성 일시
    updated_at      DATETIME NOT NULL,                                              -- 수정 일시
    UNIQUE KEY uk_product_common_option_group_link (product_id, option_group_id),   -- 유니크: 같은 메뉴에 같은 그룹 중복 연결 방지
    INDEX idx_product_common_option_group_link_product (product_id, sort),          -- 인덱스: 메뉴별 정렬 조회
    INDEX idx_product_common_option_group_link_group (option_group_id)              -- 인덱스: 그룹 역조회(소유 메뉴 목록 — 소유권 검증)
);

CREATE TABLE PRODUCT_EXPOSURE_HOUR
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                          -- 노출 시간대 ID (PK)
    product_id BIGINT      NOT NULL,                                       -- 상품 ID (PRODUCT.id 참조)
    day_type   VARCHAR(20) NOT NULL,                                       -- 요일 유형 (DAILY, WEEKDAY, WEEKEND, HOLIDAY, MONDAY..SUNDAY)
    start_time TIME,                                                       -- 노출 시작 시각 (NULL이면 종일)
    end_time   TIME,                                                       -- 노출 종료 시각 (NULL이면 종일. start보다 작으면 자정 넘김)
    created_at DATETIME    NOT NULL,                                       -- 생성 일시
    updated_at DATETIME    NOT NULL,                                       -- 수정 일시
    INDEX idx_product_exposure_hour_product_id (product_id),               -- 인덱스: 상품별 조회
    UNIQUE KEY uk_product_exposure_hour (product_id, day_type)             -- 유니크: 상품·요일 중복 방지
);

CREATE TABLE PRODUCT_IMAGE_CHANGE_REQUEST
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,                       -- 요청 ID (PK)
    product_id    BIGINT       NOT NULL,                                   -- 상품 ID (PRODUCT.id 참조)
    image_file_id BIGINT       NOT NULL,                                   -- 요청된 신규 이미지 파일 ID (UPLOADED_FILE.id 참조)
    status        VARCHAR(20)  NOT NULL,                                   -- 승인 상태 (PENDING, APPROVED, REJECTED, CANCELED)
    reject_reason VARCHAR(500),                                            -- 반려 사유
    created_at    DATETIME     NOT NULL,                                   -- 생성 일시
    updated_at    DATETIME     NOT NULL,                                   -- 수정 일시
    INDEX idx_product_image_change_request_product_status (product_id, status), -- 인덱스: 상품별 상태 조회
    INDEX idx_product_image_change_request_status (status)                 -- 인덱스: 검수 목록 조회
);

CREATE TABLE PRODUCT_VEGETARIAN_REQUEST
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,                     -- 요청 ID (PK)
    product_id      BIGINT        NOT NULL,                                -- 상품 ID (PRODUCT.id 참조)
    vegetarian_type VARCHAR(20)   NOT NULL,                                -- 채식 단계 (VEGAN, LACTO, OVO, LACTO_OVO, PESCO)
    ingredients     VARCHAR(1000) NOT NULL,                                -- 채소 외 포함 재료 (검수 근거)
    description     VARCHAR(1000),                                         -- 메뉴 설명 (검수 근거)
    status          VARCHAR(20)   NOT NULL,                                -- 승인 상태 (PENDING, APPROVED, REJECTED, CANCELED)
    reject_reason   VARCHAR(500),                                          -- 반려 사유
    created_at      DATETIME      NOT NULL,                                -- 생성 일시
    updated_at      DATETIME      NOT NULL,                                -- 수정 일시
    INDEX idx_product_vegetarian_request_product_status (product_id, status), -- 인덱스: 상품별 상태 조회
    INDEX idx_product_vegetarian_request_status (status)                   -- 인덱스: 검수 목록 조회
);

CREATE TABLE SHOP
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY, -- 장소 ID (PK)
    ceo_id            BIGINT,                            -- 소유 점주 ID (CEO.id 참조, NULL이면 미배정)
    station_id        BIGINT        NOT NULL,            -- 지하철역 ID (STATION.id 참조)
    name              VARCHAR(255)  NOT NULL UNIQUE,     -- 장소 이름
    latitude          DECIMAL(9, 6) NOT NULL,            -- 위도
    longitude         DECIMAL(9, 6) NOT NULL,            -- 경도
    rating            DOUBLE,                            -- 평균 평점
    road_address      VARCHAR(500),                      -- 도로명 주소
    lot_address       VARCHAR(500),                      -- 지번 주소
    phone_number      VARCHAR(20),                       -- 대표 전화번호
    thumbnail_image_file_id    BIGINT,                   -- 썸네일 이미지 파일 ID (UPLOADED_FILE.id 참조)
    trademark_image_file_id    BIGINT,                   -- 상표 이미지 파일 ID (승인 완료 시 반영, UPLOADED_FILE.id 참조)
    is_permanently_closed      TINYINT(1)    NOT NULL DEFAULT 0, -- 폐업 여부 (1: 폐업)
    is_hidden                  TINYINT(1)    NOT NULL DEFAULT 0, -- 노출정지 여부 (1: 배민앱 완전 비노출)
    is_closed_on_public_holidays TINYINT(1)  NOT NULL DEFAULT 0, -- 공휴일 휴무 여부
    min_order_amount           INT           NOT NULL DEFAULT 0, -- 최소주문금액 (0: 미설정, 설정 시 5000~30000, 배달 주문에만 적용)
    scheduled_order_enabled    TINYINT(1)    NOT NULL DEFAULT 0, -- 예약주문 운영 여부 (0: 미운영, 1: 운영)
    cup_deposit_enabled        TINYINT(1)    NOT NULL DEFAULT 0, -- 일회용컵 보증금제 대상 사업자 여부 (관리자만 변경, 1: 대상)
    created_at        DATETIME      NOT NULL,            -- 생성 일시
    updated_at        DATETIME      NOT NULL,            -- 수정 일시
    INDEX idx_shop_ceo_id (ceo_id)
);

CREATE TABLE POINT
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 회원 포인트 ID (PK)
    member_id               BIGINT   NOT NULL UNIQUE,            -- 회원 ID (MEMBER.id 참조)
    available_points        INT      NOT NULL DEFAULT 0,         -- 사용 가능 포인트
    expired_this_month      INT      NOT NULL DEFAULT 0,         -- 이번 달 만료 예정 포인트
    created_at              DATETIME NOT NULL,                   -- 생성 일시
    updated_at              DATETIME NOT NULL,                   -- 수정 일시
    INDEX idx_point_member_id (member_id)                        -- 인덱스: 회원별 조회
);

CREATE TABLE POINT_HISTORY
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                    -- 포인트 이력 ID (PK)
    member_id         BIGINT       NOT NULL,                                -- 회원 ID (MEMBER.id 참조)
    point_type        VARCHAR(50)  NOT NULL,                                -- 포인트 유형 (EARN, USE, EXPIRE 등)
    point_amount      INT          NOT NULL,                                -- 포인트 변동 금액
    reason            VARCHAR(200) NOT NULL,                                -- 변동 사유
    created_at        DATETIME     NOT NULL,                                -- 생성 일시
    updated_at        DATETIME     NOT NULL,                                -- 수정 일시
    INDEX idx_point_history_member_id (member_id),                         -- 인덱스: 회원별 조회
    INDEX idx_point_history_created_at (created_at)                        -- 인덱스: 생성 일시별 조회
);

CREATE TABLE NOTICE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 공지사항 ID (PK)
    title      VARCHAR(200)  NOT NULL,            -- 공지 제목
    content    VARCHAR(1000) NOT NULL,            -- 공지 내용
    is_visible TINYINT(1)    NOT NULL DEFAULT 1,  -- 노출 여부 (1: 노출)
    is_deleted TINYINT(1)    NOT NULL DEFAULT 0,  -- 삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)
    created_at DATETIME      NOT NULL,            -- 생성 일시
    updated_at DATETIME      NOT NULL,            -- 수정 일시
    INDEX idx_notice_active (is_deleted, is_visible), -- 인덱스: 삭제·노출 여부 복합 조회
    INDEX idx_notice_created_at (created_at)      -- 인덱스: 생성 일시별 조회
);

CREATE TABLE POLICY_DOCUMENT
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,                           -- 약관 문서 ID (PK)
    type           VARCHAR(50)  NOT NULL,                                       -- 약관 유형 (TERMS, PRIVACY 등)
    version        VARCHAR(20)  NOT NULL,                                       -- 버전
    title          VARCHAR(200) NOT NULL,                                       -- 제목
    content        LONGTEXT     NOT NULL,                                       -- 내용 (HTML)
    is_current     TINYINT(1)   NOT NULL DEFAULT 0,                             -- 현재 적용 버전 여부 (1: 적용 중)
    mandatory      TINYINT(1)   NOT NULL DEFAULT 1,                             -- 필수 동의 여부 (1: 필수)
    effective_date DATETIME     NOT NULL,                                       -- 시행 일시
    created_by     VARCHAR(100),                                                -- 생성자
    updated_by     VARCHAR(100),                                                -- 수정자
    created_at     DATETIME     NOT NULL,                                       -- 생성 일시
    updated_at     DATETIME     NOT NULL,                                       -- 수정 일시
    UNIQUE KEY uk_policy_document_type_version (type, version),                 -- 유니크: 유형·버전 중복 방지
    INDEX idx_policy_document_type_current (type, is_current),                  -- 인덱스: 유형·현재 버전 조회
    INDEX idx_policy_document_type (type),                                      -- 인덱스: 유형별 조회
    INDEX idx_policy_document_effective_date (effective_date)                   -- 인덱스: 시행 일시별 조회
);

CREATE TABLE PARTNERSHIP_REQUEST
(
    id                         BIGINT AUTO_INCREMENT PRIMARY KEY,                                    -- 제휴 신청 ID (PK)
    business_name              VARCHAR(200) NOT NULL,                                                -- 사업체명
    address                    VARCHAR(500) NOT NULL,                                                -- 사업체 주소
    address_detail             VARCHAR(500),                                                         -- 사업체 상세 주소
    contact_name               VARCHAR(100) NOT NULL,                                                -- 담당자 이름
    contact_phone              VARCHAR(20)  NOT NULL,                                                -- 담당자 연락처
    consultation_requested_at  DATETIME     NOT NULL,                                                -- 상담 희망 일시
    status                     VARCHAR(20)  NOT NULL,                                                -- 처리 상태 (PENDING, IN_PROGRESS, COMPLETED)
    is_deleted                 TINYINT(1)   NOT NULL DEFAULT 0,                                      -- 삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)
    created_at                 DATETIME     NOT NULL,                                                -- 생성 일시
    updated_at                 DATETIME     NOT NULL,                                                -- 수정 일시
    INDEX idx_partnership_request_business_name (business_name),                                     -- 인덱스: 사업체명별 조회
    INDEX idx_partnership_request_consultation_date (consultation_requested_at),                     -- 인덱스: 상담 희망 일시별 조회
    INDEX idx_partnership_request_status (is_deleted, status)                                        -- 인덱스: 삭제·처리상태 복합 조회
);

CREATE TABLE SHOP_OWNER_MESSAGE_HISTORY
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,                           -- 사장님 메시지 이력 ID (PK)
    shop_id      BIGINT   NOT NULL,                                           -- 장소 ID (SHOP.id 참조)
    message       TEXT,                                                        -- 메시지 내용
    created_at    DATETIME NOT NULL,                                           -- 생성 일시
    updated_at    DATETIME NOT NULL,                                           -- 수정 일시
    INDEX idx_shop_owner_message_history_shop_id (shop_id)                  -- 인덱스: 장소별 조회
);

CREATE TABLE SHOP_AMENITY_CATEGORY
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 편의시설 카테고리 ID (PK)
    amenity                 VARCHAR(50)  NOT NULL UNIQUE,        -- 편의시설 코드 (WIFI, PARKING 등)
    display_name            VARCHAR(100) NOT NULL,               -- 화면 표시 이름
    active_image_file_id    BIGINT       NOT NULL,               -- 활성 상태 이미지 파일 ID (UPLOADED_FILE.id 참조)
    inactive_image_file_id  BIGINT       NOT NULL,               -- 비활성 상태 이미지 파일 ID (UPLOADED_FILE.id 참조)
    sort                    INT          NOT NULL,               -- 정렬 순서
    is_visible              TINYINT(1)   NOT NULL DEFAULT 1,     -- 노출 여부 (1: 노출)
    created_at              DATETIME     NOT NULL,               -- 생성 일시
    updated_at              DATETIME     NOT NULL,               -- 수정 일시
    INDEX idx_amenity_category_active (is_visible, sort)         -- 인덱스: 노출·정렬 복합 조회
);

CREATE TABLE SHOP_AMENITY
(
    id                        BIGINT AUTO_INCREMENT PRIMARY KEY,                        -- 가게 편의시설 ID (PK)
    shop_id                  BIGINT NOT NULL,                                          -- 장소 ID (SHOP.id 참조)
    shop_amenity_category_id BIGINT NOT NULL,                                          -- 편의시설 카테고리 ID (SHOP_AMENITY_CATEGORY.id 참조)
    UNIQUE KEY uk_shop_amenity (shop_id, shop_amenity_category_id),                  -- 유니크: 장소·편의시설 중복 방지
    INDEX idx_shop_amenity_shop_id (shop_id)                                         -- 인덱스: 장소별 조회
);

CREATE TABLE SHOP_BOOKMARK
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                   -- 가게 북마크 ID (PK)
    shop_id   BIGINT   NOT NULL,                                   -- 장소 ID (SHOP.id 참조)
    member_id  BIGINT   NOT NULL,                                   -- 회원 ID (MEMBER.id 참조)
    created_at DATETIME NOT NULL,                                   -- 생성 일시
    updated_at DATETIME NOT NULL,                                   -- 수정 일시
    UNIQUE KEY uk_shop_bookmark (shop_id, member_id),             -- 유니크: 장소·회원 중복 방지
    INDEX idx_shop_bookmark_shop_id (shop_id),                   -- 인덱스: 장소별 조회
    INDEX idx_shop_bookmark_member_id (member_id)                  -- 인덱스: 회원별 조회
);

CREATE TABLE SHOP_BUSINESS_HOUR
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,               -- 영업 시간 ID (PK)
    shop_id         BIGINT      NOT NULL,                            -- 장소 ID (SHOP.id 참조)
    day_type         VARCHAR(20) NOT NULL,                            -- 요일 유형 (MON, TUE, WEEKDAY, WEEKEND 등)
    open_time        TIME,                                            -- 영업 시작 시간
    close_time       TIME,                                            -- 영업 종료 시간
    is_closed        TINYINT(1),                                      -- 휴무 여부 (1: 휴무)
    is_open_24_hours TINYINT(1),                                      -- 24시간 영업 여부 (1: 24시간)
    INDEX idx_shop_business_hour_shop_id (shop_id),                -- 인덱스: 장소별 조회
    UNIQUE KEY uk_shop_business_hour (shop_id, day_type)            -- 유니크: 장소·요일 중복 방지
);

CREATE TABLE SHOP_BREAK_TIME
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 휴게 시간 ID (PK)
    shop_id         BIGINT      NOT NULL,                        -- 장소 ID (SHOP.id 참조)
    day_type         VARCHAR(20) NOT NULL,                        -- 요일 유형 (MON, TUE, WEEKDAY, WEEKEND 등)
    start_time TIME        NOT NULL,                              -- 휴게 시작 시간
    end_time   TIME        NOT NULL,                              -- 휴게 종료 시간
    INDEX idx_shop_break_time_shop_id (shop_id),               -- 인덱스: 장소별 조회
    UNIQUE KEY uk_shop_break_time (shop_id, day_type)           -- 유니크: 장소·요일 중복 방지
);

CREATE TABLE SHOP_CLOSED_DAY
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 정기 휴무일 ID (PK)
    shop_id         BIGINT      NOT NULL,                          -- 장소 ID (SHOP.id 참조)
    closed_day_type  VARCHAR(50) NOT NULL,                          -- 휴무 유형 (EVERY_MON, FIRST_SAT 등)
    INDEX idx_shop_closed_day_shop_id (shop_id)                  -- 인덱스: 장소별 조회
);

CREATE TABLE SHOP_ORDER_METHOD
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,                      -- 주문 방식 ID (PK)
    shop_id     BIGINT       NOT NULL,                                  -- 장소 ID (SHOP.id 참조)
    order_method VARCHAR(50)  NOT NULL,                                  -- 주문 방식 (DINE_IN, TAKEOUT, DELIVERY 등)
    created_at   DATETIME     NOT NULL,                                  -- 생성 일시
    updated_at   DATETIME     NOT NULL,                                  -- 수정 일시
    UNIQUE KEY uk_shop_order_method (shop_id, order_method),           -- 유니크: 장소·주문방식 중복 방지
    INDEX idx_shop_order_method_shop_id (shop_id)                     -- 인덱스: 장소별 조회
);

CREATE TABLE SHOP_CHOICE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 가게 픽 ID (PK)
    shop_id   BIGINT       NOT NULL,             -- 장소 ID (SHOP.id 참조)
    title      VARCHAR(200) NOT NULL,             -- 픽 제목
    content    TEXT,                              -- 픽 내용
    created_at DATETIME     NOT NULL,             -- 생성 일시
    updated_at DATETIME     NOT NULL              -- 수정 일시
);

CREATE TABLE SHOP_FOOD_TYPE_CATEGORY
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,    -- 음식 유형 카테고리 ID (PK)
    food_type              VARCHAR(50)  NOT NULL UNIQUE,          -- 음식 유형 코드 (KOREAN, JAPANESE 등)
    display_name           VARCHAR(100) NOT NULL,                 -- 화면 표시 이름
    active_image_file_id   BIGINT       NOT NULL,                 -- 활성 상태 이미지 파일 ID (UPLOADED_FILE.id 참조)
    inactive_image_file_id BIGINT       NOT NULL,                 -- 비활성 상태 이미지 파일 ID (UPLOADED_FILE.id 참조)
    sort                   INT          NOT NULL,                 -- 정렬 순서
    is_visible             TINYINT(1)   NOT NULL DEFAULT 1,       -- 노출 여부 (1: 노출)
    created_at             DATETIME     NOT NULL,                 -- 생성 일시
    updated_at             DATETIME     NOT NULL,                 -- 수정 일시
    INDEX idx_food_type_category_active (is_visible, sort)        -- 인덱스: 노출·정렬 복합 조회
);

CREATE TABLE SHOP_FOOD_TYPE
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,                      -- 가게 음식 유형 ID (PK)
    shop_id                    BIGINT NOT NULL,                                        -- 장소 ID (SHOP.id 참조)
    shop_food_type_category_id BIGINT NOT NULL,                                        -- 음식 유형 카테고리 ID (SHOP_FOOD_TYPE_CATEGORY.id 참조)
    UNIQUE KEY uk_shop_food_type (shop_id, shop_food_type_category_id),              -- 유니크: 장소·음식유형 중복 방지
    INDEX idx_shop_food_type_shop_id (shop_id)                                       -- 인덱스: 장소별 조회
);

CREATE TABLE SHOP_PHOTO_CATEGORY
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                    -- 사진 카테고리 ID (PK)
    shop_id   BIGINT       NOT NULL,                                -- 장소 ID (SHOP.id 참조)
    name       VARCHAR(100) NOT NULL,                                -- 카테고리 이름
    created_at DATETIME     NOT NULL,                                -- 생성 일시
    updated_at DATETIME     NOT NULL,                                -- 수정 일시
    INDEX idx_shop_photo_category_shop_id (shop_id)               -- 인덱스: 장소별 조회
);

CREATE TABLE SHOP_PHOTO_CATEGORY_IMAGE
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,                             -- 사진 카테고리 이미지 ID (PK)
    shop_photo_category_id BIGINT   NOT NULL,                                             -- 사진 카테고리 ID (SHOP_PHOTO_CATEGORY.id 참조)
    image_file_id           BIGINT   NOT NULL,                                             -- 이미지 파일 ID (UPLOADED_FILE.id 참조)
    sort                    INT      NOT NULL,                                             -- 정렬 순서
    is_visible              TINYINT(1) NOT NULL DEFAULT 1,                                 -- 노출 여부 (1: 노출, 0: 숨김)
    created_at              DATETIME NOT NULL,                                             -- 생성 일시
    updated_at              DATETIME NOT NULL,                                             -- 수정 일시
    INDEX idx_shop_photo_category_image_category_id (shop_photo_category_id)             -- 인덱스: 사진 카테고리별 조회
);

CREATE TABLE SHOP_BANNER_IMAGE
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 가게 배너 이미지 ID (PK)
    shop_id       BIGINT   NOT NULL,                             -- 장소 ID (SHOP.id 참조)
    image_file_id  BIGINT   NOT NULL,                             -- 이미지 파일 ID (UPLOADED_FILE.id 참조)
    sort           INT,                                           -- 정렬 순서
    INDEX idx_shop_banner_image_shop_id (shop_id)              -- 인덱스: 장소별 조회
);

CREATE TABLE SHOP_TEMPORARY_CLOSURE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,     -- 임시 휴무 ID (PK)
    shop_id    BIGINT   NOT NULL,                     -- 장소 ID (SHOP.id 참조)
    start_date DATE     NOT NULL,                     -- 임시 휴무 시작일
    end_date   DATE     NOT NULL,                     -- 임시 휴무 종료일
    created_at DATETIME NOT NULL,                     -- 생성 일시
    updated_at DATETIME NOT NULL,                     -- 수정 일시
    INDEX idx_shop_temporary_closure_shop_id (shop_id)
);

CREATE TABLE SHOP_PHONE_NUMBER
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 전화번호 ID (PK)
    shop_id      BIGINT      NOT NULL,                -- 장소 ID (SHOP.id 참조)
    phone_number VARCHAR(20) NOT NULL,                -- 전화번호
    is_primary   TINYINT(1)  NOT NULL DEFAULT 0,      -- 대표 여부 (1: 대표)
    is_virtual   TINYINT(1)  NOT NULL DEFAULT 0,      -- 가상번호(안심번호) 여부
    created_at   DATETIME    NOT NULL,                -- 생성 일시
    updated_at   DATETIME    NOT NULL,                -- 수정 일시
    INDEX idx_shop_phone_number_shop_id (shop_id)
);

CREATE TABLE SHOP_CONVENIENCE_INFO
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY, -- 편의정보 ID (PK)
    shop_id              BIGINT       NOT NULL,             -- 장소 ID (SHOP.id 참조)
    is_parking_available TINYINT(1)   NOT NULL DEFAULT 0,   -- 주차 가능 여부
    is_parking_paid      TINYINT(1)   NOT NULL DEFAULT 0,   -- 주차 유료 여부
    is_valet_available   TINYINT(1)   NOT NULL DEFAULT 0,   -- 발렛 가능 여부
    is_valet_paid        TINYINT(1)   NOT NULL DEFAULT 0,   -- 발렛 유료 여부
    directions_guide     VARCHAR(200),                      -- 찾아오는 길 안내 (최대 200자)
    display_latitude     DECIMAL(9, 6),                     -- 노출 위치 위도 (실위치 반경 1km 이내)
    display_longitude    DECIMAL(9, 6),                     -- 노출 위치 경도
    created_at           DATETIME     NOT NULL,             -- 생성 일시
    updated_at           DATETIME     NOT NULL,             -- 수정 일시
    UNIQUE KEY uk_shop_convenience_info_shop_id (shop_id)
);

CREATE TABLE SHOP_IMAGE_CHANGE_REQUEST
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,    -- 요청 ID (PK)
    shop_id       BIGINT       NOT NULL,                -- 장소 ID (SHOP.id 참조)
    image_type    VARCHAR(20)  NOT NULL,               -- 이미지 유형 (TRADEMARK, THUMBNAIL)
    image_file_id BIGINT       NOT NULL,                -- 요청된 신규 이미지 파일 ID (UPLOADED_FILE.id 참조)
    status        VARCHAR(20)  NOT NULL,               -- 승인 상태 (PENDING, APPROVED, REJECTED, CANCELED)
    reject_reason VARCHAR(500),                         -- 반려 사유
    created_at    DATETIME     NOT NULL,                -- 생성 일시
    updated_at    DATETIME     NOT NULL,                -- 수정 일시
    INDEX idx_shop_image_change_request_shop_id_status (shop_id, status),
    INDEX idx_shop_image_change_request_status_image_type (status, image_type)
);

CREATE TABLE SHOP_SUSPENSION
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,     -- 임시중지 ID (PK)
    shop_id      BIGINT      NOT NULL,                  -- 장소 ID (SHOP.id 참조)
    reason       VARCHAR(30) NOT NULL,                  -- 사유 (EARLY_CLOSE, OPEN_DELAY, SHOP_CIRCUMSTANCE, UNREACHABLE, TERMINATION_REQUEST, BAD_WEATHER)
    order_method VARCHAR(20),                           -- 대상 주문유형 (TABLE, RESERVATION, DELIVERY, TAKEOUT / NULL이면 전체)
    start_at     DATETIME    NOT NULL,                  -- 임시중지 시작 시각
    end_at       DATETIME    NOT NULL,                  -- 임시중지 종료 시각
    released_at  DATETIME,                              -- 해제 시각 (NULL이면 미해제)
    created_at   DATETIME    NOT NULL,                  -- 생성 일시
    updated_at   DATETIME    NOT NULL,                  -- 수정 일시
    INDEX idx_shop_suspension_shop_id (shop_id)
);

CREATE TABLE SHOP_CONTENT_BOARD
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,    -- 콘텐츠보드 ID (PK)
    shop_id       BIGINT       NOT NULL,                -- 장소 ID (SHOP.id 참조)
    content_type  VARCHAR(10)  NOT NULL,               -- 콘텐츠 형태 (IMAGE, GIF, VIDEO)
    topic         VARCHAR(20)  NOT NULL,               -- 주제 (EXTERIOR, INTERIOR, FOOD_STORY, NEWS)
    image_file_id BIGINT,                               -- 이미지/GIF 파일 ID (IMAGE/GIF일 때, UPLOADED_FILE.id 참조)
    youtube_url   VARCHAR(500),                         -- 유튜브 링크 (VIDEO일 때)
    description   VARCHAR(50),                          -- 설명글 (최대 50자)
    is_hidden     TINYINT(1)   NOT NULL DEFAULT 0,      -- 관리자 숨김 여부
    created_at    DATETIME     NOT NULL,                -- 생성 일시
    updated_at    DATETIME     NOT NULL,                -- 수정 일시
    INDEX idx_shop_content_board_shop_id (shop_id)
);

CREATE TABLE SHOP_NOTICE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,        -- 점주 공지 ID (PK)
    shop_id    BIGINT     NOT NULL,                      -- 장소 ID (SHOP.id 참조)
    content    TEXT       NOT NULL,                      -- 공지 본문 (최대 2000자, 애플리케이션 검증)
    is_exposed TINYINT(1) NOT NULL DEFAULT 0,            -- 앱 노출 여부 (가게당 최대 1건, 도메인 서비스가 불변식 소유)
    is_hidden  TINYINT(1) NOT NULL DEFAULT 0,            -- 관리자 게시중단 여부
    created_at DATETIME   NOT NULL,                      -- 생성 일시
    updated_at DATETIME   NOT NULL,                      -- 수정 일시
    INDEX idx_shop_notice_shop_id (shop_id),
    INDEX idx_shop_notice_exposed (shop_id, is_exposed, is_hidden)
);

CREATE TABLE SHOP_NOTICE_IMAGE
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,    -- 공지 이미지 ID (PK)
    shop_notice_id BIGINT   NOT NULL,                    -- 점주 공지 ID (SHOP_NOTICE.id 참조)
    image_file_id  BIGINT   NOT NULL,                    -- 이미지 파일 ID (UPLOADED_FILE.id 참조)
    sort_order     INT      NOT NULL,                    -- 노출 순서 (0부터 시작)
    created_at     DATETIME NOT NULL,                    -- 생성 일시
    updated_at     DATETIME NOT NULL,                    -- 수정 일시
    INDEX idx_shop_notice_image_notice_id (shop_notice_id)
);

CREATE TABLE SHOP_HYGIENE_BADGE
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY, -- 위생 뱃지 ID (PK)
    shop_id               BIGINT      NOT NULL,              -- 장소 ID (SHOP.id 참조)
    badge_type            VARCHAR(30) NOT NULL,             -- 인증 유형 (FOOD_SAFETY_CERTIFIED, CESCO_BLUE, CESCO_WHITE)
    certified_date        DATE        NOT NULL,              -- 인증일
    last_inspection_month VARCHAR(7),                        -- 세스코 최근 점검월 (예: 2026-03)
    created_at            DATETIME    NOT NULL,              -- 생성 일시
    updated_at            DATETIME    NOT NULL,              -- 수정 일시
    INDEX idx_shop_hygiene_badge_shop_id (shop_id)
);

CREATE TABLE PROHIBITED_WORD
(
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 금칙어 ID (PK)
    word   VARCHAR(100) NOT NULL,                         -- 금칙어
    reason VARCHAR(200)                                   -- 등록 불가 사유 분류
);

CREATE TABLE STATION
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY, -- 지하철역 ID (PK)
    station_name VARCHAR(255) NOT NULL              -- 역 이름
);

CREATE TABLE REVIEW
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY, -- 리뷰 ID (PK)
    shop_id          BIGINT     NOT NULL,               -- 장소 ID (SHOP.id 참조)
    product_id        BIGINT     NOT NULL,               -- 상품 ID (PRODUCT.id 참조)
    member_id         BIGINT     NOT NULL,               -- 작성자 회원 ID (MEMBER.id 참조)
    order_id          BIGINT,                            -- 주문 ID (ORDERS.id 참조, NULL 시 비인증 리뷰)
    content           TEXT       NOT NULL,               -- 리뷰 내용
    total_rating      DOUBLE     NOT NULL,               -- 종합 평점
    taste_rating      DOUBLE,                            -- 맛 평점
    amount_rating     DOUBLE,                            -- 양 평점
    price_rating      DOUBLE,                            -- 가격 평점
    atmosphere_rating DOUBLE,                            -- 분위기 평점
    kindness_rating   DOUBLE,                            -- 친절 평점
    hygiene_rating    DOUBLE,                            -- 위생 평점
    will_revisit      TINYINT(1) NOT NULL DEFAULT 0,     -- 재방문 의향 (1: 있음)
    is_hidden         TINYINT(1) NOT NULL DEFAULT 0,     -- 숨김 여부 (1: 숨김)
    is_owner_only     TINYINT(1) NOT NULL DEFAULT 0,     -- 사장님만보기 여부 (1: 비공개, 작성자 본인·점주·관리자만 열람)
    delivery_rating   INT,                               -- 배달 평점 (1~5, 배달 주문에만. NULL이면 미평가). 점주 전용 노출 — 고객 앱 미노출
    delivery_comment  VARCHAR(500),                      -- 배달 평가 내용 (점주 전용, 고객 앱 미노출)
    created_at        DATETIME   NOT NULL,               -- 생성 일시
    updated_at        DATETIME   NOT NULL,               -- 수정 일시
    INDEX idx_review_product_id (product_id),            -- 인덱스: 상품별 조회
    INDEX idx_review_order_id (order_id),                -- 인덱스: 주문별 조회
    INDEX idx_review_shop_id_created_at (shop_id, created_at) -- 인덱스: 가게별 최신순 조회·기간 통계 (점주 리뷰 관리)
);

CREATE TABLE REVIEW_COMMENT
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,              -- 리뷰 댓글 ID (PK)
    review_id  BIGINT     NOT NULL,                            -- 리뷰 ID (REVIEW.id 참조)
    member_id  BIGINT     NOT NULL,                            -- 작성자 회원 ID (MEMBER.id 참조)
    content    TEXT       NOT NULL,                            -- 댓글 내용
    is_hidden  TINYINT(1) NOT NULL DEFAULT 0,                  -- 숨김 여부 (1: 숨김)
    created_at DATETIME   NOT NULL,                            -- 생성 일시
    updated_at DATETIME   NOT NULL,                            -- 수정 일시
    INDEX idx_review_comment_review_id (review_id),            -- 인덱스: 리뷰별 조회
    INDEX idx_review_comment_member_id (member_id)             -- 인덱스: 회원별 조회
);

CREATE TABLE REVIEW_IMAGE
(
    id               BIGINT   AUTO_INCREMENT PRIMARY KEY, -- 리뷰 이미지 ID (PK)
    review_id        BIGINT   NOT NULL,                   -- 리뷰 ID (REVIEW.id 참조)
    image_file_id    BIGINT   NOT NULL,                   -- 이미지 파일 ID (UPLOADED_FILE.id 참조)
    sort             INT      NOT NULL,                   -- 정렬 순서
    created_at       DATETIME NOT NULL,                   -- 생성 일시
    updated_at       DATETIME NOT NULL,                   -- 수정 일시
    INDEX idx_review_image_review_id (review_id)          -- 인덱스: 리뷰별 조회
);

CREATE TABLE REVIEW_LIKE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 리뷰 좋아요 ID (PK)
    review_id  BIGINT   NOT NULL,                             -- 리뷰 ID (REVIEW.id 참조)
    member_id  BIGINT   NOT NULL,                             -- 회원 ID (MEMBER.id 참조)
    created_at DATETIME NOT NULL,                             -- 생성 일시
    updated_at DATETIME NOT NULL,                             -- 수정 일시
    UNIQUE KEY uk_review_like (review_id, member_id),         -- 유니크: 리뷰·회원 중복 방지
    INDEX idx_review_like_review_id (review_id),              -- 인덱스: 리뷰별 조회
    INDEX idx_review_like_member_id (member_id)               -- 인덱스: 회원별 조회
);

CREATE TABLE REVIEW_PRODUCT
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 리뷰 상품 ID (PK)
    review_id  BIGINT NOT NULL,                                    -- 리뷰 ID (REVIEW.id 참조)
    product_id BIGINT NOT NULL,                                    -- 상품 ID (PRODUCT.id 참조)
    INDEX idx_review_product_review_id (review_id),                -- 인덱스: 리뷰별 조회
    INDEX idx_review_product_product_id (product_id),              -- 인덱스: 상품별 조회
    UNIQUE KEY uk_review_product (review_id, product_id)           -- 유니크: 리뷰·상품 중복 방지
);

CREATE TABLE REVIEW_REPLY
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 리뷰 대댓글 ID (PK)
    comment_id         BIGINT     NOT NULL,                       -- 댓글 ID (REVIEW_COMMENT.id 참조)
    member_id          BIGINT     NOT NULL,                       -- 작성자 회원 ID (MEMBER.id 참조)
    reply_to_member_id BIGINT,                                    -- 답글 대상 회원 ID (MEMBER.id 참조)
    content            TEXT       NOT NULL,                       -- 대댓글 내용
    is_hidden          TINYINT(1) NOT NULL DEFAULT 0,             -- 숨김 여부 (1: 숨김)
    created_at         DATETIME   NOT NULL,                       -- 생성 일시
    updated_at         DATETIME   NOT NULL,                       -- 수정 일시
    INDEX idx_review_reply_comment_id (comment_id),               -- 인덱스: 댓글별 조회
    INDEX idx_review_reply_member_id (member_id)                  -- 인덱스: 회원별 조회
);

CREATE TABLE REVIEW_TAG
(
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,       -- 리뷰 태그 ID (PK)
    review_id BIGINT NOT NULL,                         -- 리뷰 ID (REVIEW.id 참조)
    tag_id    BIGINT NOT NULL,                         -- 태그 ID (TAG.id 참조)
    INDEX idx_review_tag_review_id (review_id),        -- 인덱스: 리뷰별 조회
    INDEX idx_review_tag_tag_id (tag_id)               -- 인덱스: 태그별 조회
);

-- 사장님 답변 — 리뷰당 1건, 점주 전용.
-- REVIEW_COMMENT 는 회원이 다는 댓글이고 member_id 만 있어 점주를 표현할 수 없으므로,
-- 기존 회원 댓글/답글 구조를 건드리지 않고 별도 테이블로 둡니다(web/admin 회귀 차단).
CREATE TABLE REVIEW_OWNER_REPLY
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,          -- 사장님 답변 ID (PK)
    review_id  BIGINT   NOT NULL,                          -- 리뷰 ID (REVIEW.id 참조)
    shop_id    BIGINT   NOT NULL,                          -- 가게 ID (SHOP.id 참조)
    ceo_id     BIGINT   NOT NULL,                          -- 작성 점주 ID (CEO.id 참조)
    content    TEXT     NOT NULL,                          -- 답변 내용 (최대 1000자, 애플리케이션 검증)
    created_at DATETIME NOT NULL,                          -- 생성 일시
    updated_at DATETIME NOT NULL,                          -- 수정 일시
    UNIQUE KEY uk_review_owner_reply_review_id (review_id), -- 유니크: 리뷰당 답변 1건 제약의 물리적 보증
    INDEX idx_review_owner_reply_shop_id (shop_id)          -- 인덱스: 가게별 조회(미답변 탭 판정 join)
);

-- 리뷰 게시중단 요청 — 점주 요청 → 관리자 심사 → 승인 시 REVIEW.is_hidden 반영.
-- PENDING 중복은 UNIQUE 로 막을 수 없습니다(MySQL 부분 인덱스 미지원, 취소 후 재요청 허용 필요)
-- → 애플리케이션의 existsByReviewIdAndStatus 가 차단합니다.
CREATE TABLE REVIEW_BLIND_REQUEST
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 게시중단 요청 ID (PK)
    review_id     BIGINT       NOT NULL,               -- 리뷰 ID (REVIEW.id 참조)
    shop_id       BIGINT       NOT NULL,               -- 가게 ID (SHOP.id 참조)
    ceo_id        BIGINT       NOT NULL,               -- 요청 점주 ID (CEO.id 참조)
    reason        VARCHAR(20)  NOT NULL,               -- 요청 사유 (ADVERTISEMENT, PROFANITY, IRRELEVANT, PRIVACY, ETC)
    detail_reason VARCHAR(500),                        -- 상세 사유 (reason=ETC 면 필수 / 그 외 선택)
    status        VARCHAR(20)  NOT NULL,               -- 처리 상태 (PENDING, APPROVED, REJECTED, CANCELED, EXPIRED, DELETED)
    reject_reason VARCHAR(500),                        -- 반려 사유 (REJECTED 일 때만 / 취소 시 비움)
    blind_until   DATETIME,                            -- 재노출 예정일시 (승인 시각 + 30일. APPROVED 일 때만 값)
    created_at    DATETIME     NOT NULL,               -- 생성 일시 (= 요청 접수 시각)
    updated_at    DATETIME     NOT NULL,               -- 수정 일시
    INDEX idx_review_blind_request_review_id (review_id),                    -- 인덱스: 리뷰별 이력 조회
    INDEX idx_review_blind_request_shop_id_created_at (shop_id, created_at), -- 인덱스: 가게별 최신순
    INDEX idx_review_blind_request_status (status),                          -- 인덱스: 관리자 심사 대기 큐
    INDEX idx_review_blind_request_blind_until (blind_until)                 -- 인덱스: 만료 재노출 배치
);

-- 게시중단 요청 증빙 서류 — 신분증·위임장·사업자등록증 등을 첨부합니다.
-- REVIEW_IMAGE 와 동형(불변, 감사 시각 없음)입니다.
-- 개수 제한(3개)을 스키마가 아니라 애플리케이션(Bean Validation)에 두는 이유는 개수가 정책이기 때문입니다.
CREATE TABLE REVIEW_BLIND_REQUEST_ATTACHMENT
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY, -- 첨부 ID (PK)
    blind_request_id   BIGINT NOT NULL,                   -- 게시중단 요청 ID (REVIEW_BLIND_REQUEST.id 참조)
    attachment_file_id BIGINT NOT NULL,                   -- 첨부 파일 ID (UPLOADED_FILE.id 참조)
    sort               INT    NOT NULL,                   -- 정렬 순서
    INDEX idx_review_blind_request_attachment_request_id (blind_request_id) -- 인덱스: 요청별 조회
);

-- 메뉴 평가 — REVIEW 와 독립된 애그리거트입니다(부모-자식 관계가 아닙니다).
-- review_id FK 를 두지 않는 이유: "매장 평가와 메뉴 평가 중 어느 것을 먼저 하든, 하나만 하든 성립해야 한다"는
-- 요구가 review_id 를 두는 순간 구조적으로 불가능해집니다. REVIEW 와의 유일한 연결고리는 order_id 입니다.
-- 댓글·대댓글·좋아요·사장님답변·사장님만보기가 없는 것은 의도적입니다 — 소셜 기능은 REVIEW 에만 둡니다.
-- UNIQUE(order_product_id): 주문 항목당 평가 1건. member_id 를 키에 넣지 않는 이유는 ORDER_PRODUCT 가
-- 이미 한 주문(=한 회원)에 귀속되어 작성자가 확정되기 때문입니다(넣으면 남의 주문 항목에 평가를 덧붙이는 구멍).
-- shop_id/product_id/order_id 는 order_product_id 에서 역조회 가능하지만, 상품 평점 재집계·가게 단위 조회·
-- REVIEW 조인이 매번 ORDER_PRODUCT 조인을 타지 않도록 주문 시점 확정값을 스냅샷합니다.
CREATE TABLE MENU_REVIEW
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,           -- 메뉴 평가 ID (PK)
    member_id        BIGINT       NOT NULL,                       -- 작성 회원 ID (MEMBER.id 참조)
    shop_id          BIGINT       NOT NULL,                       -- 가게 ID (SHOP.id 참조, 비정규화)
    product_id       BIGINT       NOT NULL,                       -- 평가 대상 상품 ID (PRODUCT.id 참조)
    order_id         BIGINT       NOT NULL,                       -- 주문 ID (ORDERS.id 참조, REVIEW 와의 연결고리)
    order_product_id BIGINT       NOT NULL,                       -- 주문 상품 ID (ORDER_PRODUCT.id 참조, 작성 근거)
    rating           INT          NOT NULL,                       -- 메뉴 평점 (1~5)
    comment          VARCHAR(300),                                -- 짧은 코멘트 (선택, NULL 허용)
    hidden           TINYINT(1)   NOT NULL DEFAULT 0,             -- 관리자 게시중단 여부 (1: 숨김)
    created_at       DATETIME     NOT NULL,                       -- 생성 일시
    updated_at       DATETIME     NOT NULL,                       -- 수정 일시
    UNIQUE KEY uk_menu_review_order_product (order_product_id),   -- 유니크: 주문항목당 평가 1건
    INDEX idx_menu_review_product_id (product_id),                -- 인덱스: 상품 평점 재집계·상품 상세 목록
    INDEX idx_menu_review_member_created (member_id, created_at), -- 인덱스: 랭킹·등급 기간 집계
    INDEX idx_menu_review_shop_id (shop_id)                       -- 인덱스: 가게 단위 조회
);

-- 가게 리뷰 노출 정렬 설정 — 고객 앱 리뷰 탭의 기본 정렬을 점주가 정합니다.
-- 설정 행이 없으면 LATEST 로 간주하므로 기존 가게 백필이 필요 없습니다.
-- SHOP 에 컬럼을 추가하지 않는 이유: 리뷰 표시 설정이 늘어날 여지가 있고 SHOP 은 이미 컬럼이 19개입니다.
CREATE TABLE SHOP_REVIEW_DISPLAY_SETTING
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 리뷰 노출 설정 ID (PK)
    shop_id    BIGINT      NOT NULL,                               -- 가게 ID (SHOP.id 참조)
    sort_type  VARCHAR(20) NOT NULL,                               -- 앱 노출 기본 정렬 (RECOMMENDED, LATEST, OLDEST)
    created_at DATETIME    NOT NULL,                               -- 생성 일시
    updated_at DATETIME    NOT NULL,                               -- 수정 일시
    UNIQUE KEY uk_shop_review_display_setting_shop_id (shop_id)    -- 유니크: 가게당 설정 1행
);

CREATE TABLE TAG
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY, -- 태그 ID (PK)
    tag_name VARCHAR(255) NOT NULL              -- 태그 이름
);

CREATE TABLE ORDERS
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 주문 ID (PK)
    member_id               BIGINT       NOT NULL,               -- 회원 ID (MEMBER.id 참조)
    shop_id                BIGINT       NOT NULL,               -- 장소 ID (SHOP.id 참조)
    order_number            VARCHAR(50)  NOT NULL UNIQUE,        -- 주문 번호
    order_method            VARCHAR(50)  NOT NULL,               -- 주문 방법 (TABLE, RESERVATION, DELIVERY, TAKEOUT)
    order_status            VARCHAR(20)  NOT NULL,               -- 주문 상태 (PENDING, CONFIRMED, CANCELLED 등)
    orderer_name            VARCHAR(100) NOT NULL,               -- 주문자 이름
    orderer_phone           VARCHAR(20)  NOT NULL,               -- 주문자 연락처
    orderer_email           VARCHAR(100),                        -- 주문자 이메일
    total_product_amount    INT          NOT NULL DEFAULT 0,     -- 상품 금액 합계
    product_discount_amount INT          NOT NULL DEFAULT 0,     -- 상품 할인 금액
    coupon_discount_amount  INT          NOT NULL DEFAULT 0,     -- 쿠폰 할인 금액
    point_discount_amount   INT          NOT NULL DEFAULT 0,     -- 포인트 할인 금액
    total_discount_amount   INT          NOT NULL DEFAULT 0,     -- 총 할인 금액
    delivery_tip_amount     INT          NOT NULL DEFAULT 0,     -- 배달팁 (final_amount 가산 항목 — 보증금과 함께 둘뿐)
    cup_deposit_amount      INT          NOT NULL DEFAULT 0,     -- 자원순환보증금 합계. 비과세·점주 매출 아님·중개이용료 대상 아님·최소주문금액 산정 제외. final_amount에만 가산
    final_amount            INT          NOT NULL DEFAULT 0,     -- 최종 결제 금액 (= 상품 금액 - 총 할인 + 배달팁 + 보증금)
    delivery_road_address   VARCHAR(500),                        -- 주문 시점 배달 도로명 주소 (스냅샷)
    delivery_lot_address    VARCHAR(500),                        -- 주문 시점 배달 지번 주소 (스냅샷)
    delivery_detail_address VARCHAR(200),                        -- 주문 시점 상세 주소 (스냅샷)
    delivery_admin_dong_id  BIGINT,                              -- 주문 시점 행정동 ID (스냅샷)
    delivery_latitude       DECIMAL(9, 6),                       -- 주문 시점 배달지 위도 (스냅샷)
    delivery_longitude      DECIMAL(9, 6),                       -- 주문 시점 배달지 경도 (스냅샷)
    delivery_distance_meters INT,                                -- 주문 시점 직선거리(m) — 배달팁 산출 근거
    scheduled_at            DATETIME,                            -- 수령 예약 시각(슬롯 시작). NULL이면 즉시 주문
    scheduled_slot_end_at   DATETIME,                            -- 수령 예약 슬롯 종료 시각(배달 30분 범위). 포장은 scheduled_at과 동일
    member_coupon_id        BIGINT,                              -- 사용한 회원 쿠폰 ID (MEMBER_COUPON.id 참조)
    used_point              INT          NOT NULL DEFAULT 0,     -- 사용 포인트
    earned_point            INT          NOT NULL DEFAULT 0,     -- 적립 포인트
    created_at              DATETIME     NOT NULL,               -- 생성 일시
    updated_at              DATETIME     NOT NULL,               -- 수정 일시
    is_deleted              BOOLEAN      NOT NULL DEFAULT FALSE,  -- 삭제 여부 (Soft Delete)
    INDEX idx_orders_member_id (member_id),                      -- 인덱스: 회원별 조회
    INDEX idx_orders_shop_id (shop_id),                        -- 인덱스: 장소별 조회
    INDEX idx_orders_order_status (order_status),                -- 인덱스: 주문 상태별 조회
    INDEX idx_orders_created_at (created_at),                    -- 인덱스: 생성 일시별 조회
    INDEX idx_orders_scheduled_at (scheduled_at)                 -- 인덱스: 수령 예약 시각별 조회
);

CREATE TABLE ORDER_PRODUCT
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 주문 상품 ID (PK)
    order_id          BIGINT       NOT NULL,                     -- 주문 ID (ORDERS.id 참조)
    product_id        BIGINT       NOT NULL,                     -- 상품 ID (PRODUCT.id 참조)
    name              VARCHAR(255) NOT NULL,                     -- 주문 시점 상품명 (스냅샷)
    image_file_id     BIGINT,                                    -- 주문 시점 상품 이미지 파일 ID (UPLOADED_FILE.id 스냅샷)
    quantity          INT          NOT NULL DEFAULT 1,           -- 수량
    original_price    INT          NOT NULL DEFAULT 0,           -- 정가
    discount_price    INT,                                       -- 할인가
    total_option_price INT         NOT NULL DEFAULT 0,           -- 옵션 금액 합계 (보증금은 포함되지 않음)
    cup_deposit_amount INT         NOT NULL DEFAULT 0,           -- 이 라인의 보증금 합계(수량 반영). total_option_price·total_price 에는 포함되지 않음
    total_price       INT          NOT NULL DEFAULT 0,           -- 상품 총 금액
    created_at        DATETIME     NOT NULL,                     -- 생성 일시
    updated_at        DATETIME     NOT NULL,                     -- 수정 일시
    INDEX idx_order_product_order_id (order_id),                 -- 인덱스: 주문별 조회
    INDEX idx_order_product_product_id (product_id),             -- 인덱스: 상품별 조회
    INDEX idx_order_product_image_file_id (image_file_id)        -- 인덱스: 이미지 파일 조인
);

CREATE TABLE ORDER_PRODUCT_OPTION
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 주문 상품 옵션 ID (PK)
    order_product_id  BIGINT       NOT NULL,                              -- 주문 상품 ID (ORDER_PRODUCT.id 참조)
    option_group_id   BIGINT,                                             -- 옵션 그룹 ID (스냅샷, NULL 가능)
    option_group_name VARCHAR(100) NOT NULL,                              -- 주문 시점 옵션 그룹 이름 (스냅샷)
    option_id         BIGINT,                                             -- 옵션 ID (스냅샷, NULL 가능)
    option_name       VARCHAR(100) NOT NULL,                              -- 주문 시점 옵션 이름 (스냅샷)
    additional_price  INT          NOT NULL DEFAULT 0,                    -- 옵션 추가 금액 (보증금은 별도 컬럼)
    option_group_type VARCHAR(20)  NOT NULL DEFAULT 'NORMAL',             -- 주문 시점 옵션그룹 유형 스냅샷 (NORMAL, CUP_DEPOSIT)
    cup_count         INT,                                                -- 주문 시점 일회용컵 제공 개수 스냅샷 (환급 단위)
    deposit_amount    INT          NOT NULL DEFAULT 0,                    -- 주문 시점 보증금 금액 스냅샷(= cup_count * 당시 요율). additional_price 와 별개 항목
    created_at        DATETIME     NOT NULL,                              -- 생성 일시
    updated_at        DATETIME     NOT NULL,                              -- 수정 일시
    INDEX idx_order_product_option_order_product_id (order_product_id)    -- 인덱스: 주문 상품별 조회
);

CREATE TABLE PAYMENT
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 결제 ID (PK)
    order_id             BIGINT      NOT NULL UNIQUE,            -- 주문 ID (ORDERS.id 참조)
    payment_method       VARCHAR(30) NOT NULL,                   -- 결제 수단 (CARD, VIRTUAL_ACCOUNT 등)
    payment_status       VARCHAR(20) NOT NULL,                   -- 결제 상태 (PENDING, DONE, CANCELLED 등)
    amount               INT         NOT NULL DEFAULT 0,         -- 결제 금액
    pg_provider          VARCHAR(30),                            -- PG사 (TOSS 등)
    pg_tid               VARCHAR(100),                           -- PG사 거래 ID
    pg_order_id          VARCHAR(100),                           -- PG사 주문 ID
    card_company         VARCHAR(50),                            -- 카드사
    card_number          VARCHAR(30),                            -- 카드 번호 (마스킹)
    installment_months   INT,                                    -- 할부 개월 수 (0: 일시불)
    approved_at          DATETIME,                               -- 결제 승인 일시
    cancelled_at         DATETIME,                               -- 결제 취소 일시
    cancel_reason        VARCHAR(500),                           -- 취소 사유
    receipt_url          VARCHAR(500),                           -- 영수증 URL
    cash_receipt_number  VARCHAR(50),                            -- 현금영수증 번호
    cash_receipt_type    VARCHAR(20),                            -- 현금영수증 유형 (소득공제, 지출증빙)
    created_at           DATETIME    NOT NULL,                   -- 생성 일시
    updated_at           DATETIME    NOT NULL,                   -- 수정 일시
    INDEX idx_payment_order_id (order_id),                       -- 인덱스: 주문별 조회
    INDEX idx_payment_payment_status (payment_status),           -- 인덱스: 결제 상태별 조회
    INDEX idx_payment_pg_tid (pg_tid),                           -- 인덱스: PG사 거래 ID 조회
    INDEX idx_payment_created_at (created_at)                    -- 인덱스: 생성 일시별 조회
);

CREATE TABLE PAYMENT_REFUND
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 환불 ID (PK)
    payment_id    BIGINT      NOT NULL,                               -- 결제 ID (PAYMENT.id 참조)
    refund_amount INT         NOT NULL DEFAULT 0,                     -- 환불 금액
    refund_reason VARCHAR(500),                                       -- 환불 사유
    refund_status VARCHAR(20) NOT NULL,                               -- 환불 상태 (PENDING, COMPLETED, FAILED 등)
    pg_refund_id  VARCHAR(100),                                       -- PG사 환불 ID
    refunded_at   DATETIME,                                           -- 환불 완료 일시
    created_at    DATETIME    NOT NULL,                               -- 생성 일시
    updated_at    DATETIME    NOT NULL,                               -- 수정 일시
    INDEX idx_payment_refund_payment_id (payment_id),                 -- 인덱스: 결제별 조회
    INDEX idx_payment_refund_refund_status (refund_status)            -- 인덱스: 환불 상태별 조회
);

CREATE TABLE UPLOADED_FILE
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 업로드 파일 ID (PK)
    original_filename VARCHAR(500)  NOT NULL,             -- 원본 파일명
    stored_filename   VARCHAR(500)  NOT NULL,             -- 저장 파일명 (UUID 등)
    file_path         VARCHAR(1000) NOT NULL,             -- 파일 저장 경로
    file_size         BIGINT        NOT NULL,             -- 파일 크기 (bytes)
    content_type      VARCHAR(100)  NOT NULL,             -- MIME 타입
    created_at        DATETIME      NOT NULL,             -- 생성 일시
    updated_at        DATETIME      NOT NULL,             -- 수정 일시
    INDEX idx_uploaded_file_created_at (created_at)       -- 인덱스: 생성 일시별 조회
);

CREATE TABLE FAQ_CATEGORY
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,              -- FAQ 카테고리 ID (PK)
    name         VARCHAR(100) NOT NULL,                          -- 카테고리 이름
    sort         INT          NOT NULL,                          -- 정렬 순서
    is_visible   TINYINT(1)   NOT NULL DEFAULT 1,                -- 노출 여부 (1: 노출)
    is_deleted   TINYINT(1)   NOT NULL DEFAULT 0,                -- 삭제 여부 (1: 삭제됨, Soft Delete)
    created_at   DATETIME     NOT NULL,                          -- 생성 일시
    updated_at   DATETIME     NOT NULL,                          -- 수정 일시
    INDEX idx_faq_category_active (is_deleted, is_visible, sort) -- 인덱스: 삭제·노출·정렬 복합 조회
);

CREATE TABLE FAQ
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,                    -- FAQ ID (PK)
    faq_category_id BIGINT        NOT NULL,                               -- FAQ 카테고리 ID (FAQ_CATEGORY.id 참조)
    question        VARCHAR(500)  NOT NULL,                               -- 질문
    answer          TEXT          NOT NULL,                               -- 답변
    sort            INT           NOT NULL,                               -- 정렬 순서
    is_visible      TINYINT(1)    NOT NULL DEFAULT 1,                     -- 노출 여부 (1: 노출)
    is_deleted      TINYINT(1)    NOT NULL DEFAULT 0,                     -- 삭제 여부 (1: 삭제됨, Soft Delete)
    created_at      DATETIME      NOT NULL,                               -- 생성 일시
    updated_at      DATETIME      NOT NULL,                               -- 수정 일시
    INDEX idx_faq_category_id (faq_category_id),                          -- 인덱스: 카테고리별 조회
    INDEX idx_faq_active (faq_category_id, is_deleted, is_visible, sort)  -- 인덱스: 카테고리·삭제·노출·정렬 복합 조회
);

CREATE TABLE TOSS_PAYMENT_RECORD
(
    id                                  BIGINT AUTO_INCREMENT PRIMARY KEY,   -- Toss 결제 기록 ID (PK)
    payment_id                          BIGINT       NOT NULL,               -- 결제 ID (PAYMENT.id 참조)
    version                             VARCHAR(20),                         -- Toss API 응답 버전
    payment_key                         VARCHAR(200),                        -- Toss 결제 키 (결제 고유 식별자)
    type                                VARCHAR(20),                         -- 결제 타입 (NORMAL, BILLING, BRANDPAY 등)
    order_id                            VARCHAR(64),                         -- 주문 번호
    order_name                          VARCHAR(100),                        -- 주문명
    m_id                                VARCHAR(14),                         -- 상점 아이디 (MID)
    currency                            VARCHAR(10),                         -- 결제 통화
    method                              VARCHAR(30),                         -- 결제 수단 (카드, 가상계좌 등)
    total_amount                        INT,                                 -- 총 결제 금액
    balance_amount                      INT,                                 -- 취소 가능 금액 (잔고)
    status                              VARCHAR(30),                         -- 결제 처리 상태
    requested_at                        DATETIME,                            -- 결제 요청 일시
    approved_at                         DATETIME,                            -- 결제 승인 일시
    use_escrow                          TINYINT(1),                          -- 에스크로 사용 여부
    last_transaction_key                VARCHAR(64),                         -- 마지막 거래 키
    supplied_amount                     INT,                                 -- 공급가액
    vat                                 INT,                                 -- 부가세
    culture_expense                     TINYINT(1),                          -- 문화비 지출 여부
    tax_free_amount                     INT,                                 -- 면세 금액
    tax_exemption_amount                INT,                                 -- 과세 제외 금액
    is_partial_cancelable               TINYINT(1),                          -- 부분 취소 가능 여부
    card_amount                         INT,                                 -- 카드사 청구 금액
    card_issuer_code                    VARCHAR(10),                         -- 카드 발급사 코드
    card_acquirer_code                  VARCHAR(10),                         -- 카드 매입사 코드
    card_number                         VARCHAR(20),                         -- 카드 번호 (마스킹)
    card_installment_plan_months        INT,                                 -- 할부 개월 수 (0: 일시불)
    card_approve_no                     VARCHAR(8),                          -- 카드 승인 번호
    card_use_card_point                 TINYINT(1),                          -- 카드 포인트 사용 여부
    card_type                           VARCHAR(20),                         -- 카드 종류 (신용, 체크, 기프트 등)
    card_owner_type                     VARCHAR(20),                         -- 카드 소유자 유형 (개인, 법인 등)
    card_acquire_status                 VARCHAR(30),                         -- 카드 매입 상태
    card_is_interest_free               TINYINT(1),                          -- 무이자 할부 여부
    card_interest_payer                 VARCHAR(20),                         -- 할부 수수료 부담 주체
    virtual_account_type                VARCHAR(20),                         -- 가상계좌 유형 (일반, 고정)
    virtual_account_number              VARCHAR(20),                         -- 가상계좌 번호
    virtual_account_bank_code           VARCHAR(10),                         -- 가상계좌 은행 코드
    virtual_account_customer_name       VARCHAR(100),                        -- 가상계좌 발급 구매자명
    virtual_account_depositor_name      VARCHAR(100),                        -- 가상계좌 입금자명
    virtual_account_due_date            DATETIME,                            -- 가상계좌 입금 기한
    virtual_account_refund_status       VARCHAR(30),                         -- 가상계좌 환불 상태
    virtual_account_expired             TINYINT(1),                          -- 가상계좌 만료 여부
    virtual_account_settlement_status   VARCHAR(30),                         -- 가상계좌 정산 상태
    mobile_phone_customer_mobile_phone  VARCHAR(15),                         -- 휴대폰 결제 구매자 번호
    mobile_phone_settlement_status      VARCHAR(30),                         -- 휴대폰 결제 정산 상태
    mobile_phone_receipt_url            VARCHAR(500),                        -- 휴대폰 결제 영수증 URL
    gift_certificate_approve_no         VARCHAR(8),                          -- 상품권 결제 승인 번호
    gift_certificate_settlement_status  VARCHAR(30),                         -- 상품권 정산 상태
    transfer_bank_code                  VARCHAR(10),                         -- 계좌이체 은행 코드
    transfer_settlement_status          VARCHAR(30),                         -- 계좌이체 정산 상태
    receipt_url                         VARCHAR(500),                        -- 영수증 URL
    checkout_url                        VARCHAR(500),                        -- 결제창 URL
    easy_pay_provider                   VARCHAR(30),                         -- 간편결제사 코드
    easy_pay_amount                     INT,                                 -- 간편결제 결제 금액
    easy_pay_discount_amount            INT,                                 -- 간편결제 즉시 할인 금액
    country                             VARCHAR(2),                          -- 결제 국가 코드 (ISO-3166)
    failure_code                        VARCHAR(50),                         -- 결제 실패 에러 코드
    failure_message                     VARCHAR(510),                        -- 결제 실패 에러 메시지
    cash_receipt_type                   VARCHAR(20),                         -- 현금영수증 유형 (소득공제, 지출증빙)
    cash_receipt_key                    VARCHAR(200),                        -- 현금영수증 키
    cash_receipt_issue_number           VARCHAR(9),                          -- 현금영수증 발급 번호
    cash_receipt_url                    VARCHAR(500),                        -- 현금영수증 확인 URL
    cash_receipt_amount                 INT,                                 -- 현금영수증 처리 금액
    cash_receipt_tax_free_amount        INT,                                 -- 현금영수증 면세 처리 금액
    discount_amount                     INT,                                 -- 즉시 할인 금액
    created_at                          DATETIME     NOT NULL,               -- 생성 일시
    updated_at                          DATETIME     NOT NULL,               -- 수정 일시
    INDEX idx_toss_payment_record_payment_id (payment_id),                   -- 인덱스: 결제별 조회
    INDEX idx_toss_payment_record_payment_key (payment_key),                 -- 인덱스: 결제 키별 조회
    INDEX idx_toss_payment_record_order_id (order_id),                       -- 인덱스: 주문 ID별 조회
    INDEX idx_toss_payment_record_status (status)                            -- 인덱스: 결제 상태별 조회
);

CREATE TABLE MEMBER_REFERRAL
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,                   -- 추천인 이력 ID (PK)
    referrer_id BIGINT      NOT NULL,                                -- 추천인 회원 ID (MEMBER.id 참조)
    referee_id  BIGINT      NOT NULL,                                -- 피추천인 회원 ID (MEMBER.id 참조)
    status      VARCHAR(20) NOT NULL DEFAULT 'PENDING',             -- 추천 상태 (PENDING, COMPLETED 등)
    created_at  DATETIME    NOT NULL,                                -- 생성 일시
    updated_at  DATETIME    NOT NULL,                                -- 수정 일시
    UNIQUE KEY uq_member_referral_referee_id (referee_id),           -- 유니크: 피추천인 중복 방지 (1인 1회)
    INDEX idx_member_referral_referrer_id (referrer_id),             -- 인덱스: 추천인별 조회
    INDEX idx_member_referral_status (status)                        -- 인덱스: 상태별 조회
);

CREATE TABLE MEMBER_WITHDRAWAL
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 회원 탈퇴 ID (PK)
    member_id     BIGINT       NOT NULL,                         -- 탈퇴 회원 ID (MEMBER.id 참조)
    reason        VARCHAR(50)  NOT NULL,                         -- 탈퇴 사유 코드
    reason_detail VARCHAR(500),                                  -- 탈퇴 사유 상세
    created_at    DATETIME     NOT NULL,                         -- 생성 일시
    updated_at    DATETIME     NOT NULL,                         -- 수정 일시
    INDEX idx_member_withdrawal_member_id (member_id)            -- 인덱스: 회원별 조회
);

CREATE TABLE SMS_VERIFICATION
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 휴대폰 인증 ID (PK)
    phone_number      VARCHAR(11)  NOT NULL,                              -- 인증 휴대폰 번호
    verification_code VARCHAR(6)   NOT NULL,                              -- 인증 코드 (6자리)
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',            -- 인증 상태 (PENDING, VERIFIED, EXPIRED 등)
    expires_at        DATETIME     NOT NULL,                              -- 만료 일시
    verified_at       DATETIME,                                           -- 인증 완료 일시
    created_at        DATETIME     NOT NULL,                              -- 생성 일시
    INDEX idx_sms_verification_phone_number (phone_number),               -- 인덱스: 휴대폰 번호별 조회
    INDEX idx_sms_verification_expires_at (expires_at)                    -- 인덱스: 만료 일시별 조회
);

CREATE TABLE MAIL_VERIFICATION
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 이메일 인증 ID (PK)
    email             VARCHAR(100) NOT NULL,                            -- 인증 이메일 주소
    verification_code VARCHAR(6)   NOT NULL,                            -- 인증 코드 (6자리)
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',          -- 인증 상태 (PENDING, VERIFIED, EXPIRED 등)
    expires_at        DATETIME     NOT NULL,                            -- 만료 일시
    verified_at       DATETIME,                                         -- 인증 완료 일시
    created_at        DATETIME     NOT NULL,                            -- 생성 일시
    INDEX idx_mail_verification_email (email),                          -- 인덱스: 이메일별 조회
    INDEX idx_mail_verification_expires_at (expires_at)                 -- 인덱스: 만료 일시별 조회
);

CREATE TABLE RANK_PERIOD
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,     -- 랭킹 기간 ID (PK)
    start_at   DATETIME   NOT NULL,                   -- 랭킹 시작 일시
    end_at     DATETIME   NOT NULL,                   -- 랭킹 종료 일시
    is_visible TINYINT(1) NOT NULL DEFAULT 1,         -- 노출 여부 (1: 노출)
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,         -- 삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)
    created_at DATETIME   NOT NULL,                   -- 생성 일시
    updated_at DATETIME   NOT NULL,                   -- 수정 일시
    INDEX idx_rank_period_active (is_visible),        -- 인덱스: 노출 여부별 조회
    INDEX idx_rank_period_range (start_at, end_at)    -- 인덱스: 기간별 조회
);

CREATE TABLE RANK_PRIZE
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 랭킹 경품 ID (PK)
    rank_id       BIGINT       NOT NULL,                            -- 랭킹 기간 ID (RANK_PERIOD.id 참조)
    prize_rank    INT          NOT NULL,                            -- 수상 순위
    name          VARCHAR(200) NOT NULL,                            -- 경품 이름
    brand         VARCHAR(100) NOT NULL,                            -- 경품 브랜드
    image_file_id BIGINT,                                           -- 경품 이미지 파일 ID (UPLOADED_FILE.id 참조)
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,                  -- 삭제 여부 (1: 삭제, 0: 미삭제, Soft Delete)
    created_at    DATETIME     NOT NULL,                            -- 생성 일시
    updated_at    DATETIME     NOT NULL,                            -- 수정 일시
    UNIQUE KEY uk_rank_prize_rank (rank_id, prize_rank),            -- 유니크: 랭킹·순위 중복 방지
    INDEX idx_rank_prize (rank_id, prize_rank)                      -- 인덱스: 랭킹·순위 복합 조회
);

CREATE TABLE SEARCH_KEYWORD_LOG (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,     -- 검색 키워드 로그 ID (PK)
    keyword     VARCHAR(100) NOT NULL,                 -- 검색 키워드
    searched_at DATETIME     NOT NULL,                 -- 검색 일시
    INDEX idx_keyword_searched_at (keyword, searched_at) -- 인덱스: 키워드·검색 일시 복합 조회
);

CREATE TABLE POPULAR_KEYWORD (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY, -- 인기 검색어 ID (PK)
    keyword    VARCHAR(100) NOT NULL,             -- 검색어
    `rank`     INT          NOT NULL,             -- 순위
    is_new     BOOLEAN      NOT NULL DEFAULT FALSE, -- 신규 진입 여부 (true: 신규)
    is_visible BOOLEAN      NOT NULL DEFAULT TRUE,  -- 노출 여부 (true: 노출)
    created_at DATETIME     NOT NULL,             -- 생성 일시
    updated_at DATETIME     NOT NULL,             -- 수정 일시
    INDEX idx_is_visible_rank (is_visible, `rank`)  -- 인덱스: 노출·순위 복합 조회
);

CREATE TABLE RECOMMENDED_KEYWORD (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 추천 검색어 ID (PK)
    keyword    VARCHAR(100) NOT NULL,              -- 검색어
    sort_order INT          NOT NULL DEFAULT 0,    -- 정렬 순서
    is_visible BOOLEAN      NOT NULL DEFAULT TRUE, -- 노출 여부 (true: 노출)
    created_at DATETIME     NOT NULL,              -- 생성 일시
    updated_at DATETIME     NOT NULL               -- 수정 일시
);

CREATE TABLE RESERVATION_SLOT
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,               -- 슬롯 ID (PK)
    shop_id        BIGINT   NOT NULL,                               -- 장소 ID (SHOP.id 참조)
    slot_date      DATE     NOT NULL,                               -- 슬롯 날짜
    slot_time      TIME     NOT NULL,                               -- 슬롯 시간 (30분 단위)
    capacity       INT      NOT NULL,                               -- 슬롯당 정원 (팀 수)
    reserved_count INT      NOT NULL DEFAULT 0,                     -- 현재 점유 팀 수
    version        BIGINT,                                          -- 낙관적 락 버전 (@Version)
    created_at     DATETIME NOT NULL,                               -- 생성 일시
    updated_at     DATETIME NOT NULL,                               -- 수정 일시
    UNIQUE KEY uk_reservation_slot (shop_id, slot_date, slot_time) -- 유니크: 가게·날짜·시간 슬롯 중복 방지
);

CREATE TABLE RESERVATION
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,                            -- 예약 ID (PK)
    member_id        BIGINT      NOT NULL,                                         -- 예약자 회원 ID (MEMBER.id 참조)
    shop_id          BIGINT      NOT NULL,                                         -- 장소 ID (SHOP.id 참조)
    reservation_date DATE        NOT NULL,                                         -- 예약 날짜
    reservation_time TIME        NOT NULL,                                         -- 예약 시간
    party_size       INT         NOT NULL,                                         -- 방문 인원수
    status           VARCHAR(20) NOT NULL,                                         -- 예약 상태 (PENDING, CONFIRMED, REJECTED, CANCELED, COMPLETED)
    request          TEXT,                                                         -- 요청사항
    created_at       DATETIME    NOT NULL,                                         -- 생성 일시
    updated_at       DATETIME    NOT NULL,                                         -- 수정 일시
    INDEX idx_reservation_shop_slot (shop_id, reservation_date, reservation_time), -- 인덱스: 가게·날짜·시간 복합 조회
    INDEX idx_reservation_member (member_id)                                       -- 인덱스: 회원별 조회
);

CREATE TABLE ADMIN_DONG
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,       -- 행정동 ID (PK)
    code                   VARCHAR(10)   NOT NULL,                  -- 행정동 코드(10자리)
    sido_name              VARCHAR(50)   NOT NULL,                  -- 시/도
    sigungu_name           VARCHAR(50)   NOT NULL,                  -- 시/군/구
    dong_name              VARCHAR(50)   NOT NULL,                  -- 행정동
    is_active              TINYINT(1)    NOT NULL DEFAULT 1,        -- 사용 여부 (폐지 동은 DELETE 하지 않고 0으로 둔다 — 다른 테이블이 id로 참조 중)
    center_latitude        DECIMAL(9, 6) NULL,                      -- 행정동 대표점 위도(경계 내부 보장점, centroid 아님)
    center_longitude       DECIMAL(9, 6) NULL,                      -- 행정동 대표점 경도
    boundary_min_latitude  DECIMAL(9, 6) NULL,                      -- 경계 바운딩박스 최소 위도
    boundary_max_latitude  DECIMAL(9, 6) NULL,                      -- 경계 바운딩박스 최대 위도
    boundary_min_longitude DECIMAL(9, 6) NULL,                      -- 경계 바운딩박스 최소 경도
    boundary_max_longitude DECIMAL(9, 6) NULL,                      -- 경계 바운딩박스 최대 경도
    boundary               LONGTEXT      NULL,                      -- 행정동 경계 폴리곤(링 ";" 구분, 점 "," 구분, "경도 위도")
    UNIQUE KEY uk_admin_dong_code (code),                           -- 유니크: 행정동 코드
    INDEX idx_admin_dong_name (sido_name, sigungu_name, dong_name), -- 인덱스: 주소 문자열 매칭
    INDEX idx_admin_dong_center (center_latitude, center_longitude) -- 인덱스: 좌표 바운딩박스 프리필터(배달지역 환산)
);

CREATE TABLE SHOP_DELIVERY_AREA
(
    id            BIGINT   AUTO_INCREMENT PRIMARY KEY,           -- 배달가능지역 ID (PK)
    shop_id       BIGINT   NOT NULL,                             -- 장소 ID (SHOP.id 참조)
    admin_dong_id BIGINT   NOT NULL,                             -- 행정동 ID (ADMIN_DONG.id 참조)
    source        VARCHAR(20) NOT NULL DEFAULT 'MANUAL',         -- 등록 출처 (MANUAL: 행정동 직접 선택·반경 일괄, POLYGON: 지도 도형 환산)
    created_at    DATETIME NOT NULL,                             -- 생성 일시
    updated_at    DATETIME NOT NULL,                             -- 수정 일시
    INDEX idx_shop_delivery_area_shop_id (shop_id),              -- 인덱스: 가게별 조회
    UNIQUE KEY uk_shop_delivery_area (shop_id, admin_dong_id)    -- 유니크: 가게·행정동 중복 방지
);

-- 배달지역 도형(가게당 1건). 편집·표현의 원본이며 주문 배달가능 판정에는 직접 참여하지 않는다
-- (판정의 유일한 소스는 이 도형을 환산해 얻은 SHOP_DELIVERY_AREA의 행정동 집합이다).
-- 좌표를 GEOMETRY가 아니라 텍스트로 담는 이유는 GeoPolygonTextCodec Javadoc 참고.
CREATE TABLE SHOP_DELIVERY_AREA_POLYGON
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,          -- 배달지역 도형 ID (PK)
    shop_id           BIGINT        NOT NULL,                     -- 장소 ID (SHOP.id 참조)
    rings             LONGTEXT      NOT NULL,                     -- 도형(링 ";" 구분, 점 "," 구분, "경도 위도")
    center_latitude   DECIMAL(9, 6) NOT NULL,                     -- 저장 시점 가게 위도 스냅샷(7km 상한 기준점)
    center_longitude  DECIMAL(9, 6) NOT NULL,                     -- 저장 시점 가게 경도 스냅샷
    max_radius_meters INT           NOT NULL,                     -- 기준점~최원거리 정점 거리(m), 7000 이하
    ring_count        INT           NOT NULL,                     -- 링 개수(표시·검증용 비정규화)
    vertex_count      INT           NOT NULL,                     -- 총 정점 수(표시·검증용 비정규화)
    created_at        DATETIME      NOT NULL,                     -- 생성 일시
    updated_at        DATETIME      NOT NULL,                     -- 수정 일시
    UNIQUE KEY uk_shop_delivery_area_polygon_shop_id (shop_id)    -- 유니크: 가게당 1건
);

CREATE TABLE SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST
(
    id                          BIGINT        AUTO_INCREMENT PRIMARY KEY, -- 조정 신청 ID (PK)
    shop_id                     BIGINT        NOT NULL,                   -- 신청 가게 ID (SHOP.id 참조)
    counterpart_shop_name       VARCHAR(255)  NOT NULL,                   -- 상대 가맹점 상호명
    counterpart_business_number VARCHAR(12)   NOT NULL,                   -- 상대 가맹점 사업자등록번호(하이픈 제외 10자리)
    franchise_name              VARCHAR(255)  NOT NULL,                   -- 가맹본부명
    reason                      VARCHAR(1000) NOT NULL,                   -- 배달지역 중첩 사유
    consent_file_id             BIGINT        NOT NULL,                   -- 정보제공 동의서 파일 ID (UPLOADED_FILE.id 참조)
    status                      VARCHAR(20)   NOT NULL,                   -- 처리 상태 (PENDING, IN_PROGRESS, COMPLETED, REJECTED, CANCELED)
    reject_reason               VARCHAR(500),                             -- 반려 사유 (REJECTED일 때만)
    created_at                  DATETIME      NOT NULL,                   -- 생성 일시
    updated_at                  DATETIME      NOT NULL,                   -- 수정 일시
    INDEX idx_shop_delivery_area_adjustment_shop_id_status (shop_id, status), -- 인덱스: 가게별 이력·중복 신청 검사
    INDEX idx_shop_delivery_area_adjustment_status (status)                   -- 인덱스: 검수 목록 상태 필터
);

CREATE TABLE PUBLIC_HOLIDAY
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 공휴일 ID (PK)
    holiday_date  DATE        NOT NULL,                          -- 공휴일 날짜
    name          VARCHAR(50) NOT NULL,                          -- 공휴일 명칭
    is_substitute TINYINT(1)  NOT NULL DEFAULT 0,                -- 대체공휴일 여부
    UNIQUE KEY uk_public_holiday_date (holiday_date)             -- 유니크: 날짜당 1건
);

CREATE TABLE MEMBER_DELIVERY_ADDRESS
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,            -- 배달 주소 ID (PK)
    member_id      BIGINT        NOT NULL,                       -- 회원 ID (MEMBER.id 참조)
    alias          VARCHAR(50),                                  -- 주소 별칭 (집/회사 등)
    road_address   VARCHAR(500)  NOT NULL,                       -- 도로명 주소
    lot_address    VARCHAR(500),                                 -- 지번 주소
    detail_address VARCHAR(200),                                 -- 상세 주소
    admin_dong_id  BIGINT,                                       -- 행정동 ID (ADMIN_DONG.id 참조, 매칭 실패 시 NULL)
    latitude       DECIMAL(9, 6) NOT NULL,                       -- 위도 (SHOP과 동일 정밀도)
    longitude      DECIMAL(9, 6) NOT NULL,                       -- 경도
    is_default     TINYINT(1)    NOT NULL DEFAULT 0,             -- 기본 배송지 여부
    created_at     DATETIME      NOT NULL,                       -- 생성 일시
    updated_at     DATETIME      NOT NULL,                       -- 수정 일시
    INDEX idx_member_delivery_address_member_id (member_id)      -- 인덱스: 회원별 조회
);

CREATE TABLE SHOP_DELIVERY_TIP_SETTING
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 배달팁 설정 ID (PK)
    shop_id              BIGINT      NOT NULL,                   -- 장소 ID (SHOP.id 참조)
    extra_tip_type       VARCHAR(20) NOT NULL DEFAULT 'NONE',    -- 추가 배달팁 방식 (NONE, DISTANCE, REGION)
    base_distance_meters INT,                                    -- 기본배달거리(m). 1000/1500/2000/2500/3000, DISTANCE일 때만
    surcharge_unit       VARCHAR(20),                            -- 할증 단위 (PER_100M, PER_500M), DISTANCE일 때만
    surcharge_amount     INT,                                    -- 단위당 할증액(원). PER_100M:100~300, PER_500M:100~1500
    created_at           DATETIME    NOT NULL,                   -- 생성 일시
    updated_at           DATETIME    NOT NULL,                   -- 수정 일시
    UNIQUE KEY uk_shop_delivery_tip_setting_shop_id (shop_id)    -- 유니크: 가게당 1건
);

CREATE TABLE SHOP_DELIVERY_TIP_TIER
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,          -- 구간 ID (PK)
    shop_id          BIGINT   NOT NULL,                          -- 장소 ID (SHOP.id 참조)
    tier_order       INT      NOT NULL,                          -- 구간 순서 (0=기본, 1~2=추가)
    min_order_amount INT      NOT NULL,                          -- 구간 하한 주문금액 (상품 할인 후 기준)
    tip_amount       INT      NOT NULL,                          -- 배달팁 (0 이상 5,000 미만)
    created_at       DATETIME NOT NULL,                          -- 생성 일시
    updated_at       DATETIME NOT NULL,                          -- 수정 일시
    INDEX idx_shop_delivery_tip_tier_shop_id (shop_id),          -- 인덱스: 가게별 조회
    UNIQUE KEY uk_shop_delivery_tip_tier (shop_id, tier_order)   -- 유니크: 가게·순서 중복 방지
);

CREATE TABLE SHOP_DELIVERY_TIP_REGION
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 지역별 배달팁 ID (PK)
    shop_id       BIGINT   NOT NULL,                             -- 장소 ID (SHOP.id 참조)
    admin_dong_id BIGINT   NOT NULL,                             -- 행정동 ID (ADMIN_DONG.id 참조)
    tip_amount    INT      NOT NULL,                             -- 추가 배달팁 (0~10,000)
    created_at    DATETIME NOT NULL,                             -- 생성 일시
    updated_at    DATETIME NOT NULL,                             -- 수정 일시
    INDEX idx_shop_delivery_tip_region_shop_id (shop_id),        -- 인덱스: 가게별 조회
    UNIQUE KEY uk_shop_delivery_tip_region (shop_id, admin_dong_id) -- 유니크: 가게·행정동 중복 방지
);

CREATE TABLE SHOP_DELIVERY_TIP_SCHEDULE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 시간별 배달팁 ID (PK)
    shop_id    BIGINT      NOT NULL,                             -- 장소 ID (SHOP.id 참조)
    day_type   VARCHAR(20) NOT NULL,                             -- 요일 구분 (DAILY, WEEKDAY, WEEKEND, MONDAY~SUNDAY / HOLIDAY 사용 금지)
    start_time TIME        NOT NULL,                             -- 시작 시각
    end_time   TIME        NOT NULL,                             -- 종료 시각 (시작보다 이르면 자정 넘김)
    tip_amount INT         NOT NULL,                             -- 추가 배달팁 (0~10,000)
    created_at DATETIME    NOT NULL,                             -- 생성 일시
    updated_at DATETIME    NOT NULL,                             -- 수정 일시
    INDEX idx_shop_delivery_tip_schedule_shop_id (shop_id)       -- 인덱스: 가게별 조회
);

CREATE TABLE SHOP_DELIVERY_TIP_HOLIDAY
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 공휴일 배달팁 ID (PK)
    shop_id    BIGINT   NOT NULL,                                -- 장소 ID (SHOP.id 참조)
    tip_amount INT      NOT NULL,                                -- 추가 배달팁 (0~10,000)
    created_at DATETIME NOT NULL,                                -- 생성 일시
    updated_at DATETIME NOT NULL,                                -- 수정 일시
    UNIQUE KEY uk_shop_delivery_tip_holiday_shop_id (shop_id)    -- 유니크: 가게당 1건
);

CREATE TABLE SHOP_RIDER_GUIDE
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,     -- 라이더 안내 ID (PK)
    shop_id               BIGINT         NOT NULL,               -- 장소 ID (SHOP.id 참조)
    visit_guide           VARCHAR(200),                          -- 라이더 가게방문 안내 문구 (최대 200자, 미등록 시 NULL)
    pickup_road_address   VARCHAR(255),                          -- 픽업 도로명주소 (미설정 시 NULL → 가게 실주소로 폴백)
    pickup_lot_address    VARCHAR(255),                          -- 픽업 지번주소
    pickup_detail_address VARCHAR(100),                          -- 픽업 상세주소 (동/호수 등)
    pickup_latitude       DECIMAL(11, 8),                        -- 픽업 위도 (-90 ~ 90)
    pickup_longitude      DECIMAL(11, 8),                        -- 픽업 경도 (-180 ~ 180)
    created_at            DATETIME       NOT NULL,               -- 생성 일시
    updated_at            DATETIME       NOT NULL,               -- 수정 일시
    UNIQUE KEY uk_shop_rider_guide_shop_id (shop_id)             -- 유니크: 가게당 1건
);

CREATE TABLE SHOP_RIDER_GUIDE_HISTORY
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 이력 ID (PK)
    shop_id              BIGINT      NOT NULL,                   -- 장소 ID (SHOP.id 참조)
    actor_type           VARCHAR(20) NOT NULL,                   -- 변경 주체 (CEO, ADMIN)
    actor_id             BIGINT      NOT NULL,                   -- 변경 주체 ID (CEO.id 또는 ADMIN.id 참조)
    action_type          VARCHAR(20) NOT NULL,                   -- 조치 유형 (UPDATE, REVISION_REQUEST, DELETION)
    previous_visit_guide VARCHAR(200),                           -- 변경 전 문구
    new_visit_guide      VARCHAR(200),                           -- 변경 후 문구 (삭제 조치 시 NULL)
    reason               VARCHAR(200),                           -- 관리자 조치 사유 (점주 변경 시 NULL)
    created_at           DATETIME    NOT NULL,                   -- 생성 일시
    updated_at           DATETIME    NOT NULL,                   -- 수정 일시
    INDEX idx_shop_rider_guide_history_shop_id (shop_id)         -- 인덱스: 가게별 이력 조회
);

-- 가게 변경이력 (append-only)
-- 점주가 가게 설정을 변경할 때마다 1행씩 쌓이며, 기록 후에는 수정되지 않습니다.
-- 1행의 단위는 "점주가 저장 버튼을 1번 누른 것"(mutation 엔드포인트 1회 호출)이며,
-- 배달팁 구간처럼 컬렉션을 통째로 교체하는 변경도 1행으로 기록하고
-- previous_value / new_value 에 컬렉션 전체 스냅샷을 줄바꿈으로 결합해 담습니다.
-- SHOP_RIDER_GUIDE_HISTORY 는 관리자 사후 검수 전용 로그라 별도로 존치하며,
-- 점주 변경(actor_type=CEO, action_type=UPDATE)만 이 테이블에 추가 기록합니다.
CREATE TABLE SHOP_CHANGE_HISTORY
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,             -- 이력 ID (PK)
    shop_id        BIGINT      NOT NULL,                          -- 가게 ID (SHOP.id 참조)
    category       VARCHAR(40) NOT NULL,                          -- 대분류 (OPERATION, DELIVERY, SHOP_INFO, IMAGE, RIDER)
    change_type    VARCHAR(40) NOT NULL,                          -- 중분류 (BUSINESS_HOUR, DELIVERY_TIP_TIER 등 29종)
    action_type    VARCHAR(20) NOT NULL,                          -- 조치 유형 (CREATE, UPDATE, DELETE)
    actor_type     VARCHAR(20) NOT NULL,                          -- 변경 주체 (CEO, ADMIN)
    actor_id       BIGINT      NOT NULL,                          -- 변경 주체 ID (CEO.id 또는 ADMIN.id 참조)
    previous_value TEXT,                                          -- 변경 전 요약 (등록 시 NULL)
    new_value      TEXT,                                          -- 변경 후 요약 (삭제 시 NULL)
    created_at     DATETIME    NOT NULL,                          -- 생성 일시 (= 변경 발생 시각)
    updated_at     DATETIME    NOT NULL,                          -- 수정 일시 (append-only 라 created_at 과 동일)
    -- 대분류 미선택(전체) 조회 경로: shop_id 등치 + created_at 레인지 + 정렬까지 커버
    INDEX idx_shop_change_history_shop_created (shop_id, created_at),
    -- 대분류 선택 조회 경로: 등치 2개 뒤에 레인지가 와야 레인지가 인덱스에 살아남음
    INDEX idx_shop_change_history_shop_category_created (shop_id, category, created_at)
);

-- 요청처리 현황 인덱스 — 점주가 낸 신청(상표·대표이미지 변경, 배달지역 조정)의 통합 목록.
-- 이 테이블은 파생 읽기모델이고 진실원은 유형별 원본 테이블입니다.
-- 원본 상태 전이와 같은 트랜잭션에서 ShopRequestIndexRecorder 가 동기화하며,
-- 상세 조회는 여기서 request_type / source_request_id 만 얻어 원본을 투영합니다.
-- 이 행의 id 가 요청의 유일한 대외 식별자여서, 유형별 FK 없는 범용 문의 스레드가
-- 이 식별자 위에서만 성립합니다(아래 SHOP_REQUEST_COMMENT).
CREATE TABLE SHOP_REQUEST_INDEX
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 요청 인덱스 ID (PK, 요청의 대외 식별자)
    shop_id             BIGINT       NOT NULL,              -- 가게 ID (SHOP.id 참조)
    request_type        VARCHAR(40)  NOT NULL,              -- 요청 유형 (TRADEMARK_CHANGE, THUMBNAIL_CHANGE, DELIVERY_AREA_ADJUSTMENT, REVIEW_BLIND)
    source_request_id   BIGINT       NOT NULL,              -- 원본 요청 행 ID (유형별 원본 테이블의 id 참조)
    summary             VARCHAR(255) NOT NULL,              -- 요청 내용 한 줄 요약 (목록의 "무엇을 요청했는지")
    status              VARCHAR(20)  NOT NULL,              -- 통합 상태 (PENDING, IN_PROGRESS, APPROVED, REJECTED, CANCELED)
    reject_reason       VARCHAR(500),                       -- 반려 사유 (REJECTED일 때만)
    attachment_file_id  BIGINT,                             -- 첨부 파일 ID (UPLOADED_FILE.id 참조)
    requested_by_ceo_id BIGINT,                             -- 요청 점주 ID (CEO.id 참조 / 백필분은 NULL — 원본에 요청자 기록이 없음)
    processed_at        DATETIME,                           -- 최근 상태 전이 시각 (접수 직후 NULL / 백필분은 updated_at 근사치)
    created_at          DATETIME     NOT NULL,              -- 생성 일시 (= 요청 접수 시각)
    updated_at          DATETIME     NOT NULL,              -- 수정 일시
    UNIQUE KEY uk_shop_request_index_type_source (request_type, source_request_id), -- 유니크: 원본 1건당 인덱스 1행(동기화 멱등성 보증)
    INDEX idx_shop_request_index_shop_id_created_at (shop_id, created_at),          -- 인덱스: 가게별 최신순 목록(기본 정렬)
    INDEX idx_shop_request_index_shop_id_status (shop_id, status)                   -- 인덱스: 가게별 상태 필터
);

-- 요청건 문의 스레드 — 점주 문의와 담당자 답변이 오가는 대화(append-only, 수정·삭제 없음).
-- 조회 정렬만 이 저장소에서 유일하게 작성순(ASC)입니다 — 대화이므로 오간 순서대로 읽혀야 합니다.
-- 처리 상태 제약이 없어 반려·취소·승인 이후에도 작성할 수 있습니다(반려 사유 문의가 주요 사용례).
CREATE TABLE SHOP_REQUEST_COMMENT
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY, -- 요청 댓글 ID (PK)
    shop_request_index_id BIGINT      NOT NULL,              -- 요청 인덱스 ID (SHOP_REQUEST_INDEX.id 참조)
    author_type           VARCHAR(20) NOT NULL,              -- 작성자 유형 (CEO, ADMIN)
    author_id             BIGINT      NOT NULL,              -- 작성자 ID (CEO.id 또는 ADMIN.id 참조)
    content               TEXT        NOT NULL,              -- 댓글 내용
    created_at            DATETIME    NOT NULL,              -- 생성 일시
    updated_at            DATETIME    NOT NULL,              -- 수정 일시 (append-only 라 created_at 과 동일)
    INDEX idx_shop_request_comment_request_id (shop_request_index_id, id) -- 인덱스: 스레드 조회(작성순) + 목록의 건수 집계
);


-- ----------------------------------------------------------------------------
-- 점주 로그인 이력 (append-only)
--
-- 개인정보처리시스템 접속기록. 점주가 로그인하면 회원의 개인정보(주문자 이름/연락처/주소 등)를
-- 열람할 수 있으므로, 로그인 시점이 곧 개인정보 접속 시점이다. 로그인 1회 = 1행이며 기록 후에는
-- 수정되지 않는다.
--
-- 존재하지 않는 아이디로의 로그인 시도는 기록하지 않는다 — 귀속할 점주가 없어 어떤 점주의
-- 접속기록에도 속하지 않고, 임의 문자열이 쌓이면 계정 존재 여부를 탐색하는 표면이 된다.
--
-- 조회 화면은 최근 90일로 제한하지만 90일이 지난 행도 삭제하지 않고 보관한다(고객센터 요청 시
-- 최대 2년 조회 지원).
-- ----------------------------------------------------------------------------
CREATE TABLE CEO_LOGIN_HISTORY
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 이력 ID (PK)
    ceo_id         BIGINT       NOT NULL,                  -- 점주 ID (CEO.id 참조)
    result         VARCHAR(20)  NOT NULL,                  -- 결과 (SUCCESS, FAILURE)
    failure_reason VARCHAR(20),                            -- 실패 사유 (BAD_CREDENTIALS, ACCOUNT_INACTIVE / 성공 시 NULL)
    ip_address     VARCHAR(45),                            -- 접속 IP (IPv6 최대 45자, 판별 불가 시 NULL)
    user_agent     VARCHAR(500),                           -- 접속 기기 정보 (500자 초과분은 절단해 저장)
    created_at     DATETIME     NOT NULL,                  -- 생성 일시 (= 로그인 시각)
    updated_at     DATETIME     NOT NULL,                  -- 수정 일시 (append-only 라 created_at 과 동일)
    -- 본인 이력 최신순 조회 경로: 등치(ceo_id) 뒤에 레인지(created_at)가 와야 레인지가 인덱스에 살아남음
    INDEX idx_ceo_login_history_ceo_created (ceo_id, created_at)
);


-- ----------------------------------------------------------------------------
-- 자주 쓰는 문구
--
-- 점주가 사장님 답변 작성 시 골라 넣을 문구를 미리 등록해 두는 테이블.
-- 원문 규격: 최대 1,000자 · 최대 5개 · 이름 미입력 시 내용 앞부분을 표시명으로 사용.
--
-- 귀속은 가게(shop)가 아니라 점주 계정(ceo)이다 — 한 점주가 여러 가게를 맡아도 문구를 공유한다.
--
-- content 는 TEXT 가 아니라 VARCHAR(1000) 이다 — 상한이 확정돼 있어 DB 가 직접 보증한다.
--
-- name 은 NULL 을 허용하며, 비었을 때 화면에 보이는 표시명(내용 앞부분)은 저장하지 않는다.
-- 파생값을 저장하면 내용을 수정할 때 어긋나므로 조회 시점에 CeoReplyPhraseQueryService 가 계산한다.
--
-- 주의: 5개 상한은 이 DDL 이 아니라 애플리케이션(CeoReplyPhraseService)이 강제한다.
--       MySQL 에 행 수 제약이 없어서이며, 동시 요청으로 6개가 될 수 있으나 표시용 목록이라 피해가 없다.
-- ----------------------------------------------------------------------------
CREATE TABLE CEO_REPLY_PHRASE
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 자주 쓰는 문구 ID (PK)
    ceo_id     BIGINT        NOT NULL,              -- 점주 ID (CEO.id 참조)
    name       VARCHAR(50),                         -- 문구 이름 (NULL 허용 — 비우면 내용 앞부분을 표시명으로 사용)
    content    VARCHAR(1000) NOT NULL,              -- 문구 내용 (최대 1000자)
    sort       INT           NOT NULL,              -- 정렬 순서
    created_at DATETIME      NOT NULL,              -- 생성 일시
    updated_at DATETIME      NOT NULL,              -- 수정 일시
    INDEX idx_ceo_reply_phrase_ceo_id (ceo_id)      -- 인덱스: 점주별 조회
);


-- ----------------------------------------------------------------------------
-- 가게-점주 배정 이력 (append-only)
--
-- 개인정보처리시스템 접근권한 부여/말소 기록. 점주 계정과 가게가 연결된 시점이 곧 권한 부여
-- 시점이고, 해제된 시점이 말소 시점이다. 관리자가 가게에 점주를 배정·해제할 때 1행씩 쌓인다.
--
-- 재배정(A -> B)은 REVOKE(A) + GRANT(B) 2행으로 남긴다. 한 행에 before/after를 담으면
-- "언제부터 언제까지 권한이 있었는가"를 읽을 수 없다.
--
-- actor_admin_id 만 두고 actor_type 을 두지 않는 이유: 배정/해제는 관리자만 할 수 있어
-- (점주에게는 권한 등급 개념 자체가 없다 — docs/domain/ceo.md) 항상 같은 값이 들어가는 컬럼이
-- 되기 때문이다.
--
-- 백필하지 않는다: 기존 SHOP.ceo_id 에 이미 배정된 가게들에 대해 GRANT 행을 소급 생성하지 않는다.
-- 실제 배정 시각을 알 수 없어(SHOP.created_at 은 가게 자체의 것이지 연결의 것이 아니다) 사실과
-- 다른 시각이 기록되기 때문이다.
-- ----------------------------------------------------------------------------
CREATE TABLE SHOP_CEO_ASSIGNMENT_HISTORY
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,      -- 이력 ID (PK)
    shop_id        BIGINT      NOT NULL,                   -- 가게 ID (SHOP.id 참조)
    ceo_id         BIGINT      NOT NULL,                   -- 대상 점주 ID (CEO.id 참조)
    action_type    VARCHAR(20) NOT NULL,                   -- 조치 유형 (GRANT, REVOKE)
    actor_admin_id BIGINT      NOT NULL,                   -- 조치한 관리자 ID (ADMIN.id 참조)
    created_at     DATETIME    NOT NULL,                   -- 생성 일시 (= 조치 시각)
    updated_at     DATETIME    NOT NULL,                   -- 수정 일시 (append-only 라 created_at 과 동일)
    -- 점주 본인 이력 조회 경로 (ceo-api)
    INDEX idx_shop_ceo_assignment_history_ceo_created (ceo_id, created_at),
    -- 가게 기준 조회 경로 (관리자 확인용)
    INDEX idx_shop_ceo_assignment_history_shop_created (shop_id, created_at)
);

-- ----------------------------------------------------------------------------
-- 인앱 알림함 (Phase 4)
--
-- 원문: "파트너님이 댓글을 달면, 고객에게 바로 '알림'이 갑니다."
-- 이번 범위는 인앱 알림함까지이며 FCM 푸시 발송은 포함하지 않는다.
--
-- MEMBER.push_notification_enabled 를 이 테이블에 적용하지 않는다: 그 플래그는 푸시 수신 동의이고,
-- 알림함은 사용자가 앱 안에서 직접 열어 보는 것이라 성격이 다르다. 추후 FCM 발송 경로에만 적용한다.
--
-- type / target_type 은 네이티브 ENUM 이 아니라 VARCHAR + 허용값 주석으로 둔다(전역 규칙).
-- 엔티티 쪽에는 @Enumerated(EnumType.STRING) 과 @Column(length = 30, columnDefinition = "VARCHAR(30)")
-- 을 반드시 병기해야 한다 — columnDefinition 을 빠뜨리면 MySQLDialect 가 네이티브 ENUM 을 기대해
-- ddl-auto=validate 가 "wrong column type ... expecting [enum (...)]" 로 부팅을 거부한다.
-- ----------------------------------------------------------------------------
CREATE TABLE NOTIFICATION
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 알림 ID (PK)
    member_id   BIGINT       NOT NULL,              -- 수신 회원 ID (MEMBER.id 참조)
    type        VARCHAR(30)  NOT NULL,              -- 알림 유형 (REVIEW_OWNER_REPLY)
    title       VARCHAR(100) NOT NULL,              -- 알림 제목
    body        VARCHAR(500) NOT NULL,              -- 알림 본문
    target_type VARCHAR(30),                        -- 이동 대상 유형 (REVIEW / 대상 없으면 NULL)
    target_id   BIGINT,                             -- 이동 대상 식별자 (대상 없으면 NULL)
    is_read     TINYINT(1)   NOT NULL DEFAULT 0,    -- 읽음 여부 (1: 읽음)
    read_at     DATETIME,                           -- 읽은 일시 (미읽음이면 NULL)
    created_at  DATETIME     NOT NULL,              -- 생성 일시
    updated_at  DATETIME     NOT NULL,              -- 수정 일시
    -- 회원별 최신순 목록 조회 경로
    INDEX idx_notification_member_created (member_id, created_at),
    -- 미읽음 배지 카운트 조회 경로
    INDEX idx_notification_member_unread (member_id, is_read)
);
