package com.smoothTalkAI.backend.user;

import com.smoothTalkAI.backend.common.BaseTimeEntity;
import com.smoothTalkAI.backend.conversation.Conversation;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "token_balance")
    @Builder.Default
    private Long tokenBalance = 0L;

    @Column(name = "last_free_token_date")
    private java.time.LocalDate lastFreeTokenDate;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Conversation> conversations = new ArrayList<>();

    public void updateProfile(String newName, String newProfileImage) {
        this.name = newName;
        this.profileImage = newProfileImage;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void addTokens(Long amount) {
        if (this.tokenBalance == null) {
            this.tokenBalance = 0L;
        }
        this.tokenBalance += amount;
    }

    public boolean deductToken() {
        if (this.tokenBalance == null) {
            this.tokenBalance = 0L;
        }
        if (this.tokenBalance > 0) {
            this.tokenBalance--;
            return true;
        }
        return false;
    }

    public boolean grantDailyFreeTokens() {
        java.time.LocalDate today = java.time.LocalDate.now();
        // 토큰이 0개 이하일 때만 매일 무료 토큰 지급 (오늘 이미 받지 않았어야 함)
        if ((this.tokenBalance == null || this.tokenBalance <= 0) && 
            (this.lastFreeTokenDate == null || !this.lastFreeTokenDate.equals(today))) {
            this.addTokens(3L);
            this.lastFreeTokenDate = today;
            return true;
        }
        return false;
    }
}
