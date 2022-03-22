package com.minerva.MinervaDatabase.database.payload.request;

import lombok.Data;

@Data
public class RefreshJwtRequest {
    private String refreshToken;
}
