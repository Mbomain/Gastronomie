import org.example.DAO.IngredientDAOImpl;
import org.example.Entity.Ingredient;
import org.example.Entity.StockMovement;
import org.example.Entity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class IngredientDAOImplTest {
    private IngredientDAOImpl ingredientDAO;

    @BeforeEach
    void setUp() {
        ingredientDAO = new IngredientDAOImpl();
    }

    // Test pour récupérer un ingrédient par son ID
    @Test
    void testFindIngredientById() {
        Ingredient ingredient = ingredientDAO.findIngredientById("1");
        assertNotNull(ingredient);
        assertEquals("Saucisse", ingredient.getName());
    }

    // Test pour récupérer tous les ingrédients
    @Test
    void testFindAllIngredients() {
        List<Ingredient> ingredients = ingredientDAO.findAllIngredients();
        assertNotNull(ingredients);
        assertEquals(6, ingredients.size());
    }

    // Test pour ajouter un mouvement de stock
    @Test
    void testAddStockMovement() {
        StockMovement stockMovement = new StockMovement(
                "SM10",
                "6",
                5.0,
                Unit.G,
                "OUT",
                LocalDateTime.of(2025, 2, 10, 12, 0)  // Date du mouvement
        );

        // Ajoute le mouvement de stock
        ingredientDAO.addStockMovement(stockMovement);

        // Récupère les mouvements de stock pour l'ingrédient "Riz"
        List<StockMovement> movements = ingredientDAO.findStockMovementsByIngredientId("6");
        assertFalse(movements.isEmpty());

        // Vérifie que le dernier mouvement de stock est celui que nous avons ajouté
        StockMovement lastMovement = movements.get(0);
        assertEquals(5.0, lastMovement.getQuantity(), 0.001);
        assertEquals("OUT", lastMovement.getMovementType());
        assertEquals(Unit.G, lastMovement.getUnit());
    }

    // Test pour récupérer les mouvements de stock d'un ingrédient
    @Test
    void testFindStockMovementsByIngredientId() {
        List<StockMovement> movements = ingredientDAO.findStockMovementsByIngredientId("1");
        assertNotNull(movements);
        assertEquals(1, movements.size());
    }

    // Test pour calculer la quantité disponible d'un ingrédient à une date donnée
    @Test
    void testFindAvailableQuantity() {
        LocalDateTime date = LocalDateTime.of(2025, 2, 24, 23, 59, 59);

        double availableQuantity;

        // Sel (ID '5')
        availableQuantity = ingredientDAO.findAvailableQuantity("5", date);
        assertEquals(1000.0, availableQuantity, 0.001);

        // Riz (ID '6')
        availableQuantity = ingredientDAO.findAvailableQuantity("6", date);
        assertEquals(5000.0, availableQuantity, 0.001);
    }
}