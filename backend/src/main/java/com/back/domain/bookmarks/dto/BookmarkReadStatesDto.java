package com.back.domain.bookmarks.dto;

public record BookmarkReadStatesDto(
        int totalCount,
        double avgRate,
        ReadStateCount readState
) {

}
