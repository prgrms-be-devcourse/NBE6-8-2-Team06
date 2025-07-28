package com.back.domain.bookmark.controller;

import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.bookmarks.controller.BookmarkController;
import com.back.domain.bookmarks.dto.BookmarkDto;
import com.back.domain.bookmarks.dto.BookmarkDetailDto;
import com.back.domain.bookmarks.dto.BookmarkModifyResponseDto;
import com.back.domain.bookmarks.dto.BookmarkReadStatesDto;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.service.BookmarkService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
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
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setup() {
        Category category = new Category("테스트");
        categoryRepository.save(category);

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
        Member member = new Member("test","test@test.com","test");
        memberRepository.save(member);
    }

    @Test
    @DisplayName("북마크 추가")
    @WithUserDetails("test")
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

        BookmarkDetailDto bookmarkDto = bookmarkService.getBookmarkById(id);

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 조회 성공".formatted(bookmarkDto.bookmarkDto().id())))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.user_id").value(bookmarkDto.bookmarkDto().memberId()))
                .andExpect(jsonPath("$.data.book_id").value(bookmarkDto.bookmarkDto().bookId()))
                .andExpect(jsonPath("$.date.read_state").value(bookmarkDto.bookmarkDto().readState()))
                .andExpect(jsonPath("$.data.read_page").value(bookmarkDto.bookmarkDto().readPage()))
                .andExpect(jsonPath("$.data.created_at").value(Matchers.startsWith(bookmarkDto.createAt().toString().substring(0,18))))
                .andExpect(jsonPath("$.data.read_start_at").value(Matchers.startsWith(bookmarkDto.startReadDate().toString().substring(0,18))))
                .andExpect(jsonPath("$.data.read_end_at").value(Matchers.startsWith(bookmarkDto.endReadDate().toString().substring(0,18))))
                .andExpect(jsonPath("$.data.reading_duration").value(bookmarkDto.readingDuration()));
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
    @DisplayName("북마크 다건 조회 - 목록")
    void t4() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/api/bookmarks/list")
                )
                .andDo(print());

        List<BookmarkDto> bookmarksDtoList = bookmarkService.toList();

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmarksToList"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(bookmarksDtoList.size()));

        for(int i=0;i<bookmarksDtoList.size();i++) {
             BookmarkDto bookmarksDto = bookmarksDtoList.get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(bookmarksDto.id()))
                    .andExpect(jsonPath("$[%d].member_id".formatted(i)).value(bookmarksDto.memberId()))
                    .andExpect(jsonPath("$[%d].book_id".formatted(i)).value(bookmarksDto.bookId()))
                    .andExpect(jsonPath("$[%d].read_state".formatted(i)).value(bookmarksDto.readState()))
                    .andExpect(jsonPath("$[%d].read_page".formatted(i)).value(bookmarksDto.readPage()))
                    .andExpect(jsonPath("$[%d].reading_rate".formatted(i)).value(bookmarksDto.readingRate()))
                    .andExpect(jsonPath("$[%d].date".formatted(i)).value(Matchers.startsWith(bookmarksDto.date().toString().substring(0,18))));
        }
    }

    @Test
    @DisplayName("북마크 다건 조회 - 페이지")
    void t5() throws Exception {
        Member member = memberRepository.findByEmail("test@test.com").get();
        ResultActions resultActions = mvc
                .perform(
                        get("/api/bookmarks")
                )
                .andDo(print());

        Page<BookmarkDto> bookmarksDtoPage = bookmarkService.toPage(member,0,10, null, null, null);

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmarksToPage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(bookmarksDtoPage.getNumberOfElements()))
                .andExpect(jsonPath("$.page_number").value(bookmarksDtoPage.getNumber()))
                .andExpect(jsonPath("$.page_size").value(bookmarksDtoPage.getSize()))
                .andExpect(jsonPath("$.total_page").value(bookmarksDtoPage.getTotalPages()))
                .andExpect(jsonPath("$.total_elements").value(bookmarksDtoPage.getTotalElements()))
                .andExpect(jsonPath("$.is_last").value(bookmarksDtoPage.isLast()));

        for(int i=0;i<bookmarksDtoPage.getSize(); i++) {
            BookmarkDto bookmarksDto = bookmarksDtoPage.getContent().get(i);
            resultActions
                    .andExpect(jsonPath("$[%d].data.id".formatted(i)).value(bookmarksDto.id()))
                    .andExpect(jsonPath("$[%d].data.member_id".formatted(i)).value(bookmarksDto.memberId()))
                    .andExpect(jsonPath("$[%d].data.book_id".formatted(i)).value(bookmarksDto.bookId()))
                    .andExpect(jsonPath("$[%d].data.read_state".formatted(i)).value(bookmarksDto.readState()))
                    .andExpect(jsonPath("$[%d].data.read_page".formatted(i)).value(bookmarksDto.readPage()))
                    .andExpect(jsonPath("$[%d].data.reading_rate".formatted(i)).value(bookmarksDto.readingRate()))
                    .andExpect(jsonPath("$[%d].data.date".formatted(i)).value(Matchers.startsWith(bookmarksDto.date().toString().substring(0,18))));
        }
    }

    @Test
    @DisplayName("북마크 수정")
    @WithUserDetails("test")
    void t6() throws Exception {
        int id=1;
        ResultActions resultActions = mvc.perform(
                post("/api/bookmarks/"+id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "readState" : "READING",
                          "startReadDate" : "2025-07-22",
                          "readPage" : 100
                        }
                        """)
        ).andDo(print());

        Bookmark bookmark = bookmarkService.findById(id);
        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("modifyBookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 북마크가 수정되었습니다.".formatted(id)))
                .andExpect(jsonPath("$.data.bookId").value(bookmark.getId()))
                .andExpect(jsonPath("$.data.member_id").value(bookmark.getMember().getId()))
                .andExpect(jsonPath("$.data.book_id").value(bookmark.getBook().getId()))
                .andExpect(jsonPath("$.data.read_state").value(bookmark.getReadState()))
                .andExpect(jsonPath("$.data.read_page").value(bookmark.getReadPage()))
                .andExpect(jsonPath("$.data.reading_rate").value(bookmark.calculateReadingRate()))
                .andExpect(jsonPath("$.data.date").value(Matchers.startsWith(bookmark.getDisplayDate().toString().substring(0,18))));
    }

    @Test
    @DisplayName("북마크 삭제")
    @WithUserDetails("test")
    void t7() throws Exception {
        int id = 1;
        ResultActions resultActions = mvc
                .perform(
                        delete("/api/bookmarks/"+id)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("deleteBookmark"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d 북마크가 삭제되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("북마크 내책 목록 상태 조회")
    void t8() throws Exception {
        Member member = memberRepository.findByEmail("test@test.com").get();
        ResultActions resultActions = mvc
                .perform(
                        get("/api/bookmarks/read-states")
                )
                .andDo(print());

        BookmarkReadStatesDto bookmarkReadStatesDto = bookmarkService.getReadStatesCount(member);

        resultActions
                .andExpect(handler().handlerType(BookmarkController.class))
                .andExpect(handler().methodName("getBookmark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("조회 성공"))
                .andExpect(jsonPath("$.data.total_count").value(bookmarkReadStatesDto.totalCount()))
                .andExpect(jsonPath("$.data.avg_rate").value(bookmarkReadStatesDto.avgRate()))
                .andExpect(jsonPath("$.data.read_state.READ").value(bookmarkReadStatesDto.readState().READ()))
                .andExpect(jsonPath("$.data.read_state.READING").value(bookmarkReadStatesDto.readState().READING()))
                .andExpect(jsonPath("$.data.read_state.WISH").value(bookmarkReadStatesDto.readState().WISH()));
    }
}
