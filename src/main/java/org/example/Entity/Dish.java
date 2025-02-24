package org.example.Entity;

import lombok.*;
import java.time.LocalDateTime;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class Dish {
    private String id;
    private String name;
    private double unitPrice;
    private Set<DishIngredient> Ingredients;

    public Dish() {
        this.Ingredients = new HashSet<>();
    }

    /**
     * Calcule le coût des ingrédients pour une date donnée
     */
    public double calculateIngredientsCost(LocalDateTime date) {
        if (Ingredients == null || Ingredients.isEmpty()) {
            return 0.0;
        }
        double totalCost = 0.0;
        for (DishIngredient di : Ingredients) {
            totalCost += calculateIngredientCost(di, date);
        }
        return totalCost;
    }

    /**
     * Calcule le coût avec les prix les plus récents
     */
    public double calculateIngredientsCost() {
        return calculateIngredientsCost(null);
    }


    private double calculateIngredientCost(DishIngredient dishIngredient, LocalDateTime date) {
        Price price = findAppropriatePrice(dishIngredient.getIngredient(), date);
        validateUnits(dishIngredient, price);
        return calculateCost(dishIngredient.getQuantity(), price);
    }

    private Price findAppropriatePrice(Ingredient ingredient, LocalDateTime date) {
        Price appropriatePrice = null;
        LocalDateTime mostRecentDate = null;

        for (Price price : ingredient.getPrices()) {
            if (date == null || !price.getDatePriceExpend().isAfter(date)) {
                if (mostRecentDate == null || price.getDatePriceExpend().isAfter(mostRecentDate)) {
                    mostRecentDate = price.getDatePriceExpend();
                    appropriatePrice = price;
                }
            }
        }
        if (appropriatePrice == null) {
            throw new IllegalStateException("Prix non trouvé pour " + ingredient.getName());
        }
        return appropriatePrice;
    }

    private void validateUnits(DishIngredient dishIngredient, Price price) {
        if (dishIngredient.getUnit() != price.getUnit()) {
            System.out.println("Incompatible units detected for: " + dishIngredient.getIngredient().getName());
            System.out.println("Dish Ingredient Unit: " + dishIngredient.getUnit());
            System.out.println("Price Unit: " + price.getUnit());
            throw new IllegalStateException("Unités incompatibles pour " + dishIngredient.getIngredient().getName());
        }
    }

    private double calculateCost(double quantity, Price price) {
        return price.getValue() * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Dish dish = (Dish) o;
        return Double.compare(unitPrice, dish.unitPrice) == 0 && Objects.equals(id, dish.id) && Objects.equals(name, dish.name) && Objects.equals(Ingredients, dish.Ingredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, unitPrice, Ingredients);
    }
}
