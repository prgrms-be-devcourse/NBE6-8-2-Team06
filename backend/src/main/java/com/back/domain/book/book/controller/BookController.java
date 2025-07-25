package com.back.domain.book.book.controller;


import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.service.BookService;
import com.back.global.dto.PageResponseDto;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    //전체 책 조회(DB내부만)
    @GetMapping
    public RsData<PageResponseDto<BookSearchDto>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        if (page < 0) {
            throw new ServiceException("400-1", "페이지 번호는 0 이상이어야 합니다.");
        }

        if (size <= 0) {
            throw new ServiceException("400-2", "페이지 크기는 1 이상이어야 합니다.");
        }

        Sort.Direction direction;
        if (sortDir.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        } else if (sortDir.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        } else {
            throw new ServiceException("400-3", "정렬 방향은 'asc' 또는 'desc'만 허용됩니다.");
        }

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<BookSearchDto> books = bookService.getAllBooks(pageable);
        PageResponseDto<BookSearchDto> pageResponse = new PageResponseDto<>(books);


        return new RsData<>("200-1", "전체 책 조회 성공", pageResponse);
    }

    @GetMapping("/search")
    public RsData<List<BookSearchDto>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "20") int limit) {

        if (query == null || query.trim().isEmpty()) {
            throw new ServiceException("400-6", "검색어를 입력해주세요.");
        }

        if (limit <= 0) {
            throw new ServiceException("400-4", "limit은 1 이상이어야 합니다.");
        }

        if (limit > 100) {
            throw new ServiceException("400-5", "limit은 100 이하여야 합니다.");
        }

        List<BookSearchDto> books = bookService.searchBooks(query.trim(), limit);

        if (books.isEmpty()) {
            return new RsData<>("200-2", "검색 결과가 없습니다.", books);
        }

        return new RsData<>("200-1", books.size() + "개의 책을 찾았습니다.", books);
    }


}
