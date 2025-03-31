package integration;

import org.home.exercise.swift.entity.SwiftCodeEntity;
import org.home.exercise.swift.repository.SwiftCodeRepository;
import org.home.exercise.swift.service.SwiftExelLoaderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = org.home.exercise.swift.Application.class)
@ActiveProfiles("test")
public class SwiftExelLoaderServiceIntegrationTest {
    @Autowired
    private SwiftExelLoaderService loaderService;

    @Autowired
    private SwiftCodeRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldImportValidRowsAndSkipInvalid() {
        loaderService.processExel();

        List<SwiftCodeEntity> all = repository.findAll();
        assertEquals(4, all.size());
        assertTrue(all.stream().anyMatch(e -> e.getSwiftCode().equals("AAISALTRXXX")));
        assertTrue(all.stream().anyMatch(e -> e.getSwiftCode().equals("ADCRBGS1XXX")));
        assertTrue(all.stream().noneMatch(e -> e.getName().equals("ABV INVESTMENTS LTD")));
        assertTrue(all.stream().noneMatch(e -> e.getSwiftCode().equals("AFAAUYM1XXX")));
    }

    @Test
    void runOnlyOnce() {
        loaderService.processExel();
        List<SwiftCodeEntity> all = repository.findAll();
        assertEquals(4, all.size());
        loaderService.processExel();
        all = repository.findAll();
        assertEquals(4, all.size());
    }
}
