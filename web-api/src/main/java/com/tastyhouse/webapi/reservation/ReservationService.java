package com.tastyhouse.webapi.reservation;

import com.tastyhouse.core.domain.reservation.application.ReservationQueryService;
import com.tastyhouse.core.domain.reservation.application.dto.result.ReservationResult;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.reservation.response.ReservationCompleteDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예약 조회 응답 가공 전용 web-api 서비스.
 * core-module이 반환하는 raw 파일 경로를 {@link FileService}로 접근 가능한 URL로 변환해
 * 다른 가게 이미지 응답과 동일한 형식(완전한 URL)을 보장한다.
 */
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationQueryService reservationQueryService;
    private final FileService fileService;

    @Transactional(readOnly = true)
    public ReservationCompleteDetailResponse getDetail(Long memberId, Long reservationId) {
        ReservationResult result = reservationQueryService.findDetail(memberId, reservationId);
        return ReservationCompleteDetailResponse.from(result, fileService.getUrlByPath(result.shopImageUrl()));
    }
}
