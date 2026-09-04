package com.tastyhouse.ceoapi.ceo.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.ceoapi.ceo.adapter.in.web.request.CeoReplyPhraseCreateRequest;
import com.tastyhouse.ceoapi.ceo.adapter.in.web.response.CeoReplyPhraseResponse;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCommandUseCase;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseCreateCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseDeleteCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseUpdateCommand;
import com.tastyhouse.application.ceo.port.in.CeoReplyPhraseQueryUseCase;
import com.tastyhouse.application.auth.security.CeoUserDetails;

/**
 * 자주 쓰는 문구 API.
 *
 * <p>가게 식별자를 받지 않는다 — 문구는 점주 계정 단위라 가게에 종속되지 않고, 인가는 토큰의
 * {@code ceoId}와 문구의 {@code ceoId} 일치로 수행한다.
 */
@Tag(name = "Ceo Reply Phrase", description = "점주 자주 쓰는 문구 API")
@RestController
@RequestMapping("/api/ceos")
public class CeoReplyPhraseApiController {

    private final CeoReplyPhraseCommandUseCase ceoReplyPhraseCommandUseCase;
    private final CeoReplyPhraseQueryUseCase ceoReplyPhraseQueryService;

    public CeoReplyPhraseApiController(
        CeoReplyPhraseCommandUseCase ceoReplyPhraseCommandUseCase,
        CeoReplyPhraseQueryUseCase ceoReplyPhraseQueryService
    ) {
        this.ceoReplyPhraseCommandUseCase = ceoReplyPhraseCommandUseCase;
        this.ceoReplyPhraseQueryService = ceoReplyPhraseQueryService;
    }

    @Operation(
        summary = "자주 쓰는 문구 목록 조회",
        description = "로그인한 점주 본인이 등록한 자주 쓰는 문구를 정렬 순서대로 조회합니다. "
            + "최대 5건이라 페이징하지 않습니다. 문구 이름을 입력하지 않은 문구는 표시명으로 내용 앞부분이 내려갑니다."
    )
    @GetMapping("/v1/reply-phrases")
    public ResponseEntity<ApiResponse<List<CeoReplyPhraseResponse>>> getReplyPhrases(
        @AuthenticationPrincipal CeoUserDetails userDetails
    ) {
        List<CeoReplyPhraseResponse> response =
            ceoReplyPhraseQueryService.getReplyPhrases(userDetails.getCeoId()).stream()
                .map(CeoReplyPhraseResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(
        summary = "자주 쓰는 문구 등록",
        description = "자주 쓰는 문구를 등록합니다. 최대 5개까지 등록할 수 있으며, 이미 5개면 409를 반환합니다. "
            + "정렬 순서는 서버가 채우므로 보내지 않습니다. 금칙어가 포함되면 저장되지 않습니다."
    )
    @PostMapping("/v1/reply-phrases")
    public ResponseEntity<ApiResponse<Long>> createReplyPhrase(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @Valid @RequestBody CeoReplyPhraseCreateRequest request
    ) {
        CeoReplyPhraseCreateCommand command = request.toCommand(userDetails.getCeoId());
        Long replyPhraseId = ceoReplyPhraseCommandUseCase.register(command);
        return ResponseEntity.ok(ApiResponse.success(replyPhraseId));
    }

    @Operation(
        summary = "자주 쓰는 문구 수정",
        description = "등록한 자주 쓰는 문구의 이름과 내용을 수정합니다. 문구가 없으면 404, 다른 점주의 문구면 403을 반환합니다."
    )
    @PutMapping("/v1/reply-phrases/{id}")
    public ResponseEntity<ApiResponse<Void>> updateReplyPhrase(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id,
        @Valid @RequestBody CeoReplyPhraseCreateRequest request
    ) {
        CeoReplyPhraseUpdateCommand command = request.toCommand(userDetails.getCeoId(), id);
        ceoReplyPhraseCommandUseCase.modify(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
        summary = "자주 쓰는 문구 삭제",
        description = "등록한 자주 쓰는 문구를 삭제합니다. 삭제해도 남은 문구의 정렬 순서는 다시 매기지 않습니다."
    )
    @DeleteMapping("/v1/reply-phrases/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReplyPhrase(
        @AuthenticationPrincipal CeoUserDetails userDetails,
        @PathVariable Long id
    ) {
        CeoReplyPhraseDeleteCommand command = CeoReplyPhraseDeleteCommand.of(userDetails.getCeoId(), id);
        ceoReplyPhraseCommandUseCase.remove(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
