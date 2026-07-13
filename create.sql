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
    created_at         DATETIME     NOT NULL,                     -- 생성 일시
    updated_at         DATETIME     NOT NULL,                     -- 수정 일시
    INDEX idx_coupon_active (is_visible),                         -- 인덱스: 노출 여부별 조회
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
    created_at   DATETIME    NOT NULL,                               -- 생성 일시
    updated_at   DATETIME    NOT NULL,                               -- 수정 일시
    INDEX idx_event_winner_event_id (event_id),                      -- 인덱스: 이벤트별 조회
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
    status              VARCHAR(20)  NOT NULL,                  -- 이벤트 상태 (UPCOMING, ONGOING, ENDED 등)
    start_at            DATETIME     NOT NULL,                  -- 이벤트 시작 일시
    end_at              DATETIME     NOT NULL,                  -- 이벤트 종료 일시
    created_at          DATETIME     NOT NULL,                  -- 생성 일시
    updated_at          DATETIME     NOT NULL,                  -- 수정 일시
    INDEX idx_event_status (status),                            -- 인덱스: 상태별 조회
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

CREATE TABLE FOLLOW
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,                     -- 팔로우 ID (PK)
    follower_id  BIGINT   NOT NULL,                                     -- 팔로워 회원 ID (MEMBER.id 참조)
    following_id BIGINT   NOT NULL,                                     -- 팔로잉 회원 ID (MEMBER.id 참조)
    created_at   DATETIME NOT NULL,                                     -- 생성 일시
    updated_at   DATETIME NOT NULL,                                     -- 수정 일시
    UNIQUE KEY uk_follow_follower_following (follower_id, following_id), -- 유니크: 중복 팔로우 방지
    INDEX idx_follow_follower_id (follower_id),                          -- 인덱스: 팔로워별 조회
    INDEX idx_follow_following_id (following_id)                         -- 인덱스: 팔로잉별 조회
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
    is_visible          TINYINT(1)    NOT NULL DEFAULT 1,            -- 노출 여부 (1: 노출)
    sort                INT           NOT NULL,                      -- 정렬 순서
    created_at          DATETIME      NOT NULL,                      -- 생성 일시
    updated_at          DATETIME      NOT NULL,                      -- 수정 일시
    INDEX idx_product_shop_id (shop_id),                           -- 인덱스: 장소별 조회
    INDEX idx_product_category (shop_id, product_category_id),      -- 인덱스: 장소·카테고리 복합 조회
    INDEX idx_product_representative (shop_id, is_representative),  -- 인덱스: 장소·대표상품 조회
    INDEX idx_product_active (shop_id, is_visible, sort)            -- 인덱스: 장소·노출·정렬 복합 조회
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
    is_visible       TINYINT(1)   NOT NULL DEFAULT 1,                                -- 노출 여부 (1: 노출)
    created_at       DATETIME     NOT NULL,                                          -- 생성 일시
    updated_at       DATETIME     NOT NULL,                                          -- 수정 일시
    INDEX idx_product_common_option_group_id (option_group_id),                      -- 인덱스: 옵션 그룹별 조회
    INDEX idx_product_common_option_active (option_group_id, is_visible, sort)       -- 인덱스: 옵션 그룹·노출·정렬 복합 조회
);

