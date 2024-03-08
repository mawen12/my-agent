package com.mawen.repository;

import com.mawen.model.Article;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2023/9/18
 */
@Repository
public interface ArticleRepository extends ElasticsearchRepository<Article, String> {



}
