package com.ide.project.domain.space.entity;

import com.ide.project.domain.user.entity.User;
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
@Table(name = "spaces")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    @Column(name = "invite_code", length = 20, unique = true)
    private String inviteCode;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 원하는 값만 노출하기 위한 builder
    @Builder
    public Space(User mentor, String name, String description, String inviteCode) {
        this.mentor = mentor;
        this.name = name;
        this.description = description;
        this.inviteCode = inviteCode;
        this.isPublic = false;
        this.isActive = true;
    }

    // 업데이트때 사용
    public void update(String name, String description, Boolean isActive) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (isActive != null) this.isActive = isActive;
    }

}

