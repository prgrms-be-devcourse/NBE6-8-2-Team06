package com.back.domain.book.book.service;

import com.back.domain.book.author.entity.Author;
import com.back.domain.book.author.repository.AuthorRepository;
import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.book.client.aladin.AladinApiClient;
import com.back.domain.book.client.aladin.dto.AladinBookDto;
import com.back.domain.book.wrote.entity.Wrote;
import com.back.domain.book.wrote.repository.WroteRepository;
import com.back.domain.review.review.entity.Review;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final WroteRepository wroteRepository;
    private final AladinApiClient aladinApiClient;

    /**
     * 하이브리드 접근방식 - DB 우선, 없으면 API 검색
     */
    @Transactional
    public List<BookSearchDto> searchBooks(String query, int limit) {
        // 1. DB에서 먼저 확인
        List<Book> booksFromDb = bookRepository.findByTitleOrAuthorContaining(query);
        if (!booksFromDb.isEmpty()) {
            log.info("DB에서 찾은 책: {} 권", booksFromDb.size());
            return convertToDto(booksFromDb.stream().limit(limit).toList());
        }

        log.info("DB에 없어서 알라딘 API에서 검색: {}", query);

        // 2. API에서 검색
        List<AladinBookDto> apiBooks = aladinApiClient.searchBooks(query, limit);

        // 3. API 결과를 엔티티로 변환하고 저장
        List<Book> savedBooks = apiBooks.stream()
                .map(this::convertAndSaveBook)
                .filter(book -> book != null)
                .collect(Collectors.toList());

        // 4. 상세 정보 보완
        savedBooks = enrichMissingDetails(savedBooks);

        return convertToDto(savedBooks);
    }

    /**
     * ISBN으로 책 조회 - DB에 없으면 API에서 가져와서 저장
     */
    @Transactional
    public BookSearchDto getBookByIsbn(String isbn) {
        Optional<Book> bookFromDb = bookRepository.findByIsbn13(isbn);

        if (bookFromDb.isPresent()) {
            return convertToDto(bookFromDb.get());
        }

        // API에서 검색
        AladinBookDto apiBook = aladinApiClient.getBookByIsbn(isbn);
        if (apiBook == null) {
            return null;
        }

        Book savedBook = convertAndSaveBook(apiBook);
        return savedBook != null ? convertToDto(savedBook) : null;
    }

    /**
     * 전체 책 조회 (페이징)
     */
    public Page<BookSearchDto> getAllBooks(Pageable pageable) {
        log.info("전체 책 조회: page={}, size={}, sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<Book> bookPage = bookRepository.findAll(pageable);
            return bookPage.map(this::convertToDto);
        } catch (Exception e) {
            log.error("전체 책 조회 중 오류 발생: {}", e.getMessage());
            throw new ServiceException("500-1", "전체 책 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 책 평균 평점 업데이트
     */
    @Transactional
    public void updateBookAvgRate(Book book) {
        float avgRate = calculateAvgRateForBook(book);
        book.setAvgRate(avgRate);
        bookRepository.save(book);
        log.info("책 평균 평점 업데이트: {} -> {}", book.getTitle(), avgRate);
    }

    /**
     * AladinBookDto를 Book 엔티티로 변환하고 저장
     */
    private Book convertAndSaveBook(AladinBookDto apiBook) {
        try {
            // 이미 존재하는 책인지 확인
            if (apiBook.getIsbn13() != null) {
                Optional<Book> existingBook = bookRepository.findByIsbn13(apiBook.getIsbn13());
                if (existingBook.isPresent()) {
                    log.info("이미 존재하는 ISBN: {}", apiBook.getIsbn13());
                    return existingBook.get();
                }
            }

            // Book 엔티티 생성
            Book book = new Book();
            book.setTitle(apiBook.getTitle());
            book.setImageUrl(apiBook.getImageUrl());
            book.setPublisher(apiBook.getPublisher());
            book.setIsbn13(apiBook.getIsbn13());
            book.setTotalPage(apiBook.getTotalPage());
            book.setPublishedDate(apiBook.getPublishedDate());
            book.setAvgRate(0.0f);

            // 카테고리 설정
            String categoryName = extractCategoryFromPath(apiBook.getCategoryName(), apiBook.getMallType());
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> {
                        log.info("새 카테고리 생성: {}", categoryName);
                        return categoryRepository.save(new Category(categoryName));
                    });
            book.setCategory(category);

            // 책 저장
            Book savedBook = bookRepository.save(book);
            log.info("책 저장 완료: {}", savedBook.getTitle());

            // 작가 정보 저장
            saveAuthors(apiBook.getAuthors(), savedBook);

            return savedBook;

        } catch (Exception e) {
            log.error("책 저장 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 작가 정보 저장
     */
    private void saveAuthors(List<String> authorNames, Book book) {
        if (authorNames == null || authorNames.isEmpty()) {
            return;
        }

        for (String authorName : authorNames) {
            try {
                // 작가가 이미 존재하는지 확인
                Author author = authorRepository.findByName(authorName)
                        .orElseGet(() -> authorRepository.save(new Author(authorName)));

                // 이미 이 책과 작가의 관계가 존재하는지 확인
                boolean relationExists = wroteRepository.existsByAuthorAndBook(author, book);
                if (!relationExists) {
                    Wrote wrote = new Wrote(author, book);
                    wroteRepository.save(wrote);
                    log.info("작가-책 관계 저장: {} - {}", authorName, book.getTitle());
                }
            } catch (Exception e) {
                log.error("작가 정보 저장 중 오류: {} - {}", authorName, e.getMessage());
            }
        }
    }

    /**
     * 부족한 상세 정보 보완
     */
    private List<Book> enrichMissingDetails(List<Book> books) {
        return books.stream()
                .peek(book -> {
                    if (needsDetailEnrichment(book)) {
                        log.info("상세 정보 보완 시도: {} (ISBN: {})", book.getTitle(), book.getIsbn13());
                        enrichBookWithDetailInfo(book);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * 상세 정보 보완이 필요한지 판단
     */
    private boolean needsDetailEnrichment(Book book) {
        return book.getIsbn13() != null &&
                (book.getTotalPage() == 0 || book.getAuthors().isEmpty());
    }

    /**
     * 개별 ISBN 조회로 상세 정보 보완
     */
    private void enrichBookWithDetailInfo(Book book) {
        AladinBookDto detailBook = aladinApiClient.getBookDetails(book.getIsbn13());
        if (detailBook == null) {
            return;
        }

        boolean updated = false;

        // 페이지 수 보완
        if (book.getTotalPage() == 0 && detailBook.getTotalPage() > 0) {
            book.setTotalPage(detailBook.getTotalPage());
            updated = true;
        }

        // 저자 정보 보완
        if (book.getAuthors().isEmpty() && detailBook.getAuthors() != null) {
            saveAuthors(detailBook.getAuthors(), book);
            updated = true;
        }

        if (updated) {
            bookRepository.save(book);
            log.info("상세 정보 보완 완료: {} (페이지: {}, 저자: {})",
                    book.getTitle(), book.getTotalPage(), book.getAuthors().size());
        }
    }

    /**
     * 카테고리 경로에서 2번째 깊이 추출
     */
    private String extractCategoryFromPath(String categoryName, String mallType) {
        if (categoryName != null && !categoryName.isEmpty()) {
            log.debug("원본 카테고리 경로: {}", categoryName);

            String[] categoryParts = categoryName.split(">");

            if (categoryParts.length > 1) {
                String secondLevelCategory = categoryParts[1].trim();
                log.debug("추출된 카테고리: {}", secondLevelCategory);
                return secondLevelCategory;
            }

            if (categoryParts.length > 0) {
                String firstLevelCategory = categoryParts[0].trim();
                log.debug("첫 번째 레벨 카테고리 사용: {}", firstLevelCategory);
                return firstLevelCategory;
            }
        }

        // 기본 카테고리
        String fallbackCategory = getFallbackCategory(mallType);
        log.debug("기본 카테고리 사용: {}", fallbackCategory);
        return fallbackCategory;
    }

    /**
     * mallType 기반 기본 카테고리
     */
    private String getFallbackCategory(String mallType) {
        if (mallType == null) {
            return "기타";
        }

        switch (mallType) {
            case "BOOK":
                return "국내도서";
            case "FOREIGN":
                return "외국도서";
            case "EBOOK":
                return "전자책";
            default:
                return "기타";
        }
    }

    /**
     * 책의 평균 평점 계산
     */
    private float calculateAvgRateForBook(Book book) {
        List<Review> reviews = book.getReviews();
        if (reviews == null || reviews.isEmpty()) {
            return 0.0f;
        }

        double average = reviews.stream()
                .mapToInt(Review::getRate)
                .average()
                .orElse(0.0);

        return (float) average;
    }

    /**
     * Book 엔티티를 DTO로 변환
     */
    private List<BookSearchDto> convertToDto(List<Book> books) {
        return books.stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * 단일 Book 엔티티를 DTO로 변환
     */
    private BookSearchDto convertToDto(Book book) {
        return BookSearchDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .imageUrl(book.getImageUrl())
                .publisher(book.getPublisher())
                .isbn13(book.getIsbn13())
                .totalPage(book.getTotalPage())
                .publishedDate(book.getPublishedDate())
                .avgRate(book.getAvgRate())
                .categoryName(book.getCategory().getName())
                .authors(book.getAuthors().stream()
                        .map(wrote -> wrote.getAuthor().getName())
                        .toList())
                .build();
    }
}