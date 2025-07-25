package com.back.global.initData;


import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
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

    @Bean
    ApplicationRunner baseInitDataApplicationRunner(){
        return args->{
//            self.initReviewData(); // 리뷰 테스트 시 주석 해제
        };
    }

    @Transactional
    public void initReviewData() {
        memberService.join("testUser", "email", "password");
        Category category = categoryRepository.save(new Category("Test Category"));
        bookRepository.save(new Book("Text Book", "Publisher", category));

    }
}
