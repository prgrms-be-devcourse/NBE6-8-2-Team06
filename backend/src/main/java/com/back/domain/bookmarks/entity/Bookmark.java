package com.back.domain.bookmarks.entity;

import com.back.domain.book.book.entity.Book;
import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.member.member.entity.Member;
import com.back.domain.note.entity.Note;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Bookmark extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ReadState readState;
    private int readPage;
    private LocalDateTime startReadDate;
    private LocalDateTime endReadDate;

    public Bookmark(Book book) {
        this.book = book;
    }

    public void updateReadState(ReadState readState) {
        this.readState = readState;
    }

    public void updateReadPage(int readPage) {
        this.readPage = readPage;
    }
    public void updateStartReadDate(LocalDateTime startReadDate) {
        this.startReadDate = startReadDate;
    }
    public void updateEndReadDate(LocalDateTime endReadDate) {
        this.endReadDate = endReadDate;
    }

    @OneToMany(mappedBy = "bookmark", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true) //임시 맵핑 - note
    private List<Note> notes = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    public int calculateReadingRate(){
        int totalPage = book.getTotalPage();
        if(totalPage == 0) return 0;
        if(readPage >= totalPage) return 100;
        if(readPage <= 0) return 0;
        double rate = ((double) readPage/totalPage) * 100;
        return (int) Math.round(rate);
    }

    public long calculateReadingDuration(){
        LocalDateTime effectiveEnd = (endReadDate == null) ? LocalDateTime.now() : endReadDate;
        return ChronoUnit.DAYS.between(startReadDate, effectiveEnd);
    }

    public LocalDateTime getDisplayDate(){
        return readState==ReadState.BEFORE_READING ? getCreateDate():readState==ReadState.READING ? startReadDate:endReadDate;
    }
}
