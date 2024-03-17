package com.mawen.controller;

import com.mawen.model.Article;
import com.mawen.service.ArticleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2023/9/18
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @PutMapping
    public Article insert(@RequestBody Article article) {
        return articleService.insert(article);
    }

    @GetMapping("/{id}")
    public Article findById(@PathVariable("id") String id) {
        return articleService.findById(id);
    }

    @GetMapping("/{pageNumber}/{pageSize}")
    public Page<Article> finalByPage(@PathVariable("pageNumber") int pageNumber, @PathVariable("pageSize") int pageSize) {
        return articleService.findAll(PageRequest.of(pageNumber, pageSize));
    }
}
