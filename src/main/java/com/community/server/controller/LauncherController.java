package com.community.server.controller;

import com.community.server.dto.ClientDto;
import com.community.server.service.LauncherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/launcher")
public class LauncherController {

    @Autowired
    private LauncherService launcherService;

    @GetMapping
    public ClientDto getLauncher(@RequestParam String launcher) {
        return launcherService.getLauncher(launcher);
    }

}
