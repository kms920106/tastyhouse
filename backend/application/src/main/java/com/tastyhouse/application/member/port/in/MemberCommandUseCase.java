package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

/**
 * 회원 쓰기 인바운드 포트 — HTTP 경계에서 들어오는 회원 정보 변경 유스케이스를 담는다.
 *
 * <p><b>가입 경로(가입·소셜 가입·소셜 계정 저장)는 이 포트에 없다.</b> 그 세 연산은 도메인 타입
 * ({@code MemberGender}·{@code MemberSocialAccount})을 인자로 받는데 Command record는 경계 타입만
 * 담을 수 있고(챕터 02 §2), 호출자도 컨트롤러가 아니라 같은 계층의 협력 빈(소셜로그인 서비스 4종·
 * {@code CredentialLoginService})이라 인바운드 포트를 경유할 이유가 없다. 그 호출자들은 구현
 * ({@code MemberCommandService})을 그대로 주입한다.
 */
@WebApp
public interface MemberCommandUseCase {

    void updateProfile(MemberProfileUpdateCommand command);

    void updatePersonalInfo(MemberPersonalInfoUpdateCommand command);

    void updatePassword(MemberPasswordUpdateCommand command);

    void withdraw(MemberWithdrawCommand command);
}
