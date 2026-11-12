package com.library.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.model.Loan;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findAllByOrderByBorrowDateDescIdDesc();

    List<Loan> findByReturnDateIsNullOrderByDueDateAsc();

    List<Loan> findByReturnDateIsNotNullOrderByReturnDateDesc();

    List<Loan> findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate date);

    List<Loan> findByMemberIdOrderByBorrowDateDesc(Long memberId);

    List<Loan> findByBookId(Long bookId);

    List<Loan> findByBookIdOrderByBorrowDateDescIdDesc(Long bookId);

    long countByMemberIdAndReturnDateIsNull(Long memberId);

    long countByBookIdAndReturnDateIsNull(Long bookId);

    boolean existsByBookIdAndMemberIdAndReturnDateIsNull(Long bookId, Long memberId);

    long countByReturnDateIsNull();

    long countByReturnDateIsNullAndDueDateBefore(LocalDate date);
}
