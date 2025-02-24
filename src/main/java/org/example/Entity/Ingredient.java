package org.example.Entity;

import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class Ingredient {
    private String idIngredient;
    private String name;
    private LocalDateTime updateDateTime;
    private Set<Price> prices;
    private Set<DishIngredient> dishIngredients;

    public Ingredient() {
        this.prices = new HashSet<>();
        this.dishIngredients = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return idIngredient == that.idIngredient && Objects.equals(name, that.name) && Objects.equals(updateDateTime, that.updateDateTime) && Objects.equals(prices, that.prices) && Objects.equals(dishIngredients, that.dishIngredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idIngredient, name, updateDateTime, prices, dishIngredients);
    }
}

