package com.ide.project.domain.feedback.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id", nullable = false)
    private Long submissionId;

    @Column(name = "mentor_id", nullable = false)
    private Long mentorId;

    @Column(name = "mentor_nickname", nullable = false)
    private String mentorNickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    // HIGHLIGHT 전용 필드
    @Column(name = "start_line")
    private Integer startLine;

    @Column(name = "end_line")
    private Integer endLine;

    @Column(name = "start_char")
    private Integer startChar;

    @Column(name = "end_char")
    private Integer endChar;

    @Column(length = 10)
    private String color;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Feedback(Long submissionId, Long mentorId, String mentorNickname, FeedbackType type,
                    String content, Integer startLine, Integer endLine, Integer startChar, Integer endChar, String color) {
        this.submissionId = submissionId;
        this.mentorId = mentorId;
        this.mentorNickname = mentorNickname;
        this.type = type;
        this.content = content;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startChar = startChar;
        this.endChar = endChar;
        this.color = color;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
