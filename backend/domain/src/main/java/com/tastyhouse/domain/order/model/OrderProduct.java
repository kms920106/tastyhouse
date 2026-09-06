package com.tastyhouse.domain.order.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;

public class OrderProduct {
    private final Long id;
    private final OrderId orderId;
    private final ProductId productId;
    private final String name;

    private final String priceName;
    private final UploadedFileId imageFileId;
    private final Integer quantity;
    private final Integer originalPrice;
    private final Integer discountPrice;
    private Integer totalOptionPrice;
    private Integer totalPrice;

    private Integer cupDepositAmount;

    private OrderProduct(
        Long id,
        OrderId orderId,
        ProductId productId,
        String name,
        String priceName,
        UploadedFileId imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.name = name;
        this.priceName = priceName;
        this.imageFileId = imageFileId;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
        this.cupDepositAmount = cupDepositAmount != null ? cupDepositAmount : 0;
    }

    public static OrderProduct of(
        OrderId orderId,
        ProductId productId,
        String name,
        String priceName,
        UploadedFileId imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        return new OrderProduct(
            null,
            orderId,
            productId,
            name,
            priceName,
            imageFileId,
            quantity != null ? quantity : 1,
            originalPrice != null ? originalPrice : 0,
            discountPrice,
            totalOptionPrice != null ? totalOptionPrice : 0,
            totalPrice != null ? totalPrice : 0,
            cupDepositAmount != null ? cupDepositAmount : 0
        );
    }

    public static OrderProduct reconstitute(
        Long id,
        OrderId orderId,
        ProductId productId,
        String name,
        String priceName,
        UploadedFileId imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        return new OrderProduct(
            id,
            orderId,
            productId,
            name,
            priceName,
            imageFileId,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            cupDepositAmount
        );
    }

    public void updatePrices(Integer totalOptionPrice, Integer totalPrice, Integer cupDepositAmount) {
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
        this.cupDepositAmount = cupDepositAmount != null ? cupDepositAmount : 0;
    }

    public Integer getCupDepositAmount() {
        return this.cupDepositAmount;
    }

    public Long getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public String getName() {
        return this.name;
    }

    public String getPriceName() {
        return this.priceName;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public Integer getOriginalPrice() {
        return this.originalPrice;
    }

    public Integer getDiscountPrice() {
        return this.discountPrice;
    }

    public Integer getTotalOptionPrice() {
        return this.totalOptionPrice;
    }

    public Integer getTotalPrice() {
        return this.totalPrice;
    }

    public OrderProductId getOrderProductId() {
        return OrderProductId.of(this.id);
    }
}
