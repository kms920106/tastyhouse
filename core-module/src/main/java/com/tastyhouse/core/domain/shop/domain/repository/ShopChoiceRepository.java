package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.shop.application.dto.result.EditorChoiceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ShopChoiceRepository {

    List<EditorChoiceDto> findEditorChoice();

    Page<EditorChoiceDto> findEditorChoice(Pageable pageable);
}
