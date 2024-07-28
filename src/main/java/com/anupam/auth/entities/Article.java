package com.anupam.auth.entities;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "articles")
@Data
public class Article {
    @Id
    private ObjectId id;
    private String title;
    private String content;
    private String author;
    private int likes;
    private LocalDateTime createdAt;

    public Article() {
        createdAt = LocalDateTime.now();
    }
}
