package com.back.domain.bookmarks.controller;

import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.bookmarks.dto.BookmarkCreateRequestDto;
import com.back.domain.bookmarks.dto.BookmarkCreateResponseDto;
import com.back.domain.bookmarks.dto.BookmarkDto;
import com.back.domain.bookmarks.dto.BookmarksDto;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.service.BookmarkService;
import com.back.global.dto.PageResponseDto;
import com.back.global.rsData.RsData;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.back.domain.book.book.entity.Book;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final BookRepository bookRepository;

    @PostMapping
    @Transactional
    public RsData<BookmarkCreateResponseDto> addBookmark(@RequestBody BookmarkCreateRequestDto bookmarkCreateRequestDto) {
        Book book = bookRepository.findById(bookmarkCreateRequestDto.bookId()).get();
        Bookmark bookmark = bookmarkService.save(book);
        return new RsData<>(
                    "201-1",
                    "%d 번 책이 내 책 목록에 추가되었습니다.",
                    new BookmarkCreateResponseDto(bookmark)
                );
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public RsData<BookmarkDto> getBookmark(@PathVariable int id) {
        Bookmark bookmark = bookmarkService.getBookmarkById(id);
        return new RsData<>("200-1", "%d번 조회 성공".formatted(id), new BookmarkDto(bookmark));
    }

    @GetMapping("/list")
    @Transactional(readOnly = true)
    public RsData<List<BookmarksDto>> getBookmarksToList() {
        List<Bookmark> bookmarks = bookmarkService.toList();
        if(bookmarks.isEmpty()){
            return new RsData<>("404-1", "데이터가 없습니다.", null);
        }
        return new RsData<>("200-1", "%d개 조회 성공".formatted(bookmarks.size()), bookmarks.stream().map(BookmarksDto::new).collect(Collectors.toList()));
    }

    @GetMapping("")
    @Transactional(readOnly = true)
    public RsData<PageResponseDto<BookmarksDto>> getBookmarksToPage(@RequestParam(value = "page", defaultValue = "0")int page,
                                                                    @RequestParam(value = "size", defaultValue = "10")int size,
                                                                    @RequestParam(value = "category", required = false) String category,
                                                                    @RequestParam(value = "read_state", required = false) String read_state,
                                                                    @RequestParam(value = "keyword", required = false) String keyword) {
        Page<BookmarksDto> bookmarksDtoPage = bookmarkService.toPage(page, size, category, read_state, keyword);
        if(bookmarksDtoPage.isEmpty()){
            return new RsData<>("404-1", "데이터가 없습니다.", null);
        }
        return new RsData<>("200-1", "%d번 페이지 조회 성공".formatted(page), new PageResponseDto<>(bookmarksDtoPage));
    }
}
