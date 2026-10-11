package com.tastyhouse.application.event.port.out;

public record EventSearchCondition(
    String name,
    String status
) {

    public static EventSearchCondition of(String name, String status) {
        return new EventSearchCondition(name, status);
    }
}
