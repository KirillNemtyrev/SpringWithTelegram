package com.community.server.game;

import com.community.server.entity.ChatEntity;
import com.community.server.entity.MemberEntity;
import com.community.server.enums.GameStatusEnum;
import com.community.server.repository.ChatRepository;
import com.community.server.repository.MemberRepository;
import com.community.server.telegram.TelegramBot;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VoteGame extends TimerTask {
    private TelegramBot telegramBot;
    private GameStorage gameStorage;
    private ChatRepository chatRepository;
    private MemberRepository memberRepository;

    private Integer countdown;
    private Long chatId;

    public VoteGame(TelegramBot telegramBot, GameStorage gameStorage, ChatRepository chatRepository, MemberRepository memberRepository, Integer countdown, Long chatId) {
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

        chatEntity.setGameStatus(GameStatusEnum.VOTE_GAME);
        chatRepository.save(chatEntity);
        List<MemberEntity> members = memberRepository.findByUuid(chatEntity.getUuid());

        telegramBot.sendVote(chatId);
        for(MemberEntity memberEntity : members) telegramBot.sendKeyBoardForVote(members, memberEntity, chatEntity.getUuid(), chatId);
    }

    @Override
    public void run() {
        if(countdown-- <= 0) {
            ResultGame resultGame = new ResultGame(telegramBot, gameStorage, chatRepository, memberRepository, 5, chatId);

            Timer timer = gameStorage.getTimer(chatId);
            timer.cancel();

            timer = new Timer();
            timer.schedule(resultGame, 1000, 1000);

            gameStorage.putTimer(chatId, timer);
        }
    }
}
