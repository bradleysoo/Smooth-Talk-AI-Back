package com.smoothTalkAI.backend.conversation;

import com.smoothTalkAI.backend.common.BaseTimeEntity;
import com.smoothTalkAI.backend.common.JsonListConverter;
import com.smoothTalkAI.backend.common.JsonMapConverter;
import com.smoothTalkAI.backend.message.Message;
import com.smoothTalkAI.backend.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversations")
public class Conversation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "analysis_summary", columnDefinition = "text")
    private String analysisSummary;

    @Convert(converter = JsonListConverter.class)
    @Column(name = "analysis_advice", columnDefinition = "text")
    private List<String> analysisAdvice;

    @Convert(converter = JsonListConverter.class)
    @Column(name = "analysis_sample_replies", columnDefinition = "text")
    private List<String> analysisSampleReplies;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "analysis_message_frequency", columnDefinition = "text")
    private Map<String, Integer> analysisMessageFrequency;

    @Convert(converter = JsonMapConverter.class)
    @Column(name = "analysis_time_frequency", columnDefinition = "text")
    private Map<String, Integer> analysisTimeFrequency;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    public void changeTitle(String newTitle) {
        this.title = newTitle;
        touch();
    }

    public void renameWithSnippet(String messageText) {
        if (messageText == null || messageText.isBlank()) {
            return;
        }
        String snippet = messageText.length() > 50 ? messageText.substring(0, 50) + "..." : messageText;
        changeTitle(snippet);
    }

    public void applyAnalysis(String summary, List<String> advice, List<String> samples,
            Map<String, Integer> messageFrequency, Map<String, Integer> timeFrequency) {
        this.analysisSummary = summary;
        this.analysisAdvice = advice;
        this.analysisSampleReplies = samples;
        this.analysisMessageFrequency = messageFrequency;
        this.analysisTimeFrequency = timeFrequency;
        touch();
    }

    public void markUpdated() {
        touch();
    }
}
