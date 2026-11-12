package com.library.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.service.LoanService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/loans")
public class LoanApiController {

    public record BorrowRequest(@NotNull Long bookId, @NotNull Long memberId) {
    }

    private final LoanService loanService;

    public LoanApiController(LoanService loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<Loan> list(@RequestParam(required = false) LoanStatus status) {
        return loanService.findAll(status);
    }

    @GetMapping("/{id}")
    public Loan get(@PathVariable Long id) {
        return loanService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Loan borrow(@Valid @RequestBody BorrowRequest request) {
        return loanService.borrow(request.bookId(), request.memberId());
    }

    @PostMapping("/{id}/return")
    public Loan returnBook(@PathVariable Long id) {
        return loanService.returnBook(id);
    }
}
