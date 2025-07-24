package com.back.domain.bookmarks.service;

import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.dto.BookmarkDetailDto;
import com.back.domain.bookmarks.dto.BookmarkDto;
import com.back.domain.bookmarks.dto.BookmarkModifyResponseDto;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import jakarta.persistence.criteria.Join;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.back.domain.book.book.entity.Book;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public Bookmark save(int bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("%d번 등록된 책이 없습니다.".formatted(bookId)));
        Bookmark bookmark = new Bookmark(book);
        return bookmarkRepository.save(bookmark);
    }

    public List<BookmarkDto> toList(){
        return bookmarkRepository.findAll().stream().map(BookmarkDto::new).toList();
    }

    public Page<BookmarkDto> toPage(int pageNumber, int pageSize, String category, String state, String keyword){
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
                        builder.like(bookJoin.get("authors").get("author").get("name"), "%"+keyword+"%")
                );
            });
        }
        Page<Bookmark> bookmarks = bookmarkRepository.findAll(spec, pageable);
        return bookmarks.map(BookmarkDto::new);
    }

    public BookmarkDetailDto getBookmarkById(int bookmarkId) {
        Bookmark bookmark = findById(bookmarkId);
        Review review = new Review();
        return new BookmarkDetailDto(bookmark, review);
    }

    public Bookmark findById(int bookmarkId) {
        return bookmarkRepository.findById(bookmarkId).orElseThrow(() -> new NoSuchElementException("%d번 데이터가 없습니다.".formatted(bookmarkId)));
    }

    public BookmarkModifyResponseDto modifyBookmarkById(int id, String state, LocalDateTime startReadDate, LocalDateTime endReadDate, int readPage) {
        Bookmark bookmark = findById(id);
        if(state != null){
            ReadState readState = ReadState.valueOf(state.toUpperCase());
            bookmark.updateReadState(readState);
        }
        if(startReadDate != null){
            bookmark.updateStartReadDate(startReadDate);
        }
        if(endReadDate != null){
            bookmark.updateEndReadDate(endReadDate);
        }
        if(readPage > 0){
            bookmark.updateReadPage(readPage);
        }
        return new BookmarkModifyResponseDto(bookmarkRepository.save(bookmark));
    }

    public void deleteBookmarkById(int bookmarkId) {
        bookmarkRepository.deleteById(bookmarkId);
    }
}
