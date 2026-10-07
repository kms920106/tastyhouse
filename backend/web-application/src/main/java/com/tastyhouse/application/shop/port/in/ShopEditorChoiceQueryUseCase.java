package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.EditorChoiceResult;

public interface ShopEditorChoiceQueryUseCase {

    List<EditorChoiceResult> searchEditorChoices(int page, int size);
}
