package net.fenerbahcesozluk.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "categories")
public class Category extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String name;

  @Column(length = 500)
  private String description;

  @Column(name = "icon")
  private String icon;

  @Column(name = "display_order")
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "is_active")
  @Builder.Default
  private boolean isActive = true;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<Topic> topics = new ArrayList<>();
}
