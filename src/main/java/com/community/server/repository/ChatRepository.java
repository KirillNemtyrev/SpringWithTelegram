package com.community.server.repository;

import com.community.server.entity.ChatEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    Optional<ChatEntity> findByChatId(Long id);
    Optional<ChatEntity> findByUuid(String uuid);
}
