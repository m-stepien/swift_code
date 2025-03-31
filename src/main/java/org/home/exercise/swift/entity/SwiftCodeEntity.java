package org.home.exercise.swift.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.home.exercise.swift.dto.HeadquarterResponseDto;
import org.home.exercise.swift.dto.BranchListItemDto;
import org.home.exercise.swift.dto.BranchDto;

import java.util.List;

@Entity
@Table(name="swift_code")
public class SwiftCodeEntity {
    @Id
    private String swiftCode;
    private String countryISO2;
    private String countryName;
    private boolean isHeadquarter;
    private String address;
    private String name;

    public SwiftCodeEntity(){}

    public SwiftCodeEntity(BranchDto branchDto){
        this.address = branchDto.address();
        this.swiftCode = branchDto.swiftCode().toUpperCase();
        this.name = branchDto.bankName();
        this.countryName = branchDto.countryName().toUpperCase();
        this.countryISO2 = branchDto.countryISO2().toUpperCase();
        this.isHeadquarter = branchDto.isHeadquarter();
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public SwiftCodeEntity setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
        return this;
    }

    public String getCountryISO2() {
        return countryISO2;
    }

    public SwiftCodeEntity setCountryISO2(String countryISO2) {
        this.countryISO2 = countryISO2.toUpperCase();
        return this;
    }

    public String getCountryName() {
        return countryName;
    }

    public SwiftCodeEntity setCountryName(String countryName) {
        this.countryName = countryName.toUpperCase();
        return this;
    }

    public boolean isHeadquarter() {
        return isHeadquarter;
    }

    public SwiftCodeEntity setHeadquarter(boolean headquarter) {
        isHeadquarter = headquarter;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public SwiftCodeEntity setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getName() {
        return name;
    }

    public SwiftCodeEntity setName(String name) {
        this.name = name;
        return this;
    }

    public BranchDto toSingleBranch(){
        if(this.getSwiftCode()==null) {
            return null;
        }
        return new BranchDto(this.getAddress(), this.getName(), this.getCountryISO2().toUpperCase(),
                this.getCountryName().toUpperCase(), this.isHeadquarter(), this.getSwiftCode());
    }

    public HeadquarterResponseDto toHeadquarter(List<BranchListItemDto> branches){
        return new HeadquarterResponseDto(this.getAddress(), this.getName(), this.getCountryISO2().toUpperCase(),
                this.getCountryName().toUpperCase(), this.isHeadquarter(), this.getSwiftCode(), branches);
    }
}
