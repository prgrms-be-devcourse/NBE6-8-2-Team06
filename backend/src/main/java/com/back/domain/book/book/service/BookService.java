package com.back.domain.book.book.service;

import com.back.domain.book.author.entity.Author;
import com.back.domain.book.author.repository.AuthorRepository;
import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.book.wrote.entity.Wrote;
import com.back.domain.book.wrote.repository.WroteRepository;
import com.back.global.exception.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aladin.api.key}")
    private String aladinApiKey;

    @Value("${aladin.api.base-url}")
    private String aladinBaseUrl;

    // 기존의 하드코딩된 CATEGORY_MAPPING 완전 제거!

    /**
     * 방안 3: 하이브리드 접근방식 - OptResult 사용 + 필요시 보완
     */
    @Transactional
    public List<BookSearchDto> searchBooks(String query, int limit) {
        // 1. DB에서 먼저 확인
        List<Book> booksFromDb = bookRepository.findByTitleOrAuthorContaining(query);
        if (!booksFromDb.isEmpty()) {
            log.info("DB에서 찾은 책: {} 권", booksFromDb.size());
            // limit 적용해서 반환
            return convertToDto(booksFromDb.stream().limit(limit).toList());
        }

        log.info("DB에 없어서 알라딘 API에서 검색: {}", query);

        // 2. API에서 검색 (limit 적용)
        List<Book> booksFromApi = searchBooksFromAladinApiWithLimit(query, limit);

        // 3. 상세 정보 보완
        booksFromApi = enrichMissingDetails(booksFromApi);

        return convertToDto(booksFromApi);
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

        // callApiAndParseBooks 활용
        String url = String.format(
                "%s/ItemLookUp.aspx?ttbkey=%s&itemIdType=ISBN13&ItemId=%s&output=js&Version=20131101&OptResult=authors",
                aladinBaseUrl, aladinApiKey, isbn
        );

        List<Book> books = callApiAndParseBooks(url, "ISBN조회");

        return books.isEmpty() ? null : convertToDto(books.get(0));
    }

    /**
     * OptResult=authors를 포함한 알라딘 API 검색
     */
    private List<Book> searchBooksFromAladinApiWithLimit(String query, int limit) {
        List<Book> allBooks = new ArrayList<>();

        // 각 카테고리별로 적절히 분배해서 검색
        int limitPerCategory = Math.max(1, limit / 3); // 3개 카테고리로 분배

        try {
            // 국내도서 검색
            String bookUrl = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=1&SearchTarget=Book&output=js&Version=20131101&OptResult=authors",
                    aladinBaseUrl, aladinApiKey, query, limitPerCategory
            );
            List<Book> books = callApiAndParseBooks(bookUrl, "국내도서");
            allBooks.addAll(books);

            // 외국도서 검색
            String foreignUrl = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=1&SearchTarget=Foreign&output=js&Version=20131101&OptResult=authors",
                    aladinBaseUrl, aladinApiKey, query, limitPerCategory
            );
            List<Book> foreignBooks = callApiAndParseBooks(foreignUrl, "외국도서");
            allBooks.addAll(foreignBooks);

            // 전자책 검색
            String ebookUrl = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=1&SearchTarget=eBook&output=js&Version=20131101&OptResult=authors",
                    aladinBaseUrl, aladinApiKey, query, limitPerCategory
            );
            List<Book> ebooks = callApiAndParseBooks(ebookUrl, "전자책");
            allBooks.addAll(ebooks);

        } catch (Exception e) {
            log.error("알라딘 API 검색 중 오류: {}", e.getMessage());
        }

        // 전체 결과에서 limit 적용
        return allBooks.stream().limit(limit).toList();
    }


    /**
     * 부족한 상세 정보 보완 (페이지 수, 추가 저자 정보 등)
     */
    private List<Book> enrichMissingDetails(List<Book> books) {
        return books.stream()
                .peek(book -> {
                    // 페이지 수가 없고 ISBN이 있으면 개별 조회로 보완
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
                (book.getTotalPage() == 0 ||
                        book.getAuthors().isEmpty());
    }

    /**
     * 개별 ISBN 조회로 상세 정보 보완
     */
    private void enrichBookWithDetailInfo(Book book) {
        try {
            String url = String.format(
                    "%s/ItemLookUp.aspx?ttbkey=%s&itemIdType=ISBN13&ItemId=%s&output=js&Version=20131101&OptResult=authors",
                    aladinBaseUrl, aladinApiKey, book.getIsbn13()
            );

            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                JsonNode itemNode = itemsNode.get(0);

                // 페이지 수 보완
                if (book.getTotalPage() == 0) {
                    updatePageInfo(book, itemNode);
                }

                // 상세 저자 정보 보완 (기존 저자가 없는 경우만)
                if (book.getAuthors().isEmpty()) {
                    parseAndSaveAuthors(itemNode, book);
                }

                log.info("상세 정보 보완 완료: {} (페이지: {}, 저자: {})",
                        book.getTitle(), book.getTotalPage(), book.getAuthors().size());
            }

        } catch (Exception e) {
            log.warn("상세 정보 조회 실패: ISBN {}, Error: {}", book.getIsbn13(), e.getMessage());
        }
    }

    /**
     * 페이지 정보 업데이트
     */
    private void updatePageInfo(Book book, JsonNode itemNode) {
        JsonNode subInfoNode = itemNode.get("subInfo");
        if (subInfoNode != null) {
            JsonNode itemPageNode = subInfoNode.get("itemPage");
            if (itemPageNode != null && !itemPageNode.isNull()) {
                book.setTotalPage(itemPageNode.asInt());
                bookRepository.save(book);
                log.debug("페이지 수 업데이트: {} -> {}", book.getTitle(), book.getTotalPage());
            }
        }
    }

    /**
     * API 호출 및 파싱 공통 메서드
     */
    private List<Book> callApiAndParseBooks(String url, String searchType) {
        try {
            log.debug("{} 검색 API 호출: {}", searchType, url);
            String response = restTemplate.getForObject(url, String.class);
            return parseApiResponse(response);
        } catch (Exception e) {
            log.error("{} API 호출 중 오류: {}", searchType, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * API 응답 파싱
     */
    private List<Book> parseApiResponse(String response) {
        List<Book> books = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode itemsNode = rootNode.get("item");

            if (itemsNode != null && itemsNode.isArray()) {
                for (JsonNode itemNode : itemsNode) {
                    Book book = parseAndSaveBookFromJson(itemNode);
                    if (book != null) {
                        books.add(book);
                    }
                }
            }

        } catch (Exception e) {
            log.error("API 응답 파싱 중 오류: {}", e.getMessage());
        }

        return books;
    }

    /**
     * JSON에서 Book 생성 및 저장 (작가 정보 포함)
     */
    private Book parseAndSaveBookFromJson(JsonNode itemNode) {
        try {
            Book book = parseBookFromJson(itemNode);
            if (book == null) {
                return null;
            }

            // 이미 존재하는 책인지 확인
            if (book.getIsbn13() != null) {
                Optional<Book> existingBook = bookRepository.findByIsbn13(book.getIsbn13());
                if (existingBook.isPresent()) {
                    log.info("이미 존재하는 ISBN: {}", book.getIsbn13());
                    return existingBook.get();
                }
            }

            // 책 저장
            Book savedBook = bookRepository.save(book);
            log.info("책 저장 완료: {}", savedBook.getTitle());

            // 작가 정보 저장
            parseAndSaveAuthors(itemNode, savedBook);

            return savedBook;

        } catch (Exception e) {
            log.error("책 저장 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * JSON에서 Book 엔티티 생성 (도서 관련 타입만 처리)
     */
    private Book parseBookFromJson(JsonNode itemNode) {
        try {
            // mallType 체크 - 도서 관련 타입이 아니면 null 반환
            String mallType = getJsonValue(itemNode, "mallType");
            if (mallType != null && !isBookRelatedType(mallType)) {
                log.debug("도서가 아닌 타입이므로 건너뜀: {}", mallType);
                return null;
            }

            Book book = new Book();

            book.setTitle(getJsonValue(itemNode, "title"));
            book.setImageUrl(getJsonValue(itemNode, "cover"));
            book.setPublisher(getJsonValue(itemNode, "publisher"));

            String isbn13 = getJsonValue(itemNode, "isbn13");
            if (isbn13 != null && !isbn13.isEmpty()) {
                book.setIsbn13(isbn13);
            } else {
                String isbn = getJsonValue(itemNode, "isbn");
                if (isbn != null && isbn.length() == 13) {
                    book.setIsbn13(isbn);
                }
            }

            // 페이지 수 설정 (OptResult로 subInfo가 있을 수도 있음)
            setPageInfo(book, itemNode);

            String pubDateStr = getJsonValue(itemNode, "pubDate");
            if (pubDateStr != null && !pubDateStr.isEmpty()) {
                try {
                    LocalDateTime pubDate = parsePubDate(pubDateStr);
                    book.setPublishedDate(pubDate);
                } catch (Exception e) {
                    log.warn("출간일 파싱 실패: {}", pubDateStr);
                }
            }

            // 평점 설정
            JsonNode customerReviewRankNode = itemNode.get("customerReviewRank");
            if (customerReviewRankNode != null && !customerReviewRankNode.isNull()) {
                // 알라딘 API는 0~10점 체계이므로 5점 만점으로 변환
                float avgRate = customerReviewRankNode.floatValue() / 2.0f;
                book.setAvgRate(avgRate);
            } else {
                book.setAvgRate(0.0f);
            }

            //간단한 카테고리 추출 - 2번째 깊이 사용
            String categoryName = extractCategoryFromPath(itemNode);

            // 카테고리 찾기 또는 생성
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> {
                        log.info("새 카테고리 생성: {}", categoryName);
                        return categoryRepository.save(new Category(categoryName));
                    });

            book.setCategory(category);

            return book;

        } catch (Exception e) {
            log.error("Book 엔티티 생성 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 카테고리 경로에서 2번째 깊이 추출하는 간단한 방법
     */
    private String extractCategoryFromPath(JsonNode itemNode) {
        String categoryName = getJsonValue(itemNode, "categoryName");

        if (categoryName != null && !categoryName.isEmpty()) {
            log.debug("원본 카테고리 경로: {}", categoryName);

            // '>' 기준으로 분리
            String[] categoryParts = categoryName.split(">");

            // 2번째 깊이 사용 (인덱스 1)
            if (categoryParts.length > 1) {
                String secondLevelCategory = categoryParts[1].trim();
                log.debug("추출된 카테고리: {}", secondLevelCategory);
                return secondLevelCategory;
            }

            // 2번째가 없으면 첫 번째 사용
            if (categoryParts.length > 0) {
                String firstLevelCategory = categoryParts[0].trim();
                log.debug("첫 번째 레벨 카테고리 사용: {}", firstLevelCategory);
                return firstLevelCategory;
            }
        }

        // categoryName이 없으면 mallType 기반 기본값
        String mallType = getJsonValue(itemNode, "mallType");
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
     * 페이지 정보 설정 (검색 API에서도 subInfo가 있을 수 있음)
     */
    private void setPageInfo(Book book, JsonNode itemNode) {
        // 기본 itemPage 먼저 확인
        JsonNode totalPageNode = itemNode.get("itemPage");
        if (totalPageNode != null && !totalPageNode.isNull()) {
            book.setTotalPage(totalPageNode.asInt());
            return;
        }

        // subInfo의 itemPage 확인 (OptResult=authors로 인해 있을 수 있음)
        JsonNode subInfoNode = itemNode.get("subInfo");
        if (subInfoNode != null) {
            JsonNode subPageNode = subInfoNode.get("itemPage");
            if (subPageNode != null && !subPageNode.isNull()) {
                book.setTotalPage(subPageNode.asInt());
                return;
            }
        }

        // 정보가 없으면 0으로 설정 (나중에 개별 조회로 보완)
        book.setTotalPage(0);
    }

    /**
     * 도서 관련 타입인지 확인
     */
    private boolean isBookRelatedType(String mallType) {
        return "BOOK".equals(mallType) ||
                "FOREIGN".equals(mallType) ||
                "EBOOK".equals(mallType);
    }

    /**
     * 작가 정보 파싱 및 저장
     */
    private void parseAndSaveAuthors(JsonNode itemNode, Book book) {
        List<String> authorNames = new ArrayList<>();

        // 기본 author 필드에서 작가 정보 추출
        String authorString = getJsonValue(itemNode, "author");
        if (authorString != null && !authorString.isEmpty()) {
            // 작가가 여러 명인 경우 구분자로 분리 (쉼표, 세미콜론 등)
            String[] authors = authorString.split("[,;]");
            for (String authorName : authors) {
                String trimmedName = authorName.trim();
                if (!trimmedName.isEmpty()) {
                    authorNames.add(trimmedName);
                }
            }
        }

        // subInfo의 authors 배열에서 상세 작가 정보 추출 (OptResult=authors로 인해 있을 수 있음)
        JsonNode subInfoNode = itemNode.get("subInfo");
        if (subInfoNode != null) {
            JsonNode authorsNode = subInfoNode.get("authors");
            if (authorsNode != null && authorsNode.isArray()) {
                for (JsonNode authorNode : authorsNode) {
                    String authorName = getJsonValue(authorNode, "authorName");
                    if (authorName != null && !authorName.isEmpty()) {
                        authorNames.add(authorName.trim());
                    }
                }
            }
        }

        // 중복 제거
        authorNames = authorNames.stream().distinct().toList();

        // 작가 정보 저장
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
     * JSON에서 문자열 값 추출
     */
    private String getJsonValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }

    /**
     * 출간일 파싱 (다양한 형식 지원)
     */
    private LocalDateTime parsePubDate(String pubDateStr) {
        try {
            if (pubDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDateTime.parse(pubDateStr + "T00:00:00");
            }

            if (pubDateStr.matches("\\d{4}-\\d{2}")) {
                return LocalDateTime.parse(pubDateStr + "-01T00:00:00");
            }

            if (pubDateStr.matches("\\d{4}")) {
                return LocalDateTime.parse(pubDateStr + "-01-01T00:00:00");
            }

            return LocalDateTime.now();

        } catch (Exception e) {
            log.warn("날짜 파싱 실패, 현재 시간으로 설정: {}", pubDateStr);
            return LocalDateTime.now();
        }
    }

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