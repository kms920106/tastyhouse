package com.tastyhouse.webapi.member.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressItemResult;
import com.tastyhouse.application.member.port.out.MemberDeliveryAddressQueryPort;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberDeliveryAddressItemResponse;
import com.tastyhouse.webapi.member.application.port.in.MemberDeliveryAddressQueryUseCase;

/**
 * 회원 배달 주소록 조회 서비스.
 *
 * <p>표현용 조회이므로 write 포트가 아니라 읽기 포트({@link MemberDeliveryAddressQueryPort})로
 * 투영한다 — 행정동명 조인이 필요한데 도메인 모델에는 그 값이 없다.
 *
 * <p>CQRS 교차 주입 금지 — 이 서비스는 write 포트를 주입하지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class MemberDeliveryAddressQueryService implements MemberDeliveryAddressQueryUseCase {

    private final MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort;

    public MemberDeliveryAddressQueryService(MemberDeliveryAddressQueryPort memberDeliveryAddressQueryPort) {
        this.memberDeliveryAddressQueryPort = memberDeliveryAddressQueryPort;
    }

    @Override
    public List<MemberDeliveryAddressItemResponse> getMyDeliveryAddresses(Long memberId) {
        return memberDeliveryAddressQueryPort.findByMemberId(MemberId.of(memberId))
            .stream()
            .map(this::toMemberDeliveryAddressItemResponse)
            .toList();
    }

    /** Result를 원시타입 낱개로 언패킹해 Response에 넘긴다(Response는 domain-free·infra-free). */
    private MemberDeliveryAddressItemResponse toMemberDeliveryAddressItemResponse(MemberDeliveryAddressItemResult result) {
        return MemberDeliveryAddressItemResponse.from(
            result.id(),
            result.alias(),
            result.roadAddress(),
            result.lotAddress(),
            result.detailAddress(),
            result.adminDongId(),
            result.regionName(),
            result.latitude(),
            result.longitude(),
            result.defaultAddress()
        );
    }
}
