package com.community.server.controller;

import com.community.server.dto.ProfileDto;
import com.community.server.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ProfileDto getProfile(HttpServletRequest httpServletRequest){
        return profileService.getProfile(httpServletRequest);
    }

}
