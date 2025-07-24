package com.back.domain.book.book.service;

import com.back.domain.book.book.dto.BookSearchDto;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
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
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BookService bookService;

    private Category defaultCategory;

    @BeforeEach
    void setUp() {
        // application.yml의 값들을 테스트용으로 설정
        ReflectionTestUtils.setField(bookService, "aladinApiKey", "test-api-key");
        ReflectionTestUtils.setField(bookService, "aladinBaseUrl", "http://www.aladin.co.kr/ttb/api");

        defaultCategory = new Category("일반");
    }

    @Test
    @DisplayName("DB에서 책을 찾을 수 있는 경우 - API 호출하지 않음")
    void searchBooks_WhenBooksFoundInDB_ShouldReturnFromDB() {
        // Given
        String query = "자바";
        Book book = createTestBook();
        when(bookRepository.findByTitleContainingIgnoreCase(query))
                .thenReturn(List.of(book));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 1, 10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("테스트 책");

        // API 호출되지 않았는지 확인
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("DB에 책이 없는 경우 - 알라딘 API 호출")
    void searchBooks_WhenBooksNotFoundInDB_ShouldCallAladinAPI() throws Exception {
        // Given
        String query = "새로운책";
        String apiResponse = createMockApiResponse();

        when(bookRepository.findByTitleContainingIgnoreCase(query))
                .thenReturn(List.of());
        when(categoryRepository.findByName("일반"))
                .thenReturn(Optional.of(defaultCategory));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);
        when(objectMapper.readTree(apiResponse))
                .thenReturn(createMockJsonNode());
        when(bookRepository.findByIsbn13(anyString()))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 1, 10);

        // Then
        verify(restTemplate).getForObject(contains("ItemSearch.aspx"), eq(String.class));
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에서 찾을 수 있는 경우")
    void getBookByIsbn_WhenBookFoundInDB_ShouldReturnFromDB() {
        // Given
        String isbn = "9788966261024";
        Book book = createTestBook();
        when(bookRepository.findByIsbn13(isbn))
                .thenReturn(Optional.of(book));

        // When
        BookSearchDto result = bookService.getBookByIsbn(isbn);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsbn13()).isEqualTo(isbn);

        // API 호출되지 않았는지 확인
        verify(restTemplate, never()).getForObject(anyString(), eq(String.class));
    }

    @Test
    @DisplayName("ISBN으로 책 조회 - DB에 없는 경우 API 호출")
    void getBookByIsbn_WhenBookNotFoundInDB_ShouldCallAladinAPI() throws Exception {
        // Given
        String isbn = "9788966261024";
        String apiResponse = createMockApiResponse();

        when(bookRepository.findByIsbn13(isbn))
                .thenReturn(Optional.empty());
        when(categoryRepository.findByName("일반"))
                .thenReturn(Optional.of(defaultCategory));
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn(apiResponse);
        when(objectMapper.readTree(apiResponse))
                .thenReturn(createMockJsonNode());
        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BookSearchDto result = bookService.getBookByIsbn(isbn);

        // Then
        verify(restTemplate).getForObject(contains("ItemLookUp.aspx"), eq(String.class));
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("알라딘 API 호출 실패 시 빈 리스트 반환")
    void searchBooks_WhenAPICallFails_ShouldReturnEmptyList() {
        // Given
        String query = "실패테스트";
        when(bookRepository.findByTitleContainingIgnoreCase(query))
                .thenReturn(List.of());
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("API 호출 실패"));

        // When
        List<BookSearchDto> result = bookService.searchBooks(query, 1, 10);

        // Then
        assertThat(result).isEmpty();
    }

    private Book createTestBook() {
        Book book = new Book();
        book.setTitle("테스트 책");
        book.setImageUrl("http://test.com/image.jpg");
        book.setPublisher("테스트 출판사");
        book.setIsbn13("9788966261024");
        book.setTotalPage(300);
        book.setAvgRate(4.5f);
        book.setCategory(defaultCategory);
        return book;
    }

    private String createMockApiResponse() {
        return """
            {
                "version": "20131101",
                "title": "알라딘 상품 검색",
                "item": [
                    {
                        "title": "테스트 API 책",
                        "cover": "http://image.aladin.co.kr/test.jpg",
                        "publisher": "테스트 출판사",
                        "isbn13": "9788966261024",
                        "itemPage": 250,
                        "pubDate": "2024-01-15"
                    }
                ]
            }
            """;
    }

    private com.fasterxml.jackson.databind.JsonNode createMockJsonNode() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(createMockApiResponse());
    }
}