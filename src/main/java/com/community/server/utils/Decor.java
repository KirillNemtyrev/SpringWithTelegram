package com.community.server.utils;

import com.community.server.entity.ChatEntity;
import com.community.server.model.DecorModel;
import com.community.server.repository.ChatRepository;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

@Component
public class Decor {

    private final ChatRepository chatRepository;

    public Decor(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public DecorModel getDecor(Long chatId){
        ChatEntity chatEntity = chatRepository.findByChatId(chatId).orElse(null);

        ReadFile readFile = new ReadFile();
        File file = new File("decor/" + chatId + ".json");
        File standard = new File("decor/default.json");

        boolean flag = (chatEntity != null ? chatEntity.getPremium() : null) != null && new Date().after(chatEntity.getPremium()) && file.isFile();
        String output = readFile.get(flag ? file : standard);
        return new Gson().fromJson(output, DecorModel.class);
    }

}
