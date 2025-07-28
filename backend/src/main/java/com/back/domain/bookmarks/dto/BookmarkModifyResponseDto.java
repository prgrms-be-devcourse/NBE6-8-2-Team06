package com.back.domain.bookmarks.dto;

import com.back.domain.bookmarks.entity.Bookmark;

public record BookmarkModifyResponseDto(
        BookmarkDto bookmark
) {
    public BookmarkModifyResponseDto(Bookmark bookmark){
        this(
                new BookmarkDto(bookmark, null)
        );
    }
}
