package com.back.domain.bookmarks.entity;

import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.note.entity.Note;
import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.back.domain.book.book.entity.Book;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Bookmark extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ReadState readState;
    private int readPage;
    private LocalDateTime startReadTime;
    private LocalDateTime endReadTime;

    public Bookmark(Book book) {
        this.book = book;
    }

    public void updateReadState(ReadState readState) {
        this.readState = readState;
    }

    public void updateReadPage(int readPage) {
        this.readPage = readPage;
    }
    public void updateStartReadTime(LocalDateTime startReadTime) {
        this.startReadTime = startReadTime;
    }
    public void updateEndReadTime(LocalDateTime endReadTime) {
        this.endReadTime = endReadTime;
    }

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true) //임시 맵핑 - note
    private List<Note> notes = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
