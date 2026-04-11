package com.tastyhouse.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통 - 엔티티 미존재
    ENTITY_NOT_FOUND(404, "ENTITY_NOT_FOUND", "요청한 데이터를 찾을 수 없습니다."),

    // 주문
    ORDER_NOT_FOUND(404, "ORDER_NOT_FOUND", "주문을 찾을 수 없습니다."),
    ORDER_ACCESS_DENIED(403, "ORDER_ACCESS_DENIED", "본인의 주문만 조회할 수 있습니다."),
    ORDER_PRODUCT_NOT_FOUND(404, "ORDER_PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),
    ORDER_OPTION_GROUP_NOT_FOUND(404, "ORDER_OPTION_GROUP_NOT_FOUND", "옵션 그룹을 찾을 수 없습니다."),
    ORDER_OPTION_NOT_FOUND(404, "ORDER_OPTION_NOT_FOUND", "옵션을 찾을 수 없습니다."),
    ORDER_PRODUCT_SOLD_OUT(400, "ORDER_PRODUCT_SOLD_OUT", "품절된 상품입니다."),
    ORDER_PRODUCT_AMOUNT_MISMATCH(400, "ORDER_PRODUCT_AMOUNT_MISMATCH", "상품 금액이 일치하지 않습니다."),
    ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH(400, "ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH", "상품 할인 금액이 일치하지 않습니다."),
    ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH(400, "ORDER_COUPON_DISCOUNT_AMOUNT_MISMATCH", "쿠폰 사용 금액이 일치하지 않습니다."),
    ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH(400, "ORDER_POINT_DISCOUNT_AMOUNT_MISMATCH", "포인트 사용 금액이 일치하지 않습니다."),
    ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH(400, "ORDER_TOTAL_DISCOUNT_AMOUNT_MISMATCH", "할인 금액이 일치하지 않습니다."),
    ORDER_FINAL_AMOUNT_MISMATCH(400, "ORDER_FINAL_AMOUNT_MISMATCH", "결제 금액이 일치하지 않습니다."),
    ORDER_MINIMUM_AMOUNT_NOT_MET(400, "ORDER_MINIMUM_AMOUNT_NOT_MET", "최소 주문 금액을 충족하지 않습니다."),

    // 쿠폰
    COUPON_NOT_FOUND(404, "COUPON_NOT_FOUND", "쿠폰을 찾을 수 없습니다."),
    COUPON_ACCESS_DENIED(403, "COUPON_ACCESS_DENIED", "본인의 쿠폰만 사용할 수 있습니다."),
    COUPON_INFO_NOT_FOUND(404, "COUPON_INFO_NOT_FOUND", "쿠폰 정보를 찾을 수 없습니다."),
    COUPON_NOT_AVAILABLE(400, "COUPON_NOT_AVAILABLE", "사용할 수 없는 쿠폰입니다."),

    // 결제
    PAYMENT_ORDER_ACCESS_DENIED(403, "PAYMENT_ORDER_ACCESS_DENIED", "본인의 주문만 결제할 수 있습니다."),
    PAYMENT_ACCESS_DENIED(403, "PAYMENT_ACCESS_DENIED", "본인의 결제만 처리할 수 있습니다."),
    PAYMENT_INVALID_ORDER_STATUS(400, "PAYMENT_INVALID_ORDER_STATUS", "결제할 수 없는 주문 상태입니다."),
    PAYMENT_ALREADY_IN_PROGRESS(400, "PAYMENT_ALREADY_IN_PROGRESS", "이미 결제가 진행 중인 주문입니다."),
    PAYMENT_NOT_PENDING_APPROVAL(400, "PAYMENT_NOT_PENDING_APPROVAL", "승인 대기 중인 결제만 처리할 수 있습니다."),
    PAYMENT_AMOUNT_MISMATCH(400, "PAYMENT_AMOUNT_MISMATCH", "결제 금액이 일치하지 않습니다."),
    PAYMENT_APPROVAL_FAILED(400, "PAYMENT_APPROVAL_FAILED", "결제 승인에 실패했습니다."),
    PAYMENT_REFUND_AMOUNT_EXCEEDED(400, "PAYMENT_REFUND_AMOUNT_EXCEEDED", "환불 금액이 결제 금액을 초과할 수 없습니다."),
    PAYMENT_NOT_COMPLETED(400, "PAYMENT_NOT_COMPLETED", "완료된 결제만 환불할 수 있습니다."),
    PAYMENT_NOT_PENDING(400, "PAYMENT_NOT_PENDING", "대기 중인 결제만 완료 처리할 수 있습니다."),
    PAYMENT_NOT_ON_SITE(400, "PAYMENT_NOT_ON_SITE", "현장결제만 완료 처리할 수 있습니다."),

    // 회원
    MEMBER_NOT_FOUND(404, "MEMBER_NOT_FOUND", "회원을 찾을 수 없습니다."),
    MEMBER_USERNAME_DUPLICATED(409, "MEMBER_USERNAME_DUPLICATED", "이미 사용 중인 아이디입니다."),
    MEMBER_EMAIL_ALREADY_REGISTERED(409, "MEMBER_EMAIL_ALREADY_REGISTERED", "이미 가입된 이메일입니다."),
    MEMBER_NICKNAME_DUPLICATED(409, "MEMBER_NICKNAME_DUPLICATED", "이미 사용 중인 닉네임입니다."),
    MEMBER_PHONE_ALREADY_REGISTERED(409, "MEMBER_PHONE_ALREADY_REGISTERED", "이미 가입된 휴대폰번호입니다."),
    MEMBER_SIGNUP_PHONE_REQUIRED(400, "MEMBER_SIGNUP_PHONE_REQUIRED", "회원가입 시 휴대폰 인증이 필요합니다."),
    MEMBER_SIGNUP_EMAIL_REQUIRED(400, "MEMBER_SIGNUP_EMAIL_REQUIRED", "회원가입 시 이메일 인증이 필요합니다."),
    MEMBER_INFO_AUTH_EXPIRED(400, "MEMBER_INFO_AUTH_EXPIRED", "개인정보 수정 인증이 만료되었습니다. 비밀번호를 다시 인증해주세요."),
    MEMBER_PHONE_SMS_REQUIRED(400, "MEMBER_PHONE_SMS_REQUIRED", "휴대폰번호 변경 시 SMS 인증이 필요합니다."),
    MEMBER_PHONE_AUTH_EXPIRED(400, "MEMBER_PHONE_AUTH_EXPIRED", "휴대폰 인증이 만료되었습니다. SMS 인증을 다시 진행해주세요."),
    MEMBER_PHONE_MISMATCH(400, "MEMBER_PHONE_MISMATCH", "인증된 휴대폰번호와 입력한 휴대폰번호가 일치하지 않습니다."),
    MEMBER_EMAIL_AUTH_EXPIRED(400, "MEMBER_EMAIL_AUTH_EXPIRED", "이메일 인증이 만료되었습니다. 이메일 인증을 다시 진행해주세요."),
    MEMBER_EMAIL_MISMATCH(400, "MEMBER_EMAIL_MISMATCH", "인증된 이메일과 입력한 아이디(이메일)가 일치하지 않습니다."),
    MEMBER_PASSWORD_MISMATCH(400, "MEMBER_PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    MEMBER_PASSWORD_CONFIRM_MISMATCH(400, "MEMBER_PASSWORD_CONFIRM_MISMATCH", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."),
    MEMBER_PASSWORD_SAME_AS_OLD(400, "MEMBER_PASSWORD_SAME_AS_OLD", "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
    MEMBER_PASSWORD_RESET_TOKEN_INVALID(400, "MEMBER_PASSWORD_RESET_TOKEN_INVALID", "비밀번호 재설정 토큰이 유효하지 않거나 만료되었습니다."),
    MEMBER_COUPON_NOT_FOUND(404, "MEMBER_COUPON_NOT_FOUND", "회원 쿠폰을 찾을 수 없습니다."),

    // 인증 (SMS)
    VERIFICATION_CODE_NOT_FOUND(400, "VERIFICATION_CODE_NOT_FOUND", "발송된 인증번호가 없습니다. 인증번호를 다시 요청해주세요."),
    VERIFICATION_CODE_EXPIRED(400, "VERIFICATION_CODE_EXPIRED", "인증번호가 만료되었습니다. 인증번호를 다시 요청해주세요."),
    VERIFICATION_CODE_MISMATCH(400, "VERIFICATION_CODE_MISMATCH", "인증번호가 일치하지 않습니다."),

    // 인증 (이메일)
    EMAIL_VERIFICATION_CODE_NOT_FOUND(400, "EMAIL_VERIFICATION_CODE_NOT_FOUND", "발송된 인증번호가 없습니다. 인증번호를 다시 요청해주세요."),
    EMAIL_VERIFICATION_CODE_EXPIRED(400, "EMAIL_VERIFICATION_CODE_EXPIRED", "인증번호가 만료되었습니다. 인증번호를 다시 요청해주세요."),
    EMAIL_VERIFICATION_CODE_MISMATCH(400, "EMAIL_VERIFICATION_CODE_MISMATCH", "인증번호가 일치하지 않습니다."),

    // 포인트
    POINT_NOT_FOUND(404, "POINT_NOT_FOUND", "포인트 정보를 찾을 수 없습니다."),
    POINT_INSUFFICIENT(400, "POINT_INSUFFICIENT", "포인트가 부족합니다."),

    // 파일
    FILE_EMPTY(400, "FILE_EMPTY", "파일이 비어있습니다."),
    FILE_SIZE_EXCEEDED(400, "FILE_SIZE_EXCEEDED", "파일 크기는 10MB를 초과할 수 없습니다."),
    FILE_TYPE_NOT_ALLOWED(400, "FILE_TYPE_NOT_ALLOWED", "허용되지 않는 파일 형식입니다. (jpg, png, gif, webp만 가능)"),
    FILE_EXTENSION_NOT_ALLOWED(400, "FILE_EXTENSION_NOT_ALLOWED", "허용되지 않는 파일 확장자입니다. (jpg, png, gif, webp만 가능)"),
    FILE_EXTENSION_UNKNOWN(400, "FILE_EXTENSION_UNKNOWN", "파일 확장자를 확인할 수 없습니다."),
    FILE_NOT_FOUND(404, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    FILE_STORE_FAILED(500, "FILE_STORE_FAILED", "파일 저장에 실패했습니다."),
    FILE_DELETE_FAILED(500, "FILE_DELETE_FAILED", "파일 삭제에 실패했습니다."),

    // 리뷰
    REVIEW_NOT_FOUND(404, "REVIEW_NOT_FOUND", "리뷰를 찾을 수 없습니다."),
    REVIEW_ACCESS_DENIED(403, "REVIEW_ACCESS_DENIED", "본인의 리뷰만 수정/삭제할 수 있습니다."),
    REVIEW_ALREADY_EXISTS(400, "REVIEW_ALREADY_EXISTS", "이미 리뷰를 작성한 상품입니다."),
    REVIEW_ORDER_ITEM_NOT_FOUND(404, "REVIEW_ORDER_ITEM_NOT_FOUND", "주문 상품을 찾을 수 없습니다."),
    REVIEW_ORDER_ACCESS_DENIED(403, "REVIEW_ORDER_ACCESS_DENIED", "본인의 주문에 대해서만 리뷰를 작성할 수 있습니다."),

    // 플레이스
    PLACE_NOT_FOUND(404, "PLACE_NOT_FOUND", "존재하지 않는 플레이스입니다."),
    PLACE_STATION_NOT_FOUND(404, "PLACE_STATION_NOT_FOUND", "존재하지 않는 전철역입니다."),
    PLACE_AMENITY_CATEGORY_NOT_FOUND(404, "PLACE_AMENITY_CATEGORY_NOT_FOUND", "존재하지 않는 편의시설 카테고리입니다."),
    PLACE_IMAGE_CATEGORY_NOT_FOUND(404, "PLACE_IMAGE_CATEGORY_NOT_FOUND", "존재하지 않는 이미지 카테고리입니다."),

    // 정책
    POLICY_NOT_FOUND(404, "POLICY_NOT_FOUND", "정책 문서를 찾을 수 없습니다."),
    POLICY_CURRENT_NOT_FOUND(404, "POLICY_CURRENT_NOT_FOUND", "현재 유효한 정책을 찾을 수 없습니다."),
    POLICY_VERSION_NOT_FOUND(404, "POLICY_VERSION_NOT_FOUND", "해당 버전의 정책을 찾을 수 없습니다."),

    // 상품
    PRODUCT_NOT_FOUND(404, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),

    // 공지사항
    NOTICE_NOT_FOUND(404, "NOTICE_NOT_FOUND", "공지사항을 찾을 수 없습니다."),

    // 배너
    BANNER_NOT_FOUND(404, "BANNER_NOT_FOUND", "배너를 찾을 수 없습니다."),

    // 랭크
    RANK_TYPE_UNKNOWN(400, "RANK_TYPE_UNKNOWN", "알 수 없는 랭크 타입입니다."),

    // 추천인
    REFERRAL_REFERRER_NOT_FOUND(400, "REFERRAL_REFERRER_NOT_FOUND", "존재하지 않는 추천인 닉네임입니다."),
    REFERRAL_SELF_NOT_ALLOWED(400, "REFERRAL_SELF_NOT_ALLOWED", "자기 자신을 추천인으로 설정할 수 없습니다."),
    REFERRAL_ALREADY_EXISTS(400, "REFERRAL_ALREADY_EXISTS", "이미 추천인이 등록되어 있습니다."),

    // 팔로우
    FOLLOW_SELF_NOT_ALLOWED(400, "FOLLOW_SELF_NOT_ALLOWED", "자기 자신을 팔로우할 수 없습니다."),
    FOLLOW_ALREADY_EXISTS(400, "FOLLOW_ALREADY_EXISTS", "이미 팔로우한 사용자입니다."),
    FOLLOW_NOT_FOUND(400, "FOLLOW_NOT_FOUND", "팔로우 관계가 존재하지 않습니다."),
    FOLLOW_TARGET_NOT_FOUND(404, "FOLLOW_TARGET_NOT_FOUND", "팔로우 대상 회원을 찾을 수 없습니다."),
    FOLLOWER_REMOVE_ACCESS_DENIED(403, "FOLLOWER_REMOVE_ACCESS_DENIED", "본인의 팔로워만 삭제할 수 있습니다."),

    // 소셜 로그인
    SOCIAL_ACCOUNT_ALREADY_REGISTERED(409, "SOCIAL_ACCOUNT_ALREADY_REGISTERED", "이미 가입된 소셜 계정입니다."),
    SOCIAL_EMAIL_REQUIRED(400, "SOCIAL_EMAIL_REQUIRED", "카카오 이메일 제공 동의가 필요합니다."),
    SOCIAL_OAUTH_FAILED(502, "SOCIAL_OAUTH_FAILED", "소셜 로그인 처리 중 오류가 발생했습니다."),
    KAKAO_TEMP_TOKEN_EXPIRED(400, "KAKAO_TEMP_TOKEN_EXPIRED", "카카오 인증이 만료되었습니다. 다시 시도해주세요."),
    NAVER_TEMP_TOKEN_EXPIRED(400, "NAVER_TEMP_TOKEN_EXPIRED", "네이버 인증이 만료되었습니다. 다시 시도해주세요."),
    FACEBOOK_TEMP_TOKEN_EXPIRED(400, "FACEBOOK_TEMP_TOKEN_EXPIRED", "페이스북 인증이 만료되었습니다. 다시 시도해주세요."),
    APPLE_TEMP_TOKEN_EXPIRED(400, "APPLE_TEMP_TOKEN_EXPIRED", "애플 인증이 만료되었습니다. 다시 시도해주세요."),
    APPLE_ID_TOKEN_INVALID(400, "APPLE_ID_TOKEN_INVALID", "애플 인증 토큰이 유효하지 않습니다."),

    // Rate Limiting
    RATE_LIMIT_EXCEEDED(429, "RATE_LIMIT_EXCEEDED", "요청 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요.");

    private final int httpStatusCode;
    private final String code;
    private final String defaultMessage;
}
