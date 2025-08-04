package com.back.domain.bookmarks.service;

import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.bookmarks.constant.ReadState;
import com.back.domain.bookmarks.dto.*;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.member.member.entity.Member;
import com.back.domain.review.review.entity.Review;
import com.back.domain.review.review.repository.ReviewRepository;
import com.back.domain.review.review.service.ReviewService;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    private final ReviewService reviewService;
    private final ReviewRepository reviewRepository;

    public Bookmark save(int bookId, Member member) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new NoSuchElementException("%d번 등록된 책이 없습니다.".formatted(bookId)));
        if(bookmarkRepository.existsByMemberAndBook(member, book)) {
            throw new IllegalStateException("이미 추가된 책입니다.");
        }
        Bookmark bookmark = new Bookmark(book, member);
        return bookmarkRepository.save(bookmark);
    }

    public List<BookmarkDto> toList(Member member){
        List<Bookmark> bookmarks = bookmarkRepository.findByMember(member);
        return convertToBookmarkDtoList(bookmarks, member);
    }

    //queryDsl
    public Page<BookmarkDto> toPage(Member member, int page, int size, String sort, String category, String state, String keyword) {
        String[] sorts = sort.split(",",2);
        Pageable pageable = PageRequest.of(page, size, sorts[1].equals("desc") ? Sort.by(sorts[0]).descending() : Sort.by(sorts[0]).ascending());

        Page<Bookmark> bookmarks = bookmarkRepository.search(member, category, state, keyword, pageable);
        List<BookmarkDto> dtoList = convertToBookmarkDtoList(bookmarks.getContent(), member);
        return new PageImpl<>(dtoList, pageable, bookmarks.getTotalElements());
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
        Bookmark bookmark = findByIdAndMember(id, member);
        if(endReadDate != null){
            bookmark.updateEndReadDate(endReadDate);
        }
        if(startReadDate != null){
            bookmark.updateStartReadDate(startReadDate);
        }
        if(readPage > 0){
            bookmark.updateReadPage(readPage);
        }
        if(state != null){
            ReadState readState = ReadState.valueOf(state.toUpperCase());
            bookmark.updateReadState(readState);
        }
        bookmarkRepository.flush();
        return new BookmarkModifyResponseDto(bookmark);
    }

    public void deleteBookmark(Member member, int bookmarkId) {
        Bookmark bookmark = findByIdAndMember(bookmarkId, member);
        bookmarkRepository.delete(bookmark);
        if(getReview(bookmark) != null) {
            reviewService.deleteReview(bookmark.getBook(), member);
        }
    }

    private Bookmark findByIdAndMember(int bookmarkId, Member member) {
        return bookmarkRepository.findByIdAndMember(bookmarkId, member).orElseThrow(() -> new ServiceException("403-1", "해당 북마크에 대한 권한이 없습니다."));
    }

    public BookmarkReadStatesDto getReadStatesCount(Member member, String category, String readState, String keyword) {
        ReadStateCount readStateCount = bookmarkRepository.countReadState(member, category, readState, keyword);
        long totalCount = readStateCount.READ()+readStateCount.READING()+readStateCount.WISH();
        double avgRate = 0.0;
        if(category == null && readState == null && keyword == null){
            avgRate = getAvgRate(member);
        }
        System.out.println("totalCount:"+totalCount+",avgRate:"+avgRate+",read:"+readStateCount.READ()+",reading:"+readStateCount.READING()+",wish:"+readStateCount.WISH());
        return new BookmarkReadStatesDto(
               totalCount , avgRate, readStateCount
        );
    }

    private double getAvgRate(Member member) {
        return reviewRepository.findAverageRatingByMember(member).orElse(0.0);
    }

    private Review getReview(Bookmark bookmark) {
        return reviewService.findByBookAndMember(bookmark.getBook(), bookmark.getMember()).orElse(null);
    }

    private ReadState getReadStateByMemberAndBook(Member member, Book book) {
        Optional<Bookmark> bookmark = bookmarkRepository.findByMemberAndBookWithFresh(member, book);
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

    private List<BookmarkDto> convertToBookmarkDtoList(List<Bookmark> bookmarks, Member member) {
        if(bookmarks == null || bookmarks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Review> reviews = reviewRepository.findAllByMember(member);
        Map<Integer, Review> reviewMap = reviews.stream().collect(Collectors.toMap(review -> review.getBook().getId(), review -> review));


        return bookmarks.stream().map(bookmark -> {
            Review review = reviewMap.get(bookmark.getBook().getId());
            return new BookmarkDto(bookmark, review);
        }).toList();

    }

    public Bookmark getLatestBookmark(Member member) {
        return bookmarkRepository.getBookmarkByMemberOrderByIdDesc(member).orElseThrow(() -> new NoSuchElementException("조회가능한 북마크가 없습니다."));
    }
}
