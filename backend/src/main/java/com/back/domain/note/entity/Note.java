package com.back.domain.note.entity;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Note extends BaseEntity {
    private String title;
    private String content;
    private String page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id")
    private Bookmark bookmark;

    public Note(String title, String content, String page, Bookmark bookmark) {
        this.title = title;
        this.content = content;
        this.bookmark = bookmark;
        this.page = page;
    }

    public String CreateDateParsing(LocalDateTime createDate) {
        // LocalDate로 변환 (시간 제외)
        String date = createDate.toLocalDate().toString();

        return date;
    }

    public String UpdateDateParsing(LocalDateTime updateDate) {
        // LocalDate로 변환 (시간 제외)
        String date = updateDate.toLocalDate().toString();

        return date;
    }
}
