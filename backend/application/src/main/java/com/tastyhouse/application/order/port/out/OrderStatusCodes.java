package com.tastyhouse.application.order.port.out;

public final class OrderStatusCodes {
    public static final String PENDING = "PENDING";
    public static final String CONFIRMED = "CONFIRMED";
    public static final String PREPARING = "PREPARING";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";

    private OrderStatusCodes() {
    }
}
