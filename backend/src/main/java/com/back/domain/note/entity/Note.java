package com.back.domain.note.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Note extends BaseEntity {
    private String title;
    private String content;

//    // 연관관계 매핑(엔티티 병합 전)
//    @ManyToOne
//    private Bookmark bookmark;
}
