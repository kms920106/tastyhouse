package com.tastyhouse.domain.shop.model;

import java.util.Set;

public final class DeliveryTipPolicy {
    public static final int TIER_MAX_COUNT = 3;

    public static final int TIER_TIP_UPPER_BOUND_EXCLUSIVE = 5000;

    public static final int EXTRA_TIP_UPPER_BOUND = 10000;

    public static final Set<Integer> BASE_DISTANCE_OPTIONS = Set.of(1000, 1500, 2000, 2500, 3000);

    private DeliveryTipPolicy() {
    }
}
