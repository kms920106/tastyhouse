package com.tastyhouse.application.grade.port.in;

import java.util.List;

import com.tastyhouse.application.grade.port.out.GradeInfoResult;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface GradeQueryUseCase {

    List<GradeInfoResult> getGradeInfoList();
}
