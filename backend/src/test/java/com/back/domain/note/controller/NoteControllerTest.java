package com.back.domain.note.controller;

import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.note.entity.Note;
import com.back.domain.note.service.NoteService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // security 필터 비활성(나중에 제거해야함)
public class NoteControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private NoteService noteService;

    @Test
    @DisplayName("노트 단건 조회")
    @Transactional
    @Rollback
    void t1() throws Exception {
        int bookmarkId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/%d/notes/%d".formatted(bookmarkId, id))
                )
                .andDo(print());

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();
        Note note = noteService.findNoteById(bookmark, id).get();

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("getItem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(note.getId()))
                .andExpect(jsonPath("$.createDate").value(Matchers.startsWith(note.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.modifyDate").value(Matchers.startsWith(note.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.title").value(note.getTitle()))
                .andExpect(jsonPath("$.content").value(note.getContent()));
    }

    @Test
    @DisplayName("노트 다건 조회")
    @Transactional
    @Rollback
    void t2() throws Exception {
        int bookmarkId = 1;

        ResultActions resultActions = mvc
                .perform(
                        get("/bookmarks/%d/notes".formatted(bookmarkId))
                )
                .andDo(print());

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();
        List<Note> notes = bookmark.getNotes();

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("getItems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(notes.size()));

        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);

            resultActions
                    .andExpect(jsonPath("$[%d].id".formatted(i)).value(note.getId()))
                    .andExpect(jsonPath("$[%d].createDate".formatted(i)).value(Matchers.startsWith(note.getCreateDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].modifyDate".formatted(i)).value(Matchers.startsWith(note.getModifyDate().toString().substring(0, 20))))
                    .andExpect(jsonPath("$[%d].title".formatted(i)).value(note.getTitle()))
                    .andExpect(jsonPath("$[%d].content".formatted(i)).value(note.getContent()));
        }
    }

    @Test
    @DisplayName("노트 작성")
    @Transactional
    @Rollback
    void t3() throws Exception {
        int bookmarkId = 1;

        ResultActions resultActions = mvc
                .perform(
                        post("/bookmarks/%d/notes".formatted(bookmarkId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content("""
                                        {
                                            "title": "테스트 제목",
                                            "content": "테스트 내용"
                                        }
                                        """)
                )
                .andDo(print());

        Bookmark bookmark = noteService.findBookmarkById(bookmarkId).get();
        Note note = bookmark.getNotes().getLast();

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("write"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").value("%d번 노트가 작성되었습니다.".formatted(note.getId())))
                .andExpect(jsonPath("$.data.id").value(note.getId()))
                .andExpect(jsonPath("$.data.createDate").value(Matchers.startsWith(note.getCreateDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.modifyDate").value(Matchers.startsWith(note.getModifyDate().toString().substring(0, 20))))
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용"));
    }

    @Test
    @DisplayName("노트 수정")
    @Transactional
    @Rollback
    void t4() throws Exception {
        int bookmarkId = 1;
        int id = 1;

        ResultActions resultActions = mvc
                .perform(
                        put("/bookmarks/%d/notes/%d".formatted(bookmarkId, id))
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding(StandardCharsets.UTF_8)
                                .content("""
                                        {
                                            "title": "테스트 제목 new",
                                            "content": "테스트 내용 new"
                                        }
                                        """)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("modify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 노트가 수정되었습니다.".formatted(id)));
    }

    @Test
    @DisplayName("노트 삭제")
    @Transactional
    @Rollback
    void t5() throws Exception {
        int bookmarkId = 1;
        int id = 3;

        ResultActions resultActions = mvc
                .perform(
                        delete("/bookmarks/%d/notes/%d".formatted(bookmarkId, id))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(NoteController.class))
                .andExpect(handler().methodName("delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("%d번 노트가 삭제되었습니다.".formatted(id)));
    }
}
