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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private WroteRepository wroteRepository;

    @Mock
    private RestTemplate restTemplate;

    // ObjectMapper는 실제 객체 사용
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private BookService bookService;

    private Category defaultCategory;
    private Author testAuthor;

    @BeforeEach
    void setUp() {
        // application.yml의 값들을 테스트용으로 설정
        ReflectionTestUtils.setField(bookService, "aladinApiKey", "test-api-key");
        ReflectionTestUtils.setField(bookService, "aladinBaseUrl", "http://www.aladin.co.kr/ttb/api");
        // 실제 ObjectMapper 주입
        ReflectionTestUtils.setField(bookService, "objectMapper", objectMapper);

        defaultCategory = new Category("일반");
        testAuthor = new Author("테스트 작가");
    }

    @Test
    @DisplayName("DB에서 책을 찾을 수 있는 경우 - API 호출하지 않음")
    void searchBooks_WhenBooksFoundInDB_ShouldReturnFromDB() {
        // Given
        String query = "자바";
        Book book = createTestBookWithAuthor();
        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of(book));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 1, 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 책");
        assertThat(result.get(0).getAuthors()).contains("테스트 작가");

        // API 호출되지 않았는지 확인
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("DB에 책이 없는 경우 - 알라딘 API 호출")
    void searchBooks_WhenBooksNotFoundInDB_ShouldCallAladinAPI() {
        // Given
        String query = "새로운책";
        String apiResponse = createMockApiResponseWithAuthors();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(categoryRepository.findByName("국내도서"))
                .thenReturn(Optional.of(defaultCategory));
        when(authorRepository.findByName("J.K. 롤링"))
                .thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 1, 10);

        // Then
        verify(restTemplate, atLeastOnce()).getForObject(contains("ItemSearch.aspx"), eq(String.class));
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
        verify(authorRepository, atLeastOnce()).save(any(Author.class));
        verify(wroteRepository, atLeastOnce()).save(any(Wrote.class));
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에서 찾을 수 있는 경우")
    void getBookByIsbn_WhenBookFoundInDB_ShouldReturnFromDB() {
        // Given
        String isbn = "9788966261024";
        Book book = createTestBookWithAuthor();
        when(bookRepository.findByIsbn13(isbn))
                .thenReturn(Optional.of(book));

        // When
        BookSearchDto result = bookService.getBookByIsbn(isbn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn13()).isEqualTo(isbn);
        assertThat(result.getAuthors()).contains("테스트 작가");

        // API 호출되지 않았는지 확인
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에 없는 경우 API 호출")
    void getBookByIsbn_WhenBookNotFoundInDB_ShouldCallAladinAPI() {
        // Given
        String isbn = "9788966261024";
        String apiResponse = createMockApiResponseWithAuthors();

        when(bookRepository.findByIsbn13(isbn))
                .thenReturn(Optional.empty());
        when(categoryRepository.findByName("국내도서"))
                .thenReturn(Optional.of(defaultCategory));
        when(authorRepository.findByName("J.K. 롤링"))
                .thenReturn(Optional.empty());
        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookSearchDto result = bookService.getBookByIsbn(isbn);

        // Then
        verify(restTemplate).getForObject(contains("ItemLookUp.aspx"), eq(String.class));
        verify(bookRepository).save(any(Book.class));
        verify(authorRepository).save(any(Author.class));
        verify(wroteRepository).save(any(Wrote.class));
    }

    @Test
    @DisplayName("도서 관련 타입만 저장 - BOOK, FOREIGN, EBOOK")
    void parseBook_ShouldSaveOnlyBookTypes() {
        // Given - BOOK 타입 응답
        String apiResponse = createMockApiResponseWithMallType("BOOK");

        when(bookRepository.findByTitleOrAuthorContaining("book"))
                .thenReturn(List.of());
        when(categoryRepository.findByName("국내도서"))
                .thenReturn(Optional.of(new Category("국내도서")));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<BookSearchDto> result = bookService.searchBooks("book", 1, 10);

        // Then
        verify(categoryRepository, atLeastOnce()).findByName("국내도서");
        verify(bookRepository, atLeastOnce()).save(any(Book.class));
    }

    @Test
    @DisplayName("비도서 타입은 저장하지 않음 - MUSIC, DVD")
    void parseBook_ShouldNotSaveNonBookTypes() {
        // Given - MUSIC 타입 응답
        String apiResponse = createMockApiResponseWithMallType("MUSIC");

        when(bookRepository.findByTitleOrAuthorContaining("music"))
                .thenReturn(List.of());
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);

        // When
        List<BookSearchDto> result = bookService.searchBooks("music", 1, 10);

        // Then
        assertThat(result).isEmpty(); // 음반은 저장되지 않음
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("중복된 작가 정보 처리 - 이미 존재하는 작가는 새로 생성하지 않음")
    void saveAuthors_WhenAuthorAlreadyExists_ShouldNotCreateDuplicate() {
        // Given
        String query = "기존작가책";
        String apiResponse = createMockApiResponseWithAuthors();

        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(categoryRepository.findByName("국내도서"))
                .thenReturn(Optional.of(defaultCategory));
        when(authorRepository.findByName("J.K. 롤링"))
                .thenReturn(Optional.of(testAuthor)); // 이미 존재하는 작가
        when(wroteRepository.existsByAuthorAndBook(any(Author.class), any(Book.class)))
                .thenReturn(false);
        when(wroteRepository.save(any(Wrote.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bookService.searchBooks(query, 1, 10);

        // Then
        verify(authorRepository, never()).save(any(Author.class)); // 새로운 작가 생성 안 함
        verify(wroteRepository, atLeastOnce()).save(any(Wrote.class)); // 관계는 생성
    }

    @Test
    @DisplayName("알라딘 API 호출 실패 시 빈 리스트 반환")
    void searchBooks_WhenAPICallFails_ShouldReturnEmptyList() {
        // Given
        String query = "실패테스트";
        when(bookRepository.findByTitleOrAuthorContaining(query))
                .thenReturn(List.of());
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 1, 10);

        // Then
        assertThat(result).isEmpty();
        verify(authorRepository, never()).save(any(Author.class));
        verify(wroteRepository, never()).save(any(Wrote.class));
    }

    // Helper methods
    private Book createTestBookWithAuthor() {
        Book book = new Book();
        book.setTitle("테스트 책");
        book.setImageUrl("http://test.com/image.jpg");
        book.setPublisher("테스트 출판사");
        book.setIsbn13("9788966261024");
        book.setTotalPage(300);
        book.setAvgRate(4.5f);
        book.setCategory(defaultCategory);

        // 작가 관계 설정
        Wrote wrote = new Wrote(testAuthor, book);
        book.getAuthors().add(wrote);

        return book;
    }

    private String createMockApiResponseWithAuthors() {
        return """
            {
                "version": "20131101",
                "title": "알라딘 상품 검색",
                "item": [
                    {
                        "title": "해리 포터와 마법사의 돌",
                        "author": "J.K. 롤링",
                        "cover": "http://image.aladin.co.kr/test.jpg",
                        "publisher": "문학수첩",
                        "isbn13": "9788966261024",
                        "itemPage": 250,
                        "pubDate": "2024-01-15",
                        "customerReviewRank": 8,
                        "mallType": "BOOK",
                        "subInfo": {
                            "authors": [
                                {
                                    "authorName": "J.K. 롤링",
                                    "authorType": "author"
                                }
                            ]
                        }
                    }
                ]
            }
            """;
    }

    private String createMockApiResponseWithMallType(String mallType) {
        String mallTypeJson = mallType != null ? String.format("\"mallType\": \"%s\",", mallType) : "";

        return String.format("""
            {
                "version": "20131101",
                "title": "알라딘 상품 검색",
                "item": [
                    {
                        "title": "Test Book",
                        "author": "Test Author",
                        "cover": "http://image.aladin.co.kr/test.jpg",
                        "publisher": "Test Publisher",
                        "isbn13": "9788966261024",
                        "itemPage": 250,
                        "pubDate": "2024-01-15",
                        %s
                        "customerReviewRank": 8
                    }
                ]
            }
            """, mallTypeJson);
    }
}