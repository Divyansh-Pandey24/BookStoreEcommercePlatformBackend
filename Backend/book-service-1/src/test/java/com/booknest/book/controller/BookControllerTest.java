package com.booknest.book.controller;

import com.booknest.book.dto.BookRequest;
import com.booknest.book.dto.BookResponse;
import com.booknest.book.exception.GlobalExceptionHandler;
import com.booknest.book.exception.ResourceNotFoundException;
import com.booknest.book.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Integration Tests (MockMvc)")
class BookControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private BookResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleResponse = new BookResponse();
        sampleResponse.setBookId(1L);
        sampleResponse.setTitle("Clean Code");
        sampleResponse.setPrice(499.0);
        sampleResponse.setStock(10);
    }

    private BookRequest validBookRequest() {
        BookRequest req = new BookRequest();
        req.setTitle("Clean Code");
        req.setAuthor("Robert C. Martin");
        req.setGenre("TECH");
        req.setPrice(499.0);
        req.setStock(10);
        return req;
    }

    @Test
    @DisplayName("GET /books: success → 200 OK")
    void getAllBooks_success() throws Exception {
        when(bookService.getAllBooks()).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /books/{id}: success → 200 OK")
    void getBookById_success() throws Exception {
        when(bookService.getBookById(1L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /books/{id}: not found → 404 Not Found")
    void getBookById_notFound_fails() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(get("/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    @DisplayName("POST /books: success → 201 Created")
    void addBook_success() throws Exception {
        BookRequest req = validBookRequest();

        when(bookService.addBook(any(BookRequest.class), eq("ADMIN"))).thenReturn(sampleResponse);

        mockMvc.perform(post("/books")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    @DisplayName("POST /books: unauthorized → 400 Bad Request")
    void addBook_unauthorized_fails() throws Exception {
        BookRequest req = validBookRequest();

        when(bookService.addBook(any(BookRequest.class), eq("CUSTOMER")))
                .thenThrow(new RuntimeException("Only admins can add books"));

        mockMvc.perform(post("/books")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Only admins can add books"));
    }

    @Test
    @DisplayName("GET /books/search: success → 200 OK")
    void searchBooks_success() throws Exception {
        when(bookService.searchBooks("Clean")).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/books/search").param("keyword", "Clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    @DisplayName("GET /books/price-range: success → 200 OK")
    void getByPriceRange_success() throws Exception {
        when(bookService.getBooksByPriceRange(100.0, 500.0)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/books/price-range")
                        .param("min", "100.0")
                        .param("max", "500.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(499.0));
    }
}
