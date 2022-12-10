package com.community.server.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @NaturalId(mutable=true)
    private String uuid = UUID.randomUUID().toString();

    @NotBlank
    @Size(min=2, max = 40)
    private String name;

    @NotBlank
    @NaturalId(mutable=true)
    @Size(min=6, max = 40)
    private String username;

    @NaturalId(mutable=true)
    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @NotBlank
    @Size(max = 100)
    private String password;

    @CreatedDate
    private Date createDate = new Date();

    @LastModifiedDate
    private Date lastModifyDate = new Date();

    private Date subscribe = null;

    private String recoveryCode;
    private Date recoveryCodeValid;

    private Long telegramId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<RoleEntity> roles = new HashSet<>();

    public UserEntity() {}

    public UserEntity(String name, String username, String email, String password) {
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
