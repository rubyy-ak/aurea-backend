package de.htwberlin.aurea.dto;

import java.math.BigDecimal;

public record WeatherRequest(
        BigDecimal temperatureCelsius,
        String condition,
        String city
) {
}