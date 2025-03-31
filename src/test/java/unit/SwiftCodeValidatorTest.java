package unit;

import org.home.exercise.swift.component.SwiftCodeValidator;
import org.home.exercise.swift.exception.Iso2DontMatchSwiftCodeException;
import org.home.exercise.swift.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SwiftCodeValidatorTest {
    private final SwiftCodeValidator validator = new SwiftCodeValidator();

    @Test
    void testCheckRequiredFieldExistThrowsException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.checkRequiredFieldExist(null, "PL", "PKO", "Poland"));
        assertTrue(ex.getMessage().contains("swiftCode"));
        ex = assertThrows(ValidationException.class,
                () -> validator.checkRequiredFieldExist("PTFIPLPWFSD", null, "PKO", "Poland"));
        assertTrue(ex.getMessage().contains("countryIso2"));
        ex = assertThrows(ValidationException.class,
                () -> validator.checkRequiredFieldExist("PTFIPLPWFSD", "PL", null, "Poland"));
        assertTrue(ex.getMessage().contains("bankName"));
        ex = assertThrows(ValidationException.class,
                () -> validator.checkRequiredFieldExist("PTFIPLPWFSD", "PL", "PKO", null));
        assertTrue(ex.getMessage().contains("countryName"));
    }

    @Test
    void testCheckRequiredFieldExistPositive() {
        assertDoesNotThrow(() ->
                validator.checkRequiredFieldExist("PKOPPLPWXXX", "PL", "PKO", "Poland"));
    }

    @Test
    void testCheckLengthOfSwiftCodeInvalid() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.checkLengthOfSwiftCode("PKOPP"));
        assertTrue(ex.getMessage().contains("Incorrect length of swift code:"));
        assertThrows(ValidationException.class,
                () -> validator.checkLengthOfSwiftCode("PKOPPLPWX"));
        assertThrows(ValidationException.class,
                () -> validator.checkLengthOfSwiftCode("PKOPPLPWXXXX"));
    }

    @Test
    void testCheckLengthOfSwiftCodePositiveBic11orBic8() {
        assertDoesNotThrow(() -> validator.checkLengthOfSwiftCode("PKOPPLPWXXX"));
        assertDoesNotThrow(() -> validator.checkLengthOfSwiftCode("PKOPPLPW"));
    }

    @Test
    void testCheckLengthOfIso2CodeInvalid() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> validator.checkLengthOfIso2Code("PLN"));
        assertTrue(ex.getMessage().contains("Incorrect length of iso 2"));
        assertThrows(ValidationException.class,
                () -> validator.checkLengthOfIso2Code("P"));
    }

    @Test
    void testCheckLengthOfIso2CodePositive() {
        assertDoesNotThrow(() -> validator.checkLengthOfIso2Code("PL"));
    }

    @Test
    void testCheckHeadquarterStatusInvalid() {
        assertThrows(ValidationException.class,
                () -> validator.checkHeadquarterStatus("PKOPPLPWXXX", false));
        assertThrows(ValidationException.class,
                () -> validator.checkHeadquarterStatus("PKOPPLPW", false));
        assertThrows(ValidationException.class,
                () -> validator.checkHeadquarterStatus("PKOPPLPWXXXX", true));
        assertThrows(ValidationException.class,
                () -> validator.checkHeadquarterStatus("PKOPPLPCCC", true));
        assertThrows(ValidationException.class,
                () -> validator.checkHeadquarterStatus("PKOPPL", true));
    }

    @Test
    void testCheckHeadquarterStatusPositive() {
        assertDoesNotThrow(()->validator.checkHeadquarterStatus("PKOPPLPWXXX", true));
        assertDoesNotThrow(()->validator.checkHeadquarterStatus("PKOPPLPW", true));
        assertDoesNotThrow(()->validator.checkHeadquarterStatus("PKOPPLPWXXXX", false));
        assertDoesNotThrow(()->validator.checkHeadquarterStatus("PKOPPLPCCC", false));
        assertDoesNotThrow(()->validator.checkHeadquarterStatus("PKOPPL", false));

    }

    @Test
    void testCheckMatchOfCountryIsoAndSwiftCodeInvalid() {
        Iso2DontMatchSwiftCodeException ex = assertThrows(Iso2DontMatchSwiftCodeException.class,
                () -> validator.checkMatchOfCountryIsoAndSwiftCode("PKOPDEPWXXX", "PL"));
        assertTrue(ex.getMessage().contains("ISO2"));
    }

    @Test
    void testCheckMatchOfCountryIsoAndSwiftCodePositive() {
        assertDoesNotThrow(() ->
                validator.checkMatchOfCountryIsoAndSwiftCode("PKOPPLPWXXX", "PL"));
        assertDoesNotThrow(() ->
                validator.checkMatchOfCountryIsoAndSwiftCode("pkopplpwxxx", "pl"));
        assertDoesNotThrow(() ->
                validator.checkMatchOfCountryIsoAndSwiftCode("pkopplpwxxx", "PL"));
    }
}

