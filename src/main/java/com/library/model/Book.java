package com.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Author is required")
    @Size(max = 120)
    private String author;

    @NotBlank(message = "ISBN is required")
    @Size(max = 20)
    @Column(unique = true)
    private String isbn;

    @Size(max = 60)
    private String genre;

    @Min(value = 0, message = "Year must be positive")
    @Max(value = 2100, message = "Year looks invalid")
    private Integer publishedYear;

    @Size(max = 1000)
    @Column(length = 1000)
    private String description;

    @Min(value = 1, message = "At least one copy is required")
    private int totalCopies = 1;

    @Min(0)
    private int availableCopies = 1;

    public Book() {
    }

    public Book(String title, String author, String isbn, String genre, Integer publishedYear, int copies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.genre = genre;
        this.publishedYear = publishedYear;
        this.totalCopies = copies;
        this.availableCopies = copies;
    }

    public Book(String title, String author, String isbn, String genre, Integer publishedYear, int copies,
                String description) {
        this(title, author, isbn, genre, publishedYear, copies);
        this.description = description;
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getTotalCopies() { return totalCopies; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = availableCopies; }
}
