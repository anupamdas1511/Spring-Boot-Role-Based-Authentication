package com.anupam.auth.repositories;

import com.anupam.auth.entities.Article;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ArticleRepository extends MongoRepository<Article, ObjectId> {
    List<Article> findByTitleContaining(String title);
    List<Article> findByIdIn(List<ObjectId> ids);
}
