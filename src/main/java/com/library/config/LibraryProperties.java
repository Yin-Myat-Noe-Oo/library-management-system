package com.library.config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Library rules, bound from the {@code library.*} properties. */
@ConfigurationProperties(prefix = "library")
public record LibraryProperties(int loanPeriodDays, int maxActiveLoansPerMember, BigDecimal finePerDay) {
}
