package com.booknest.book.controller;

import com.booknest.book.dto.BookRequest;
import com.booknest.book.dto.BookResponse;
import com.booknest.book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Unit Tests (Pure Mockito)")
class BookControllerTest {

    @Mock private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private BookResponse sampleResponse;

    @BeforeEach
    void setUp() {
        sampleResponse = new BookResponse();
        sampleResponse.setBookId(1L);
        sampleResponse.setTitle("Clean Code");
        sampleResponse.setPrice(499.0);
        sampleResponse.setStock(10);
    }

    @Test
    @DisplayName("getAllBooks: returns list")
    void getAllBooks_success() {
        when(bookService.getAllBooks()).thenReturn(List.of(sampleResponse));

        ResponseEntity<List<BookResponse>> response = bookController.getAllBooks();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("addBook: success → 201 Created")
    void addBook_success() {
        BookRequest req = new BookRequest();
        req.setTitle("New Book");

        when(bookService.addBook(any(BookRequest.class), eq("ADMIN"))).thenReturn(sampleResponse);

        ResponseEntity<BookResponse> response = bookController.addBook(req, "ADMIN");

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        assertThat(response.getBody().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("deleteBook: success → 204 No Content")
    void deleteBook_success() {
        doNothing().when(bookService).deleteBook(1L, "ADMIN");

        ResponseEntity<Void> response = bookController.deleteBook(1L, "ADMIN");

        assertThat(response.getStatusCodeValue()).isEqualTo(204);
    }
}

