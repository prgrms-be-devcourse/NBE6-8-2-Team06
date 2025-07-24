package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.entity.Bookmark;

import java.time.LocalDateTime;

public record BookmarksDto(
        int id,
        int user_id,
        int book_id,
        //book 내용 추가 예정
        String read_state,
        int read_page,
        int reading_rate,
        LocalDateTime date
        //내가 쓴 리뷰 추가 예정
) {
    public BookmarksDto(Bookmark bookmark){
        this(
                bookmark.getId(),
                bookmark.getMember().getId(),
                bookmark.getBook().getId(),
                bookmark.getReadState().toString(),
                bookmark.getReadPage(),
                calculateReadingRate(0, bookmark.getReadPage()),
                bookmark.getReadState()==ReadState.BEFORE_READING ? bookmark.getCreateDate():bookmark.getReadState()==ReadState.READING ? bookmark.getStartReadTime():bookmark.getEndReadTime()
        );
    }
    private static int calculateReadingRate(int total_page, int read_page){
        if(total_page == 0) return 0;
        if(read_page >= total_page) return 100;
        if(read_page <= 0) return 0;
        double rate = ((double) read_page/total_page) * 100;
        return (int) Math.round(rate);
    }
}
