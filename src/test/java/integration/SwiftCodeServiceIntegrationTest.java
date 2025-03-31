package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.home.exercise.swift.dto.BranchDto;
import org.home.exercise.swift.repository.SwiftCodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(classes = org.home.exercise.swift.Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SwiftCodeServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SwiftCodeRepository repository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    @Test
    void testCreateAndGetHeadquarter() throws Exception {
        BranchDto dto = new BranchDto(
                "ul. Warszawska 1", "PKO", "PL",
                "POLAND", true,"PKOPPLPWXXX");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/PKOPPLPWXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankName", is("PKO")))
                .andExpect(jsonPath("$.countryISO2", is("PL")))
                .andExpect(jsonPath("$.isHeadquarter", is(true)))
                .andExpect(jsonPath("$.address", is("ul. Warszawska 1")));
    }

    @Test
    void testGetBranchDoesNotContainBranchesField() throws Exception {
        BranchDto hq = new BranchDto("HQ St", "ING", "PL", "POLAND", true, "INGBPLPWXXX");
        BranchDto branch = new BranchDto("Branch St", "ING", "PL", "POLAND", false, "INGBPLPW001");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hq)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(branch)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/INGBPLPW001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("INGBPLPW001"))
                .andExpect(jsonPath("$.isHeadquarter").value(false))
                .andExpect(jsonPath("$.branches").doesNotExist());
    }

    @Test
    void testGetHeadquarterWithNoBranchesReturnsEmptyBranchesList() throws Exception {
        BranchDto hq = new BranchDto(
                "HQ Only", "MBK", "PL", "POLAND", true, "MBKBPLPWXXX"
        );

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(hq)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/MBKBPLPWXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("MBKBPLPWXXX"))
                .andExpect(jsonPath("$.isHeadquarter").value(true))
                .andExpect(jsonPath("$.branches").isArray())
                .andExpect(jsonPath("$.branches").isEmpty());
    }

    @Test
    void testGetNonExistingSwiftCodeReturns404() throws Exception {
        mockMvc.perform(get("/v1/swift-codes/NOSUCHCODEX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetHeadquarterWithBranchesAndExcludeUnrelated() throws Exception {
        BranchDto dto = new BranchDto(
                "ul. Centralna 1", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX"
        );
        BranchDto dto1 = new BranchDto(
                "ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPW001"
        );
        BranchDto dto2 = new BranchDto(
                "ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPW002"
        );
        BranchDto dto3 = new BranchDto(
                "ul. 1", "ING", "PL",
                "POLAND", true, "INGBPLPWXXX"
        );
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/PKOPPLPWXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCode").value("PKOPPLPWXXX"))
                .andExpect(jsonPath("$.branches.length()").value(2))
                .andExpect(jsonPath("$.branches[?(@.swiftCode=='PKOPPLPW001')]").exists())
                .andExpect(jsonPath("$.branches[?(@.swiftCode=='PKOPPLPW002')]").exists())
                .andExpect(jsonPath("$.branches[?(@.swiftCode=='INGBPLPWXXX')]").doesNotExist());
    }

    @Test
    void testGetBanksInCountry() throws Exception {
        BranchDto dto = new BranchDto("Mickiewicza 1", "ING", "PL",
                "POLAND", true, "INGBPLPWXXX");
        BranchDto dto2 = new BranchDto("Mickiewicza 2", "ING", "PL",
                "POLAND", false, "INGBPLPWC21");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/country/PL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCodes", hasSize(2)))
                .andExpect(jsonPath("$.swiftCodes[0].bankName", is("ING")));
    }

    @Test
    void testGetBanksInCountryLowerCase() throws Exception {
        BranchDto dto = new BranchDto("Mickiewicza 1", "ING", "PL",
                "POLAND", true, "INGBPLPWXXX");
        BranchDto dto2 = new BranchDto("Mickiewicza 2", "ING", "PL",
                "POLAND", false, "INGBPLPWC21");
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/swift-codes/country/pl"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.swiftCodes", hasSize(2)))
                .andExpect(jsonPath("$.swiftCodes[0].bankName", is("ING")));
    }

    @Test
    void testCreateDuplicateSwiftCodeReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void testCreateSecoundHeadquarterSwiftCodeReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX");
        BranchDto dto2 = new BranchDto("ul. 2", "PKO", "PL",
                "POLAND", true, "PKOPPLPW");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void testCreateSwiftCodeInvalidIso2DifferentInSwiftCodeReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "OL",
                "POLAND", true, "PKOPPLPWXXX");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void testCreateSwiftCodeInvalidIsHeadquarterFalseReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWXXX");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void testCreateSwiftCodeInvalidIsHeadquarterTrueReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", true, "PKOPPLPWCZU");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    void testCreateSwiftCodeMissingDataReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", null, "PL",
                "POLAND", false, null);

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message",
                        containsString("swiftCode")));
    }

    @Test
    void testCreateSwiftDoesntMatchCountryInDataFromDbReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZU");
        BranchDto dto2 = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZZ");
        BranchDto dto3 = new BranchDto("ul. 1", "PKO", "PL",
                "POLSKA", false, "PKOPPLPWCZY");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto3)))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.message",
                        containsString("Provided country name doesn't match founded ")));
    }

    @Test
    void testCreateSwiftDoesntMatchBankNameInDataFromDbReturnsError() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZU");
        BranchDto dto2 = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZZ");
        BranchDto dto3 = new BranchDto("ul. 1", "ZPO", "PL",
                "POLAND", false, "PKOPPLPWCZY");

        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto3)))
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.message",
                        containsString("Provided bank name doesn't match founded ")));
    }

    @Test
    void testDeleteBranch() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZU");
        BranchDto dto2 = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZZ");
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/v1/swift-codes/PKOPPLPWCZU"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Deleted 1 records")));
    }

    @Test
    void testDeleteOnlyHeadquarter() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX");
        BranchDto dto2 = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZZ");
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/v1/swift-codes/PKOPPLPWXXX"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Deleted 1 records")));
    }

    @Test
    void testDeleteHeadquarterWithRelatedBranches() throws Exception {
        BranchDto dto = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", true, "PKOPPLPWXXX");
        BranchDto dto2 = new BranchDto("ul. 1", "PKO", "PL",
                "POLAND", false, "PKOPPLPWCZZ");
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/v1/swift-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/v1/swift-codes/PKOPPLPW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Deleted 2 records")));
    }

    @Test
    void testDeleteNonExistingSwiftCode() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/DOESNOTEXIS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", containsString("Deleted 0 records")));
    }

    @Test
    void testDeleteInvalidSwiftCode() throws Exception {
        mockMvc.perform(delete("/v1/swift-codes/DDD"))
                .andExpect(status().isNotAcceptable());
    }
}
