package org.example.Entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Price {
    private double value;
    private Unit unit;
    private LocalDateTime datePriceExpend;
    private double quantity;
    private Ingredient ingredient;
}
