package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.config.LibraryProperties;
import com.library.exception.LibraryException;
import com.library.exception.NotFoundException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.Member;
import com.library.repository.LoanRepository;

@Service
@Transactional
public class LoanService {

    private final LoanRepository loans;
    private final BookService bookService;
    private final MemberService memberService;
    private final LibraryProperties props;

    public LoanService(LoanRepository loans, BookService bookService, MemberService memberService,
                       LibraryProperties props) {
        this.loans = loans;
        this.bookService = bookService;
        this.memberService = memberService;
        this.props = props;
    }

    @Transactional(readOnly = true)
    public List<Loan> findAll(LoanStatus status) {
        if (status == null) {
            return loans.findAllByOrderByBorrowDateDescIdDesc();
        }
        return switch (status) {
            case ACTIVE -> loans.findByReturnDateIsNullOrderByDueDateAsc();
            case OVERDUE -> loans.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate.now());
            case RETURNED -> loans.findByReturnDateIsNotNullOrderByReturnDateDesc();
        };
    }

    @Transactional(readOnly = true)
    public List<Loan> findByMember(Long memberId) {
        memberService.findById(memberId);
        return loans.findByMemberIdOrderByBorrowDateDesc(memberId);
    }

    @Transactional(readOnly = true)
    public List<Loan> findByBook(Long bookId) {
        bookService.findById(bookId);
        return loans.findByBookIdOrderByBorrowDateDescIdDesc(bookId);
    }

    @Transactional(readOnly = true)
    public Loan findById(Long id) {
        return loans.findById(id).orElseThrow(() -> new NotFoundException("Loan", id));
    }

    public Loan borrow(Long bookId, Long memberId) {
        Book book = bookService.findById(bookId);
        Member member = memberService.findById(memberId);

        if (!member.isActive()) {
            throw new LibraryException(member.getName() + "'s membership is inactive");
        }
        if (!book.isAvailable()) {
            throw new LibraryException("No copies of \"" + book.getTitle() + "\" are available");
        }
        if (loans.countByMemberIdAndReturnDateIsNull(memberId) >= props.maxActiveLoansPerMember()) {
            throw new LibraryException(member.getName() + " has reached the limit of "
                    + props.maxActiveLoansPerMember() + " active loans");
        }
        if (loans.existsByBookIdAndMemberIdAndReturnDateIsNull(bookId, memberId)) {
            throw new LibraryException(member.getName() + " already has a copy of \"" + book.getTitle() + "\"");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        LocalDate today = LocalDate.now();
        return loans.save(new Loan(book, member, today, today.plusDays(props.loanPeriodDays())));
    }

    public Loan returnBook(Long loanId) {
        Loan loan = findById(loanId);
        if (loan.isReturned()) {
            throw new LibraryException("Loan #" + loanId + " was already returned");
        }
        LocalDate today = LocalDate.now();
        loan.setReturnDate(today);
        loan.setFine(calculateFine(loan.getDueDate(), today));

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        return loan;
    }

    BigDecimal calculateFine(LocalDate dueDate, LocalDate returnDate) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
        return daysLate > 0 ? props.finePerDay().multiply(BigDecimal.valueOf(daysLate)) : BigDecimal.ZERO;
    }
}
