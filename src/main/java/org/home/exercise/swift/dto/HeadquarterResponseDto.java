package org.home.exercise.swift.dto;

import java.util.List;

public record HeadquarterResponseDto(String address, String bankName, String countryISO2, String countryName,
                                     boolean isHeadquarter, String swiftCode, List<BranchListItemDto> branches) implements SwiftCodeResponse {
}
