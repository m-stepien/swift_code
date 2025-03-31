package unit;

import org.home.exercise.swift.utils.BranchClassifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BranchClassifierTest {

    @Test
    public void testIsHeadquarterPositvie() {
        String swiftCode = "BCGMMCM1XXX";
        assertTrue(BranchClassifier.isHeadquarter(swiftCode));
    }

    @Test
    void testIsHeadquarterNegative() {
        String swiftCode = "BCHICLR10R2";
        assertFalse(BranchClassifier.isHeadquarter(swiftCode));
    }

    @Test
    void testIsHeadquarterBic8() {
        String swiftCode = "BCHICLR1";
        assertTrue(BranchClassifier.isHeadquarter(swiftCode));
    }

    @Test
    void testIsHeadquarterNegativeToShort() {
        String swiftCode = "XXX";
        assertFalse(BranchClassifier.isHeadquarter(swiftCode));
    }
}
