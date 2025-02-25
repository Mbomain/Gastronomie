package org.example.DAO;

import org.example.DataSource.ConnectionJDBC;
import org.example.Entity.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class DishDAOImpl {
    private static final String SELECT_DISH = """
        SELECT id_dish, name, unit_price FROM Dish WHERE id_dish = ?
    """;

    private static final String SELECT_DISH_INGREDIENTS = """
        SELECT 
            di.quantity as dish_ingredient_quantity,
            di.unit as dish_ingredient_unit,
            i.id_ingredient, 
            i.name as ingredient_name, 
            i.update_datetime
        FROM Dish_Ingredient di
        JOIN Ingredient i ON di.id_ingredient_Ingredient = i.id_ingredient
        WHERE di.id_dish_Dish = ? 
    """;

    private static final String SELECT_INGREDIENT_PRICES = """
        SELECT 
            value, 
            unit as price_unit, 
            date_price_expend, 
            quantity as price_quantity
        FROM Price 
        WHERE id_ingredient_Ingredient = ?
    """;

    private final ConnectionJDBC connectionJDBC;

    public DishDAOImpl(ConnectionJDBC connectionJDBC) {
        this.connectionJDBC = connectionJDBC;
    }

    public DishDAOImpl() {
        this.connectionJDBC = new ConnectionJDBC();
    }

    public Dish findById(String dishId) {
        try (Connection connection = connectionJDBC.getConnection()) {
            Dish dish = findBasicById(connection, dishId);
            if (dish != null) {
                dish.setIngredients(findIngredientsByDishId(connection, dishId));
            }
            return dish;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors du chargement du plat: " + dishId, e);
        }
    }

    private Dish findBasicById(Connection connection, String dishId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_DISH)) {
            ps.setObject(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Dish(
                            String.valueOf(rs.getObject("id_dish")),
                            rs.getString("name"),
                            rs.getDouble("unit_price"),
                            new ArrayList<>()
                    );
                }
            }
        }
        return null;
    }

    private ArrayList<DishIngredient> findIngredientsByDishId(Connection connection, String dishId) throws SQLException {
        Map<String, DishIngredient> dishIngredientsMap = new HashMap<>();
        Dish dish = findBasicById(connection, dishId);

        try (PreparedStatement ps = connection.prepareStatement(SELECT_DISH_INGREDIENTS)) {
            ps.setObject(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ingredientId = rs.getString("id_ingredient");

                    Ingredient ingredient = new Ingredient();
                    ingredient.setIdIngredient(ingredientId);
                    ingredient.setName(rs.getString("ingredient_name"));
                    ingredient.setUpdateDateTime(getLocalDateTime(rs, "update_datetime"));
                    ingredient.setPrices(new ArrayList<>());

                    DishIngredient dishIngredient = new DishIngredient(
                            dish,
                            ingredient,
                            rs.getDouble("dish_ingredient_quantity"),
                            Unit.valueOf(rs.getString("dish_ingredient_unit").trim())
                    );

                    dishIngredientsMap.put(ingredientId, dishIngredient);
                }
            }
        }

        for (DishIngredient dishIngredient : dishIngredientsMap.values()) {
            loadPricesForIngredient(connection, dishIngredient.getIngredient());
        }

        return new ArrayList<>(dishIngredientsMap.values());
    }

    private void loadPricesForIngredient(Connection connection, Ingredient ingredient) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_INGREDIENT_PRICES)) {
            ps.setString(1, ingredient.getIdIngredient());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Price price = new Price(
                            rs.getDouble("value"),
                            Unit.valueOf(rs.getString("price_unit").trim()),
                            getLocalDateTime(rs, "date_price_expend"),
                            rs.getDouble("price_quantity"),
                            ingredient
                    );
                    ingredient.addPrice(price);
                }
            }
        }
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }
}
