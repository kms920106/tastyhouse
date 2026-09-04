package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 점주가 입력하는 메뉴명의 중복·특수문자를 검증하는 검증기.
 *
 * <p><b>도메인 모델이 아니라 ceo-api에 두는 이유</b>: 특수문자 화이트리스트는 애그리거트 불변식이 아니라
 * <b>점주 입력 경로 한정 정책</b>이다. admin-api의 상품 CRUD와 BBQ 크롤링 배치도 같은 {@code Product}를
 * 만들지만 그쪽 이름은 외부에서 들어오므로 이 화이트리스트를 만족한다는 보장이 없다 —
 * {@code Product.of(...)}에 넣으면 기존 등록 경로가 조용히 깨진다. 소유권 검증기
 * ({@code ShopOwnershipValidator})와 같은 자리·같은 성격의 ceo 전용 인가/입력 정책이다.
 *
 * <p>중복 검사는 write 포트({@link ProductRepository})를 쓴다 — 저장 직전 command 경로의 선행 조건이라
 * 표현용 투영만 제공하는 query DAO로는 대체할 수 없다.
 */
@Component
@CeoApp
public class ProductNameValidator {

    /**
     * 메뉴명 허용 문자 — 한글(음절·자모)·영숫자·공백에 더해 특수문자 {@code : , . / ~ % & ( ) + [ ] ™ ®}만
     * 허용한다. 화이트리스트로 두는 이유는 블랙리스트가 이모지·전각기호 같은 신규 유입을 놓치기 때문이다.
     */
    private static final Pattern ALLOWED_NAME =
        Pattern.compile("^[가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9\\s:,./~%&()+\\[\\]™®]*$");

    private final ProductRepository productRepository;

    public ProductNameValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** 신규 등록용 — 가게 안에서 이름이 유일해야 한다. */
    public void validateForCreate(Long shopId, String name) {
        validateCharacters(name);
        if (productRepository.existsByShopIdAndName(ShopId.of(shopId), name)) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }

    /** 변경용 — 자기 자신은 중복 대상에서 제외한다(이름을 그대로 둔 채 다른 필드만 바꾸는 경우). */
    public void validateForUpdate(Long shopId, Long productId, String name) {
        validateCharacters(name);
        if (productRepository.existsByShopIdAndNameAndIdNot(ShopId.of(shopId), name, ProductId.of(productId))) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_DUPLICATED);
        }
    }

    private void validateCharacters(String name) {
        if (name != null && !ALLOWED_NAME.matcher(name).matches()) {
            throw new BusinessException(ErrorCode.PRODUCT_NAME_INVALID_CHARACTER);
        }
    }
}
