package com.mawen.model;

import lombok.Data;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2023/9/18
 */
@Data
@Document(indexName = "blog", createIndex = true)
public class Article {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Keyword)
    private String desc;

}
