package com.back.domain.book.book.repository;

import com.back.domain.book.book.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Integer> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    Optional<Book> findByIsbn13(String isbn13);

    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.authors w " +
            "LEFT JOIN w.author a " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Book> findByTitleOrAuthorContaining(@Param("keyword") String keyword);

    /**
     * 페이지 수가 0보다 큰 유효한 책들만 제목이나 작가명으로 검색
     */
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.authors w " +
            "LEFT JOIN w.author a " +
            "WHERE b.totalPage > 0 AND (" +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    List<Book> findValidBooksByTitleOrAuthorContaining(@Param("query") String query);
    /**
     * 페이지 수가 0보다 큰 유효한 책들만 페이징 조회
     */
    @Query("SELECT b FROM Book b WHERE b.totalPage > 0")
    Page<Book> findAllValidBooks(Pageable pageable);

    /**
     * ISBN으로 유효한 책 조회 (페이지 수 > 0)
     */
    @Query("SELECT b FROM Book b WHERE b.isbn13 = :isbn13 AND b.totalPage > 0")
    Optional<Book> findValidBookByIsbn13(@Param("isbn13") String isbn13);



    List<Book> findByCategoryId(Long categoryId);

    List<Book> findByPublisherContainingIgnoreCase(String publisher);
}
