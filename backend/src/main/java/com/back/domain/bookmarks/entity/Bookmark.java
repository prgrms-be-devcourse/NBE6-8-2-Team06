package com.back.domain.bookmarks.entity;

import com.back.domain.bookmarks.constant.ReadState;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Bookmark extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ReadState readState;
    private int readPage;
    private LocalDateTime startReadTime;
    private LocalDateTime endReadTime;

    /*
    @OneToMany(cascade = {mappedBy = "bookmark" CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true) //임시 맵핑 - note
    private List<String> notes = new ArrayList<>();

    @OneToOne
    @JoinColumn("book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
     */
}
