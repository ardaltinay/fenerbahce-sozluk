package net.fenerbahcesozluk.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "news", indexes = {@Index(name = "idx_news_pub_date", columnList = "pubDate"),
        @Index(name = "idx_news_guid", columnList = "guid", unique = true)})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String link;

    @Column(length = 2000)
    private String description;

    @Column(length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private String source; // e.g. "TRT Spor"

    @Column(nullable = false)
    private LocalDateTime pubDate;

    @Column(unique = true)
    private String guid; // Unique identifier from RSS to prevent duplicates

}
