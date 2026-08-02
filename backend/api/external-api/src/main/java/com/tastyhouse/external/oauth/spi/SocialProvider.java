package com.tastyhouse.external.oauth.spi;

/**
 * 소셜 로그인 제공자 식별자.
 *
 * <p>도메인 enum {@code MemberSocialProvider}(영속 컬럼)와 이름이 겹치지만 별개 타입이다 —
 * 이쪽은 외부 연동 어댑터를 고르기 위한 wire 측 식별자이고, 저쪽은 DB에 저장되는 도메인 값이다.
 * 소비 측(web-api)이 둘을 명시적으로 매핑하며, 이 seam이 wire enum을 도메인으로 새지 않게 한다.
 */
public enum SocialProvider {

    KAKAO, NAVER, FACEBOOK, APPLE
}
