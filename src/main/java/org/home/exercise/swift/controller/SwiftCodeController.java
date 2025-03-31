package org.home.exercise.swift.controller;

import jakarta.validation.Valid;
import org.home.exercise.swift.dto.BranchDto;
import org.home.exercise.swift.dto.MessageResponseDto;
import org.home.exercise.swift.dto.Iso2ResponseDto;
import org.home.exercise.swift.dto.SwiftCodeResponse;
import org.home.exercise.swift.service.SwiftCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/swift-codes")
public class SwiftCodeController {

    private final SwiftCodeService swiftCodeService;

    @Autowired
    public SwiftCodeController(SwiftCodeService swiftCodeService) {
        this.swiftCodeService = swiftCodeService;
    }

    @GetMapping("/{swift-code}")
    public ResponseEntity<SwiftCodeResponse> getSwiftCodes(@PathVariable("swift-code") String swiftCode) {
        SwiftCodeResponse response = this.swiftCodeService.getSwiftBySwiftCode(swiftCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/country/{iso2}")
    public ResponseEntity<Iso2ResponseDto> getSwiftCodesInCountry(@PathVariable("iso2") String iso2) {
        Iso2ResponseDto response = this.swiftCodeService.getBanksInCountry(iso2);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<MessageResponseDto> createSwiftCode(@Valid @RequestBody BranchDto request) {
        this.swiftCodeService.createSwiftCode(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponseDto("Swift code created successfully"));
    }

    @DeleteMapping("/{swift-code}")
    public ResponseEntity<MessageResponseDto> deleteSwiftCode(@PathVariable("swift-code") String swiftCode) {
        int numberOfDeletedRecord = this.swiftCodeService.deleteSwiftCode(swiftCode);
        return ResponseEntity.ok(new MessageResponseDto("Delete completed successful. Deleted "
                + numberOfDeletedRecord + " records"));
    }
}
