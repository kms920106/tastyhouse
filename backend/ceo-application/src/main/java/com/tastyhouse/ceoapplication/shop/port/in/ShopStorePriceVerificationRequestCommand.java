package com.tastyhouse.ceoapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 매장 가격 인증 요청 command.
 *
 * <p>업로드 파일({@code MultipartFile})은 이 command에 담지 않는다 — 서비스 메서드의 <b>별도 파라미터</b>로
 * 유지한다(챕터 02 §6). Command가 서블릿 업로드 타입을 보유하면 application 계층이 web 플럼빙에
 * 결합되고 직렬화·재실행이 불가능해진다.
 *
 * <p>{@code items}는 파싱 전 JSON 배열 <b>문자열</b> 그대로 담는다(경계 타입 {@code String}).
 * 등록이 multipart라 대상 목록을 JSON 바디로 받을 수 없어 문자열 파트로 오는데, 이것을 어디서 푸느냐가
 * 규칙 셋에 걸린다 — 컨트롤러는 {@code controllersShouldBeDomainFree}, Request record는
 * {@code requestResponseRecordsShouldBeDomainAndInfraFree}에 막혀 파싱 실패를
 * {@code BusinessException}으로 번역할 수 없고, 서비스는 {@code ..request..}를 import할 수 없다(§5).
 * 셋을 모두 만족하는 유일한 형태가 <b>문자열을 그대로 넘기고 서비스가 command로 파싱</b>하는 것이다.
 *
 * <p>파싱 실패와 빈 목록이 같은 {@code SHOP_STORE_PRICE_VERIFICATION_TARGET_EMPTY}로 나가던 계약은
 * 그대로 보존된다 — 둘 다 서비스가 던진다.
 */
public record ShopStorePriceVerificationRequestCommand(
    Long ceoId,
    Long shopId,
    String items
) {
    public ShopStorePriceVerificationRequestCommand {
        if (ceoId == null || shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
