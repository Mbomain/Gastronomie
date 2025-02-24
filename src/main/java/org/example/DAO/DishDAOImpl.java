package org.example.DAO;

import org.example.DataSource.ConnectionJDBC;
import org.example.Entity.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class DishDAOImpl {
    private static final String SELECT_DISH = """
        SELECT id_dish, name, unit_price FROM Dish WHERE id_dish = ?
    """;

    private static final String SELECT_INGREDIENTS_AND_PRICES = """
        SELECT 
            di.quantity as dish_ingredient_quantity,
            di.unit as dish_ingredient_unit,
            i.id_ingredient, i.name as ingredient_name, i.update_datetime,
            p.value, p.unit as price_unit, p.date_price_expend, p.quantity as price_quantity
        FROM Dish_Ingredient di
        JOIN Ingredient i ON di.id_ingredient_Ingredient = i.id_ingredient
        LEFT JOIN Price p ON i.id_ingredient = p.id_ingredient_Ingredient
        WHERE di.id_dish_Dish = ?
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
            if (dish != null) dish.setIngredients(findIngredientsByDishId(connection, dishId));
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
                            new HashSet<>()
                    );
                }
            }
        }
        return null;
    }

    private Set<DishIngredient> findIngredientsByDishId(Connection connection, String dishId) throws SQLException {
        Set<DishIngredient> dishIngredients = new HashSet<>();
        Dish dish = findBasicById(connection, dishId);

        try (PreparedStatement ps = connection.prepareStatement(SELECT_INGREDIENTS_AND_PRICES)) {
            ps.setObject(1, dishId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setIdIngredient(rs.getString("id_ingredient"));
                    ingredient.setName(rs.getString("ingredient_name"));
                    ingredient.setUpdateDateTime(getLocalDateTime(rs, "update_datetime"));

                    if (rs.getObject("value") != null) {
                        Price price = new Price(
                                rs.getDouble("value"),
                                Unit.valueOf(rs.getString("price_unit").trim()),
                                getLocalDateTime(rs, "date_price_expend"),
                                rs.getDouble("price_quantity"),
                                ingredient
                        );
                        ingredient.getPrices().add(price);
                    }

                    dishIngredients.add(new DishIngredient(
                            dish,
                            ingredient,
                            rs.getDouble("dish_ingredient_quantity"),
                            Unit.valueOf(rs.getString("dish_ingredient_unit").trim())
                    ));
                }
            }
        }
        return dishIngredients;
    }

    private LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return ts != null ? ts.toLocalDateTime() : null;
    }


}