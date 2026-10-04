package com.tastyhouse.application.shared.exception;

public enum AdminErrorCode implements ApplicationErrorCodeSpec {

    ADMIN_USERNAME_DUPLICATED(409, "ADMIN_USERNAME_DUPLICATED", "이미 사용 중인 관리자 아이디입니다."),
    ADMIN_AUTHENTICATION_FAILED(401, "ADMIN_AUTHENTICATION_FAILED", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ADMIN_ACCOUNT_INACTIVE(401, "ADMIN_ACCOUNT_INACTIVE", "비활성화된 관리자 계정입니다."),

    CEO_NOT_FOUND(404, "CEO_NOT_FOUND", "점주를 찾을 수 없습니다."),

    FILE_NOT_FOUND(404, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),

    REVIEW_REPLY_NOT_FOUND(404, "REVIEW_REPLY_NOT_FOUND", "답글을 찾을 수 없습니다."),

    SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND(404, "SHOP_FOOD_TYPE_CATEGORY_NOT_FOUND", "존재하지 않는 음식 종류 카테고리입니다."),
    SHOP_PHOTO_CATEGORY_NOT_FOUND(404, "SHOP_PHOTO_CATEGORY_NOT_FOUND", "존재하지 않는 포토 카테고리입니다."),
    SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND(404, "SHOP_PHOTO_CATEGORY_IMAGE_NOT_FOUND", "존재하지 않는 포토 이미지입니다."),
    SHOP_CHOICE_NOT_FOUND(404, "SHOP_CHOICE_NOT_FOUND", "존재하지 않는 테하 초이스입니다."),

    SHOP_NOTICE_ALREADY_HIDDEN(409, "SHOP_NOTICE_ALREADY_HIDDEN", "이미 게시중단된 공지입니다."),
    SHOP_NOTICE_NOT_HIDDEN(409, "SHOP_NOTICE_NOT_HIDDEN", "게시중단 상태가 아닌 공지입니다."),

    SHOP_HYGIENE_BADGE_NOT_FOUND(404, "SHOP_HYGIENE_BADGE_NOT_FOUND", "존재하지 않는 위생 인증 뱃지입니다."),

    SHOP_CEO_ALREADY_ASSIGNED(409, "SHOP_CEO_ALREADY_ASSIGNED", "이미 해당 점주가 배정된 가게입니다."),
    SHOP_CEO_NOT_ASSIGNED(409, "SHOP_CEO_NOT_ASSIGNED", "담당 점주가 배정되지 않은 가게입니다."),

    POLICY_NOT_FOUND(404, "POLICY_NOT_FOUND", "정책 문서를 찾을 수 없습니다."),

    NOTICE_NOT_FOUND(404, "NOTICE_NOT_FOUND", "공지사항을 찾을 수 없습니다."),

    FAQ_NOT_FOUND(404, "FAQ_NOT_FOUND", "FAQ를 찾을 수 없습니다."),
    FAQ_CATEGORY_NOT_FOUND(404, "FAQ_CATEGORY_NOT_FOUND", "FAQ 카테고리를 찾을 수 없습니다."),
    FAQ_CATEGORY_HAS_ITEMS(400, "FAQ_CATEGORY_HAS_ITEMS", "소속된 FAQ가 있어 카테고리를 삭제할 수 없습니다."),

    BUG_REPORT_NOT_FOUND(404, "BUG_REPORT_NOT_FOUND", "버그 제보를 찾을 수 없습니다."),

    BANNER_NOT_FOUND(404, "BANNER_NOT_FOUND", "배너를 찾을 수 없습니다."),

    RANK_PERIOD_NOT_FOUND(404, "RANK_PERIOD_NOT_FOUND", "랭킹 기간을 찾을 수 없습니다."),
    RANK_PRIZE_NOT_FOUND(404, "RANK_PRIZE_NOT_FOUND", "랭킹 경품을 찾을 수 없습니다."),

    EVENT_ANNOUNCEMENT_NOT_FOUND(404, "EVENT_ANNOUNCEMENT_NOT_FOUND", "당첨자 발표 공지를 찾을 수 없습니다."),
    EVENT_ANNOUNCEMENT_ALREADY_EXISTS(409, "EVENT_ANNOUNCEMENT_ALREADY_EXISTS", "이미 당첨자 발표 공지가 등록된 이벤트입니다."),
    EVENT_WINNER_NOT_FOUND(404, "EVENT_WINNER_NOT_FOUND", "당첨자를 찾을 수 없습니다."),

    PARTNERSHIP_REQUEST_NOT_FOUND(404, "PARTNERSHIP_REQUEST_NOT_FOUND", "제휴 신청을 찾을 수 없습니다.");

    private final int httpStatusCode;
    private final String code;
    private final String defaultMessage;

    AdminErrorCode(int httpStatusCode, String code, String defaultMessage) {
        this.httpStatusCode = httpStatusCode;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    @Override
    public int getHttpStatusCode() {
        return this.httpStatusCode;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getDefaultMessage() {
        return this.defaultMessage;
    }
}
