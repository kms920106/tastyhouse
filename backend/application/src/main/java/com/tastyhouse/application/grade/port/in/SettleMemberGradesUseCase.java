package com.tastyhouse.application.grade.port.in;

import com.tastyhouse.application.shared.marker.BatchApp;

@BatchApp
public interface SettleMemberGradesUseCase {

    void updateAllMemberGrades();
}
