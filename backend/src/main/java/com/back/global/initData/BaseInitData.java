package com.back.global.initData;


import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import com.back.domain.book.book.service.BookService;
import com.back.domain.book.category.entity.Category;
import com.back.domain.book.category.repository.CategoryRepository;
import com.back.domain.member.member.service.MemberService;
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
    private final BookService bookService;

    @Bean
    ApplicationRunner baseInitDataApplicationRunner(){
        return args->{
//            self.initReviewData(); // 리뷰 테스트 시 주석 해제
//            self.initBookData(); // 책 데이터 초기화
        };
    }

    @Transactional
    public void initReviewData() {
        memberService.join("testUser", "email", "password");
        Category category = categoryRepository.save(new Category("Test Category"));
        bookRepository.save(new Book("Text Book", "Publisher", category));

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
                    bookService.searchBooks(query, 1, 3);


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
}
