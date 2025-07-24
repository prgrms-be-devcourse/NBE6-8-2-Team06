package com.back.domain.book.book.repository;

import com.back.domain.book.book.entity.Book;
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

    List<Book> findByCategoryId(Long categoryId);

    List<Book> findByPublisherContainingIgnoreCase(String publisher);
}
