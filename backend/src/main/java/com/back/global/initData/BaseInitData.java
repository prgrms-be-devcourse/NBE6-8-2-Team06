package com.back.global.initData;


import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.book.service.BookService;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.bookmarks.entity.Bookmark;
import com.back.domain.bookmarks.repository.BookmarkRepository;
import com.back.domain.bookmarks.service.BookmarkService;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.note.repository.NoteRepository;
import com.back.domain.note.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
    @Autowired
    @Lazy
    private BaseInitData self;
    private final MemberService memberService;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final NoteService noteService;
    private final NoteRepository noteRepository;
    private final BookmarkService bookmarkService;
    private final BookmarkRepository bookmarkRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private MemberRepository memberRepository;


    @Bean
    ApplicationRunner baseInitDataApplicationRunner(){
        return args->{
//            self.initReviewData(); // 리뷰 테스트 시 주석 해제
            self.initBookData(); // 책 데이터 초기화
//            self.initNoteData(); // Note 관련 데이터
            self.initBookmarkData(); // Bookmark 데이터 초기화
        };
    }

    @Transactional
    public void initReviewData() {
        if (bookRepository.count() > 0) {
            return; // 이미 데이터가 존재하면 초기화하지 않음
        }
        for (int i = 1; i <= 10; i++) {
            memberService.join("testUser" + i, "email" + i + "@a.a", ("password" + i));
        }
//        Category category = categoryRepository.save(new Category("Test Category"));
//        bookRepository.save(new Book("Text Book", "Publisher", category));

    }

    @Transactional
    public void initBookData() {
        // 이미 데이터가 있으면 초기화하지 않음
        if (bookRepository.count() > 0) {
            System.out.println("책 데이터가 이미 존재합니다. 초기화하지 않습니다.");
            return;
        }


        try {
            // 다양한 장르의 인기 도서들을 검색해서 DB에 저장
            String[] searchQueries = {
                    "자바의 정석",           // 프로그래밍
                    "해리포터",             // 소설
                    "미움받을 용기",         // 자기계발
                    "사피엔스",             // 인문학
                    "코스모스",             // 과학
                    "1984",                // 소설
                    "어린왕자"             // 소설
            };

            int totalBooksAdded = 0;

            for (String query : searchQueries) {
                try {

                    // BookService의 searchBooks 메서드를 사용해서 데이터 수집
                    // 각 검색어당 최대 3권씩 가져오기
                    bookService.searchBooks(query, 3);


                    // API 호출 간격 조절 (너무 빠르게 호출하지 않도록)
                    Thread.sleep(500);

                } catch (Exception e) {
                    // 하나의 검색어 실패가 전체를 중단시키지 않도록 continue
                    continue;
                }
            }

            System.out.println("초기 데이터 로딩 완료. 총 " + bookRepository.count() + "권의 책이 저장되었습니다.");

        } catch (Exception e) {
            System.out.println("초기 데이터 로딩 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void initSpecificBooks() {

        String[] specificISBNs = {
                "9788970503806",  // 자바의 정석 3판
                "9788966262281",  // 클린 코드
                "9788932473901",  // 해리포터와 마법사의 돌
                "9788996991304"  // 미움받을 용기
        };

        int addedCount = 0;
        for (String isbn : specificISBNs) {
            try {
                bookService.getBookByIsbn(isbn);
                addedCount++;

                // API 호출 간격 조절
                Thread.sleep(300);

            } catch (Exception e) {
                System.out.println("ISBN: " + isbn + " 책 추가 실패: " + e.getMessage());
            }
        }

        System.out.println("특정 ISBN 책 초기화 완료. 총 " + addedCount + "권의 책이 추가되었습니다.");
    }
  
    public void initNoteData() {
        if (noteService.count() > 0) {
            return;
        }

        Book book = new Book("Text Book", "Publisher", categoryRepository.save(new Category("Test Category")));
        bookRepository.save(book);
        Bookmark bookmark = bookmarkRepository.save(new Bookmark(book, null));
        int id = bookmark.getId();

        noteService.write(id,"제목1", "내용1", "1");
        noteService.write(id,"제목2", "내용2", "2");
        noteService.write(id,"제목3", "내용3", "3");
        noteService.write(id,"제목4", "내용4", "4");
    }

    public void initBookmarkData(){
        if (bookmarkRepository.count() > 0) return;
        Member member;
        if(memberRepository.findByEmail("email@test.com").isEmpty()) {
            member = memberService.join("testUser", "email@test.com", passwordEncoder.encode("password"));
        }
        member = memberRepository.findByEmail("email@test.com").get();
        Book book1 = bookRepository.findById(1).get();
        Book book2 = bookRepository.findById(2).get();
        Book book3 = bookRepository.findById(3).get();
        Bookmark bookmark1 = bookmarkService.save(book1.getId(), member);
        Bookmark bookmark2 = bookmarkService.save(book2.getId(), member);
        bookmarkService.save(book3.getId(), member);
        //bookmarkService.modifyBookmark(member, bookmark1.getId(), "READ", LocalDateTime.of(2025,07,22,12,20), LocalDateTime.now(),book1.getTotalPage());
        //bookmarkService.modifyBookmark(member, bookmark2.getId(), "READING", LocalDateTime.now(), null, 101);
    }
}