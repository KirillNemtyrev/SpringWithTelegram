package com.community.server.service;

import com.community.server.dto.ClientDto;
import com.community.server.utils.ReadFile;
import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class LauncherService {

    public ClientDto getLauncher(String launcher) {

        File file = new File("client/" + launcher + ".json");
        ReadFile readFile = new ReadFile(file);
        return new Gson().fromJson(readFile.get(), ClientDto.class);

    }

}
