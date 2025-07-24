package com.back.domain.note.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
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

//     bookmark 병합해야함
//    @ManyToOne
//    @JoinColumn(name = "bookmark_id")
//    private Bookmark bookmark;

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
//        this.bookmark = bookmark;
    }
}
