package com.back.domain.review.review.entity;

import com.back.domain.book.book.entity.Book;
import com.back.domain.member.member.entity.Member;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Review extends BaseEntity {
    @ManyToOne
    private Member member;
    @ManyToOne
    private Book book;
    private String content;
    private int rate;


}
