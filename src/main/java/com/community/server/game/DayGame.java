package com.community.server.game;

import com.community.server.entity.ChatEntity;
import com.community.server.entity.MemberEntity;
import com.community.server.enums.GameStatusEnum;
import com.community.server.enums.RolePlayerEnum;
import com.community.server.enums.StatusPlayerEnum;
import com.community.server.repository.ChatRepository;
import com.community.server.repository.MemberRepository;
import com.community.server.telegram.TelegramBot;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DayGame extends TimerTask {

    private TelegramBot telegramBot;
    private GameStorage gameStorage;
    private ChatRepository chatRepository;
    private MemberRepository memberRepository;

    private Integer countdown;
    private Long chatId;

    public DayGame(TelegramBot telegramBot, GameStorage gameStorage, ChatRepository chatRepository, MemberRepository memberRepository, Integer countdown, Long chatId) {
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

        chatEntity.setGameStatus(GameStatusEnum.DAY_GAME);
        chatRepository.save(chatEntity);
        telegramBot.sendDay(chatId, chatEntity.getDay());
    }

    @Override
    public void run(){
        if(countdown-- <= 0) {
            VoteGame voteGame = new VoteGame(telegramBot, gameStorage, chatRepository, memberRepository, 15, chatId);

            Timer timer = gameStorage.getTimer(chatId);
            timer.cancel();

            timer = new Timer();
            timer.schedule(voteGame, 1000, 1000);

            gameStorage.putTimer(chatId, timer);
        }
    }

    public String getMorning(String uuid) {

        List<MemberEntity> members = memberRepository.findByUuid(uuid);
        for (MemberEntity memberEntity : members){

            if(memberEntity.getRole() == RolePlayerEnum.PEACE_PLAYER || memberEntity.getStatus() != StatusPlayerEnum.LIVE_PLAYER){
                continue;
            }

        }

        return "";
    }
}
