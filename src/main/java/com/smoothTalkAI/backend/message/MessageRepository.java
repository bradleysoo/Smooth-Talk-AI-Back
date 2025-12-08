package com.smoothTalkAI.backend.message;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    long countByConversationId(Long conversationId);

    void deleteByConversationId(Long conversationId);

    Optional<Message> findByIdAndConversationId(Long id, Long conversationId);
}

