package com.back.global.initData;


import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.note.repository.NoteRepository;
import com.back.domain.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final NoteService noteService;
    private final NoteRepository noteRepository;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner(){
        return args->{
//            self.initReviewData(); // 리뷰 테스트 시 주석 해제
//            self.initNoteData(); // Note 관련 데이터
        };
    }

    @Transactional
    public void initReviewData() {
        if (bookRepository.count() > 0) {
            return; // 이미 데이터가 존재하면 초기화하지 않음
        }
        memberService.join("testUser", "email", "password");
        Category category = categoryRepository.save(new Category("Test Category"));
        bookRepository.save(new Book("Text Book", "Publisher", category));

    }

    @Transactional
    public void initNoteData() {
        if (noteService.count() > 0) {
            return;
        }

        Bookmark bookmark = bookmarkRepository.save(new Bookmark(null));
        int id = bookmark.getId();

        noteService.write(id,"제목1", "내용1");
        noteService.write(id,"제목2", "내용2");
        noteService.write(id,"제목3", "내용3");
        noteService.write(id,"제목1", "내용4");
    }

    @Autowired
    private BookmarkRepository bookmarkRepository;
}
