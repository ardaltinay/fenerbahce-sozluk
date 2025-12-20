package net.fenerbahcesozluk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

  private UUID id;
  private String name;
  private String description;
  private String slug;
  private String icon;
  private Integer displayOrder;
  private Integer topicCount;
}
