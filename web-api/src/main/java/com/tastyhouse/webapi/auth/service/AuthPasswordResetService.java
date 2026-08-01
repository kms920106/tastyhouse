package com.tastyhouse.webapi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.mail.domain.model.MailVerificationPurpose;
import com.tastyhouse.domain.mail.domain.service.MailVerificationService;
import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.auth.response.AuthPasswordResetTokenResponse;
import com.tastyhouse.webapi.member.service.MemberCommandService;

/**
 * 비밀번호 재설정 파사드.
 *
 * <p><b>트랜잭션 원자성 판정(단계별)</b> — 파사드 자신은 트랜잭션을 열지 않고, DB 원자성이 실제로 필요한
 * 구간만 하위 서비스가 트랜잭션을 갖는다. 파사드가 전체를 감싸면 JWT 서명 검증·Redis 접근처럼 DB와 무관한
 * 단계까지 커넥션을 점유하고, 반대로 하위가 각각 열면 원자여야 할 구간이 쪼개진다.
 * <ul>
 *   <li>{@link #sendPasswordResetCode} — 아이디 존재 확인은 단순 read라 원자성이 필요 없다. 인증코드
 *       발급은 "기존 미완료 인증 만료 + 새 인증 저장 + 발송"이 함께 성립해야 하는 구간이라
 *       {@link MailVerificationService#issue} 호출을 감싸는 <b>단일 트랜잭션</b>이 필요하고, 그 경계는
 *       이 메서드가 갖는다({@code @Transactional}).
 *       <p><b>메일 발송이 이 트랜잭션 안에 남아 있는 이유</b>: 발송을 커밋 이후로 미루는 것이 일반 원칙이나
 *       (외부 I/O를 DB 트랜잭션에서 빼는 것), 이 도메인에서는 <em>발송 실패 시 인증 레코드도 함께
 *       롤백되는 것이 올바른 시맨틱</em>이다 — 인증코드는 발송되지 않으면 존재 가치가 0이고, DB에 PENDING
 *       코드만 남는 유령 레코드보다 오류 응답이 낫다. 그래서 발송 책임은 도메인 서비스의 {@code issue}가
 *       소유하며(발송 누락을 구조적으로 불가능하게 만든 과거 버그 대응), 이 파사드가 발송을 별도로
 *       호출하거나 커밋 후로 분리하지 않는다. 상세 근거는 루트 {@code CLAUDE.md}의 "인증코드 발송은 발급과
 *       원자적으로 수행하는 규칙" 참고.</li>
 *   <li>{@link #verifyPasswordResetCode} — 인증코드 확인(코드 대조 + 상태 전이 + 저장)이 read-then-write
 *       구간이므로 <b>단일 트랜잭션</b>이 필요하다. 뒤따르는 재설정 토큰 발급은 JWT 서명뿐이라 DB와
 *       무관하지만, 같은 메서드에 남겨 두어도 커넥션 점유가 서명 연산 한 번뿐이라 분리 이득이 없다.</li>
 *   <li>{@link #resetPassword} — 토큰 검증은 JWT 서명(DB 무관), 아이디→식별자 해석은 단순 read,
 *       "기존 비밀번호와 동일" 검사와 실제 변경은 <b>read-then-write이므로 반드시 한 트랜잭션</b>이다.
 *       그 원자 구간을 {@link MemberCommandService#updatePassword} 하나로 내렸고, 이 메서드에는
 *       {@code @Transactional}을 두지 않는다 — 원자 구간 밖(JWT 검증)까지 트랜잭션에 넣지 않는다.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthPasswordResetService {

    private final MemberRepository memberRepository;
    private final MailVerificationService mailVerificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberCommandService memberCommandService;

    /**
     * 비밀번호 재설정 인증코드를 발급하고 메일로 발송한다.
     *
     * <p>존재하지 않는 아이디여도 성공으로 응답한다(계정 존재 여부 노출 방지 — 기존 동작 보존).
     */
    @Transactional
    public void sendPasswordResetCode(String username) {
        if (!memberRepository.existsByUsername(username)) {
            log.info("비밀번호 재설정 요청: 존재하지 않는 아이디. username={}", username);
            return;
        }

        // 발급이 발송까지 수행한다(문구는 도메인 소유 MailVerificationMessage). 과거에는 이 파사드가
        // MailSender를 직접 주입해 문구 상수와 함께 발송을 호출했는데, 그 구조 때문에 인증코드 발송
        // API 경로는 발송 호출을 빠뜨려 코드가 저장만 되던 버그가 있었다.
        mailVerificationService.issue(username, MailVerificationPurpose.PASSWORD_RESET);
    }

    /**
     * 인증코드를 확인하고 비밀번호 재설정 토큰을 발급한다.
     */
    @Transactional
    public AuthPasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode) {
        mailVerificationService.confirm(username, verificationCode);

        String passwordResetToken = jwtTokenProvider.createPasswordResetToken(username);

        return AuthPasswordResetTokenResponse.from(passwordResetToken);
    }

    /**
     * 재설정 토큰으로 비밀번호를 변경한다.
     *
     * <p>이 메서드에 {@code @Transactional}이 없는 것은 의도다 — 위 클래스 Javadoc의 단계별 판정 참고.
     */
    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        if (!jwtTokenProvider.validatePasswordResetToken(passwordResetToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_RESET_TOKEN_INVALID);
        }

        String username = jwtTokenProvider.getUsernameFromPasswordResetToken(passwordResetToken);

        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 아이디→식별자 해석만 여기서 하고, "기존 비밀번호와 동일" 검사는 변경과 같은 트랜잭션·같은 회원
        // 로드 안에서 수행하도록 MemberCommandService.updatePassword로 내렸다(read-then-write 원자화).
        // 예외 코드(MEMBER_PASSWORD_SAME_AS_OLD)와 검사 순서는 그대로다.
        memberCommandService.updatePassword(member.getId(), newPassword, newPasswordConfirm);
    }
}
