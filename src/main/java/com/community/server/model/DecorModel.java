package com.community.server.model;

import lombok.Data;

@Data
public class DecorModel {
    private String join;
    private String start;
    private String register;
    private String reminder;
    private String minimum;
    private String game;
    private String day;
    private String night;
    private String vote;
    private String role;
    private String resultKilled;
    private String resultNoKilled;

    private RoleDescriptionModel mafia;
    private RoleDescriptionModel police;
    private RoleDescriptionModel medic;
    private RoleDescriptionModel whore;
    private RoleDescriptionModel peace;
}
