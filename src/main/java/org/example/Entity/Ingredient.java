package org.example.Entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class Ingredient {
    private String idIngredient;
    private String name;
    private LocalDateTime updateDateTime;
    private List<Price> prices;
    private List<DishIngredient> dishIngredients;

    public Ingredient() {
        this.prices = new ArrayList<>();
        this.dishIngredients = new ArrayList<>();
    }

    public void addPrice(Price price) {
        this.prices.add(price);
    }

    public void addDishIngredient(DishIngredient dishIngredient) {
        this.dishIngredients.add(dishIngredient);
    }
}
