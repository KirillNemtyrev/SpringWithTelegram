package com.community.server.game;

import com.community.server.entity.ChatEntity;
import com.community.server.entity.MemberEntity;
import com.community.server.enums.GameStatusEnum;
import com.community.server.repository.ChatRepository;
import com.community.server.repository.MemberRepository;
import com.community.server.telegram.TelegramBot;
import lombok.SneakyThrows;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NightGame extends TimerTask {
    private TelegramBot telegramBot;
    private GameStorage gameStorage;
    private ChatRepository chatRepository;
    private MemberRepository memberRepository;

    private Integer countdown;
    private Long chatId;

    public NightGame(TelegramBot telegramBot, GameStorage gameStorage, ChatRepository chatRepository, MemberRepository memberRepository, Integer countdown, Long chatId) {
        this.telegramBot = telegramBot;
        this.gameStorage = gameStorage;
        this.chatRepository = chatRepository;
        this.memberRepository = memberRepository;
        this.countdown = countdown;
        this.chatId = chatId;

        ChatEntity chatEntity = chatRepository.findByChatId(chatId).orElse(null);
        if(chatEntity == null){
            return;
        }

        List<MemberEntity> members = memberRepository.findByUuid(chatEntity.getUuid());

        chatEntity.setDay(chatEntity.getDay() + 1);
        chatEntity.setGameStatus(GameStatusEnum.NIGHT_GAME);

        chatRepository.save(chatEntity);
        telegramBot.sendNight(chatId, chatEntity.getDay());
        for(MemberEntity memberEntity : members) telegramBot.sendKeyBoardForRoles(members, memberEntity, chatEntity.getUuid(), chatId);
    }

    @SneakyThrows
    @Override
    public void run() {
        if(countdown-- <= 0) {
            DayGame dayGame = new DayGame(telegramBot, gameStorage, chatRepository, memberRepository, 20, chatId);

            Timer timer = gameStorage.getTimer(chatId);
            timer.cancel();

            timer = new Timer();
            timer.schedule(dayGame, 1000, 1000);

            gameStorage.putTimer(chatId, timer);
        }
    }
}
