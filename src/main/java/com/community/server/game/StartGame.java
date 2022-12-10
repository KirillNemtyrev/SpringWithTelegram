package com.community.server.game;

import com.community.server.entity.ChatEntity;
import com.community.server.entity.MemberEntity;
import com.community.server.enums.GameStatusEnum;
import com.community.server.repository.ChatRepository;
import com.community.server.repository.MemberRepository;
import com.community.server.telegram.TelegramBot;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class StartGame extends TimerTask {

    private TelegramBot telegramBot;

    private GameStorage gameStorage;

    private ChatRepository chatRepository;

    private MemberRepository memberRepository;

    private Integer countdown;
    private Long chatId;

    private List<Integer> messages = new ArrayList<>();

    public StartGame(Integer countdown, String uuid, Long chatId, TelegramBot telegramBot, GameStorage gameStorage, ChatRepository chatRepository, MemberRepository memberRepository) {

        this.countdown = countdown;
        this.chatId = chatId;
        this.telegramBot = telegramBot;
        this.gameStorage = gameStorage;
        this.chatRepository = chatRepository;
        this.memberRepository = memberRepository;

        Message message = telegramBot.sendStartMessage(chatId, uuid);
        messages.add(message.getMessageId());
    }

    @SneakyThrows
    @Override
    public void run() {

        countdown -= 1;
        if(countdown == 30 || countdown == 10) {

            ChatEntity chatEntity = chatRepository.findByChatId(chatId).orElse(null);
            if(chatEntity == null){

                Timer timer = gameStorage.getTimer(chatId);
                timer.cancel();

                return;
            }

            Message message = telegramBot.sendReminderMessage(chatId, messages.get(0), countdown);
            messages.add(message.getMessageId());
        }

        if(countdown <= 0) {
            telegramBot.finish(chatId);
        }
    }

    public List<Integer> getMessages(){
        return messages;
    }

    public Integer getFirstMessage(){
        return messages.get(0);
    }

}
