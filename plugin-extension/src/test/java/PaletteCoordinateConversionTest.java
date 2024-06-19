import de.verdox.mccreativelab.util.PaletteUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PaletteCoordinateConversionTest {

    @Test
    public void testConversion(){
        int x = 250;
        int localX = PaletteUtil.worldXToPaletteXCoordinate(x);

        Assertions.assertEquals(10, localX);
    }

    @Test
    public void testConversionNegative(){
        int x = -249;
        int localX = PaletteUtil.worldXToPaletteXCoordinate(x);

        Assertions.assertEquals(7, localX);
    }

}
