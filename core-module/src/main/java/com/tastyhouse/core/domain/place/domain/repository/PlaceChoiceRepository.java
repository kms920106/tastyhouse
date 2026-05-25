package com.tastyhouse.core.domain.place.domain.repository;

import com.tastyhouse.core.domain.place.application.dto.result.EditorChoiceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PlaceChoiceRepository {

    List<EditorChoiceDto> findEditorChoice();

    Page<EditorChoiceDto> findEditorChoice(Pageable pageable);
}
