package com.smoothTalkAI.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.info("Initializing/Updating database constraints for SenderType...");
        try {
            // PostgreSQL: 기존 제약 조건이 있다면 삭제 (IF EXISTS)
            jdbcTemplate.execute("ALTER TABLE messages DROP CONSTRAINT IF EXISTS messages_sender_check");
            
            // 새로운 제약 조건 추가 ('SYSTEM' 타입 허용)
            jdbcTemplate.execute("ALTER TABLE messages ADD CONSTRAINT messages_sender_check CHECK (sender IN ('USER', 'OTHER', 'SYSTEM'))");
            
            log.info("✅ Successfully updated 'messages_sender_check' constraint to include 'SYSTEM'.");
        } catch (Exception e) {
            // 테이블이 없거나 권한 문제 등
            log.warn("⚠️ Failed to update database constraint: {}", e.getMessage());
        }
    }
}
