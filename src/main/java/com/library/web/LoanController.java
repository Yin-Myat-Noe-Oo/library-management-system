package com.library.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.exception.LibraryException;
import com.library.model.Loan;
import com.library.model.LoanStatus;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final MemberService memberService;

    public LoanController(LoanService loanService, BookService bookService, MemberService memberService) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.memberService = memberService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) LoanStatus status, Model model) {
        model.addAttribute("loans", loanService.findAll(status));
        model.addAttribute("status", status);
        model.addAttribute("statuses", LoanStatus.values());
        model.addAttribute("books", bookService.findAll(null));
        model.addAttribute("members", memberService.findActive());
        return "loans/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("loan", loanService.findById(id));
        return "loans/detail";
    }

    @PostMapping("/borrow")
    public String borrow(@RequestParam Long bookId, @RequestParam Long memberId,
                         @RequestParam(required = false) String back, RedirectAttributes flash) {
        try {
            Loan loan = loanService.borrow(bookId, memberId);
            flash.addFlashAttribute("success", loan.getMember().getName() + " borrowed \""
                    + loan.getBook().getTitle() + "\" — due " + loan.getDueDate());
        } catch (LibraryException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return redirect(back);
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, @RequestParam(required = false) String back,
                             RedirectAttributes flash) {
        try {
            Loan loan = loanService.returnBook(id);
            String fine = loan.getFine().signum() > 0 ? " — late fine $" + loan.getFine() : "";
            flash.addFlashAttribute("success", "\"" + loan.getBook().getTitle() + "\" returned" + fine);
        } catch (LibraryException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
        }
        return redirect(back);
    }

    /** Only follow local paths, so the form cannot be used as an open redirect. */
    private static String redirect(String back) {
        boolean local = back != null && back.startsWith("/") && !back.startsWith("//") && !back.contains("\\");
        return "redirect:" + (local ? back : "/loans");
    }
}
