package org.home.exercise.swift.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.home.exercise.swift.utils.BranchClassifier;
import org.home.exercise.swift.component.SwiftCodeValidator;
import org.home.exercise.swift.entity.SwiftCodeEntity;
import org.home.exercise.swift.exception.ValidationException;
import org.home.exercise.swift.repository.SwiftCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class SwiftExelLoaderService {
    private final SwiftCodeRepository swiftCodeRepository;
    private final SwiftCodeValidator swiftCodeValidator;
    private final ResourceLoader resourceLoader;
    private static final int BATCH_SIZE = 200;
    @Value("${swift.loader.file-path}")
    private String filePath;
    private static final Logger logger = LoggerFactory.getLogger(SwiftExelLoaderService.class);

    @Autowired
    public SwiftExelLoaderService(SwiftCodeRepository swiftCodeRepository, SwiftCodeValidator swiftCodeValidator,
                                  ResourceLoader resourceLoader) {
        this.swiftCodeRepository = swiftCodeRepository;
        this.swiftCodeValidator = swiftCodeValidator;
        this.resourceLoader = resourceLoader;
    }


    public void processExel() {
        logger.info("Starting exel data import from file {}", filePath);
        if (this.swiftCodeRepository.count() > 0) {
            logger.info("Skipping import, records already in database");
        }
        else {
            try (InputStream inputStream = resourceLoader.getResource(filePath).getInputStream();
                 Workbook workbook = new XSSFWorkbook(inputStream)) {
                int imported = 0, skipped = 0;
                Sheet sheet = workbook.getSheetAt(0);
                List<SwiftCodeEntity> entities = new ArrayList<>();
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    String swiftCode = getValue(row.getCell(1));
                    String bankName = getValue(row.getCell(3));
                    String address = getValue(row.getCell(4));
                    String countryIso2 = getValue(row.getCell(0));
                    String countryName = getValue(row.getCell(6));
                    try {
                        swiftCodeValidator.checkRequiredFieldExist(swiftCode, countryIso2, bankName, countryName);
                        swiftCodeValidator.checkLengthOfSwiftCode(swiftCode);
                        swiftCodeValidator.checkMatchOfCountryIsoAndSwiftCode(swiftCode, countryIso2);
                    } catch (ValidationException exception) {
                        logger.warn(exception.getMessage());
                        logger.info("Record with swift code {} skipped", swiftCode);
                        skipped++;
                        continue;
                    }
                    SwiftCodeEntity entity = new SwiftCodeEntity()
                            .setSwiftCode(swiftCode).setAddress(address).setName(bankName)
                            .setHeadquarter(BranchClassifier.isHeadquarter(swiftCode))
                            .setCountryISO2(countryIso2).setCountryName(countryName);
                    entities.add(entity);
                    imported++;
                    if (entities.size() == BATCH_SIZE) {
                        this.swiftCodeRepository.saveAll(entities);
                        entities.clear();
                    }
                }
                if (!entities.isEmpty()) {
                    this.swiftCodeRepository.saveAll(entities);
                }
                logger.info("Import completed. {} record saved, {} skipped", imported, skipped);
            } catch (Exception e) {
                logger.error("Failed to import Excel data: {}", e.getMessage());
            }
        }
    }

    private String getValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        cell.setCellType(CellType.STRING);
        String value = cell.getStringCellValue();
        return value == null || value.isBlank() ? null : value.trim();
    }

}
