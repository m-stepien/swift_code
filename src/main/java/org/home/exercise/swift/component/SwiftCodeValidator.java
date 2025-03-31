package org.home.exercise.swift.component;

import org.home.exercise.swift.exception.Iso2DontMatchSwiftCodeException;
import org.home.exercise.swift.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SwiftCodeValidator {

    public void checkRequiredFieldExist(String swiftCode, String countryIso2, String bankName, String countryName) {
        List<String> missingFields = new ArrayList<>();
        if (swiftCode == null) missingFields.add("swiftCode");
        if (countryIso2 == null) missingFields.add("countryIso2");
        if (bankName == null) missingFields.add("bankName");
        if (countryName == null) missingFields.add("countryName");
        if (!missingFields.isEmpty()) {
            throw new ValidationException("Missing required fieild in record: " + missingFields);
        }
    }

    public void checkLengthOfSwiftCode(String swiftCode) {
        if (!(swiftCode.length() == 11 || swiftCode.length() == 8)) {
            throw new ValidationException("Incorrect length of swift code: " + swiftCode);
        }
    }

    public void checkLengthOfIso2Code(String iso2){
        if (!(iso2.length() == 2))  {
            throw new ValidationException("Incorrect length of iso 2 code: " + iso2);
        }
    }

    public void checkHeadquarterStatus(String swiftCode, boolean isHeadquarter) {
        if ((swiftCode.length() == 8 || (swiftCode.length()==11 && swiftCode.endsWith("XXX")))!=isHeadquarter) {
            throw new ValidationException("Record set as headquarter but swift code doesn't end with XXX");
        }
    }

    public void checkMatchOfCountryIsoAndSwiftCode(String swiftCode, String iso2) {
        String countryCodeInSwift = swiftCode.substring(4, 6).toUpperCase();
        if (!countryCodeInSwift.equals(iso2.toUpperCase())) {
            throw new Iso2DontMatchSwiftCodeException("Provided ISO 2 code doesn't match extracted from swift code. ISO2:" +
                    iso2 + ", country code from swift code: " + countryCodeInSwift);
        }
    }
}
