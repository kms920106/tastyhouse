package com.tastyhouse.application.grade.service;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.application.grade.port.in.GradeQueryUseCase;
import com.tastyhouse.application.grade.port.out.GradeInfoResult;

@Service
@WebApp
@Transactional(readOnly = true)
public class GradeQueryService implements GradeQueryUseCase {

    /**
     * 전체 등급 세부 조건 목록 조회.
     *
     * <p>도메인 enum을 여기서 {@code name()}으로 강등해 넘긴다 — 인바운드 포트가 도메인 타입을
     * 노출하지 않게 하는 것이 챕터 06~10의 경계 규칙이다.
     */
    @Override
    public List<GradeInfoResult> getGradeInfoList() {
        return Arrays.stream(MemberGrade.values())
            .map(grade -> new GradeInfoResult(
                grade.name(),
                grade.getDisplayName(),
                grade.getMinReviewCount(),
                grade.getMaxReviewCount()))
            .toList();
    }
}
