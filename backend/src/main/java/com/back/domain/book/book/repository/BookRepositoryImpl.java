package com.back.domain.book.book.repository;

import com.back.domain.book.book.entity.Book;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.back.domain.book.author.entity.QAuthor.author;
import static com.back.domain.book.book.entity.QBook.book;
import static com.back.domain.book.wrote.entity.QWrote.wrote;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Book> findByTitleOrAuthorContaining(String keyword) {
        return queryFactory
                .selectFrom(book)
                .distinct()
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(titleContains(keyword).or(authorNameContains(keyword)))
                .fetch();
    }

    @Override
    public List<Book> findValidBooksByTitleOrAuthorContaining(String query) {
        return queryFactory
                .selectFrom(book)
                .distinct()
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(
                        isValidBook()
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .fetch();
    }

    @Override
    public Page<Book> findAllValidBooks(Pageable pageable) {
        List<Book> content = queryFactory
                .selectFrom(book)
                .where(isValidBook())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(isValidBook())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Optional<Book> findValidBookByIsbn13(String isbn13) {
        Book result = queryFactory
                .selectFrom(book)
                .where(
                        book.isbn13.eq(isbn13)
                                .and(isValidBook())
                )
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<Book> findValidBooksByCategory(String categoryName, Pageable pageable) {
        List<Book> content = queryFactory
                .selectFrom(book)
                .where(
                        book.category.name.eq(categoryName)
                                .and(isValidBook())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.count())
                .from(book)
                .where(
                        book.category.name.eq(categoryName)
                                .and(isValidBook())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<Book> findValidBooksByQueryAndCategory(String query, String categoryName, Pageable pageable) {
        List<Book> content = queryFactory
                .selectFrom(book)
                .distinct()
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(
                        isValidBook()
                                .and(book.category.name.eq(categoryName))
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(book.countDistinct())
                .from(book)
                .leftJoin(book.authors, wrote)
                .leftJoin(wrote.author, author)
                .where(
                        isValidBook()
                                .and(book.category.name.eq(categoryName))
                                .and(titleContains(query).or(authorNameContains(query)))
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    // 공통 조건 메서드들
    private BooleanExpression isValidBook() {
        return book.totalPage.gt(0);
    }

    private BooleanExpression titleContains(String keyword) {
        return keyword != null ? book.title.toLowerCase().contains(keyword.toLowerCase()) : null;
    }

    private BooleanExpression authorNameContains(String keyword) {
        return keyword != null ? author.name.toLowerCase().contains(keyword.toLowerCase()) : null;
    }
}