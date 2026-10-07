package com.tastyhouse.application.member.port.in;

public interface MemberPasswordVerifyUseCase {

    String verifyPasswordAndIssueToken(Long memberId, String password);
}
