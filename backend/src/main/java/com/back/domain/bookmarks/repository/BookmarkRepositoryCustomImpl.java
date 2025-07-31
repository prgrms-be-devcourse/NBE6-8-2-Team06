package com.back.domain.bookmarks.repository;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.entity.Member;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class BookmarkRepositoryCustomImpl implements BookmarkRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Bookmark> search(Member member, String category, String readState, String keyword, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
    }

}
