import org.example.DAO.DishDAOImpl;
import org.example.Entity.Dish;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DishDAOImplTest {

    private DishDAOImpl dishDAO;

    @BeforeEach
    void setUp() {
        dishDAO = new DishDAOImpl();
    }

    @Test
    void calculateIngredientsCostTest() {
        Dish hotDog = dishDAO.findById("HD1");

        // Date de test: 1er février 2025
        LocalDateTime testDate = LocalDateTime.of(2025, 2, 1, 0, 0, 0);

        // Calcul du coût des ingrédients pour la date donnée
        double actualCost = hotDog.calculateIngredientsCost(testDate);
        System.out.println("Coût des ingrédients pour la date " + testDate + " : " + actualCost);

        // Le coût attendu en fonction des prix définis dans les inserts SQL
        double expectedCost = 5500.0;  // Valeur attendue, basée sur le calcul de prix

        // Assertion pour vérifier que le coût réel correspond au coût attendu
        assertEquals(expectedCost, actualCost, 0.01);
    }

    @Test
    void calculateIngredientsCostWithDifferentDateTest() {
        Dish hotDog = dishDAO.findById("HD1");

        // Date de test: 1er jUIN 2009
        LocalDateTime testDate = LocalDateTime.of(2025, 6, 1, 0, 0, 0);

        // Calcul du coût des ingrédients pour la date donnée
        double actualCost = hotDog.calculateIngredientsCost(testDate);
        System.out.println("Coût des ingrédients pour la date " + testDate + " : " + actualCost);

        double expectedCost = 5500;

        // Assertion pour vérifier que le coût réel correspond au coût attendu
        assertEquals(expectedCost, actualCost, 0.01);
    }

    @Test
    void calculateGrossMargin () {
        Dish hotDog = dishDAO.findById("HD1");

        // Calcul du coût des ingrédients pour la date donnée
        double actualCost = hotDog.getGrossMargin();
        System.out.println(actualCost);
        // Le coût attendu pour cette date (1er janvier 2025)
        double expectedCost = 9500.0;

        // Assertion pour vérifier que le coût réel correspond au coût attendu
        assertEquals(expectedCost, actualCost, 0.01);
    }

}
