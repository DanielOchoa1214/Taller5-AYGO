package edu.eci.taller5.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class Message {
    private String message;
    private String clientIp;
    private LocalDateTime timestamp;

}
