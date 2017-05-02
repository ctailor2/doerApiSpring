package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
interface ListUnlockDao extends JpaRepository<ListUnlockEntity, Long> {
    @Query("SELECT lv FROM ListUnlockEntity lv " +
            "INNER JOIN lv.userEntity u " +
            "WHERE u.email = ?1 " +
            "ORDER BY lv.createdAt DESC")
    List<ListUnlockEntity> findAllUserListUnlocks(String email);
}
