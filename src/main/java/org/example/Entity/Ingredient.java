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

    // Méthode pour ajouter un prix à la liste
    public void addPrice(Price price) {
        this.prices.add(price);
    }

    // Méthode pour ajouter un ingrédient à la liste
    public void addDishIngredient(DishIngredient dishIngredient) {
        this.dishIngredients.add(dishIngredient);
    }
}
