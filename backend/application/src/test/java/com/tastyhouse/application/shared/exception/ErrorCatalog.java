package com.tastyhouse.application.shared.exception;

import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.ErrorCodeSpec;
import com.tastyhouse.apicommon.exception.ApiErrorCode;

final class ErrorCatalog {

    record Entry(String catalog, String name, int status, String code, String message) {
    }

    private ErrorCatalog() {
    }

    static List<Entry> all() {
        List<Entry> entries = new ArrayList<>();
        addSpecs(entries, DomainErrorCode.class);
        addSpecs(entries, ApplicationErrorCode.class);
        addSpecs(entries, WebErrorCode.class);
        addSpecs(entries, AdminErrorCode.class);
        addSpecs(entries, CeoErrorCode.class);
        addSpecs(entries, BatchErrorCode.class);
        for (ApiErrorCode code : ApiErrorCode.values()) {
            entries.add(new Entry(
                ApiErrorCode.class.getSimpleName(),
                code.name(),
                code.getHttpStatusCode(),
                code.getCode(),
                code.getDefaultMessage()
            ));
        }
        return entries;
    }

    private static <E extends Enum<E> & ErrorCodeSpec> void addSpecs(List<Entry> entries, Class<E> type) {
        for (E code : type.getEnumConstants()) {
            entries.add(new Entry(
                type.getSimpleName(),
                code.name(),
                code.getHttpStatusCode(),
                code.getCode(),
                code.getDefaultMessage()
            ));
        }
    }
}
