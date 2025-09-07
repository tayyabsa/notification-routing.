package com.multibank.notification_routing.repository.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class RecipientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String recipientId;

    @Column
    private String email;

    @Column
    private String phoneNumber;

    @Column
    private String pushToken;

}
