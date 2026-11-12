package com.library.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;
    private final LoanService loanService;
    private final MemberService memberService;

    public BookController(BookService bookService, LoanService loanService, MemberService memberService) {
        this.bookService = bookService;
        this.loanService = loanService;
        this.memberService = memberService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, @RequestParam(required = false) String genre,
                       Model model) {
        model.addAttribute("books", bookService.findAll(q, genre));
        model.addAttribute("genres", bookService.genres());
        model.addAttribute("q", q);
        model.addAttribute("genre", genre);
        return "books/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Book book = bookService.findById(id);
        model.addAttribute("book", book);
        model.addAttribute("loans", loanService.findByBook(id));
        model.addAttribute("members", memberService.findActive());
        model.addAttribute("related", book.getGenre() == null ? java.util.List.of()
                : bookService.byGenre(book.getGenre(), id));
        return "books/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findById(id));
        return "books/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("book") Book book, BindingResult result, RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "books/form";
        }
        Book saved;
        try {
            if (book.getId() == null) {
                saved = bookService.create(book);
                flash.addFlashAttribute("success", "Added \"" + book.getTitle() + "\" to the collection");
            } else {
                saved = bookService.update(book.getId(), book);
                flash.addFlashAttribute("success", "Updated \"" + book.getTitle() + "\"");
            }
        } catch (LibraryException ex) {
            result.reject("rule", ex.getMessage());
            return "books/form";
        }
        return "redirect:/books/" + saved.getId();
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes flash) {
        try {
            bookService.delete(id);
            flash.addFlashAttribute("success", "Book removed from the collection");
        } catch (LibraryException ex) {
            flash.addFlashAttribute("error", ex.getMessage());
            return "redirect:/books/" + id;
        }
        return "redirect:/books";
    }
}
