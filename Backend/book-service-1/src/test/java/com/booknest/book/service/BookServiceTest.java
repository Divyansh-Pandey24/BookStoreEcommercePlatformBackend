package com.booknest.book.service;

import com.booknest.book.document.BookDocument;
import com.booknest.book.dto.BookRequest;
import com.booknest.book.dto.BookResponse;
import com.booknest.book.entity.Book;
import com.booknest.book.repository.BookRepository;
import com.booknest.book.repository.BookSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @Mock private BookSearchRepository bookSearchRepository;
    @Mock private ImageStorageService imageStorageService;

    @InjectMocks
    private BookService bookService;

    private Book sampleBook;
    private BookRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleBook = new Book();
        sampleBook.setBookId(1L);
        sampleBook.setTitle("Clean Code");
        sampleBook.setAuthor("Robert Martin");
        sampleBook.setIsbn("978-0132350884");
        sampleBook.setGenre("Technology");
        sampleBook.setPrice(499.0);
        sampleBook.setStock(10);
        sampleBook.setRating(4.5);
        sampleBook.setActive(true);
        sampleBook.setFeatured(false);

        sampleRequest = new BookRequest();
        sampleRequest.setTitle("Clean Code");
        sampleRequest.setAuthor("Robert Martin");
        sampleRequest.setIsbn("978-0132350884");
        sampleRequest.setGenre("Technology");
        sampleRequest.setPrice(499.0);
        sampleRequest.setStock(10);
    }

    // ─────────────────────────── ADD BOOK ───────────────────────────

    @Test
    @DisplayName("addBook: admin role, no duplicate ISBN → book saved and synced to ES")
    void addBook_success() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.addBook(sampleRequest, "ADMIN");

        assertThat(response.getTitle()).isEqualTo("Clean Code");
        assertThat(response.getAuthor()).isEqualTo("Robert Martin");
        verify(bookRepository).save(any(Book.class));
        verify(bookSearchRepository).save(any(BookDocument.class));
    }

    @Test
    @DisplayName("addBook: non-admin role → throws RuntimeException")
    void addBook_notAdmin_throws() {
        assertThatThrownBy(() -> bookService.addBook(sampleRequest, "CUSTOMER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin privileges required");

        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("addBook: duplicate ISBN → throws RuntimeException")
    void addBook_duplicateIsbn_throws() {
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(true);

        assertThatThrownBy(() -> bookService.addBook(sampleRequest, "ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("ISBN already exists");
    }

    // ─────────────────────────── UPDATE BOOK ───────────────────────────

    @Test
    @DisplayName("updateBook: admin role, book found → updated and synced to ES")
    void updateBook_success() {
        when(bookRepository.findByBookIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.updateBook(1L, sampleRequest, "ADMIN");

        assertThat(response).isNotNull();
        verify(bookRepository).save(any(Book.class));
        verify(bookSearchRepository).save(any(BookDocument.class));
    }

    @Test
    @DisplayName("updateBook: book not found → throws RuntimeException")
    void updateBook_bookNotFound_throws() {
        when(bookRepository.findByBookIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.updateBook(99L, sampleRequest, "ADMIN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }

    // ─────────────────────────── DELETE BOOK ───────────────────────────

    @Test
    @DisplayName("deleteBook: admin, book found → soft-deletes and removes from ES")
    void deleteBook_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        bookService.deleteBook(1L, "ADMIN");

        assertThat(sampleBook.getActive()).isFalse();
        verify(bookRepository).save(sampleBook);
        verify(bookSearchRepository).deleteById("1");
    }

    @Test
    @DisplayName("deleteBook: non-admin → throws RuntimeException")
    void deleteBook_notAdmin_throws() {
        assertThatThrownBy(() -> bookService.deleteBook(1L, "CUSTOMER"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Admin privileges required");
    }

    // ─────────────────────────── UPDATE STOCK ───────────────────────────

    @Test
    @DisplayName("updateStock: admin, book found → stock updated and ES synced")
    void updateStock_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.updateStock(1L, 25, "ADMIN");

        assertThat(response).isNotNull();
        assertThat(sampleBook.getStock()).isEqualTo(25);
        verify(bookSearchRepository).save(any(BookDocument.class));
    }

    // ─────────────────────────── TOGGLE FEATURED ───────────────────────────

    @Test
    @DisplayName("toggleFeatured: flips featured flag from false to true")
    void toggleFeatured_success() {
        sampleBook.setFeatured(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        bookService.toggleFeatured(1L, "ADMIN");

        assertThat(sampleBook.getFeatured()).isTrue();
        verify(bookSearchRepository).save(any(BookDocument.class));
    }

    // ─────────────────────────── GET ALL BOOKS ───────────────────────────

    @Test
    @DisplayName("getAllBooks: returns all active books")
    void getAllBooks_returnsList() {
        when(bookRepository.findByActiveTrue()).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.getAllBooks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
    }

    // ─────────────────────────── GET BOOK BY ID ───────────────────────────

    @Test
    @DisplayName("getBookById: found → returns BookResponse")
    void getBookById_found_returnsResponse() {
        when(bookRepository.findByBookIdAndActiveTrue(1L)).thenReturn(Optional.of(sampleBook));

        BookResponse response = bookService.getBookById(1L);

        assertThat(response.getBookId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("getBookById: not found → throws RuntimeException")
    void getBookById_notFound_throws() {
        when(bookRepository.findByBookIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Book not found");
    }

    // ─────────────────────────── SEARCH BOOKS ───────────────────────────

    @Test
    @DisplayName("searchBooks: empty keyword → returns all books")
    void searchBooks_emptyKeyword_returnsAllBooks() {
        when(bookRepository.findByActiveTrue()).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.searchBooks("");

        assertThat(result).hasSize(1);
        verify(bookSearchRepository, never()).fuzzySearch(any());
    }

    @Test
    @DisplayName("searchBooks: Elasticsearch hits → returns ES docs")
    void searchBooks_elasticsearchHits_returnsEsDocs() {
        BookDocument doc = new BookDocument();
        doc.setBookId("1");
        doc.setTitle("Clean Code");
        doc.setAuthor("Robert Martin");
        doc.setPrice(499.0);
        doc.setStock(10);

        when(bookSearchRepository.fuzzySearch("clean")).thenReturn(List.of(doc));

        List<BookResponse> result = bookService.searchBooks("clean");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
        verify(bookRepository, never()).searchBooks(any());
    }

    @Test
    @DisplayName("searchBooks: Elasticsearch down → falls back to MySQL")
    void searchBooks_elasticsearchDown_fallsBackToMysql() {
        when(bookSearchRepository.fuzzySearch("clean")).thenThrow(new RuntimeException("ES down"));
        when(bookRepository.searchBooks("clean")).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.searchBooks("clean");

        assertThat(result).hasSize(1);
        verify(bookRepository).searchBooks("clean");
    }

    // ─────────────────────────── STOCK OPERATIONS ───────────────────────────

    @Test
    @DisplayName("checkStock: stock >= quantity → returns true")
    void checkStock_sufficient_returnsTrue() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        boolean result = bookService.checkStock(1L, 5);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("checkStock: stock < quantity → returns false")
    void checkStock_insufficient_returnsFalse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        boolean result = bookService.checkStock(1L, 20); // stock is 10

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("reserveStock: stock sufficient → decreases stock and returns true")
    void reserveStock_success_returnsTrue() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        boolean result = bookService.reserveStock(1L, 3);

        assertThat(result).isTrue();
        assertThat(sampleBook.getStock()).isEqualTo(7); // 10 - 3
    }

    @Test
    @DisplayName("reserveStock: insufficient stock → returns false without saving")
    void reserveStock_insufficientStock_returnsFalse() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));

        boolean result = bookService.reserveStock(1L, 15); // stock is 10

        assertThat(result).isFalse();
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("releaseStock: increases stock by given quantity")
    void releaseStock_success_stockIncreased() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        bookService.releaseStock(1L, 3);

        assertThat(sampleBook.getStock()).isEqualTo(13); // 10 + 3
        verify(bookSearchRepository).save(any(BookDocument.class));
    }

    // ─────────────────────────── UPDATE RATING ───────────────────────────

    @Test
    @DisplayName("updateRating: rounds to one decimal and saves")
    void updateRating_success_roundsToOneDecimal() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        bookService.updateRating(1L, 4.166);

        assertThat(sampleBook.getRating()).isEqualTo(4.2);
        verify(bookSearchRepository).save(any(BookDocument.class));
    }

    // ─────────────────────────── PRICE RANGE ───────────────────────────

    @Test
    @DisplayName("getBooksByPriceRange: invalid range → throws RuntimeException")
    void getBooksByPriceRange_invalidRange_throws() {
        assertThatThrownBy(() -> bookService.getBooksByPriceRange(500.0, 100.0))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid price range");
    }

    @Test
    @DisplayName("getBooksByPriceRange: valid range → returns books")
    void getBooksByPriceRange_validRange_returnsBooks() {
        when(bookRepository.findByPriceBetweenAndActiveTrue(100.0, 500.0)).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.getBooksByPriceRange(100.0, 500.0);

        assertThat(result).hasSize(1);
    }

    // ─────────────────────────── GET BOOKS BY GENRE ───────────────────────────

    @Test
    @DisplayName("getBooksByGenre: returns list of books")
    void getBooksByGenre_returnsList() {
        when(bookRepository.findByGenreIgnoreCaseAndActiveTrue("Technology")).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.getBooksByGenre("Technology");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGenre()).isEqualTo("Technology");
    }

    // ─────────────────────────── GET FEATURED BOOKS ───────────────────────────

    @Test
    @DisplayName("getFeaturedBooks: returns list of featured books")
    void getFeaturedBooks_returnsList() {
        sampleBook.setFeatured(true);
        when(bookRepository.findByFeaturedTrueAndActiveTrue()).thenReturn(List.of(sampleBook));

        List<BookResponse> result = bookService.getFeaturedBooks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeatured()).isTrue();
    }

    // ─────────────────────────── UPLOAD COVER IMAGE ───────────────────────────

    @Test
    @DisplayName("uploadCoverImage: admin, book found → uploads and saves")
    void uploadCoverImage_success() {
        org.springframework.web.multipart.MultipartFile mockFile = mock(org.springframework.web.multipart.MultipartFile.class);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(sampleBook));
        when(imageStorageService.saveImage(mockFile)).thenReturn("uploads/books/new-cover.jpg");
        when(bookRepository.save(any(Book.class))).thenReturn(sampleBook);

        BookResponse response = bookService.uploadCoverImage(1L, mockFile, "ADMIN");

        assertThat(response).isNotNull();
        assertThat(sampleBook.getCoverImageUrl()).isEqualTo("uploads/books/new-cover.jpg");
        verify(bookRepository).save(sampleBook);
        verify(bookSearchRepository).save(any(BookDocument.class));
    }

    // ─────────────────────────── SYNC ALL TO ES ───────────────────────────

    @Test
    @DisplayName("syncAllBooksToElasticsearch: admin → clears ES and syncs all active books")
    void syncAllBooksToElasticsearch_success() {
        when(bookRepository.findAll()).thenReturn(List.of(sampleBook));

        String result = bookService.syncAllBooksToElasticsearch("ADMIN");

        assertThat(result).contains("Synced 1 books to Elasticsearch");
        verify(bookSearchRepository, times(1)).save(any(BookDocument.class));
    }
}
