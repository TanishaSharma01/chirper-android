package com.example.hackathon.dao.model;

import java.util.UUID;

public record Message(UUID id, UUID poster, UUID thread, long timestamp, String message) {}