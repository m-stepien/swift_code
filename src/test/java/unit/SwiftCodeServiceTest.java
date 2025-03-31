package unit;

import org.home.exercise.swift.component.SwiftCodeValidator;
import org.home.exercise.swift.dto.*;
import org.home.exercise.swift.entity.SwiftCodeEntity;
import org.home.exercise.swift.exception.FieldMismatchException;
import org.home.exercise.swift.exception.HeadquarterAlreadyExistException;
import org.home.exercise.swift.exception.NotFoundException;
import org.home.exercise.swift.exception.RecordAlreadyExistException;
import org.home.exercise.swift.repository.SwiftCodeRepository;
import org.home.exercise.swift.service.SwiftCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SwiftCodeServiceTest {
    @Mock
    private SwiftCodeRepository repository;
    @Mock
    private SwiftCodeValidator validator;
    @InjectMocks
    private SwiftCodeService swiftCodeService;


    @Test
    void testGetSwiftBySwiftCodeReturnsHeadquarter() {
        String swiftCode = "PKOPPLPWXXX";
        SwiftCodeEntity entity = mock(SwiftCodeEntity.class);
        HeadquarterResponseDto dto = mock(HeadquarterResponseDto.class);
        List<BranchListItemDto> branches = List.of();
        when(repository.findById(swiftCode)).thenReturn(Optional.of(entity));
        when(repository.findBranchesRelatedToHeadquarter("PKOPPLPW")).thenReturn(branches);
        when(entity.toHeadquarter(branches)).thenReturn(dto);
        var result = swiftCodeService.getSwiftBySwiftCode(swiftCode);
        assertEquals(dto, result);
        verify(repository).findById(swiftCode);
        verify(repository).findBranchesRelatedToHeadquarter("PKOPPLPW");
    }

    @Test
    void testGetSwiftBySwiftCodeReturnsHeadquarterBic8() {
        String swiftCode = "PKOPPLPW";
        String expectedBic11 = "PKOPPLPWXXX";
        SwiftCodeEntity entity = mock(SwiftCodeEntity.class);
        HeadquarterResponseDto dto = mock(HeadquarterResponseDto.class);
        List<BranchListItemDto> branches = List.of();
        when(repository.findById(expectedBic11)).thenReturn(Optional.of(entity));
        when(repository.findBranchesRelatedToHeadquarter("PKOPPLPW")).thenReturn(branches);
        when(entity.toHeadquarter(branches)).thenReturn(dto);
        var result = swiftCodeService.getSwiftBySwiftCode(swiftCode);
        assertEquals(dto, result);
        verify(repository).findById(expectedBic11);
        verify(repository).findBranchesRelatedToHeadquarter("PKOPPLPW");
    }

    @Test
    void testGetSwiftBySwiftCodeReturnsHeadquarterBic8LowerCase() {
        String swiftCode = "pkopplpw";
        String expectedBic11 = "PKOPPLPWXXX";
        SwiftCodeEntity entity = mock(SwiftCodeEntity.class);
        HeadquarterResponseDto dto = mock(HeadquarterResponseDto.class);
        List<BranchListItemDto> branches = List.of();
        when(repository.findById(expectedBic11)).thenReturn(Optional.of(entity));
        when(repository.findBranchesRelatedToHeadquarter("PKOPPLPW")).thenReturn(branches);
        when(entity.toHeadquarter(branches)).thenReturn(dto);
        var result = swiftCodeService.getSwiftBySwiftCode(swiftCode);
        assertEquals(dto, result);
        verify(repository).findById(expectedBic11);
        verify(repository).findBranchesRelatedToHeadquarter("PKOPPLPW");
    }

    @Test
    void testGetSwiftBySwiftCodeReturnsBranch() {
        String swiftCode = "PKOPPLPW123";
        SwiftCodeEntity entity = mock(SwiftCodeEntity.class);
        when(repository.findById(swiftCode)).thenReturn(Optional.of(entity));
        when(entity.toSingleBranch()).thenReturn(mock());
        var result = swiftCodeService.getSwiftBySwiftCode(swiftCode);
        assertNotNull(result);
        verify(repository).findById(swiftCode);
        verify(entity).toSingleBranch();
    }

    @Test
    void testGetSwiftBySwiftCodeReturnsBranchLowerCase() {
        String swiftCode = "pkopplpw123";
        String swiftCodeUp = swiftCode.toUpperCase();
        SwiftCodeEntity entity = mock(SwiftCodeEntity.class);
        when(repository.findById(swiftCodeUp)).thenReturn(Optional.of(entity));
        when(entity.toSingleBranch()).thenReturn(mock());
        var result = swiftCodeService.getSwiftBySwiftCode(swiftCode);
        assertNotNull(result);
        verify(repository).findById(swiftCodeUp);
        verify(entity).toSingleBranch();
    }

    @Test
    void testGetSwiftBySwiftCodeThrowsWhenNotFound() {
        String swiftCode = "PKOPPLPW123";
        when(repository.findById(swiftCode)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> swiftCodeService.getSwiftBySwiftCode(swiftCode));
    }

    @Test
    void testGetBanksInCountryReturnsDataCorrectly() {
        String iso2 = "PL";
        CountryDto country = new CountryDto("PL", "POLAND");
        List<BranchListItemDto> branches = List.of(
                new BranchListItemDto("ul. Warszawska 1", "PKO", "PL", false, "PKOPPLPW123")
        );
        when(repository.findCountryNameByIso2("PL")).thenReturn(Optional.of(country));
        when(repository.findSwiftCodesForCountry("PL")).thenReturn(branches);
        Iso2ResponseDto result = swiftCodeService.getBanksInCountry(iso2);
        assertEquals("PL", result.countryISO2());
        assertEquals("POLAND", result.countryName());
        assertEquals(1, result.swiftCodes().size());
        assertEquals("PKO", result.swiftCodes().get(0).bankName());
    }

    @Test
    void testGetBanksInCountryReturnsDataCorrectlyLowerCase() {
        String iso2 = "pl";
        String expectedIso2 = "PL";
        CountryDto country = new CountryDto("PL", "POLAND");
        List<BranchListItemDto> branches = List.of(
                new BranchListItemDto("ul. Warszawska 1", "PKO", "PL", false, "PKOPPLPW123")
        );
        when(repository.findCountryNameByIso2(expectedIso2)).thenReturn(Optional.of(country));
        when(repository.findSwiftCodesForCountry(expectedIso2)).thenReturn(branches);
        Iso2ResponseDto result = swiftCodeService.getBanksInCountry(iso2);
        assertEquals("PL", result.countryISO2());
        assertEquals("POLAND", result.countryName());
        assertEquals(1, result.swiftCodes().size());
        assertEquals("PKO", result.swiftCodes().get(0).bankName());
    }

    @Test
    void testGetBanksInCountryThrowsWhenIso2NotFound() {
        String iso2 = "ZZ";
        when(repository.findCountryNameByIso2(iso2)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> swiftCodeService.getBanksInCountry(iso2));
    }

    @Test
    void testDeleteSwiftCodeDeletesBranchWhenLengthIs11() {
        String swiftCode = "PKOPPLPWXXX";
        when(repository.deleteByBic11("PKOPPLPWXXX")).thenReturn(1);
        int result = swiftCodeService.deleteSwiftCode(swiftCode);
        assertEquals(1, result);
        verify(repository).deleteByBic11("PKOPPLPWXXX");
        verify(validator).checkLengthOfSwiftCode(swiftCode);
    }

    @Test
    void testDeleteSwiftCodeDeletesHeadquarterWhenLengthIs8() {
        String swiftCode = "PKOPPLPW";
        when(repository.deleteHeadquarterByBic8("PKOPPLPW")).thenReturn(3);
        int result = swiftCodeService.deleteSwiftCode(swiftCode);
        assertEquals(3, result);
        verify(repository).deleteHeadquarterByBic8("PKOPPLPW");
        verify(validator).checkLengthOfSwiftCode(swiftCode);
    }

    @Test
    void testCreateSwiftCodeSuccessForHeadquarter() {
        BranchDto dto = new BranchDto(
                "Warszawa", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX");
        when(repository.findById("PKOPPLPWXXX")).thenReturn(Optional.empty());
        when(repository.findCountryNameRelatedToIso2("PL")).thenReturn(List.of("POLAND"));
        when(repository.findBankNameByBic8("PKOP")).thenReturn(List.of("PKO"));
        when(repository.findById("PKOPPLPWXXX")).thenReturn(Optional.empty());
        swiftCodeService.createSwiftCode(dto);
        verify(validator).checkLengthOfSwiftCode("PKOPPLPWXXX");
        verify(validator).checkMatchOfCountryIsoAndSwiftCode("PKOPPLPWXXX", "PL");
        verify(validator).checkHeadquarterStatus("PKOPPLPWXXX", true);
        verify(repository).save(any(SwiftCodeEntity.class));
    }

    @Test
    void testCreateSwiftCodeSuccessForBranch() {
        BranchDto dto = new BranchDto(
                "Warszawa", "PKO", "PL",
                "POLAND", false, "PKOPPLPW456");
        when(repository.findById("PKOPPLPW456")).thenReturn(Optional.empty());
        when(repository.findCountryNameRelatedToIso2("PL")).thenReturn(List.of("POLAND"));
        when(repository.findBankNameByBic8("PKOP")).thenReturn(List.of("PKO"));
        swiftCodeService.createSwiftCode(dto);
        verify(validator).checkLengthOfSwiftCode("PKOPPLPW456");
        verify(validator).checkHeadquarterStatus("PKOPPLPW456", false);
        verify(repository).save(any(SwiftCodeEntity.class));
    }

    @Test
    void testCreateSwiftCodeThrowsWhenAlreadyExists() {
        BranchDto dto = new BranchDto("Warszawa", "PKO", "PL", "POLAND",
                true, "PKOPPLPWXXX");
        when(repository.findById("PKOPPLPWXXX")).thenReturn(Optional.of(mock(SwiftCodeEntity.class)));
        assertThrows(RecordAlreadyExistException.class, () -> swiftCodeService.createSwiftCode(dto));
    }

    @Test
    void testCreateSwiftCodeThrowsWhenHqAlreadyExistsForBic8() {
        BranchDto dto = new BranchDto("Warszawa", "PKO", "PL",
                "Poland", true,"PKOPPLPW");
        when(repository.findById("PKOPPLPW")).thenReturn(Optional.empty());
        when(repository.findCountryNameRelatedToIso2("PL")).thenReturn(List.of("Poland"));
        when(repository.findBankNameByBic8("PKOP")).thenReturn(List.of("PKO"));
        when(repository.findById("PKOPPLPWXXX")).thenReturn(Optional.of(mock(SwiftCodeEntity.class)));
        assertThrows(HeadquarterAlreadyExistException.class, () -> swiftCodeService.createSwiftCode(dto));
    }

    @Test
    void testCreateSwiftCodeThrowsWhenCountryNameMismatch() {
        BranchDto dto = new BranchDto("Warszawa", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX");
        when(repository.findById("PKOPPLPWXXX")).thenReturn(Optional.empty());
        when(repository.findCountryNameRelatedToIso2("PL")).thenReturn(List.of("POLSKA"));
        assertThrows(FieldMismatchException.class, () -> swiftCodeService.createSwiftCode(dto));
    }

    @Test
    void testCreateSwiftCodeThrowsWhenBankNameMismatch() {
        BranchDto dto = new BranchDto("Warszawa", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX");
        when(repository.findById("PKOPPLPWXXX")).thenReturn(Optional.empty());
        when(repository.findCountryNameRelatedToIso2("PL")).thenReturn(List.of("POLAND"));
        when(repository.findBankNameByBic8("PKOP")).thenReturn(List.of("ING"));
        assertThrows(FieldMismatchException.class, () -> swiftCodeService.createSwiftCode(dto));
    }
}


