package org.example.DAO;

import org.example.DataSource.ConnectionJDBC;
import org.example.Entity.Ingredient;
import org.example.Entity.Price;
import org.example.Entity.Unit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAOImpl {
    private final ConnectionJDBC connectionJDBC = new ConnectionJDBC();

    public Ingredient getIngredientById(Long id) {
        String sql = "SELECT i.id_ingredient, i.name, i.update_datetime, p.value as costIngredient, " +
                "p.unit as price_unit, p.date_price_expend, p.quantity as price_quantity " +
                "FROM Ingredient i " +
                "JOIN Price p ON i.id_ingredient = p.id_ingredient_Ingredient " +
                "WHERE i.id_ingredient = ? " +
                "ORDER BY p.date_price_expend DESC LIMIT 1";

        try (Connection connection = connectionJDBC.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Récupération des données de l'ingrédient
                    String ingredientId = rs.getString("id_ingredient");
                    String name = rs.getString("name");
                    Timestamp ts = rs.getTimestamp("update_datetime");
                    LocalDateTime updateDatetime = ts != null ? ts.toLocalDateTime() : null;

                    // Création de l'ingrédient d'abord (cela initialise les tableaux vides)
                    Ingredient ingredient = new Ingredient();
                    ingredient.setIdIngredient(ingredientId);
                    ingredient.setName(name);
                    ingredient.setUpdateDateTime(updateDatetime);

                    // Validation et ajout du prix
                    double costIngredient = rs.getDouble("costIngredient");
                    String priceUnitString = rs.getString("price_unit");

                    Unit priceUnit = validateUnit(priceUnitString);
                    if (priceUnit == null) {
                        System.out.println("Erreur : L'unité '" + priceUnitString + "' n'est pas valide.");
                        return null;
                    }

                    LocalDateTime priceExpendDate = rs.getTimestamp("date_price_expend") != null ?
                            rs.getTimestamp("date_price_expend").toLocalDateTime() : null;
                    double priceQuantity = rs.getDouble("price_quantity");

                    Price price = new Price(costIngredient, priceUnit, priceExpendDate, priceQuantity, ingredient);
                    ingredient.addPrice(price);

                    return ingredient;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Ingredient> getAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT i.id_ingredient, i.name, i.update_datetime, p.value as costIngredient, " +
                "p.unit as price_unit, p.date_price_expend, p.quantity as price_quantity " +
                "FROM Ingredient i " +
                "JOIN Price p ON i.id_ingredient = p.id_ingredient_Ingredient " +
                "ORDER BY i.id_ingredient";

        try (Connection connection = connectionJDBC.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Création de l'ingrédient d'abord
                Ingredient ingredient = new Ingredient();
                ingredient.setIdIngredient(rs.getString("id_ingredient"));
                ingredient.setName(rs.getString("name"));

                Timestamp ts = rs.getTimestamp("update_datetime");
                ingredient.setUpdateDateTime(ts != null ? ts.toLocalDateTime() : null);

                // Validation et ajout du prix
                String priceUnitString = rs.getString("price_unit");
                Unit priceUnit = validateUnit(priceUnitString);
                if (priceUnit == null) {
                    System.out.println("Erreur : L'unité '" + priceUnitString + "' n'est pas valide.");
                    continue;
                }

                Price price = new Price(
                        rs.getDouble("costIngredient"),
                        priceUnit,
                        rs.getTimestamp("date_price_expend") != null ?
                                rs.getTimestamp("date_price_expend").toLocalDateTime() : null,
                        rs.getDouble("price_quantity"),
                        ingredient
                );

                ingredient.addPrice(price);
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    private Unit validateUnit(String unitString) {
        try {
            return Unit.valueOf(unitString.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}