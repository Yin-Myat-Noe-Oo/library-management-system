package com.library.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.model.Loan;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    public record Stats(long titles, long totalCopies, long availableCopies, long members,
                        long activeLoans, long overdueLoans) {
    }

    private final BookRepository books;
    private final MemberRepository members;
    private final LoanRepository loans;

    public DashboardService(BookRepository books, MemberRepository members, LoanRepository loans) {
        this.books = books;
        this.members = members;
        this.loans = loans;
    }

    public Stats stats() {
        LocalDate today = LocalDate.now();
        return new Stats(books.count(), books.totalCopies(), books.availableCopies(), members.count(),
                loans.countByReturnDateIsNull(), loans.countByReturnDateIsNullAndDueDateBefore(today));
    }

    public List<Loan> overdue() {
        return loans.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate.now());
    }
}
