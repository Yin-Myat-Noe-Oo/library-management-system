package com.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @Query("""
            select b from Book b
            where lower(b.title) like lower(concat('%', :q, '%'))
               or lower(b.author) like lower(concat('%', :q, '%'))
               or lower(b.isbn) like lower(concat('%', :q, '%'))
               or lower(b.genre) like lower(concat('%', :q, '%'))
            order by b.title
            """)
    List<Book> search(@Param("q") String query);

    List<Book> findAllByOrderByTitleAsc();

    List<Book> findByGenreOrderByTitleAsc(String genre);

    List<Book> findTop6ByOrderByIdDesc();

    List<Book> findTop4ByGenreAndIdNotOrderByTitleAsc(String genre, Long id);

    @Query("select distinct b.genre from Book b where b.genre is not null order by b.genre")
    List<String> findDistinctGenres();

    @Query("select coalesce(sum(b.totalCopies), 0) from Book b")
    long totalCopies();

    @Query("select coalesce(sum(b.availableCopies), 0) from Book b")
    long availableCopies();
}
