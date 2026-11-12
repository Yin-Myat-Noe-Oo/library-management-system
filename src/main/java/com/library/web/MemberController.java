package com.library.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.exception.LibraryException;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;
    private final BookService bookService;

    public MemberController(MemberService memberService, LoanService loanService, BookService bookService) {
        this.memberService = memberService;
        this.loanService = loanService;
        this.bookService = bookService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("members", memberService.findAll());
        return "members/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        var loans = loanService.findByMember(id);
        model.addAttribute("loans", loans);
        model.addAttribute("activeCount", loans.stream().filter(l -> !l.isReturned()).count());
        model.addAttribute("overdueCount", loans.stream().filter(Loan::isOverdue).count());
        model.addAttribute("totalFines", loans.stream().map(Loan::getFine)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        model.addAttribute("books", bookService.findAll(null));
        return "members/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("member", new Member());
        return "members/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.findById(id));
        return "members/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("member") Member member, BindingResult result,
                       RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "members/form";
        }
        Member saved;
        try {
            if (member.getId() == null) {
                saved = memberService.create(member);
                flash.addFlashAttribute("success", "Welcome, " + member.getName() + " — membership created");
            } else {
                saved = memberService.update(member.getId(), member);
                flash.addFlashAttribute("success", "Updated " + member.getName());
            }
        } catch (LibraryException ex) {
            result.reject("rule", ex.getMessage());
            return "members/form";
        }
        return "redirect:/members/" + saved.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        try {
            memberService.delete(id);
            flash.addFlashAttribute("success", "Member removed");
        } catch (LibraryException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/members/" + id;
        }
        return "redirect:/members";
    }
}
