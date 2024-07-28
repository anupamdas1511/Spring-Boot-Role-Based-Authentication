package com.anupam.auth.entities;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Profile {
    private String username;
    private List<Article> articles = new ArrayList<>();
}
