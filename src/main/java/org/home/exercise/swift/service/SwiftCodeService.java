package org.home.exercise.swift.service;

import org.home.exercise.swift.component.SwiftCodeValidator;
import org.home.exercise.swift.dto.*;
import org.home.exercise.swift.entity.SwiftCodeEntity;
import org.home.exercise.swift.exception.*;
import org.home.exercise.swift.repository.SwiftCodeRepository;
import org.home.exercise.swift.utils.BranchClassifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SwiftCodeService {
    private final SwiftCodeRepository swiftCodeRepository;
    private final SwiftCodeValidator validator;
    private final Logger logger = LoggerFactory.getLogger(SwiftCodeService.class);

    @Autowired
    public SwiftCodeService(SwiftCodeRepository swiftCodeRepository, SwiftCodeValidator swiftCodeValidator) {
        this.swiftCodeRepository = swiftCodeRepository;
        this.validator = swiftCodeValidator;
    }

    public SwiftCodeResponse getSwiftBySwiftCode(String swiftCode) {
        logger.info("Fetching data for SWIFT code: {}", swiftCode);
        this.validator.checkLengthOfSwiftCode(swiftCode);
        String bic11 = this.changeToBic11(swiftCode).toUpperCase();
        if (BranchClassifier.isHeadquarter(swiftCode)) {
            logger.info("SWIFT code {} identified as Headquarter", swiftCode);
            SwiftCodeEntity swiftCodeEntity = this.swiftCodeRepository
                    .findById(bic11)
                    .orElseThrow(() -> new NotFoundException("SWIFT code not found: " + swiftCode));
            String bankCode = bic11.substring(0, bic11.length() - 3);
            List<BranchListItemDto> branches = this.swiftCodeRepository.findBranchesRelatedToHeadquarter(bankCode);
            logger.info("Found {} related branches for headquarter {}", branches.size(), swiftCode);
            return swiftCodeEntity.toHeadquarter(branches);
        } else {
            logger.info("SWIFT code {} identified as Branch", swiftCode);
            SwiftCodeEntity swiftCodeEntity = this.swiftCodeRepository.findById(bic11).orElseThrow(
                    () -> new NotFoundException("SWIFT code not found: " + swiftCode));
            return swiftCodeEntity.toSingleBranch();
        }
    }

    public Iso2ResponseDto getBanksInCountry(String iso2Code) {
        logger.info("Fetching data for ISO 2 code: {}", iso2Code);
        this.validator.checkLengthOfIso2Code(iso2Code);
        CountryDto country = this.swiftCodeRepository.findCountryNameByIso2(iso2Code.toUpperCase())
                .orElseThrow(() -> new NotFoundException("ISO 2 code not found: " + iso2Code));
        List<BranchListItemDto> swiftCodes = this.swiftCodeRepository.findSwiftCodesForCountry(iso2Code.toUpperCase());
        logger.info("Found {} related swift codes for ISO 2 {}", swiftCodes.size(), iso2Code);
        return new Iso2ResponseDto(country.countryISO2(), country.countryName(), swiftCodes);
    }

    public void createSwiftCode(BranchDto swiftCode) {
        logger.info("Creating new swift code: {}", swiftCode.swiftCode());
        this.validate(swiftCode);
        if (swiftCode.isHeadquarter()) {
            if (this.swiftCodeRepository.findById(this.extractBic8(swiftCode.swiftCode().toUpperCase()) + "XXX").isPresent()) {
                logger.warn("Headquarter already exists for swift code {}", swiftCode.swiftCode());
                throw new HeadquarterAlreadyExistException("Try to create headquarter but headquarter for this bank already exist");
            }
        }
        SwiftCodeEntity swiftCodeEntity = new SwiftCodeEntity(swiftCode);
        this.swiftCodeRepository.save(swiftCodeEntity);
        logger.info("Swift code {} successfully created", swiftCode.swiftCode());
    }

    public int deleteSwiftCode(String swiftCode) {
        logger.info("Attempting to delete swift code: {}", swiftCode);
        this.validator.checkLengthOfSwiftCode(swiftCode);
        int numberOfDeletedRecord;
        if (swiftCode.length()==8) {
            logger.info("Swift code {} identified as bic8. Deleting headquarter and related branches...", swiftCode);
            numberOfDeletedRecord = this.swiftCodeRepository.deleteHeadquarterByBic8(swiftCode.toUpperCase());
        } else {
            logger.info("Swift code {} identified as bic11. Deleting swift code...", swiftCode);
            numberOfDeletedRecord = this.swiftCodeRepository.deleteByBic11(swiftCode.toUpperCase());
        }
        logger.info("Delete operation completed. Deleted {} record(s).", numberOfDeletedRecord);
        return numberOfDeletedRecord;
    }

    private void validate(BranchDto swiftCode){
        this.validator.checkLengthOfSwiftCode(swiftCode.swiftCode());
        logger.info("Swift code length validated: {}", swiftCode.swiftCode());
        this.validator.checkHeadquarterStatus(swiftCode.swiftCode(), swiftCode.isHeadquarter());
        logger.info("Headquarter status is valid: {}", swiftCode.isHeadquarter());
        if (swiftCode.countryISO2() != null) {
            logger.info("Country ISO2 matches swift code: {}", swiftCode.countryISO2());
            this.validator.checkMatchOfCountryIsoAndSwiftCode(swiftCode.swiftCode(), swiftCode.countryISO2());
        }
        if (this.swiftCodeRepository.findById(swiftCode.swiftCode()).isPresent()) {
            logger.warn("Record with swift code {} already exist in database", swiftCode.swiftCode());
            throw new RecordAlreadyExistException("Record already exist for swift code " + swiftCode.swiftCode());
        }
        if (!this.isSameCountryNameForIso2InDatabase(swiftCode.countryISO2(), swiftCode.countryName())) {
            logger.warn("Country name {} does not match database record for ISO2 {}",
                    swiftCode.countryName(), swiftCode.countryISO2());
            throw new FieldMismatchException("Provided country name doesn't match founded in database for provided ISO 2 code");
        }
        if (!this.isSameBankNameForSwiftCodeInDatabase(swiftCode.swiftCode(), swiftCode.bankName())) {
            logger.warn("Bank name {} does not match database record for swift code {}"
                    , swiftCode.bankName(), swiftCode.swiftCode());
            throw new FieldMismatchException(("Provided bank name doesn't match founded in database for provided swift code"));
        }
    }

    private boolean isSameCountryNameForIso2InDatabase(String iso2, String countryName) {
        logger.info("Comparing country name with previous data in database with ISO 2 code {}", iso2);
        List<String> countryNameInDb = this.swiftCodeRepository.findCountryNameRelatedToIso2(iso2);
        boolean isSame;
        if (countryNameInDb.isEmpty()) {
            logger.info("No previous data with iso2 {} found. Accepting current country name {}."
                    , iso2, countryName);
            isSame = true;
        } else {
            isSame = countryName.equals(countryNameInDb.get(0));
        }
        if (countryNameInDb.size() > 1) {
            logger.warn("Possible incorrect data in database. Found {} country names for ISO 2 {}"
                    , countryNameInDb.size(), iso2);
        }
        return isSame;
    }

    private boolean isSameBankNameForSwiftCodeInDatabase(String swiftCode, String bankName) {
        String bankCode = this.getBankCodeFromSwiftCode(swiftCode);
        logger.info("Comparing bank name with previous data in database with swift code {}", bankCode);
        boolean isSame;
        List<String> bankNameInDb = this.swiftCodeRepository
                .findBankNameByBic8(bankCode);
        if (bankNameInDb.isEmpty()) {
            logger.info("No previous data with bank code {} found. Accepting current bank name {}."
                    , bankCode, bankName);
            isSame = true;
        } else {
            isSame = bankName.equals(bankNameInDb.get(0));
        }
        if (bankNameInDb.size() > 1) {
            logger.warn("Possible incorrect data in database. Found {} bank names for bank code {}"
                    , bankNameInDb.size(), bankCode);
        }
        return isSame;
    }

    private String getBankCodeFromSwiftCode(String swiftCode) {
        return swiftCode.substring(0, 4);
    }

    private String changeToBic11(String swiftCode){
        if(swiftCode.length()==11){
            return swiftCode;
        }
        else{
            return swiftCode + "XXX";
        }
    }

    private String extractBic8(String swiftCode) {
        if (swiftCode.length() == 11) {
            return swiftCode.substring(0, 8);
        }
        return swiftCode;
    }
}
