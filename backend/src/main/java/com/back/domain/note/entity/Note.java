package com.back.domain.note.entity;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Note extends BaseEntity {
    @Column(nullable = false, length = 5000)
    private String title;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(nullable = true)
    private String page;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id")
    private Bookmark bookmark;

    public Note(String title, String content, String page, Bookmark bookmark) {
        this.title = title;
        this.content = content;
        this.page = page;
        this.bookmark = bookmark;
    }
}
