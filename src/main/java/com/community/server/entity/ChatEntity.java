package com.community.server.entity;

import com.community.server.enums.GameStatusEnum;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@Table(name="chats")
public class ChatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;

    private Integer day = 0;

    private String name;
    private String uuid;

    private Date premium = null;
    private GameStatusEnum gameStatus = GameStatusEnum.NO_GAME;

}
