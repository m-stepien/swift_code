package org.home.exercise.swift.repository;

import org.home.exercise.swift.dto.BranchListItemDto;
import org.home.exercise.swift.dto.CountryDto;
import org.home.exercise.swift.entity.SwiftCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SwiftCodeRepository extends JpaRepository<SwiftCodeEntity, String> {

    @Query("SELECT new org.home.exercise.swift.dto.BranchListItemDto(s.address, s.name, s.countryISO2, s.isHeadquarter, s.swiftCode)" +
            " FROM SwiftCodeEntity s WHERE s.swiftCode LIKE CONCAT(:bankCode, '%') AND s.isHeadquarter = false")
    List<BranchListItemDto> findBranchesRelatedToHeadquarter(@Param("bankCode") String swiftCode);

    @Query("SELECT new org.home.exercise.swift.dto.CountryDto(s.countryISO2, s.countryName) FROM SwiftCodeEntity s WHERE s.countryISO2 = " +
            ":iso2 GROUP BY s.countryISO2, s.countryName ORDER BY COUNT(1) LIMIT 1")
    Optional<CountryDto> findCountryNameByIso2(@Param("iso2") String iso2);

    @Query("SELECT new org.home.exercise.swift.dto.BranchListItemDto(s.address, s.name, s.countryISO2, s.isHeadquarter, s.swiftCode)" +
            " FROM SwiftCodeEntity s WHERE s.countryISO2 = :iso2")
    List<BranchListItemDto> findSwiftCodesForCountry(@Param("iso2") String iso2);

    @Query("SELECT s.countryName FROM SwiftCodeEntity s WHERE s.countryISO2=:iso2 GROUP BY s.countryName ORDER BY COUNT(s) DESC")
    List<String> findCountryNameRelatedToIso2(@Param("iso2") String iso2);

    @Query("SELECT s.name FROM SwiftCodeEntity s WHERE s.swiftCode LIKE CONCAT(:bic8, '%') GROUP BY s.name ORDER BY COUNT(s) DESC")
    List<String> findBankNameByBic8(@Param("bic8") String bic8);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SwiftCodeEntity s WHERE s.swiftCode LIKE CONCAT(:bic8, '%')")
    public int deleteHeadquarterByBic8(@Param("bic8") String bic8);

    @Transactional
    @Modifying
    @Query("DELETE FROM SwiftCodeEntity WHERE swiftCode = :bic11")
    public int deleteByBic11(@Param("bic11") String bic11);
}
