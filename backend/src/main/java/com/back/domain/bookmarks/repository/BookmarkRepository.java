package com.back.domain.bookmarks.repository;

import com.back.domain.bookmarks.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {
    Optional<Bookmark> findById(int id);
    Page<Bookmark> findAll(Pageable pageable);
    List<Bookmark> findAll();
}
