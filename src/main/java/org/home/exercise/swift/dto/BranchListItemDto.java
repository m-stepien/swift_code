package org.home.exercise.swift.dto;

public record BranchListItemDto(String address, String bankName, String countryISO2,
                                boolean isHeadquarter, String swiftCode) {
}
