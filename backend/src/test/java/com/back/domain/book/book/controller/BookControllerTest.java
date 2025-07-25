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
import org.springframework.security.test.context.support.WithMockUser;
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
    @WithMockUser // Spring Security 인증 우회
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
}