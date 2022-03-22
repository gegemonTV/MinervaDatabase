package com.minerva.MinervaDatabase.database.payload.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min=3, max=120)
    private String username;

    @NotBlank
    @Size(min =3, max = 120)
    private String alias;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @NotBlank
    private String password;

}
