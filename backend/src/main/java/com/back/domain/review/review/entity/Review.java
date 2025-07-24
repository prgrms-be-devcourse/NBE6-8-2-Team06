package com.back.domain.review.review.entity;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private Book book;
    private String content;
    private int rate;

    public Review(String content, int rate, Member member, Book book) {
        this.content = content;
        this.rate = rate;
        this.member = member;
        this.book = book;
    }
}
