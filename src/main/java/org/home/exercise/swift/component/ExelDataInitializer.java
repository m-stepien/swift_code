package org.home.exercise.swift.component;

import jakarta.annotation.PostConstruct;
import org.home.exercise.swift.service.SwiftExelLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!test")
@Component
public class ExelDataInitializer {
    private final SwiftExelLoaderService swiftExelLoaderService;
    private static final Logger logger = LoggerFactory.getLogger(ExelDataInitializer.class);

    @Autowired
    public ExelDataInitializer(SwiftExelLoaderService swiftExelLoaderService){
        this.swiftExelLoaderService = swiftExelLoaderService;
    }

    @PostConstruct
    public void init(){
        logger.info("Initializing database from Excel if empty");
        this.swiftExelLoaderService.processExel();
    }
}
