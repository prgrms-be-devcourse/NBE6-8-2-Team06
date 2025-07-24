package com.back.domain.bookmarks.controller;

import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.book.service.BookService;
import com.back.domain.bookmarks.dto.BookmarkCreateRequestDto;
import com.back.domain.bookmarks.dto.BookmarkCreateResponseDto;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.service.BookmarkService;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.back.domain.book.book.entity.Book;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final BookRepository bookRepository;

    @PostMapping
    public RsData<BookmarkCreateResponseDto> addBookmark(@RequestBody BookmarkCreateRequestDto bookmarkCreateRequestDto) {
        Book book = bookRepository.findById(bookmarkCreateRequestDto.bookId()).get();
        Bookmark bookmark = bookmarkService.save(book);
        return new RsData<>(
                    "201-1",
                    "%d 번 책이 내 책 목록에 추가되었습니다.",
                    new BookmarkCreateResponseDto(bookmark)
                );
    }
}
