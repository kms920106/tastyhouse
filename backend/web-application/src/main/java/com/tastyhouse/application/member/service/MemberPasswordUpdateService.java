package com.tastyhouse.application.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateUseCase;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
@Transactional
class MemberPasswordUpdateService implements MemberPasswordUpdateUseCase {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public MemberPasswordUpdateService(
        MemberPersistencePort memberPersistencePort,
        PasswordEncoder passwordEncoder
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void updatePassword(MemberPasswordUpdateCommand command) {
        String newPassword = command.newPassword();
        String newPasswordConfirm = command.newPasswordConfirm();
        Member member = loadMember(command.memberId());

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new ApplicationException(WebErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        member.updatePassword(passwordEncoder.encode(newPassword));
        memberPersistencePort.save(member);
    }

    private Member loadMember(Long memberId) {
        return memberPersistencePort.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.MEMBER_NOT_FOUND));
    }
}
