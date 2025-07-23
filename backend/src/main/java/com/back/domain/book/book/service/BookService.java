package com.back.domain.book.book.service;


import com.back.domain.book.book.entity.Book;
import com.back.domain.book.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

}
