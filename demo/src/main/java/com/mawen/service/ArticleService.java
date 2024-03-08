package com.mawen.service;

import com.mawen.model.Article;
import com.mawen.repository.ArticleRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2023/9/18
 */
@Service
@Slf4j
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public Article insert(Article article) {
        return articleRepository.save(article);
    }

    public Article findById(String id) {
        return articleRepository.findById(id).orElse(null);
    }


}
