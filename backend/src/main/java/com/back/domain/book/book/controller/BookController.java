package com.back.domain.book.book.controller;

import com.back.domain.book.book.dto.BookDetailDto;
import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.service.BookService;
import com.back.domain.member.member.entity.Member;
import com.back.global.dto.PageResponseDto;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "도서 관리")
public class BookController {
    private final BookService bookService;
    private final Rq rq;

    //전체 책 조회(DB내부만) - 로그인 선택사항
    @GetMapping
    @Operation(summary = "전체 책 조회")
    public RsData<PageResponseDto<BookSearchDto>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

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

        // JWT 토큰에서 사용자 정보 추출 (토큰이 없거나 유효하지 않으면 null)
        Member member = rq.getActor();

        if (member != null) {
            log.debug("로그인된 사용자로 책 조회: {}", member.getEmail());
        } else {
            log.debug("비로그인 사용자로 책 조회");
        }

        Page<BookSearchDto> books = bookService.getAllBooks(pageable, member);
        PageResponseDto<BookSearchDto> pageResponse = new PageResponseDto<>(books);

        return new RsData<>("200-1", "전체 책 조회 성공", pageResponse);
    }

    @GetMapping("/search")
    @Operation(summary = "책 검색")
    public RsData<PageResponseDto<BookSearchDto>> searchBooks(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            HttpServletRequest request) {

        if (query == null || query.trim().isEmpty()) {
            throw new ServiceException("400-6", "검색어를 입력해주세요.");
        }

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

        // JWT 토큰에서 사용자 정보 추출 (토큰이 없거나 유효하지 않으면 null)
        Member member = rq.getActor();

        Page<BookSearchDto> books = bookService.searchBooks(query.trim(), pageable, member);
        PageResponseDto<BookSearchDto> pageResponse = new PageResponseDto<>(books);

        if (books.isEmpty()) {
            return new RsData<>("200-2", "검색 결과가 없습니다.", pageResponse);
        }

        return new RsData<>("200-1", books.getTotalElements() + "개의 책을 찾았습니다.", pageResponse);
    }

    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "ISBN으로 책 검색")
    public RsData<BookSearchDto> getBookByIsbn(@PathVariable String isbn, HttpServletRequest request) {

        if (isbn == null || isbn.trim().isEmpty()) {
            throw new ServiceException("400-7", "ISBN을 입력해주세요.");
        }

        // ISBN 형식 검증 (13자리 숫자)
        String cleanIsbn = isbn.trim().replaceAll("-", "");
        if (!cleanIsbn.matches("\\d{13}")) {
            throw new ServiceException("400-8", "올바른 ISBN-13 형식이 아닙니다. (13자리 숫자)");
        }

        // JWT 토큰에서 사용자 정보 추출 (토큰이 없거나 유효하지 않으면 null)
        Member member = rq.getActor();

        BookSearchDto book = bookService.getBookByIsbn(cleanIsbn, member);

        if (book == null) {
            return new RsData<>("404-1", "해당 ISBN의 책을 찾을 수 없습니다.", null);
        }

        return new RsData<>("200-3", "ISBN으로 책 조회 성공", book);
    }

    @GetMapping("/{id}")
    @Operation(summary = "id로 책 검색 (상세 정보 포함))")
    public RsData<BookDetailDto> getBookById(
            @PathVariable int id,
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

        // JWT 토큰에서 사용자 정보 추출 (토큰이 없거나 유효하지 않으면 null)
        Member member = rq.getActor();

        BookDetailDto bookDetail = bookService.getBookDetailById(id, pageable, member);

        if (bookDetail == null) {
            return new RsData<>("404-1", "해당 ID의 책을 찾을 수 없습니다.", null);
        }

        return new RsData<>("200-4", "책 상세 조회 성공", bookDetail);
    }
}