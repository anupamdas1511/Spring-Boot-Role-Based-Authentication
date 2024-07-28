package com.anupam.auth.controller;

import com.anupam.auth.entities.Article;
import com.anupam.auth.service.ArticleService;
import com.anupam.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {
//    private final UserService userService;
    private final ArticleService articleService;

    @GetMapping("/articles")
    public ResponseEntity<List<Article>> getAllArticles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<Article> articles = articleService.searchArticlesByUsername(username);
        return new ResponseEntity<>(articles, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (username == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        article.setAuthor(username);
        Article article1 = articleService.createArticle(article);
        return new ResponseEntity<>(article1, HttpStatus.CREATED);
    }

    @PutMapping("/edit")
    public ResponseEntity<?> updateArticle(@RequestBody Article article) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (username == null || !Objects.equals(article.getAuthor(), username)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Article article1 = articleService.updateArticle(article.getId(), article);
            return new ResponseEntity<>(article1, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{articleId}")
    public ResponseEntity<?> deleteArticle(@PathVariable ObjectId articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (username == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        articleService.deleteArticle(articleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
