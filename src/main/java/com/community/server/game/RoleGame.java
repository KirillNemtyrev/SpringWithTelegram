package com.community.server.game;

import com.community.server.entity.MemberEntity;
import com.community.server.enums.RolePlayerEnum;
import com.community.server.model.RoleModel;
import com.community.server.repository.ChatRepository;
import com.community.server.repository.MemberRepository;
import com.community.server.telegram.TelegramBot;

import java.util.List;

public class RoleGame {

    private MemberRepository memberRepository;
    private ChatRepository chatRepository;
    private TelegramBot telegramBot;

    public RoleGame(MemberRepository memberRepository, ChatRepository chatRepository, TelegramBot telegramBot) {
        this.memberRepository = memberRepository;
        this.chatRepository = chatRepository;
        this.telegramBot = telegramBot;
    }

    public void distribute(String uuid) {

        List<MemberEntity> members = memberRepository.findByUuid(uuid);
        RoleModel roleModel = new RoleModel();
        int count = members.size();

        roleModel.setMafia(Math.min(count / 1, 5));
        roleModel.setPolice(count > 6 ? 1 : 0);
        roleModel.setMedic(count > 7 ? 1 : 0);
        roleModel.setWhore(count > 7 ? 1 : 0);

        int roles = roleModel.getMafia() + roleModel.getMedic() + roleModel.getPolice() + roleModel.getWhore();
        for (int i = 0; i < roles; i++){

            int index = (int) Math.floor(Math.random() * members.size());
            MemberEntity memberEntity = members.get(index);
            members.remove(index);

            if(roleModel.getMafia() > 0) {

                roleModel.setMafia(roleModel.getMafia() - 1);
                memberEntity.setRole(RolePlayerEnum.MAFIA_PLAYER);

            } else if (roleModel.getPolice() > 0) {

                roleModel.setPolice(roleModel.getPolice() - 1);
                memberEntity.setRole(RolePlayerEnum.POLICE_PLAYER);

            } else if (roleModel.getMedic() > 0) {

                roleModel.setPolice(roleModel.getMedic() - 1);
                memberEntity.setRole(RolePlayerEnum.MEDIC_PLAYER);

            } else if (roleModel.getWhore() > 0) {

                roleModel.setWhore(roleModel.getWhore() - 1);
                memberEntity.setRole(RolePlayerEnum.WHORE_PLAYER);

            }
            memberRepository.save(memberEntity);
        }
    }

    public void mailing(String uuid){
        List<MemberEntity> members = memberRepository.findByUuid(uuid);
        for(MemberEntity memberEntity : members) telegramBot.sendRole(memberEntity);
    }
}
