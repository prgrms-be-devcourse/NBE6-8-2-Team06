package com.back.domain.book.book.repository;

import com.back.domain.book.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Integer> {
    // Additional query methods can be defined here if needed
}
