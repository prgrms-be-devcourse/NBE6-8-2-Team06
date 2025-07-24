package com.back.domain.bookmarks.service;

import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.dto.BookmarksDto;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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

    public Page<BookmarksDto> toPage(int pageNumber, int pageSize, String category, String state, String keyword){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Specification<Bookmark> spec = ((root, query, criteriaBuilder) -> null);
        if(category != null){
            spec = spec.and((root, query, builder) -> {
                Join<Bookmark, Book> bookJoin = root.join("book");
                return builder.equal(bookJoin.get("category").get("name"), category);
            });
        }
        if(state != null){
            ReadState readState = ReadState.valueOf(state.toUpperCase());
            spec = spec.and((root, query, builder) -> builder.equal(root.get("state"), readState));
        }
        if(keyword != null){
            spec = spec.and((root, query, builder) -> {
                Join<Bookmark, Book> bookJoin = root.join("book");
                return builder.or(
                        builder.like(bookJoin.get("title"), "%"+keyword+"%"),
                        builder.like(bookJoin.get("author"), "%"+keyword+"%")
                );
            });
        }
        Page<Bookmark> bookmarks = bookmarkRepository.findAll(spec, pageable);
        return bookmarks.map(BookmarksDto::new);
    }

    public Bookmark getBookmarkById(int bookmarkId) {
        return bookmarkRepository.findById(bookmarkId).orElseThrow(() -> new NoSuchElementException("%d번 데이터가 없습니다.".formatted(bookmarkId)));
    }
}
