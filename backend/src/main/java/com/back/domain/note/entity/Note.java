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

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Note extends BaseEntity {
    private String title;
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id")
    private Bookmark bookmark;

    public Note(String title, String content, Bookmark bookmark) {
        this.title = title;
        this.content = content;
        this.bookmark = bookmark;
    }
}
