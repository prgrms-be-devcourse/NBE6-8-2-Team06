package com.back.domain.book.book.service;

import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
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
        List<Book> booksFromDb = bookRepository.findByTitleContainingIgnoreCase(query);

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
     * 알라딘 API에서 책 검색
     */
    private List<Book> searchBooksFromAladinApi(String query, int page, int size) {
        try {
            String url = String.format(
                    "%s/ItemSearch.aspx?ttbkey=%s&Query=%s&QueryType=Title&MaxResults=%d&start=%d&SearchTarget=Book&output=js&Version=20131101",
                    aladinBaseUrl, aladinApiKey, query, size, page
            );

            String response = restTemplate.getForObject(url, String.class);
            return parseApiResponse(response);

        } catch (Exception e) {
            log.error("알라딘 API 검색 중 오류: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 알라딘 API에서 ISBN으로 책 조회
     */
    private Book getBookFromAladinApi(String isbn) {
        try {
            String url = String.format(
                    "%s/ItemLookUp.aspx?ttbkey=%s&itemIdType=ISBN13&ItemId=%s&output=js&Version=20131101&OptResult=packing",
                    aladinBaseUrl, aladinApiKey, isbn
            );

            String response = restTemplate.getForObject(url, String.class);
            List<Book> books = parseApiResponse(response);

            return books.isEmpty() ? null : books.get(0);

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
                    Book book = parseBookFromJson(itemNode);
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
     * JSON에서 Book 엔티티 생성
     */
    private Book parseBookFromJson(JsonNode itemNode) {
        try {
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

            // 일단은 0으로 놓자
            book.setAvgRate(0.0f);

            Category defaultCategory = categoryRepository.findByName("일반")
                    .orElseGet(() -> categoryRepository.save(new Category("일반")));
            book.setCategory(defaultCategory);

            return book;

        } catch (Exception e) {
            log.error("Book 엔티티 생성 중 오류: {}", e.getMessage());
            return null;
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
                return bookRepository.findByIsbn13(book.getIsbn13()).get();
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