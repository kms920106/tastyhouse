package com.tastyhouse.application.member.port.out.write;

import java.util.List;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGrade;

public interface MemberSavePort {

    long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade);

    Member save(Member member);
}
