package com.multibank.notification_routing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventStatusRepo extends JpaRepository<EventStatusEntity, Long> {

    List<EventStatusEntity> findByStatusAndIsDeadLetter(String status, Boolean isDeadLetter);
    List<EventStatusEntity> findByStatus(String status);

}
