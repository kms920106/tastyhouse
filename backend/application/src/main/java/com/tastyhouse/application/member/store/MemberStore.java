package com.tastyhouse.application.member.store;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.write.MemberStatePort;

public class MemberStore implements MemberRepository {
    private final MemberStatePort memberStatePort;

    public MemberStore(MemberStatePort memberStatePort) {
        this.memberStatePort = memberStatePort;
    }

    @Override
    public Optional<Member> findById(MemberId memberId) {
        return memberStatePort.findById(memberId.value()).map(MemberStateMapper::toDomain);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return memberStatePort.findByUsername(username).map(MemberStateMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return memberStatePort.existsByUsername(username);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return memberStatePort.existsByNickname(nickname);
    }

    @Override
    public Optional<Member> findByNickname(String nickname) {
        return memberStatePort.findByNickname(nickname).map(MemberStateMapper::toDomain);
    }

    @Override
    public boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
        return memberStatePort.existsByPhoneNumberAndStatusNot(phoneNumber, memberStatus.name());
    }

    @Override
    public Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
        return memberStatePort.findByPhoneNumberAndStatusNot(phoneNumber, memberStatus.name())
            .map(MemberStateMapper::toDomain);
    }

    @Override
    public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
        return memberStatePort.bulkUpdateGrade(memberIds, grade == null ? null : grade.name());
    }

    @Override
    public Member save(Member member) {
        return MemberStateMapper.toDomain(memberStatePort.save(MemberStateMapper.toState(member)));
    }
}
