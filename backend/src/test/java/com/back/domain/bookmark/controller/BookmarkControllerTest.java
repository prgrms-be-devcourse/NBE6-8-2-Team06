package com.back.domain.bookmark.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.book.service.BookService;
import com.back.domain.book.category.entity.Category;
import com.back.domain.bookmarks.controller.BookmarkController;
import com.back.domain.bookmarks.service.BookmarkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class BookmarkControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private BookmarkService bookmarkService;
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setup() {
        // Book 엔티티 생성 시, @Column(nullable = false) 제약 조건이 있는 필드는 반드시 초기화해야 합니다.
        // Book 엔티티의 생성자에 맞게 필드 값을 채워주세요.
        // 예시: new Book(String title, String imageUrl, float avgRate, int totalPage, LocalDateTime publishedDate, String publisher, Category category)
        Category category = new Category("테스트");

        Book book = new Book();
        book.setTitle("테스트 도서 제목");
        book.setImageUrl("http://example.com/image.jpg");     // imageUrl
        book.setAvgRate(4.0f);                               // avgRate
        book.setTotalPage(300);                                // totalPage (nullable=false일 가능성 높음)
        book.setPublishedDate(LocalDateTime.of(2023, 1, 1, 0, 0)); // publishedDate
        book.setPublisher("테스트 출판사");                       // publisher
        book.setCategory(category);
        // DB에 저장하면 @GeneratedValue 전략에 따라 ID가 할당됩니다.
        bookRepository.save(book);
    }

    @Test
    @DisplayName("내 책 추가")
    void t1() throws Exception {
        Book book = bookRepository.findById(1).get();
        ResultActions resultActions = mockMvc.perform(
                post("/api/bookmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "bookId" : %d
                        }
                        """.formatted(book.getId()))
        ).andDo(print());

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("addBookmark"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d 번 책이 내 책 목록에 추가되었습니다.".formatted(book.getId())))
                .andExpect(jsonPath("$.data.bookId").value(1));
    }
}
