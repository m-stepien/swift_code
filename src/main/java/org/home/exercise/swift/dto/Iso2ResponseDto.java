package org.home.exercise.swift.dto;

import java.util.List;

public record Iso2ResponseDto(String countryISO2, String countryName, List<BranchListItemDto> swiftCodes){
}