CREATE TABLE PRODUCT_OPTION
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,                         -- 상품 옵션 ID (PK)
    option_group_id  BIGINT       NOT NULL,                                     -- 옵션 그룹 ID (PRODUCT_OPTION_GROUP.id 참조)
    name             VARCHAR(100) NOT NULL,                                     -- 옵션 이름
    additional_price INT          NOT NULL DEFAULT 0,                           -- 추가 금액
    sort             INT          NOT NULL,                                     -- 정렬 순서
    is_sold_out      TINYINT(1)   NOT NULL DEFAULT 0,                           -- 품절 여부 (1: 품절)
    is_visible       TINYINT(1)   NOT NULL DEFAULT 1,                           -- 노출 여부 (1: 노출)
    created_at       DATETIME     NOT NULL,                                     -- 생성 일시
    updated_at       DATETIME     NOT NULL,                                     -- 수정 일시
    INDEX idx_product_option_group_id (option_group_id),                        -- 인덱스: 옵션 그룹별 조회
    INDEX idx_product_option_active (option_group_id, is_visible, sort)         -- 인덱스: 옵션 그룹·노출·정렬 복합 조회
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
    sort               INT          NOT NULL,                                         -- 정렬 순서
    is_visible         TINYINT(1)   NOT NULL DEFAULT 1,                               -- 노출 여부 (1: 노출)
    created_at         DATETIME     NOT NULL,                                         -- 생성 일시
    updated_at         DATETIME     NOT NULL,                                         -- 수정 일시
    INDEX idx_product_option_group_product_id (product_id),                           -- 인덱스: 상품별 조회
    INDEX idx_product_option_group_active (product_id, is_visible, sort)              -- 인덱스: 상품·노출·정렬 복합 조회
);

CREATE TABLE SHOP
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY, -- 장소 ID (PK)
    station_id        BIGINT        NOT NULL,            -- 지하철역 ID (STATION.id 참조)
    name              VARCHAR(255)  NOT NULL UNIQUE,     -- 장소 이름
    latitude          DECIMAL(9, 6) NOT NULL,            -- 위도
    longitude         DECIMAL(9, 6) NOT NULL,            -- 경도
    rating            DOUBLE,                            -- 평균 평점
    road_address      VARCHAR(500),                      -- 도로명 주소
    lot_address       VARCHAR(500),                      -- 지번 주소
    phone_number      VARCHAR(20),                       -- 대표 전화번호
    thumbnail_image_file_id    BIGINT,                   -- 썸네일 이미지 파일 ID (UPLOADED_FILE.id 참조)
    is_permanently_closed      TINYINT(1)    NOT NULL DEFAULT 0, -- 폐업 여부 (1: 폐업)
    created_at        DATETIME      NOT NULL,            -- 생성 일시
    updated_at        DATETIME      NOT NULL             -- 수정 일시
);

CREATE TABLE MEMBER_POINT
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 회원 포인트 ID (PK)
    member_id               BIGINT   NOT NULL UNIQUE,            -- 회원 ID (MEMBER.id 참조)
    available_points        INT      NOT NULL DEFAULT 0,         -- 사용 가능 포인트
    expired_this_month      INT      NOT NULL DEFAULT 0,         -- 이번 달 만료 예정 포인트
    created_at              DATETIME NOT NULL,                   -- 생성 일시
    updated_at              DATETIME NOT NULL,                   -- 수정 일시
    INDEX idx_member_point_member_id (member_id)                 -- 인덱스: 회원별 조회
);

