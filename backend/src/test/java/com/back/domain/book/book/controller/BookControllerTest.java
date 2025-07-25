package com.back.domain.book.book.controller;

import com.back.domain.book.author.entity.Author;
import com.back.domain.book.author.repository.AuthorRepository;
import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.book.wrote.entity.Wrote;
import com.back.domain.book.wrote.repository.WroteRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private WroteRepository wroteRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 셋업
        Category category = new Category("소설");
        categoryRepository.save(category);

        Author author = new Author("김작가");
        authorRepository.save(author);

        Book book1 = new Book("테스트 책 1", "테스트 출판사", category);
        book1.setIsbn13("9780123456789");
        book1.setImageUrl("https://example.com/book1.jpg");
        book1.setTotalPage(200);
        book1.setPublishedDate(LocalDateTime.of(2023, 1, 1, 0, 0));
        book1.setAvgRate(4.5f);
        book1 = bookRepository.save(book1);

        Book book2 = new Book("테스트 책 2", "다른 출판사", category);
        book2.setIsbn13("9780987654321");
        book2.setImageUrl("https://example.com/book2.jpg");
        book2.setTotalPage(300);
        book2.setPublishedDate(LocalDateTime.of(2023, 6, 15, 0, 0));
        book2.setAvgRate(3.8f);
        book2 = bookRepository.save(book2);

        // 작가-책 관계 설정
        Wrote wrote1 = new Wrote(author, book1);
        Wrote wrote2 = new Wrote(author, book2);
        wroteRepository.save(wrote1);
        wroteRepository.save(wrote2);

        // 영속성 컨텍스트 플러시 및 클리어
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("전체 책 조회 - 기본 파라미터로 성공적으로 조회")
    void getAllBooks_Success() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("전체 책 조회 성공"))
                .andExpect(jsonPath("$.data.data").isArray())
                .andExpect(jsonPath("$.data.data.length()").value(2))
                .andExpect(jsonPath("$.data.data[0].title").exists())
                .andExpect(jsonPath("$.data.data[0].publisher").exists())
                .andExpect(jsonPath("$.data.data[0].categoryName").value("소설"))
                .andExpect(jsonPath("$.data.data[0].authors").isArray())
                .andExpect(jsonPath("$.data.data[0].authors[0]").value("김작가"))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.pageSize").value(10))
                .andExpect(jsonPath("$.data.pageNumber").value(0));
    }

    @Test
    @DisplayName("전체 책 조회 실패 - 잘못된 정렬 방향")
    void getAllBooks_Fail_InvalidSortDirection() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").exists())
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("전체 책 조회 실패 - 음수 페이지 번호")
    void getAllBooks_Fail_NegativePage() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("page", "-1")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-1"))
                .andExpect(jsonPath("$.msg").value("페이지 번호는 0 이상이어야 합니다."));
    }

    @Test
    @DisplayName("전체 책 조회 실패 - 0 이하의 페이지 크기")
    void getAllBooks_Fail_ZeroOrNegativeSize() throws Exception {
        mockMvc.perform(get("/api/books")
                        .param("page", "0")
                        .param("size", "0")
                        .param("sortBy", "id")
                        .param("sortDir", "desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-2"))
                .andExpect(jsonPath("$.msg").value("페이지 크기는 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("책 검색 - 제목으로 검색 성공")
    void searchBooks_ByTitle_Success() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "테스트 책")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("2개의 책을 찾았습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("테스트 책 1"))
                .andExpect(jsonPath("$.data[0].publisher").value("테스트 출판사"))
                .andExpect(jsonPath("$.data[0].categoryName").value("소설"))
                .andExpect(jsonPath("$.data[0].authors").isArray())
                .andExpect(jsonPath("$.data[0].authors[0]").value("김작가"))
                .andExpect(jsonPath("$.data[0].isbn13").value("9780123456789"))
                .andExpect(jsonPath("$.data[0].totalPage").value(200))
                .andExpect(jsonPath("$.data[0].avgRate").value(4.5))
                .andExpect(jsonPath("$.data[1].title").value("테스트 책 2"))
                .andExpect(jsonPath("$.data[1].publisher").value("다른 출판사"))
                .andExpect(jsonPath("$.data[1].isbn13").value("9780987654321"));
    }

    @Test
    @DisplayName("책 검색 - 작가명으로 검색 성공")
    void searchBooks_ByAuthor_Success() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "김작가")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("2개의 책을 찾았습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].authors").isArray())
                .andExpect(jsonPath("$.data[0].authors[0]").value("김작가"))
                .andExpect(jsonPath("$.data[1].authors").isArray())
                .andExpect(jsonPath("$.data[1].authors[0]").value("김작가"));
    }

    @Test
    @DisplayName("책 검색 - 부분 제목으로 검색 성공")
    void searchBooks_ByPartialTitle_Success() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "책 1")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1개의 책을 찾았습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("테스트 책 1"))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].imageUrl").exists())
                .andExpect(jsonPath("$.data[0].publishedDate").exists());
    }

    @Test
    @DisplayName("책 검색 - limit 제한 테스트")
    void searchBooks_WithLimitRestriction_Success() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "테스트")
                        .param("limit", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("1개의 책을 찾았습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("책 검색 - 검색 결과 없음")
    void searchBooks_NoResults_Success() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "존재하지않는책")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-2"))
                .andExpect(jsonPath("$.msg").value("검색 결과가 없습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("책 검색 실패 - query 파라미터 누락")
    void searchBooks_Fail_MissingQuery() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("limit", "20"))
                .andExpect(status().isBadRequest());
        // query는 @RequestParam이므로 누락시 400 에러
    }

    @Test
    @DisplayName("책 검색 실패 - 빈 query")
    void searchBooks_Fail_EmptyQuery() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "")
                        .param("limit", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").exists())
                .andExpect(jsonPath("$.msg").exists());
        // 빈 문자열 검색 방지를 위한 validation 필요
    }

    @Test
    @DisplayName("책 검색 실패 - 공백만 있는 query")
    void searchBooks_Fail_WhitespaceOnlyQuery() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "   ")
                        .param("limit", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").exists())
                .andExpect(jsonPath("$.msg").exists());
    }

    @Test
    @DisplayName("책 검색 실패 - limit이 0")
    void searchBooks_Fail_ZeroLimit() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "테스트")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-4"))
                .andExpect(jsonPath("$.msg").value("limit은 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("책 검색 실패 - 음수 limit")
    void searchBooks_Fail_NegativeLimit() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "테스트")
                        .param("limit", "-5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-4"))
                .andExpect(jsonPath("$.msg").value("limit은 1 이상이어야 합니다."));
    }

    @Test
    @DisplayName("책 검색 실패 - limit이 최대값 초과")
    void searchBooks_Fail_ExceedMaxLimit() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "테스트")
                        .param("limit", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-5"))
                .andExpect(jsonPath("$.msg").value("limit은 100 이하여야 합니다."));
    }

    @Test
    @DisplayName("ISBN 검색 - 정확한 ISBN으로 책 조회 성공")
    void getBookByIsbn_Success() throws Exception {
        mockMvc.perform(get("/api/books/isbn/9780123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("ISBN으로 책 조회 성공"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value("테스트 책 1"))
                .andExpect(jsonPath("$.data.publisher").value("테스트 출판사"))
                .andExpect(jsonPath("$.data.isbn13").value("9780123456789"))
                .andExpect(jsonPath("$.data.totalPage").value(200))
                .andExpect(jsonPath("$.data.avgRate").value(4.5))
                .andExpect(jsonPath("$.data.categoryName").value("소설"))
                .andExpect(jsonPath("$.data.authors").isArray())
                .andExpect(jsonPath("$.data.authors[0]").value("김작가"))
                .andExpect(jsonPath("$.data.imageUrl").value("https://example.com/book1.jpg"))
                .andExpect(jsonPath("$.data.publishedDate").exists());
    }

    @Test
    @DisplayName("ISBN 검색 - 하이픈이 포함된 ISBN으로 조회 성공")
    void getBookByIsbn_WithHyphens_Success() throws Exception {
        mockMvc.perform(get("/api/books/isbn/978-0-123-45678-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("ISBN으로 책 조회 성공"))
                .andExpect(jsonPath("$.data").isNotEmpty())
                .andExpect(jsonPath("$.data.title").value("테스트 책 1"))
                .andExpect(jsonPath("$.data.isbn13").value("9780123456789"));
    }

    @Test
    @DisplayName("ISBN 검색 실패 - 빈 ISBN")
    void getBookByIsbn_Fail_EmptyIsbn() throws Exception {
        mockMvc.perform(get("/api/books/isbn/"))
                .andExpect(status().isNotFound()); // URL 자체가 매칭되지 않음
    }

    @Test
    @DisplayName("ISBN 검색 실패 - 잘못된 ISBN 형식 (12자리)")
    void getBookByIsbn_Fail_InvalidFormat_12Digits() throws Exception {
        mockMvc.perform(get("/api/books/isbn/123456789012"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-8"))
                .andExpect(jsonPath("$.msg").value("올바른 ISBN-13 형식이 아닙니다. (13자리 숫자)"));
    }

    @Test
    @DisplayName("ISBN 검색 실패 - 잘못된 ISBN 형식 (14자리)")
    void getBookByIsbn_Fail_InvalidFormat_14Digits() throws Exception {
        mockMvc.perform(get("/api/books/isbn/12345678901234"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.resultCode").value("400-8"))
                .andExpect(jsonPath("$.msg").value("올바른 ISBN-13 형식이 아닙니다. (13자리 숫자)"));
    }


}