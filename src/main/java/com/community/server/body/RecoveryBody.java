package com.community.server.body;

import lombok.Getter;

@Getter
public class RecoveryBody {
    private String usernameOrEmail;
    private String code;
    private String newPassword;
}
