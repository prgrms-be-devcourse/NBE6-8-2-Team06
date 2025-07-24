package com.back.domain.bookmarks.service;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.back.domain.book.book.entity.Book;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    public Bookmark save(Book book) {
        Bookmark bookmark = new Bookmark(book);
        return bookmarkRepository.save(bookmark);
    }
}