CREATE TABLE MEMBER_POINT_HISTORY
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                    -- 포인트 이력 ID (PK)
    member_id         BIGINT       NOT NULL,                                -- 회원 ID (MEMBER.id 참조)
    point_type        VARCHAR(50)  NOT NULL,                                -- 포인트 유형 (EARN, USE, EXPIRE 등)
    point_amount      INT          NOT NULL,                                -- 포인트 변동 금액
    reason            VARCHAR(200) NOT NULL,                                -- 변동 사유
    created_at        DATETIME     NOT NULL,                                -- 생성 일시
    updated_at        DATETIME     NOT NULL,                                -- 수정 일시
    INDEX idx_member_point_history_member_id (member_id),                   -- 인덱스: 회원별 조회
    INDEX idx_member_point_history_created_at (created_at)                  -- 인덱스: 생성 일시별 조회
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
    created_at                 DATETIME     NOT NULL,                                                -- 생성 일시
    updated_at                 DATETIME     NOT NULL,                                                -- 수정 일시
    INDEX idx_partnership_request_business_name (business_name),                                     -- 인덱스: 사업체명별 조회
    INDEX idx_partnership_request_consultation_date (consultation_requested_at)                      -- 인덱스: 상담 희망 일시별 조회
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
    will_revisit      TINYINT(1),                        -- 재방문 의향 (1: 있음)
    is_hidden         TINYINT(1) NOT NULL DEFAULT 0,     -- 숨김 여부 (1: 숨김)
    created_at        DATETIME   NOT NULL,               -- 생성 일시
    updated_at        DATETIME   NOT NULL,               -- 수정 일시
    INDEX idx_review_product_id (product_id),            -- 인덱스: 상품별 조회
    INDEX idx_review_order_id (order_id)                 -- 인덱스: 주문별 조회
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
    final_amount            INT          NOT NULL DEFAULT 0,     -- 최종 결제 금액
    member_coupon_id        BIGINT,                              -- 사용한 회원 쿠폰 ID (MEMBER_COUPON.id 참조)
    used_point              INT          NOT NULL DEFAULT 0,     -- 사용 포인트
    earned_point            INT          NOT NULL DEFAULT 0,     -- 적립 포인트
    created_at              DATETIME     NOT NULL,               -- 생성 일시
    updated_at              DATETIME     NOT NULL,               -- 수정 일시
    INDEX idx_orders_member_id (member_id),                      -- 인덱스: 회원별 조회
    INDEX idx_orders_shop_id (shop_id),                        -- 인덱스: 장소별 조회
    INDEX idx_orders_order_status (order_status),                -- 인덱스: 주문 상태별 조회
    INDEX idx_orders_created_at (created_at)                     -- 인덱스: 생성 일시별 조회
);

CREATE TABLE ORDER_PRODUCT
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 주문 상품 ID (PK)
    order_id          BIGINT       NOT NULL,                     -- 주문 ID (ORDERS.id 참조)
    product_id        BIGINT       NOT NULL,                     -- 상품 ID (PRODUCT.id 참조)
    name              VARCHAR(255) NOT NULL,                     -- 주문 시점 상품명 (스냅샷)
    image_url         VARCHAR(500),                              -- 주문 시점 상품 이미지 URL (스냅샷)
    quantity          INT          NOT NULL DEFAULT 1,           -- 수량
    original_price    INT          NOT NULL DEFAULT 0,           -- 정가
    discount_price    INT,                                       -- 할인가
    total_option_price INT         NOT NULL DEFAULT 0,           -- 옵션 금액 합계
    total_price       INT          NOT NULL DEFAULT 0,           -- 상품 총 금액
    created_at        DATETIME     NOT NULL,                     -- 생성 일시
    updated_at        DATETIME     NOT NULL,                     -- 수정 일시
    INDEX idx_order_product_order_id (order_id),                 -- 인덱스: 주문별 조회
    INDEX idx_order_product_product_id (product_id)              -- 인덱스: 상품별 조회
);

CREATE TABLE ORDER_PRODUCT_OPTION
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 주문 상품 옵션 ID (PK)
    order_product_id  BIGINT       NOT NULL,                              -- 주문 상품 ID (ORDER_PRODUCT.id 참조)
    option_group_id   BIGINT,                                             -- 옵션 그룹 ID (스냅샷, NULL 가능)
    option_group_name VARCHAR(100) NOT NULL,                              -- 주문 시점 옵션 그룹 이름 (스냅샷)
    option_id         BIGINT,                                             -- 옵션 ID (스냅샷, NULL 가능)
    option_name       VARCHAR(100) NOT NULL,                              -- 주문 시점 옵션 이름 (스냅샷)
    additional_price  INT          NOT NULL DEFAULT 0,                    -- 옵션 추가 금액
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
    created_at   DATETIME     NOT NULL,                          -- 생성 일시
    updated_at   DATETIME     NOT NULL,                          -- 수정 일시
    INDEX idx_faq_category_active (is_visible, sort)             -- 인덱스: 노출·정렬 복합 조회
);

