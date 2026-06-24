package com.tastyhouse.core.domain.product.application.dto.command;

public record SaveProductImageCommand(
    Long productId,
    Long imageFileId,
    Integer sort,
    boolean visible
) {}
