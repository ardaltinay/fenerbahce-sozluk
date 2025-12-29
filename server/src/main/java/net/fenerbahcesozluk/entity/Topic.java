package net.fenerbahcesozluk.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "topics", indexes = {@Index(name = "idx_topic_active_entrycount", columnList = "is_active, entry_count"),
        @Index(name = "idx_topic_active_created", columnList = "is_active, created_time"),
        @Index(name = "idx_topic_title", columnList = "title")})
public class Topic extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "entry_count")
    @Builder.Default
    private Integer entryCount = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "is_locked")
    @Builder.Default
    private boolean isLocked = false;

    @Column(name = "is_pinned")
    @Builder.Default
    private boolean isPinned = false;

    @Column(name = "is_active")
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "delete_reason")
    private String deleteReason;

    // Transfermarkt integration
    @Column(name = "topic_type")
    private String topicType; // "player", "club", "general"

    @Column(name = "transfermarkt_id")
    private String transfermarktId;

    @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Entry> entries = new ArrayList<>();
}
