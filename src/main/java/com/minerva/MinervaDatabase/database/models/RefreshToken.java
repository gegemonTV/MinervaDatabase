package com.minerva.MinervaDatabase.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "user_id"),
                @UniqueConstraint(columnNames = "refresh_token")
        }
)
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "refresh_token")
    private String refreshToken;

    public RefreshToken(Long userId, String jwtRefresh) {
        this.userId = userId;
        this.refreshToken = jwtRefresh;
    }

    public RefreshToken(){}
}
