package com.back.domain.bookmarks.repository;

import com.back.domain.bookmarks.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer>, JpaSpecificationExecutor<Bookmark> {
    Optional<Bookmark> findById(int id);
}
