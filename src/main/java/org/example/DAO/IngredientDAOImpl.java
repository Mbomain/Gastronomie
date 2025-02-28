package org.example.DAO;

import org.example.DataSource.ConnectionJDBC;
import org.example.Entity.Ingredient;
import org.example.Entity.Price;
import org.example.Entity.StockMovement;
import org.example.Entity.Unit;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAOImpl {
    private final ConnectionJDBC connectionJDBC = new ConnectionJDBC();

    public Ingredient findIngredientById(String id) {
        String sql = "SELECT i.id_ingredient, i.name, i.update_datetime, p.value as costIngredient, " +
                "p.unit as price_unit, p.date_price_expend, p.quantity as price_quantity " +
                "FROM Ingredient i " +
                "JOIN Price p ON i.id_ingredient = p.id_ingredient_Ingredient " +
                "WHERE i.id_ingredient = ? " +
                "ORDER BY p.date_price_expend DESC LIMIT 1";

        try (Connection connection = connectionJDBC.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String ingredientId = rs.getString("id_ingredient");
                    String name = rs.getString("name");
                    Timestamp ts = rs.getTimestamp("update_datetime");
                    LocalDateTime updateDatetime = ts != null ? ts.toLocalDateTime() : null;

                    Ingredient ingredient = new Ingredient();
                    ingredient.setIdIngredient(ingredientId);
                    ingredient.setName(name);
                    ingredient.setUpdateDateTime(updateDatetime);

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

    // Récupérer tous les ingrédients
    public List<Ingredient> findAllIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT i.id_ingredient, i.name, i.update_datetime, " +
                "p.value as costIngredient, p.unit as price_unit, " +
                "p.date_price_expend, p.quantity as price_quantity " +
                "FROM Ingredient i " +
                "LEFT JOIN Price p ON i.id_ingredient = p.id_ingredient_Ingredient " +
                "ORDER BY i.id_ingredient, p.date_price_expend DESC";  // Tri par date de prix décroissante

        try (Connection connection = connectionJDBC.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String currentIngredientId = null;
            Ingredient currentIngredient = null;

            while (rs.next()) {
                String ingredientId = rs.getString("id_ingredient");

                if (!ingredientId.equals(currentIngredientId)) {
                    if (currentIngredient != null) {
                        ingredients.add(currentIngredient);
                    }

                    currentIngredient = new Ingredient();
                    currentIngredient.setIdIngredient(ingredientId);
                    currentIngredient.setName(rs.getString("name"));

                    Timestamp ts = rs.getTimestamp("update_datetime");
                    currentIngredient.setUpdateDateTime(ts != null ? ts.toLocalDateTime() : null);

                    currentIngredientId = ingredientId;
                }

                if (rs.getObject("costIngredient") != null) {
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
                            currentIngredient
                    );

                    currentIngredient.addPrice(price);
                }
            }

            // Ajouter le dernier ingrédient à la liste
            if (currentIngredient != null) {
                ingredients.add(currentIngredient);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ingredients;
    }

    public void addStockMovement(StockMovement stockMovement) {
        String sql = "INSERT INTO StockMovement (id_stock_movement, id_ingredient, quantity, unit, movement_type, movement_date) " +
                "VALUES (?, ?, ?, ?::unit_enum, ?, ?)";
        try (Connection connection = connectionJDBC.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, stockMovement.getIdStockMovement());
            ps.setString(2, stockMovement.getIdIngredient());
            ps.setDouble(3, stockMovement.getQuantity());
            ps.setString(4, stockMovement.getUnit().name());
            ps.setString(5, stockMovement.getMovementType());
            ps.setTimestamp(6, Timestamp.valueOf(stockMovement.getMovementDate()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<StockMovement> findStockMovementsByIngredientId(String idIngredient) {
        List<StockMovement> stockMovements = new ArrayList<>();
        String sql = "SELECT id_stock_movement, id_ingredient, quantity, unit, movement_type, movement_date " +
                "FROM StockMovement WHERE id_ingredient = ? ORDER BY movement_date DESC";
        try (Connection connection = connectionJDBC.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, idIngredient);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StockMovement stockMovement = new StockMovement(
                            rs.getString("id_stock_movement"),
                            rs.getString("id_ingredient"),
                            rs.getDouble("quantity"),
                            Unit.valueOf(rs.getString("unit")),
                            rs.getString("movement_type"),
                            rs.getTimestamp("movement_date").toLocalDateTime()
                    );
                    stockMovements.add(stockMovement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockMovements;
    }

    // Calculer la quantité disponible d'un ingrédient à une date donnée
    public double findAvailableQuantity(String idIngredient, LocalDateTime date) {
        String sql = "SELECT SUM(CASE WHEN movement_type = 'IN' THEN quantity ELSE -quantity END) AS available_quantity " +
                "FROM StockMovement " +
                "WHERE id_ingredient = ? AND movement_date <= ?";
        try (Connection connection = connectionJDBC.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, idIngredient);
            ps.setTimestamp(2, Timestamp.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("available_quantity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Unit validateUnit(String unitString) {
        try {
            return Unit.valueOf(unitString.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}