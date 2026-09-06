package com.tastyhouse.application.grade.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.grade.port.out.GradeInfoResult;

@WebApp
public interface GradeQueryUseCase {

    List<GradeInfoResult> getGradeInfoList();
}
