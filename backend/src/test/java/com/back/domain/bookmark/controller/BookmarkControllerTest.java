package com.back.domain.bookmark.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.bookmarks.controller.BookmarkController;
import com.back.domain.bookmarks.dto.BookmarkDto;
import com.back.domain.bookmarks.dto.BookmarksDto;
import com.back.domain.bookmarks.service.BookmarkService;
import org.hamcrest.Matchers;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class BookmarkControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookmarkService bookmarkService;

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
    @DisplayName("북마크 추가")
    void t1() throws Exception {
        Book book = bookRepository.findById(1).get();
        ResultActions resultActions = mvc.perform(
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

    @Test
    @DisplayName("북마크 단건 조회")
    void t2() throws Exception {
        int id = 1;
        ResultActions resultActions = mvc
                .perform(
                        get("/api/bookmarks/"+id)
                )
                .andDo(print());

        BookmarkDto bookmarkDto = new BookmarkDto(bookmarkService.getBookmarkById(id));

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 조회 성공".formatted(bookmarkDto.id())))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.user_id").value(bookmarkDto.user_id()))
                .andExpect(jsonPath("$.data.book_id").value(bookmarkDto.book_id()))
                .andExpect(jsonPath("$.date.read_state").value(bookmarkDto.read_state()))
                .andExpect(jsonPath("$.data.read_page").value(bookmarkDto.read_page()))
                .andExpect(jsonPath("$.data.created_at").value(Matchers.startsWith(bookmarkDto.created_at().toString().substring(0,18))))
                .andExpect(jsonPath("$.data.read_start_at").value(Matchers.startsWith(bookmarkDto.read_start_at().toString().substring(0,18))))
                .andExpect(jsonPath("$.data.read_end_at").value(Matchers.startsWith(bookmarkDto.read_end_at().toString().substring(0,18))))
                .andExpect(jsonPath("$.data.reading_duration").value(bookmarkDto.reading_duration()));
    }

    @Test
    @DisplayName("결제 단건 실패")
    void t3() throws Exception {
        int id = Integer.MAX_VALUE;
        ResultActions resultActions = mvc
                .perform(
                        get("/api/bookmarks/"+id)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmark"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("%d번 데이터가 없습니다.".formatted(id)));
    }

    @Test
    @DisplayName("북마크 다건 조회")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/bookmarks/list")
                )
                .andDo(print());

        List<BookmarksDto> bookmarksDtoList = bookmarkService.toList().stream().map(BookmarksDto::new).toList();

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmarksToList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(bookmarksDtoList.size()));

        for(int i=0;i<bookmarksDtoList.size();i++) {
             BookmarksDto bookmarksDto = bookmarksDtoList.get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(bookmarksDto.id()))
                    .andExpect(jsonPath("$[%d].user_id".formatted(i)).value(bookmarksDto.user_id()))
                    .andExpect(jsonPath("$[%d].book_id".formatted(i)).value(bookmarksDto.book_id()))
                    .andExpect(jsonPath("$[%d].read_state".formatted(i)).value(bookmarksDto.read_state()))
                    .andExpect(jsonPath("$[%d].read_page".formatted(i)).value(bookmarksDto.read_page()))
                    .andExpect(jsonPath("$[%d].reading_rate".formatted(i)).value(bookmarksDto.reading_rate()))
                    .andExpect(jsonPath("$[%d].date".formatted(i)).value(Matchers.startsWith(bookmarksDto.date().toString().substring(0,18))));
        }
    }
}
