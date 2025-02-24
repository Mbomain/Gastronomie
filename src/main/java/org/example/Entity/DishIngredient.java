package org.example.Entity;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class DishIngredient {
    private Dish dish;
    private Ingredient ingredient;
    private double quantity;
    private Unit unit;

}
