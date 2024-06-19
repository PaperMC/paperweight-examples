import de.verdox.mccreativelab.util.PaletteUtil;
import de.verdox.mccreativelab.util.storage.ThreeDimensionalStorage;
import de.verdox.mccreativelab.util.storage.palette.NBTPalettedContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

public class IndexingStrategyTests {

    @Test
    public void testShortViceVersa1(){
        ThreeDimensionalStorage.IndexingStrategy<Short> indexingStrategy = new ThreeDimensionalStorage.IndexingStrategy.Short(16,16,16);
        int x = 2;
        int y = 15;
        int z = 0;
        int[] expected = new int[]{x,y,z};

        short index = indexingStrategy.getIndex(x,y,z);
        int[] params = indexingStrategy.extractParameters(index);

        Assertions.assertArrayEquals(expected, params);
    }

    @Test
    public void testShortViceVersa2(){
        ThreeDimensionalStorage.IndexingStrategy<Short> indexingStrategy = new ThreeDimensionalStorage.IndexingStrategy.Short(16,16,16);
        int x = 0;
        int y = 15;
        int z = 0;
        int[] expected = new int[]{x,y,z};

        short index = indexingStrategy.getIndex(x,y,z);
        int[] params = indexingStrategy.extractParameters(index);

        Assertions.assertArrayEquals(expected, params);
    }

    @Test
    public void testShortViceVersa3(){
        ThreeDimensionalStorage.IndexingStrategy<Short> indexingStrategy = new ThreeDimensionalStorage.IndexingStrategy.Short(16,16,16);
        int x = 0;
        int y = 0;
        int z = 15;
        int[] expected = new int[]{x,y,z};

        short index = indexingStrategy.getIndex(x,y,z);
        int[] params = indexingStrategy.extractParameters(index);

        Assertions.assertArrayEquals(expected, params);
    }

    @Test
    public void testShortNoDuplicates(){
        ThreeDimensionalStorage.IndexingStrategy<Short> indexingStrategy = new ThreeDimensionalStorage.IndexingStrategy.Short(16,130,16);

        Set<Short> distinct = new HashSet<>();

        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 130; y++) {
                for (int z = 0; z < 15; z++) {
                    short index = indexingStrategy.getIndex(x,y,z);
                    if(distinct.contains(index))
                        throw new IllegalArgumentException();
                    distinct.add(index);
                }
            }
        }
    }
}
