package com.community.server.body;

import lombok.Getter;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Getter
public class SignUpBody {

    @Size(min=6, max=40)
    private String name;

    @Size(min=6, max=40)
    private String username;

    @Email
    private String email;

    private String password;
}
