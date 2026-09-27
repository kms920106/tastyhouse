package com.tastyhouse.application.shared.error;

public record ErrorDescriptor(int status, String code, String message) {
}
