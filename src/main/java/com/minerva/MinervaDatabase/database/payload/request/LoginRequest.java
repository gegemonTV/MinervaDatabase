package com.minerva.MinervaDatabase.database.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
public class LoginRequest {
    @NotBlank
    private String alias;

    @NotBlank
    private String password;
}
