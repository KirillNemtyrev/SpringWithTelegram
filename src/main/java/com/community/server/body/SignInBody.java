package com.community.server.body;

import lombok.Getter;

@Getter
public class SignInBody {
    private String usernameOrEmail;
    private String password;
}
