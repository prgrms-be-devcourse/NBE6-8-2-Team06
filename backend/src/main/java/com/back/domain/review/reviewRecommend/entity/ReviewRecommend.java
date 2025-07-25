package com.back.domain.review.reviewRecommend.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class ReviewRecommend extends BaseEntity {
    boolean isRecommended;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;

    public ReviewRecommend(Review review, Member member, boolean isRecommended) {
        this.review = review;
        this.member = member;
        this.isRecommended = isRecommended;
    }
}
