package com.multibank.notification_routing.dto;

import java.io.Serializable;

public class EventsResponseDto implements Serializable {

    private String status;

    public EventsResponseDto() {
    }

    public EventsResponseDto(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
