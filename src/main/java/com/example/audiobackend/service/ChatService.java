package com.example.audiobackend.service;

import org.springframework.stereotype.Service;

@Service
public interface ChatService {
    String chat(String prompt, String visitorId);
}
