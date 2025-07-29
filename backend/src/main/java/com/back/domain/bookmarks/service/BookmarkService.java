package com.back.domain.bookmarks.service;

import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.dto.*;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.member.member.entity.Member;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;

    public Bookmark save(int bookId, Member member) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("%d번 등록된 책이 없습니다.".formatted(bookId)));
        Bookmark bookmark = new Bookmark(book, member);
        return bookmarkRepository.save(bookmark);
    }

    public List<BookmarkDto> toList(Member member){
        return bookmarkRepository.findByMember(member).stream().map(bookmark -> {
            if(bookmark.getReadState()==ReadState.WISH) return new BookmarkDto(bookmark, null);
            Review review = getReviews(bookmark.getMember()).get(bookmark.getBook());
            return new BookmarkDto(bookmark, review);
        }).toList();
    }

    public Page<BookmarkDto> toPage(Member member, int pageNumber, int pageSize, String category, String state, String keyword){
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Specification<Bookmark> spec = ((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("member"), member));
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
        return bookmarks.map(bookmark -> {
            if(bookmark.getReadState()==ReadState.WISH) return new BookmarkDto(bookmark, null);
            Review review = getReviews(bookmark.getMember()).get(bookmark.getBook());
            return new BookmarkDto(bookmark, review);
        });
    }

    public BookmarkDetailDto getBookmarkById(Member member, int bookmarkId) {
        Bookmark bookmark = bookmarkRepository.findByIdAndMember(bookmarkId, member).orElseThrow(() -> new NoSuchElementException("%d번 데이터가 없습니다.".formatted(bookmarkId)));
        Review review = getReview(bookmark);
        return new BookmarkDetailDto(bookmark, review);
    }

    public Bookmark findById(int bookmarkId) {
        return bookmarkRepository.findById(bookmarkId).orElseThrow(() -> new NoSuchElementException("%d번 데이터가 없습니다.".formatted(bookmarkId)));
    }

    public BookmarkModifyResponseDto modifyBookmark(Member member, int id, String state, LocalDateTime startReadDate, LocalDateTime endReadDate, int readPage) {
        Bookmark bookmark = findById(id);
        bookmark.checkActor(member);
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

    public void deleteBookmark(Member member, int bookmarkId) {
        Bookmark bookmark = findById(bookmarkId);
        bookmark.checkActor(member);
        bookmarkRepository.delete(bookmark);
    }

    public BookmarkReadStatesDto getReadStatesCount(Member member) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMember(member);
        Map<ReadState, Long> countByReadState = bookmarks.stream().collect(Collectors.groupingBy(Bookmark::getReadState, Collectors.counting()));
        ReadStateCount readStateCount = new ReadStateCount(countByReadState.getOrDefault(ReadState.READ, 0L),
                countByReadState.getOrDefault(ReadState.READING, 0L),
                countByReadState.getOrDefault(ReadState.WISH, 0L));
        double avgRate = reviewRepository.findAverageRatingByMember(member).orElse(0.0);
        return new BookmarkReadStatesDto(
                bookmarks.size(), avgRate, readStateCount
        );
    }

    private Review getReview(Bookmark bookmark) {
        return reviewRepository.findByBookAndMember(bookmark.getBook(), bookmark.getMember()).orElse(null);
    }
    private Map<Book, Review> getReviews(Member member) {
        List<Review> reviews = reviewRepository.findAllByMember(member);
        return reviews.stream().collect(Collectors.toMap(Review::getBook, review -> review));
    }

    public ReadState getReadStateByMemberAndBook(Member member, Book book) {
        Optional<Bookmark> bookmark = bookmarkRepository.findByMemberAndBook(member, book);
        return bookmark.map(Bookmark::getReadState).orElse(null);
    }

    public ReadState getReadStateByMemberAndBookId(Member member, int bookId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book == null) {
            return null;
        }
        return getReadStateByMemberAndBook(member, book);
    }

    public Map<Integer, ReadState> getReadStatesForBooks(Member member, List<Integer> bookIds) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMember(member);

        return bookmarks.stream()
                .filter(bookmark -> bookIds.contains(bookmark.getBook().getId()))
                .collect(Collectors.toMap(
                        bookmark -> bookmark.getBook().getId(),
                        Bookmark::getReadState
                ));
    }
}
