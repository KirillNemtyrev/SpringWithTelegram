package com.community.server.entity;

import com.community.server.enums.RolePlayerEnum;
import com.community.server.enums.StatusPlayerEnum;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name="members")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long chatId;
    private Long vote;

    private String userName;
    private String uuid;

    private RolePlayerEnum role = RolePlayerEnum.PEACE_PLAYER;
    private StatusPlayerEnum status = StatusPlayerEnum.LIVE_PLAYER;
}
