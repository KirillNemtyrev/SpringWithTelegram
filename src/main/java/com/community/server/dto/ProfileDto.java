package com.community.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ProfileDto {
    private String login;
    private BigDecimal balance;
}
