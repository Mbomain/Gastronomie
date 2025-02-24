import org.example.DAO.DishDAOImpl;
import org.example.Entity.Dish;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DishDAOImplTest {
    @Test
    void calculateIngredientsCostTest() {
        DishDAOImpl subject = new DishDAOImpl();

        Dish hotDog = subject.findById("HD1");
        // LocalDateTime testDate = LocalDateTime.of();

        double actualCost = hotDog.calculateIngredientsCost();
        System.out.println(actualCost);

        double expectedCost = 5500.0;

        assertEquals(expectedCost, actualCost, 0.01);
    }
}
