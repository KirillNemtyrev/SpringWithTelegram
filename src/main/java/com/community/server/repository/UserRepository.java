package com.community.server.repository;

import com.community.server.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsernameOrEmail(String username, String email);
    Optional<UserEntity> findByTelegramId(Long telegramId);
    Optional<UserEntity> findByUuid(String uuid);
    List<UserEntity> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String username, String name);
    List<UserEntity> findByIdIn(List<Long> userIds);
    Optional<UserEntity> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
}
