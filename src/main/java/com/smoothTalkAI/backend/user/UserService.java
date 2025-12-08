package com.smoothTalkAI.backend.user;

import com.smoothTalkAI.backend.conversation.Conversation;
import com.smoothTalkAI.backend.conversation.ConversationRepository;
import com.smoothTalkAI.backend.exception.ApiException;
import com.smoothTalkAI.backend.security.UserPrincipal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private static final String GOOGLE_PROVIDER = "google";

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    @Transactional
    public User upsertGoogleUser(String providerUserId, String email, String name, String picture) {
        return userRepository.findByProviderAndProviderUserId(GOOGLE_PROVIDER, providerUserId)
                .map(user -> {
                    user.updateProfile(name, picture);
                    // Grant daily free tokens
                    boolean granted = user.grantDailyFreeTokens();
                    if (granted) {
                        log.info("🎁 Granted 3 daily free tokens to user {}. New balance: {}", user.getId(),
                                user.getTokenBalance());
                    }
                    return user;
                })
                .orElseGet(() -> {
                    User newUser = userRepository.save(User.builder()
                            .provider(GOOGLE_PROVIDER)
                            .providerUserId(providerUserId)
                            .email(email)
                            .name(name)
                            .profileImage(picture)
                            .tokenBalance(5L) // Initial free tokens
                            .build());

                    // Create default conversation for new user
                    Conversation defaultConversation = Conversation.builder()
                            .user(newUser)
                            .title("새 대화")
                            .build();
                    conversationRepository.save(defaultConversation);

                    return newUser;
                });
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        User user = getUserOrThrow(userId);
        user.updateLastLogin();
    }

    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public boolean deductTokenForUser(Long userId) {
        User user = getUserOrThrow(userId);
        boolean deducted = user.deductToken();
        if (deducted) {
            userRepository.save(user); // Explicit save to ensure persistence
            log.debug("💾 Saved user {} after token deduction. Balance: {}", userId, user.getTokenBalance());
        }
        return deducted;
    }

    @Transactional
    public void chargeTokens(Long userId, int amount) {
        User user = getUserOrThrow(userId);
        user.addTokens((long) amount);
        userRepository.save(user);
        log.info("💰 Charged {} tokens to user {}. New balance: {}", amount, userId, user.getTokenBalance());
    }

    public User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("사용자를 찾을 수 없습니다."));
    }

    public UserPrincipal loadUserPrincipal(Long userId) {
        return UserPrincipal.from(getUserOrThrow(userId));
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = getUserOrThrow(userId);
        // Conversations and messages will be cascade deleted due to @OneToMany(cascade
        // = CascadeType.ALL, orphanRemoval = true)
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return UserPrincipal.from(user);
    }
}