CREATE TABLE FAQ
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,                    -- FAQ ID (PK)
    faq_category_id BIGINT        NOT NULL,                               -- FAQ 카테고리 ID (FAQ_CATEGORY.id 참조)
    question        VARCHAR(500)  NOT NULL,                               -- 질문
    answer          TEXT          NOT NULL,                               -- 답변
    sort            INT           NOT NULL,                               -- 정렬 순서
    is_visible      TINYINT(1)    NOT NULL DEFAULT 1,                     -- 노출 여부 (1: 노출)
    created_at      DATETIME      NOT NULL,                               -- 생성 일시
    updated_at      DATETIME      NOT NULL,                               -- 수정 일시
    INDEX idx_faq_category_id (faq_category_id),                          -- 인덱스: 카테고리별 조회
    INDEX idx_faq_active (faq_category_id, is_visible, sort)              -- 인덱스: 카테고리·노출·정렬 복합 조회
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

CREATE TABLE PHONE_VERIFICATION
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                  -- 휴대폰 인증 ID (PK)
    phone_number      VARCHAR(11)  NOT NULL,                              -- 인증 휴대폰 번호
    verification_code VARCHAR(6)   NOT NULL,                              -- 인증 코드 (6자리)
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',            -- 인증 상태 (PENDING, VERIFIED, EXPIRED 등)
    expires_at        DATETIME     NOT NULL,                              -- 만료 일시
    verified_at       DATETIME,                                           -- 인증 완료 일시
    created_at        DATETIME     NOT NULL,                              -- 생성 일시
    INDEX idx_phone_verification_phone_number (phone_number),             -- 인덱스: 휴대폰 번호별 조회
    INDEX idx_phone_verification_expires_at (expires_at)                  -- 인덱스: 만료 일시별 조회
);

CREATE TABLE EMAIL_VERIFICATION
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,                -- 이메일 인증 ID (PK)
    email             VARCHAR(100) NOT NULL,                            -- 인증 이메일 주소
    verification_code VARCHAR(6)   NOT NULL,                            -- 인증 코드 (6자리)
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',          -- 인증 상태 (PENDING, VERIFIED, EXPIRED 등)
    expires_at        DATETIME     NOT NULL,                            -- 만료 일시
    verified_at       DATETIME,                                         -- 인증 완료 일시
    created_at        DATETIME     NOT NULL,                            -- 생성 일시
    INDEX idx_email_verification_email (email),                         -- 인덱스: 이메일별 조회
    INDEX idx_email_verification_expires_at (expires_at)                -- 인덱스: 만료 일시별 조회
);

CREATE TABLE RANK_PERIOD
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,     -- 랭킹 기간 ID (PK)
    start_at   DATETIME   NOT NULL,                   -- 랭킹 시작 일시
    end_at     DATETIME   NOT NULL,                   -- 랭킹 종료 일시
    is_visible TINYINT(1) NOT NULL DEFAULT 1,         -- 노출 여부 (1: 노출)
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

CREATE TABLE SHOP_RESERVATION_SLOT
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,                   -- 슬롯 ID (PK)
    shop_id        BIGINT   NOT NULL,                                   -- 장소 ID (SHOP.id 참조)
    slot_date      DATE     NOT NULL,                                   -- 슬롯 날짜
    slot_time      TIME     NOT NULL,                                   -- 슬롯 시간 (30분 단위)
    capacity       INT      NOT NULL,                                   -- 슬롯당 정원 (팀 수)
    reserved_count INT      NOT NULL DEFAULT 0,                         -- 현재 점유 팀 수
    version        BIGINT,                                              -- 낙관적 락 버전 (@Version)
    created_at     DATETIME NOT NULL,                                   -- 생성 일시
    updated_at     DATETIME NOT NULL,                                   -- 수정 일시
    UNIQUE KEY uk_shop_reservation_slot (shop_id, slot_date, slot_time) -- 유니크: 가게·날짜·시간 슬롯 중복 방지
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
