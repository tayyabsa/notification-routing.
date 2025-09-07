package com.multibank.notification_routing.repository;

import com.multibank.notification_routing.repository.model.RecipientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipientRepo extends JpaRepository<RecipientEntity, Long> {

    Optional<RecipientEntity> findByRecipientId(String recipientId);
}
