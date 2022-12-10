package com.community.server.game;

import com.community.server.entity.ChatEntity;
import com.community.server.entity.MemberEntity;
import com.community.server.enums.GameStatusEnum;
import com.community.server.enums.StatusPlayerEnum;
import com.community.server.repository.ChatRepository;
import com.community.server.repository.MemberRepository;
import com.community.server.telegram.TelegramBot;

import java.util.*;

public class ResultGame extends TimerTask {
    private TelegramBot telegramBot;
    private GameStorage gameStorage;
    private ChatRepository chatRepository;
    private MemberRepository memberRepository;

    private Integer countdown;
    private Long chatId;

    public ResultGame(TelegramBot telegramBot, GameStorage gameStorage, ChatRepository chatRepository, MemberRepository memberRepository, Integer countdown, Long chatId) {
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

        chatEntity.setGameStatus(GameStatusEnum.RESULT_GAME);
        chatRepository.save(chatEntity);

        List<MemberEntity> members = memberRepository.findByUuid(chatEntity.getUuid());
        Long killed = getKilled(members);

        telegramBot.sendResult(chatId, killed);
    }

    @Override
    public void run() {
        if(countdown-- <= 0) {
            NightGame nightGame = new NightGame(telegramBot, gameStorage, chatRepository, memberRepository, 30, chatId);

            Timer timer = gameStorage.getTimer(chatId);
            timer.cancel();

            timer = new Timer();
            timer.schedule(nightGame, 1000, 1000);

            gameStorage.putTimer(chatId, timer);
        }
    }

    public Long getKilled(List<MemberEntity> members){

        List<Long> list = new ArrayList<>();
        for(MemberEntity memberEntity : members){

            if(memberEntity.getStatus() != StatusPlayerEnum.LIVE_PLAYER){
                continue;
            }

            list.add(memberEntity.getVote());
        }

        return Collections.max(list);
    }

}
