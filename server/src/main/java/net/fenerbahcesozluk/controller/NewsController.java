package net.fenerbahcesozluk.controller;

import lombok.RequiredArgsConstructor;
import net.fenerbahcesozluk.entity.News;
import net.fenerbahcesozluk.service.NewsService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

  private final NewsService newsService;

  @GetMapping
  public ResponseEntity<Page<News>> getNews(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("pubDate").descending());
    return ResponseEntity.ok(newsService.getNews(pageable));
  }
}
