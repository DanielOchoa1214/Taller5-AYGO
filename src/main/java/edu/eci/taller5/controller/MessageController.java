package edu.eci.taller5.controller;

import edu.eci.taller5.model.Message;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class MessageController {

    private final Map<String, Message> messages = new ConcurrentHashMap<>();

    @GetMapping("/")
    public Resource getIndex() {
        return new ClassPathResource("/static/index.html");
    }

    @GetMapping("/app-login")
    public Resource getLogin() {
        return new ClassPathResource("/static/login.html");
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, Message>> createMessage(HttpServletRequest request, @RequestBody String message) {
        Message msg = Message.builder().message(message).clientIp(request.getRemoteAddr()).timestamp(LocalDateTime.now()).build();
        messages.put(msg.getTimestamp().toString(), msg);
        return ResponseEntity.ok().body(messages);
    }
}
