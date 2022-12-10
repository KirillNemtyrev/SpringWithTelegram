package com.community.server.repository;

import com.community.server.entity.RoleEntity;
import com.community.server.entity.RoleNameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByName(RoleNameEntity roleName);
}