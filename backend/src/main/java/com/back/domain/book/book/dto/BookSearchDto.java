package com.back.domain.book.book.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BookSearchDto {
    private int id;
    private String title;
    private String imageUrl;
    private String publisher;
    private String isbn13;
    private int totalPage;
    private LocalDateTime publishedDate;
    private float avgRate;
    private String categoryName;
    private List<String> authors;
}