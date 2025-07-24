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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * 책 검색 - DB에 없으면 API에서 가져와서 저장
     */
    @Transactional
    public List<BookSearchDto> searchBooks(String query, int page, int size) {
        List<Book> booksFromDb = bookRepository.findByTitleOrAuthorContaining(query);

        if (!booksFromDb.isEmpty()) {
            log.info("DB에서 찾은 책: {} 권", booksFromDb.size());
            return convertToDto(booksFromDb);
        }

        log.info("DB에 없어서 알라딘 API에서 검색: {}", query);
        List<Book> booksFromApi = searchBooksFromAladinApi(query, page, size);

        List<Book> savedBooks = saveBooksToDatabase(booksFromApi);

        return convertToDto(savedBooks);
    }

    /**
     * ISBN으로 책 조회 - DB에 없으면 API에서 가져와서 저장
     */
    @Transactional
    public BookSearchDto getBookByIsbn(String isbn) {
        Optional<Book> bookFromDb = bookRepository.findByIsbn13(isbn);

        if (bookFromDb.isPresent()) {
            log.info("DB에서 찾은 ISBN: {}", isbn);
            return convertToDto(bookFromDb.get());
        }

        log.info("DB에 없어서 알라딘 API에서 조회: {}", isbn);
        Book bookFromApi = getBookFromAladinApi(isbn);

        if (bookFromApi == null) {
            return null;
        }

        Book savedBook = saveBookToDatabase(bookFromApi);

        return convertToDto(savedBook);
    }

    /**
     * 알라딘 API에서 책 검색 (도서 타입만)
     */
    private List<Book> searchBooksFromAladinApi(String query, int page, int size) {
        List<Book> allBooks = new ArrayList<>();

        try {
            // 국내도서 검색
            String bookUrl = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=%d&SearchTarget=Book&output=js&Version=20131101",
                    aladinBaseUrl, aladinApiKey, query, size, page
            );
            List<Book> books = callApiAndParseBooks(bookUrl, "국내도서");
            allBooks.addAll(books);

            // 외국도서 검색
            String foreignUrl = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=%d&SearchTarget=Foreign&output=js&Version=20131101",
                    aladinBaseUrl, aladinApiKey, query, size, page
            );
            List<Book> foreignBooks = callApiAndParseBooks(foreignUrl, "외국도서");
            allBooks.addAll(foreignBooks);

            // 전자책 검색
            String ebookUrl = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=%d&SearchTarget=eBook&output=js&Version=20131101",
                    aladinBaseUrl, aladinApiKey, query, size, page
            );
            List<Book> ebooks = callApiAndParseBooks(ebookUrl, "전자책");
            allBooks.addAll(ebooks);

        } catch (Exception e) {
            log.error("알라딘 API 검색 중 오류: {}", e.getMessage());
        }

        return allBooks;
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
     * 알라딘 API에서 ISBN으로 책 조회 (도서 타입만)
     */
    private Book getBookFromAladinApi(String isbn) {
        try {
            String url = String.format(
                    "%s/ItemLookUp.aspx?ttbkey=%s&itemIdType=ISBN13&ItemId=%s&output=js&Version=20131101&OptResult=authors",
                    aladinBaseUrl, aladinApiKey, isbn
            );

            String response = restTemplate.getForObject(url, String.class);
            List<Book> books = parseApiResponse(response);

            // 도서 관련 타입만 반환
            return books.stream()
                    .filter(book -> book != null)
                    .findFirst()
                    .orElse(null);

        } catch (Exception e) {
            log.error("알라딘 API ISBN 조회 중 오류: {}", e.getMessage());
            return null;
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
                    Book book = parseBookFromJsonWithAuthors(itemNode);
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

            JsonNode totalPageNode = itemNode.get("itemPage");
            if (totalPageNode != null && !totalPageNode.isNull()) {
                book.setTotalPage(totalPageNode.asInt());
            } else {
                JsonNode subInfoNode = itemNode.get("subInfo");
                if (subInfoNode != null) {
                    JsonNode subPageNode = subInfoNode.get("itemPage");
                    if (subPageNode != null && !subPageNode.isNull()) {
                        book.setTotalPage(subPageNode.asInt());
                    }
                }
            }

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

            // 카테고리 설정 - 도서 관련 카테고리만
            String categoryName = extractCategoryFromItem(itemNode);
            Category category = categoryRepository.findByName(categoryName)
                    .orElseGet(() -> categoryRepository.save(new Category(categoryName)));
            book.setCategory(category);

            return book;

        } catch (Exception e) {
            log.error("Book 엔티티 생성 중 오류: {}", e.getMessage());
            return null;
        }
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
     * 카테고리 정보 추출 (도서 관련 타입만 처리)
     */
    private String extractCategoryFromItem(JsonNode itemNode) {
        // categoryName 필드가 있는지 확인
        String categoryName = getJsonValue(itemNode, "categoryName");
        if (categoryName != null && !categoryName.isEmpty()) {
            return categoryName;
        }

        // mallType을 기반으로 도서 관련 카테고리만 설정
        String mallType = getJsonValue(itemNode, "mallType");
        switch (mallType != null ? mallType : "") {
            case "BOOK":
                return "국내도서";
            case "FOREIGN":
                return "외국도서";
            case "EBOOK":
                return "전자책";
            default:
                return "일반"; // 기본값
        }
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

        // subInfo의 authors 배열에서 상세 작가 정보 추출
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
     * 책 목록을 DB에 저장
     */
    private List<Book> saveBooksToDatabase(List<Book> books) {
        List<Book> savedBooks = new ArrayList<>();

        for (Book book : books) {
            try {
                Book savedBook = saveBookToDatabase(book);
                if (savedBook != null) {
                    savedBooks.add(savedBook);
                }
            } catch (Exception e) {
                log.error("책 저장 중 오류: {}", e.getMessage());
            }
        }

        return savedBooks;
    }

    /**
     * 단일 책을 DB에 저장
     */
    private Book saveBookToDatabase(Book book) {
        try {
            if (book.getIsbn13() != null &&
                    bookRepository.findByIsbn13(book.getIsbn13()).isPresent()) {
                log.info("이미 존재하는 ISBN: {}", book.getIsbn13());
                Book existingBook = bookRepository.findByIsbn13(book.getIsbn13()).get();
                return existingBook;
            }

            Book savedBook = bookRepository.save(book);
            log.info("책 저장 완료: {}", savedBook.getTitle());

            return savedBook;

        } catch (Exception e) {
            log.error("책 저장 중 오류: {}", e.getMessage());
            return null;
        }
    }

    /**
     * API 응답에서 작가 정보를 파싱하고 저장하는 메서드 (수정된 버전)
     */
    private Book parseBookFromJsonWithAuthors(JsonNode itemNode) {
        Book book = parseBookFromJson(itemNode);
        if (book != null) {
            // 먼저 책을 저장
            Book savedBook = bookRepository.save(book);

            // 그 다음 작가 정보 저장
            parseAndSaveAuthors(itemNode, savedBook);

            return savedBook;
        }
        return null;
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