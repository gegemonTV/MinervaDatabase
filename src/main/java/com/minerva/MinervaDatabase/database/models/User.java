package com.minerva.MinervaDatabase.database.models;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = "alias"),
            @UniqueConstraint(columnNames = "email"),
            @UniqueConstraint(columnNames = "phone")
        }
)
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Size(max = 120)
    private String username;

    @NotBlank
    @Size(max = 120)
    private String alias;

    @NotBlank
    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String email;

    @NotBlank
    private String password;

    @NonNull
    private boolean is2FA = false;

    @NonNull
    private boolean enabled = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Column(name = "reset_password_token")
    private String resetPasswordToken;
}
