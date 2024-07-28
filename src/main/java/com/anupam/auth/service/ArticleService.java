package com.anupam.auth.service;

import com.anupam.auth.entities.Article;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface ArticleService {
    Article createArticle(Article article);
    Article updateArticle(ObjectId articleId, Article article) throws Exception;
    Optional<Article> getArticleById(ObjectId id);
    List<Article> getAllArticles();
    List<Article> searchArticlesByTitle(String title);
    List<Article> searchArticlesByUsername(String username);
    void deleteArticle(ObjectId id);
}
