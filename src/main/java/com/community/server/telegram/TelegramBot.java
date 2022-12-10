package com.community.server.telegram;

import com.community.server.entity.ChatEntity;
import com.community.server.entity.MemberEntity;
import com.community.server.enums.GameStatusEnum;
import com.community.server.enums.RolePlayerEnum;
import com.community.server.enums.StatusPlayerEnum;
import com.community.server.game.GameStorage;
import com.community.server.game.NightGame;
import com.community.server.game.RoleGame;
import com.community.server.game.StartGame;
import com.community.server.model.DecorModel;
import com.community.server.repository.ChatRepository;
import com.community.server.repository.MemberRepository;
import com.community.server.repository.UserRepository;
import com.community.server.utils.Decor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final UserRepository userRepository;

    private final ChatRepository chatRepository;

    private final GameStorage gameStorage;

    private final MemberRepository memberRepository;

    private final Decor decor;

    @Value("${bot.username}")
    private String username;

    @Value("${bot.token}")
    private String token;

    public TelegramBot(UserRepository userRepository, ChatRepository chatRepository, GameStorage gameStorage, MemberRepository memberRepository, Decor decor) {
        this.userRepository = userRepository;
        this.chatRepository = chatRepository;
        this.gameStorage = gameStorage;
        this.memberRepository = memberRepository;
        this.decor = decor;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageText = update.getMessage().getText();
            String chatName = update.getMessage().getChat().getTitle();
            String userName = update.getMessage().getChat().getFirstName();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getChat().getId();

            if(messageText.startsWith("/start") && messageText.length() == 43) {
                String uuid = messageText.substring(7);
                if(uuid.matches("^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$")){
                    joinCommand(uuid, userName, userId);
                }
                return;
            }

            switch (messageText) {
                case "/game": {
                    gameCommand(chatId, chatName);
                    break;
                }
                case "/finish": {
                    finishCommand(chatId);
                    break;
                }
            }
        }

    }

    @SneakyThrows
    public synchronized void gameCommand(Long chatId, String chatName) {

        ChatEntity chatEntity = chatRepository.findByChatId(chatId)
                .orElse(null);

        if(chatEntity == null) {

            chatEntity = new ChatEntity();
            chatEntity.setChatId(chatId);
            chatRepository.save(chatEntity);

        }

        if (chatEntity.getGameStatus() != GameStatusEnum.NO_GAME){
            return;
        }

        String uuid = UUID.randomUUID().toString();
        chatEntity.setGameStatus(GameStatusEnum.REGISTER_GAME);
        chatEntity.setName(chatName);
        chatEntity.setUuid(uuid);

        Timer timer = new Timer();
        StartGame startGame = new StartGame(60, uuid, chatId, this, gameStorage, chatRepository, memberRepository);
        timer.schedule(startGame, 1000, 1000);

        gameStorage.putTimer(chatId, timer);
        gameStorage.putObject(chatId, startGame);

        chatRepository.save(chatEntity);
    }

    @SneakyThrows
    public synchronized void finishCommand(Long id) {
        ChatEntity chatEntity = chatRepository.findByChatId(id).orElse(null);
        if(chatEntity == null || chatEntity.getGameStatus() != GameStatusEnum.REGISTER_GAME) {
            return;
        }

        finish(id);
    }

    @SneakyThrows
    public synchronized void finish(Long chatId) {

        StartGame startGame = gameStorage.getObject(chatId);
        Timer timer = gameStorage.getTimer(chatId);
        timer.cancel();

        ChatEntity chatEntity = chatRepository.findByChatId(chatId).orElse(null);
        if (chatEntity == null){
            return;
        }

        for (Integer message : startGame.getMessages()){
            deleteMessage(chatId, message);
        }

        List<MemberEntity> members = memberRepository.findByUuid(chatEntity.getUuid());
        if(members.size() < 1) {

            gameEndMinimum(chatId);

            chatEntity.setGameStatus(GameStatusEnum.NO_GAME);
            chatRepository.save(chatEntity);

            memberRepository.deleteAll(members);

        } else {

            RoleGame roleGame = new RoleGame(memberRepository, chatRepository, this);
            roleGame.distribute(chatEntity.getUuid());
            roleGame.mailing(chatEntity.getUuid());

            sendStartGame(chatId);

            timer = new Timer();
            NightGame nightGame = new NightGame(this, gameStorage, chatRepository, memberRepository, 30, chatId);
            timer.schedule(nightGame, 1000, 1000);
            gameStorage.putTimer(chatId, timer);
        }
    }

    @SneakyThrows
    public synchronized void joinCommand(String uuid, String userName, Long userId){

        ChatEntity chatEntity = chatRepository.findByUuid(uuid).orElse(null);
        if(chatEntity == null) {
            return;
        }

        MemberEntity memberEntity = memberRepository.findByUserId(userId).orElse(null);
        if(memberEntity == null) {
            sendJoinGame(userId, chatEntity.getChatId(), chatEntity.getName(), userName, uuid);
        } else {
            sendJoinedGame(userId, chatEntity.getChatId());
        }
    }

    public InlineKeyboardMarkup getJoinButton(String uuid){

        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
        inlineKeyboardButton.setText("Присоединиться");
        inlineKeyboardButton.setUrl("https://t.me/" + username + "?start=" + uuid);

        List<List<InlineKeyboardButton>> list = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        buttons.add(inlineKeyboardButton);
        list.add(buttons);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(list);
        return markupKeyboard;

    }

    @SneakyThrows
    public synchronized Message sendStartMessage(Long id, String uuid){
        DecorModel decorModel = decor.getDecor(id);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(id));
        sendMessage.setText(decorModel.getStart());
        sendMessage.setParseMode(ParseMode.HTML);
        sendMessage.setReplyMarkup(getJoinButton(uuid));
        return execute(sendMessage);
    }

    @SneakyThrows
    public synchronized Message sendReminderMessage(Long id, Integer message, Integer countdown){
        DecorModel decorModel = decor.getDecor(id);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setReplyToMessageId(message);
        sendMessage.setChatId(String.valueOf(id));
        sendMessage.setText(decorModel.getReminder().replace("{seconds}", String.valueOf(countdown)));
        sendMessage.setParseMode(ParseMode.HTML);
        return execute(sendMessage);
    }

    @SneakyThrows
    public synchronized void sendJoinGame(Long userId, Long chatId, String chatName, String userName, String uuid){

        DecorModel decorModel = decor.getDecor(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.setText(decorModel.getJoin().replace("{name}", chatName));
        sendMessage.setParseMode(ParseMode.HTML);

        execute(sendMessage);

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUserId(userId);
        memberEntity.setChatId(chatId);
        memberEntity.setUserName(userName);
        memberEntity.setUuid(uuid);
        memberRepository.save(memberEntity);

        StartGame startGame = gameStorage.getObject(chatId);
        Integer message = startGame.getFirstMessage();
        editMembersMessage(chatId, message, uuid);
    }

    @SneakyThrows
    public synchronized void editMembersMessage(Long id, Integer message, String uuid){

        List<MemberEntity> members = memberRepository.findByUuid(uuid);
        StringBuilder output = new StringBuilder();

        for(MemberEntity member : members){
            output.append(String.format("<a href=\"tg://user?id=%d\">%s</a>\n", member.getUserId(), member.getUserName()));
        }

        DecorModel decorModel = decor.getDecor(id);

        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(message);
        editMessageText.setChatId(String.valueOf(id));
        editMessageText.setText(decorModel.getRegister().replace("{list}", output).replace("{count}", String.valueOf(members.size())));
        editMessageText.setReplyMarkup(getJoinButton(uuid));
        editMessageText.setParseMode(ParseMode.HTML);
        execute(editMessageText);
    }

    @SneakyThrows
    public synchronized void sendJoinedGame(Long userId, Long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(userId));
        sendMessage.setText("\uD83E\uDD35 Вы уже принимаете участие в" + (userId.equals(chatId) ? " этой " : " другой ") + "игре!");
        sendMessage.setParseMode(ParseMode.HTML);
        execute(sendMessage);
    }

    @SneakyThrows
    public synchronized void deleteMessage(Long chatId, Integer message){
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(message);
        execute(deleteMessage);
    }

    @SneakyThrows
    public synchronized void gameEndMinimum(Long chatId){
        DecorModel decorModel = decor.getDecor(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(decorModel.getMinimum());
        sendMessage.setParseMode(ParseMode.HTML);
        execute(sendMessage);
    }

    @SneakyThrows
    public synchronized void sendStartGame(Long chatId){
        DecorModel decorModel = decor.getDecor(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(decorModel.getGame());
        sendMessage.setParseMode(ParseMode.HTML);
        execute(sendMessage);
    }

    @SneakyThrows
    public synchronized void sendRole(MemberEntity memberEntity) {
        DecorModel decorModel = decor.getDecor(memberEntity.getChatId());

        String text = decorModel.getRole()
                .replace("{smile}", memberEntity.getRole() == RolePlayerEnum.MAFIA_PLAYER ? decorModel.getMafia().getSmile() :
                        memberEntity.getRole() == RolePlayerEnum.POLICE_PLAYER ? decorModel.getPolice().getSmile() :
                                memberEntity.getRole() == RolePlayerEnum.MEDIC_PLAYER ? decorModel.getMedic().getSmile() :
                                        memberEntity.getRole() == RolePlayerEnum.WHORE_PLAYER ? decorModel.getWhore().getSmile() : decorModel.getPeace().getSmile())

                .replace("{name}", memberEntity.getRole() == RolePlayerEnum.MAFIA_PLAYER ? decorModel.getMafia().getName() :
                                memberEntity.getRole() == RolePlayerEnum.POLICE_PLAYER ? decorModel.getPolice().getName() :
                                        memberEntity.getRole() == RolePlayerEnum.MEDIC_PLAYER ? decorModel.getMedic().getName() :
                                                memberEntity.getRole() == RolePlayerEnum.WHORE_PLAYER ? decorModel.getWhore().getName() : decorModel.getPeace().getName());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(memberEntity.getUserId()));
        sendMessage.setText(text);
        sendMessage.setParseMode(ParseMode.HTML);
        execute(sendMessage);
    }

    @SneakyThrows
    public synchronized void sendNight(Long chatId, int day) {
        DecorModel decorModel = decor.getDecor(chatId);

        InputFile file = new InputFile();
        file.setMedia(new File("resources/night.jpg"));

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setCaption(decorModel.getNight().replace("{day}", String.valueOf(day)));
        sendPhoto.setPhoto(file);
        sendPhoto.setParseMode(ParseMode.HTML);
        execute(sendPhoto);
    }

    @SneakyThrows
    public synchronized void sendDay(Long chatId, int day) {
        DecorModel decorModel = decor.getDecor(chatId);

        InputFile file = new InputFile();
        file.setMedia(new File("resources/day.jpg"));

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setCaption(decorModel.getDay().replace("{day}", String.valueOf(day)));
        sendPhoto.setPhoto(file);
        sendPhoto.setParseMode(ParseMode.HTML);
        execute(sendPhoto);
    }

    @SneakyThrows
    public synchronized void sendVote(Long chatId) {
        DecorModel decorModel = decor.getDecor(chatId);

        InputFile file = new InputFile();
        file.setMedia(new File("resources/vote.jpg"));

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(String.valueOf(chatId));
        sendPhoto.setCaption(decorModel.getVote());
        sendPhoto.setPhoto(file);
        sendPhoto.setParseMode(ParseMode.HTML);
        execute(sendPhoto);
    }

    @SneakyThrows
    public synchronized void sendResult(Long chatId, Long die) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.HTML);

        DecorModel decorModel = decor.getDecor(chatId);

        if(die == null || die == 0) {
            sendMessage.setText(decorModel.getResultNoKilled());
            execute(sendMessage);
            return;
        }

        MemberEntity memberEntity = memberRepository.findByUserId(die).orElse(null);
        if(memberEntity == null) {
            sendMessage.setText(decorModel.getResultNoKilled());
            execute(sendMessage);
            return;
        }

        String user = "<a href=\"tg://user?id=" + memberEntity.getUserId() + "\">" + memberEntity.getUserName() + "</a>";
        sendMessage.setText(decorModel.getResultKilled().replace("{name}", user));
        execute(sendMessage);
    }

    @SneakyThrows
    public synchronized void sendKeyBoardForRoles(List<MemberEntity> members, MemberEntity memberEntity, String uuid, Long chatId) {

        if(memberEntity.getStatus() != StatusPlayerEnum.LIVE_PLAYER)
            return;

        DecorModel decorModel = decor.getDecor(chatId);

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        List<List<InlineKeyboardButton>> list = new ArrayList<>();

        for(MemberEntity member : members) {

            if(memberEntity == member || member.getStatus() != StatusPlayerEnum.LIVE_PLAYER ||
                    member.getRole() == memberEntity.getRole())
                continue;

            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(member.getUserName());
            inlineKeyboardButton.setCallbackData(uuid + "-" + member.getId());

            buttons.add(inlineKeyboardButton);
        }

        list.add(buttons);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(list);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(memberEntity.getUserId()));
        sendMessage.setText(memberEntity.getRole() == RolePlayerEnum.MAFIA_PLAYER ? decorModel.getMafia().getSmile() + " Кого убьём?":
                memberEntity.getRole() == RolePlayerEnum.POLICE_PLAYER ? decorModel.getPolice().getSmile() + " Кого проверим?":
                        memberEntity.getRole() == RolePlayerEnum.MEDIC_PLAYER ? decorModel.getMedic().getSmile() + " Кого вылечим?" :
                                memberEntity.getRole() == RolePlayerEnum.WHORE_PLAYER ? " К кому сходим?" : "");
        sendMessage.setReplyMarkup(markupKeyboard);
        sendMessage.setParseMode(ParseMode.HTML);
        execute(sendMessage);
    }

    @SneakyThrows
    public synchronized void sendKeyBoardForVote(List<MemberEntity> members, MemberEntity memberEntity, String uuid, Long chatId) {
        if(memberEntity.getStatus() != StatusPlayerEnum.LIVE_PLAYER) {
            return;
        }

        //DecorModel decorModel = decor.getDecor(chatId);

        List<InlineKeyboardButton> buttons = new ArrayList<>();
        List<List<InlineKeyboardButton>> list = new ArrayList<>();

        for(MemberEntity member : members) {

            if(memberEntity == member || member.getStatus() != StatusPlayerEnum.LIVE_PLAYER ||
                    member.getRole() == memberEntity.getRole())
                continue;

            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(member.getUserName());
            inlineKeyboardButton.setCallbackData(uuid + "-" + member.getId() + "-vote");

            buttons.add(inlineKeyboardButton);
        }

        list.add(buttons);

        InlineKeyboardMarkup markupKeyboard = new InlineKeyboardMarkup();
        markupKeyboard.setKeyboard(list);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(memberEntity.getUserId()));
        sendMessage.setText("За кого голосовать?");
        sendMessage.setReplyMarkup(markupKeyboard);
        sendMessage.setParseMode(ParseMode.HTML);
        execute(sendMessage);
    }
}
