package org.example.DAO;

import org.example.DataSource.ConnectionJDBC;
import org.example.Entity.Ingredient;
import org.example.Entity.Price;
import org.example.Entity.Unit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IngredientDAOImpl {
    private final ConnectionJDBC connectionJDBC = new ConnectionJDBC();

    /**
     * Récupère un ingrédient par son identifiant.
     * On effectue un JOIN avec Price pour récupérer le coût associé.
     *
     * @param id l'identifiant de l'ingrédient (sous forme de Long)
     * @return l'objet Ingredient ou null s'il n'est pas trouvé.
     */
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

                    // Récupération des prix associés à l'ingrédient
                    Set<Price> prices = new HashSet<>();
                    double costIngredient = rs.getDouble("costIngredient");
                    String priceUnitString = rs.getString("price_unit");

                    // Validation de l'unité
                    Unit priceUnit = validateUnit(priceUnitString);
                    if (priceUnit == null) {
                        System.out.println("Erreur : L'unité '" + priceUnitString + "' n'est pas valide.");
                        return null;  // Retourne null si l'unité est invalide
                    }

                    LocalDateTime priceExpendDate = rs.getTimestamp("date_price_expend") != null ?
                            rs.getTimestamp("date_price_expend").toLocalDateTime() : null;
                    double priceQuantity = rs.getDouble("price_quantity");

                    prices.add(new Price(costIngredient, priceUnit, priceExpendDate, priceQuantity, null)); // Ingredient n'est pas encore défini ici

                    // Création de l'ingrédient avec les prix associés
                    Ingredient ingredient = new Ingredient(ingredientId, name, updateDatetime, prices, new HashSet<>());
                    return ingredient;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupère la liste de tous les ingrédients.
     *
     * @return la liste d'ingrédients.
     */
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
                // Récupération des données de l'ingrédient
                String ingredientId = rs.getString("id_ingredient");
                String name = rs.getString("name");
                Timestamp ts = rs.getTimestamp("update_datetime");
                LocalDateTime updateDatetime = ts != null ? ts.toLocalDateTime() : null;

                // Récupération des prix associés à l'ingrédient
                Set<Price> prices = new HashSet<>();
                double costIngredient = rs.getDouble("costIngredient");
                String priceUnitString = rs.getString("price_unit");

                // Validation de l'unité
                Unit priceUnit = validateUnit(priceUnitString);
                if (priceUnit == null) {
                    System.out.println("Erreur : L'unité '" + priceUnitString + "' n'est pas valide.");
                    continue;  // Passer à l'ingrédient suivant si l'unité est invalide
                }

                LocalDateTime priceExpendDate = rs.getTimestamp("date_price_expend") != null ?
                        rs.getTimestamp("date_price_expend").toLocalDateTime() : null;
                double priceQuantity = rs.getDouble("price_quantity");

                prices.add(new Price(costIngredient, priceUnit, priceExpendDate, priceQuantity, null));

                // Création de l'ingrédient avec les prix associés
                Ingredient ingredient = new Ingredient(ingredientId, name, updateDatetime, prices, new HashSet<>());
                ingredients.add(ingredient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    /**
     * Valide si l'unité est valide.
     * Si l'unité n'est pas valide, renvoie null.
     *
     * @param unitString l'unité à valider.
     * @return l'unité valide ou null si l'unité n'est pas valide.
     */
    private Unit validateUnit(String unitString) {
        try {
            return Unit.valueOf(unitString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}