package com.shopeasy.model;

import java.math.BigDecimal;

public record Product(
    long id,
    String name,
    String category,
    String description,
    BigDecimal price,
    int stockQuantity
) {}