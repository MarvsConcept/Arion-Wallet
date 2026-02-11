package com.marv.arionwallet.modules.auth.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(name = "uk_role_name", columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RoleName name;

    @Column(length = 255)
    private String description;

    public Role(RoleName name,
                String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
    }
}
