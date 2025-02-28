package org.example.Entity;

import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
@AllArgsConstructor
public class Dish {
    private String id;
    private String name;
    private double unitPrice;
    private List<DishIngredient> ingredients;

    public Dish() {
        this.ingredients = new ArrayList<>();
    }

    /**
     * Calcule le coût des ingrédients pour une date donnée
     */
    public double calculateIngredientsCost(LocalDateTime date) {
        if (ingredients == null || ingredients.isEmpty()) {
            return 0.0;
        }
        double totalCost = 0.0;
        for (DishIngredient di : ingredients) {
            totalCost += calculateIngredientCost(di, date);
        }
        return totalCost;
    }

    /**
     * Calcule le coût des ingrédients avec les prix les plus récents (pour la date actuelle si null)
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
        System.out.println("Liste des prix disponibles :");
        for (Price p : ingredient.getPrices()) {
            System.out.println(p.getDatePriceExpend() + " -> " + p.getValue());
        }

        if (ingredient.getPrices() == null || ingredient.getPrices().isEmpty()) {
            throw new IllegalStateException("Aucun prix disponible pour " + ingredient.getName());
        }

        if (date != null) {
            Price closestPrice = ingredient.getPrices().stream()
                    .filter(p -> !p.getDatePriceExpend().toLocalDate().isAfter(date.toLocalDate()))
                    .max(Comparator.comparing(Price::getDatePriceExpend))
                    .orElseThrow(() -> new IllegalStateException("Aucun prix trouvé avant ou à la date " + date));

            System.out.println("Prix le plus proche avant ou à la date : " + closestPrice.getValue());
            return closestPrice;
        } else {
            // Si aucune date n'est fournie, retourner le prix le plus récent
            return ingredient.getPrices().stream()
                    .max(Comparator.comparing(Price::getDatePriceExpend))
                    .orElseThrow(() -> new IllegalStateException("Aucun prix trouvé pour " + ingredient.getName()));
        }
    }

    private void validateUnits(DishIngredient dishIngredient, Price price) {
        if (!dishIngredient.getUnit().equals(price.getUnit())) {
            throw new IllegalStateException("Unités incompatibles pour " + dishIngredient.getIngredient().getName());
        }
    }

    private double calculateCost(double quantity, Price price) {
        return price.getValue() * quantity;
    }

    public double getGrossMargin() {
        double totalCost = calculateIngredientsCost();
        return unitPrice - totalCost;
    }

    public void addIngredient(DishIngredient ingredient) {
        this.ingredients.add(ingredient);
    }
}
