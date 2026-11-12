package com.library.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.library.service.BookService;
import com.library.service.DashboardService;

@Controller
public class HomeController {

    private final DashboardService dashboard;
    private final BookService bookService;

    public HomeController(DashboardService dashboard, BookService bookService) {
        this.dashboard = dashboard;
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("stats", dashboard.stats());
        model.addAttribute("overdue", dashboard.overdue());
        model.addAttribute("recent", bookService.recentlyAdded());
        model.addAttribute("genres", bookService.genres());
        return "index";
    }
}
