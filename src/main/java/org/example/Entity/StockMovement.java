package org.example.Entity;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class StockMovement {
    private String idStockMovement;
    private String idIngredient;
    private Double quantity;
    private Unit unit;
    private String movementType; // 'IN' ou 'OUT'
    private LocalDateTime movementDate;
}
