package com.tastyhouse.adminapplication.ceo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;
import com.tastyhouse.application.ceo.port.out.CeoQueryPort;
import com.tastyhouse.adminapplication.ceo.port.in.CeoQueryUseCase;

/**
 * admin용 점주(ceo) 조회 서비스.
 *
 * <p>읽기 포트({@link CeoQueryPort})만 주입해 조회한다. 점주 계정의 생성·수정은 ceo-api가 담당하므로
 * 이 모듈에는 CommandService를 두지 않는다.
 *
 * <p>가게에 점주를 배정하는 Select 드롭다운을 채우기 위해 전체 점주 목록을 반환한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * core enum을 {@code name()} 문자열로 되돌리는 것을 포함한 표현 계약 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class CeoQueryService implements CeoQueryUseCase {

    private final CeoQueryPort ceoQueryPort;

    public CeoQueryService(CeoQueryPort ceoQueryPort) {
        this.ceoQueryPort = ceoQueryPort;
    }

    @Override
    public List<CeoListItemResult> getCeos() {
        return ceoQueryPort.findAllCeos();
    }
}
