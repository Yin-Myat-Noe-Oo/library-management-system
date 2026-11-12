package com.library.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.library.exception.LibraryException;
import com.library.exception.NotFoundException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;

@Service
@Transactional
public class BookService {

    private final BookRepository books;
    private final LoanRepository loans;

    public BookService(BookRepository books, LoanRepository loans) {
        this.books = books;
        this.loans = loans;
    }

    @Transactional(readOnly = true)
    public List<Book> findAll(String query) {
        return StringUtils.hasText(query) ? books.search(query.trim()) : books.findAllByOrderByTitleAsc();
    }

    @Transactional(readOnly = true)
    public List<Book> findAll(String query, String genre) {
        if (StringUtils.hasText(genre) && !StringUtils.hasText(query)) {
            return books.findByGenreOrderByTitleAsc(genre);
        }
        List<Book> result = findAll(query);
        return StringUtils.hasText(genre)
                ? result.stream().filter(b -> genre.equals(b.getGenre())).toList()
                : result;
    }

    @Transactional(readOnly = true)
    public List<Book> recentlyAdded() {
        return books.findTop6ByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Book> byGenre(String genre, Long excludeId) {
        return books.findTop4ByGenreAndIdNotOrderByTitleAsc(genre, excludeId);
    }

    @Transactional(readOnly = true)
    public List<String> genres() {
        return books.findDistinctGenres();
    }

    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return books.findById(id).orElseThrow(() -> new NotFoundException("Book", id));
    }

    public Book create(Book book) {
        if (books.existsByIsbn(book.getIsbn())) {
            throw new LibraryException("A book with ISBN " + book.getIsbn() + " already exists");
        }
        book.setId(null);
        book.setAvailableCopies(book.getTotalCopies());
        return books.save(book);
    }

    public Book update(Long id, Book changes) {
        Book book = findById(id);
        if (books.existsByIsbnAndIdNot(changes.getIsbn(), id)) {
            throw new LibraryException("Another book already uses ISBN " + changes.getIsbn());
        }
        int onLoan = book.getTotalCopies() - book.getAvailableCopies();
        if (changes.getTotalCopies() < onLoan) {
            throw new LibraryException("Cannot reduce copies below " + onLoan + " (currently on loan)");
        }
        book.setTitle(changes.getTitle());
        book.setAuthor(changes.getAuthor());
        book.setIsbn(changes.getIsbn());
        book.setGenre(changes.getGenre());
        book.setPublishedYear(changes.getPublishedYear());
        book.setDescription(changes.getDescription());
        book.setTotalCopies(changes.getTotalCopies());
        book.setAvailableCopies(changes.getTotalCopies() - onLoan);
        return book;
    }

    public void delete(Long id) {
        Book book = findById(id);
        if (loans.countByBookIdAndReturnDateIsNull(id) > 0) {
            throw new LibraryException("Cannot delete \"" + book.getTitle() + "\" while copies are on loan");
        }
        loans.deleteAll(loans.findByBookId(id));
        books.delete(book);
    }
}
