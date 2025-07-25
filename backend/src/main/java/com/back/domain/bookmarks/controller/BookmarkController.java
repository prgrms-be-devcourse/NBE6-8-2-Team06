package com.back.domain.bookmarks.controller;

import com.back.domain.bookmarks.dto.*;
import com.back.domain.bookmarks.service.BookmarkService;
import com.back.domain.member.member.entity.Member;
import com.back.global.dto.PageResponseDto;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final Rq rq;

    @PostMapping
    @Transactional
    public RsData<Void> addBookmark(@RequestBody BookmarkCreateRequestDto bookmarkCreateRequestDto) {
        Member member = rq.getActor();
        bookmarkService.save(bookmarkCreateRequestDto.bookId(), member);
        return new RsData<>(
                    "201-1",
                    "%d 번 책이 내 책 목록에 추가되었습니다.",
                    null
                );
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public RsData<BookmarkDetailDto> getBookmark(@PathVariable int id) {
        BookmarkDetailDto bookmark = bookmarkService.getBookmarkById(id);
        return new RsData<>("200-1", "%d번 조회 성공".formatted(id), bookmark);
    }

    @GetMapping("/list")
    @Transactional(readOnly = true)
    public RsData<List<BookmarkDto>> getBookmarksToList() {
        List<BookmarkDto> bookmarks = bookmarkService.toList();
        if(bookmarks.isEmpty()){
            return new RsData<>("404-1", "데이터가 없습니다.", null);
        }
        return new RsData<>("200-1", "%d개 조회 성공".formatted(bookmarks.size()), bookmarks);
    }

    @GetMapping("")
    @Transactional(readOnly = true)
    public RsData<PageResponseDto<BookmarkDto>> getBookmarksToPage(@RequestParam(value = "page", defaultValue = "0")int page,
                                                                         @RequestParam(value = "size", defaultValue = "10")int size,
                                                                         @RequestParam(value = "category", required = false) String category,
                                                                         @RequestParam(value = "read_state", required = false) String read_state,
                                                                         @RequestParam(value = "keyword", required = false) String keyword) {
        Page<BookmarkDto> bookmarkDtoPage = bookmarkService.toPage(page, size, category, read_state, keyword);
        if(bookmarkDtoPage.isEmpty()){
            return new RsData<>("404-1", "데이터가 없습니다.", null);
        }
        return new RsData<>("200-1", "%d번 페이지 조회 성공".formatted(page), new PageResponseDto<>(bookmarkDtoPage));
    }

    @PutMapping("/{id}")
    @Transactional
    public RsData<BookmarkModifyResponseDto> modifyBookmark(@PathVariable int id, @RequestBody BookmarkModifyRequestDto bookmarkModifyRequestDto) {
        Member member = rq.getActor();
        BookmarkModifyResponseDto bookmark = bookmarkService.modifyBookmark(member, id, bookmarkModifyRequestDto.readState(), bookmarkModifyRequestDto.startReadDate(), bookmarkModifyRequestDto.endReadDate(), bookmarkModifyRequestDto.readPage());
        return new RsData<>(
                "200-1",
                "%d번 북마크가 수정되었습니다.",
                bookmark
        );
    }

    @DeleteMapping("/{id}")
    @Transactional
    public RsData<Void> deleteBookmark(@PathVariable int id) {
        Member member = rq.getActor();
        bookmarkService.deleteBookmark(member, id);
        return new RsData<>("200-1", "%d 북마크가 삭제되었습니다.".formatted(id), null);
    }

}
