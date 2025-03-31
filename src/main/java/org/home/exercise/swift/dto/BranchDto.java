package org.home.exercise.swift.dto;

import jakarta.validation.constraints.NotBlank;

public record BranchDto(
        String address,
        @NotBlank String bankName,
        @NotBlank String countryISO2,
        @NotBlank String countryName,
        boolean isHeadquarter,
        @NotBlank String swiftCode) implements SwiftCodeResponse{
}