package com.library;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoanServiceTest {

    @Autowired BookService bookService;
    @Autowired MemberService memberService;
    @Autowired LoanService loanService;

    private Book book(int copies) {
        return bookService.create(new Book("Test Book", "Author", "ISBN-" + System.nanoTime(), "Test", 2020, copies));
    }

    private Member member() {
        return memberService.create(new Member("Tester", "t" + System.nanoTime() + "@example.com", null));
    }

    @Test
    void borrowAndReturnUpdatesAvailability() {
        Book book = book(2);
        Member member = member();

        Loan loan = loanService.borrow(book.getId(), member.getId());
        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(bookService.findById(book.getId()).getAvailableCopies()).isEqualTo(1);

        Loan returned = loanService.returnBook(loan.getId());
        assertThat(returned.getStatus()).isEqualTo(LoanStatus.RETURNED);
        assertThat(returned.getFine()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(bookService.findById(book.getId()).getAvailableCopies()).isEqualTo(2);
    }

    @Test
    void cannotBorrowWhenNoCopiesLeft() {
        Book book = book(1);
        loanService.borrow(book.getId(), member().getId());

        assertThatThrownBy(() -> loanService.borrow(book.getId(), member().getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("No copies");
    }

    @Test
    void cannotExceedLoanLimit() {
        Member member = member();
        for (int i = 0; i < 5; i++) {
            loanService.borrow(book(1).getId(), member.getId());
        }
        assertThatThrownBy(() -> loanService.borrow(book(1).getId(), member.getId()))
                .isInstanceOf(LibraryException.class)
                .hasMessageContaining("limit");
    }

    @Test
    void lateReturnIsFined() {
        Book book = book(1);
        Loan loan = loanService.borrow(book.getId(), member().getId());
        loan.setDueDate(LocalDate.now().minusDays(4));

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.OVERDUE);
        assertThat(loanService.returnBook(loan.getId()).getFine()).isEqualByComparingTo("2.00");
    }

    @Test
    void cannotDeleteBookOnLoan() {
        Book book = book(1);
        loanService.borrow(book.getId(), member().getId());

        assertThatThrownBy(() -> bookService.delete(book.getId())).isInstanceOf(LibraryException.class);
    }
}
