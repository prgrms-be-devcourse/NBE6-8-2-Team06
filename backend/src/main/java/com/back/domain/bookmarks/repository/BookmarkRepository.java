package com.back.domain.bookmarks.repository;

import com.back.domain.bookmarks.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookmarkRepository extends JpaRepository<Bookmark, Integer> {
}
