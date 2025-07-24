package com.back.domain.bookmarks.service;

import com.back.domain.bookmarks.dto.BookmarksDto;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.back.domain.book.book.entity.Book;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;

    public Bookmark save(Book book) {
        Bookmark bookmark = new Bookmark(book);
        return bookmarkRepository.save(bookmark);
    }

    public List<Bookmark> toList(){
        return bookmarkRepository.findAll();
    }

    public Page<BookmarksDto> toPage(int pageNumber, int pageSize){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Bookmark> bookmarks = bookmarkRepository.findAll(pageable);
        return bookmarks.map(BookmarksDto::new);
    }

    public Bookmark getBookmarkById(int bookmarkId) {
        return bookmarkRepository.findById(bookmarkId).orElseThrow(() -> new NoSuchElementException("%d번 데이터가 없습니다.".formatted(bookmarkId)));
    }
}
