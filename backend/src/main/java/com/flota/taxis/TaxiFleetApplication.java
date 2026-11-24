package com.flota.taxis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TaxiFleetApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaxiFleetApplication.class, args);
    }
}
