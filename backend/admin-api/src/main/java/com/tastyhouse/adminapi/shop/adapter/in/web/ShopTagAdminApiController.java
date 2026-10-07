package com.tastyhouse.adminapi.shop.adapter.in.web;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tastyhouse.application.shop.port.in.ShopTagManagementQueryUseCase;
import com.tastyhouse.application.shop.port.in.TagCreateCommand;
import com.tastyhouse.application.shop.port.in.TagCreateUseCase;
import com.tastyhouse.application.shop.port.in.TagDeleteCommand;
import com.tastyhouse.application.shop.port.in.TagDeleteUseCase;
import com.tastyhouse.apicommon.common.ApiResponse;
import com.tastyhouse.adminapi.shop.adapter.in.web.request.TagCreateRequest;
import com.tastyhouse.adminapi.shop.adapter.in.web.response.TagResponse;

@Tag(name = "Shop Tag Admin", description = "가게 태그 관리자 API")
@RestController
@RequestMapping("/api/shops")
class ShopTagAdminApiController {

    private final TagCreateUseCase tagCreateUseCase;
    private final TagDeleteUseCase tagDeleteUseCase;
    private final ShopTagManagementQueryUseCase shopTagManagementQueryUseCase;

    public ShopTagAdminApiController(
        TagCreateUseCase tagCreateUseCase,
        TagDeleteUseCase tagDeleteUseCase,
        ShopTagManagementQueryUseCase shopTagManagementQueryUseCase
    ) {
        this.tagCreateUseCase = tagCreateUseCase;
        this.tagDeleteUseCase = tagDeleteUseCase;
        this.shopTagManagementQueryUseCase = shopTagManagementQueryUseCase;
    }

    @Operation(summary = "태그 목록 조회", description = "태그 목록을 조회합니다.")
    @GetMapping("/v1/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        List<TagResponse> response = shopTagManagementQueryUseCase.getTags().stream()
            .map(TagResponse::from)
            .toList();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "태그 등록", description = "새로운 태그를 등록합니다.")
    @PostMapping("/v1/tags")
    public ResponseEntity<ApiResponse<Long>> createTag(@Valid @RequestBody TagCreateRequest request) {
        TagCreateCommand command = request.toCommand();
        Long id = tagCreateUseCase.createTag(command);
        return ResponseEntity.ok(ApiResponse.success(id));
    }

    @Operation(summary = "태그 삭제", description = "등록된 태그를 삭제합니다.")
    @DeleteMapping("/v1/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        TagDeleteCommand command = TagDeleteCommand.of(tagId);
        tagDeleteUseCase.deleteTag(command);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
