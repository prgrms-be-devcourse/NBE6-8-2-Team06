package com.back.domain.bookmarks.repository;

import com.back.domain.book.book.entity.Book;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.member.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>, JpaSpecificationExecutor<Bookmark> {
    Optional<Bookmark> findById(int id);
    Optional<Bookmark> findByBook(Book book);
    List<Bookmark> findByMember(Member member);
}
