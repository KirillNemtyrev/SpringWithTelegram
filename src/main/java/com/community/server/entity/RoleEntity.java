package com.community.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NaturalId
    @Column(length = 60)
    private RoleNameEntity name;

    public RoleEntity() {}

    public RoleEntity(RoleNameEntity name) {
        this.name = name;
    }
}
