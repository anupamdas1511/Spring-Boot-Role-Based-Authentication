package com.anupam.auth.service.impl;

import com.anupam.auth.entities.Article;
import com.anupam.auth.entities.User;
import com.anupam.auth.repositories.ArticleRepository;
import com.anupam.auth.repositories.UserRepository;
import com.anupam.auth.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    public Article createArticle(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public Article updateArticle(ObjectId articleId, Article article) throws Exception {
        Optional<Article> existingArticle = articleRepository.findById(articleId);
        if (existingArticle.isPresent()) {
            Article articleFromDb = existingArticle.get();
            articleFromDb.setTitle(article.getTitle());
            articleFromDb.setContent(article.getContent());
            return articleRepository.save(articleFromDb);
        }
        throw new Exception("Article Not Found");
    }

    @Override
    public Optional<Article> getArticleById(ObjectId id) {
        return articleRepository.findById(id);
    }

    @Override
    public List<Article> getAllArticles() {
        return articleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Override
    public List<Article> searchArticlesByTitle(String title) {
        return articleRepository.findByTitleContaining(title);
    }

    @Override
    public List<Article> searchArticlesByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return articleRepository.findByIdIn(user.get().getArticles());
        }
        return List.of();
    }

    @Override
    public void deleteArticle(ObjectId id) {
        articleRepository.deleteById(id);
    }
}
