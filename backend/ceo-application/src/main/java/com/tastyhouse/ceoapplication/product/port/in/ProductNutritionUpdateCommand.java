package com.tastyhouse.ceoapplication.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 영양정보 저장 command.
 *
 * <p>같은 타입({@code Integer})의 영양성분 필드가 11개 연달아 있어 위치 기반 조립이
 * 조용히 뒤바뀔 수 있으므로, {@code toCommand}는 반드시 이름 기반 접근자로 조립한다.
 */
public record ProductNutritionUpdateCommand(
    Long ceoId,
    Long shopId,
    Long productId,
    String servingSize,
    String totalAmount,
    String flavor,
    String size,
    Integer calorie,
    Integer sugars,
    Integer protein,
    Integer saturatedFat,
    Integer natrium,
    Integer carbohydrate,
    Integer cholesterol,
    Integer fat,
    Integer transFat,
    Integer caffeine,
    Boolean setMenu,
    List<String> allergens
) {
    public ProductNutritionUpdateCommand {
        if (ceoId == null
            || shopId == null
            || productId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
