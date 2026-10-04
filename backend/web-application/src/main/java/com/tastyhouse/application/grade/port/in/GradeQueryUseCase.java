package com.tastyhouse.application.grade.port.in;

import java.util.List;

import com.tastyhouse.application.grade.port.out.GradeInfoResult;

public interface GradeQueryUseCase {

    List<GradeInfoResult> getGradeInfoList();
}
