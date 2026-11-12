package com.library.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.service.LoanService;

/** Loads a sample collection, members and loan history on startup so the app has something to show. */
@Configuration
@Profile("!test")
public class DataSeeder {

    @Bean
    CommandLineRunner seed(BookRepository books, MemberRepository members, LoanRepository loans,
                           LoanService loanService) {
        return args -> {
            if (books.count() > 0) {
                return;
            }
            List<Book> b = books.saveAll(sampleBooks());
            List<Member> m = members.saveAll(sampleMembers());
            LocalDate today = LocalDate.now();

            // Active loans
            loanService.borrow(b.get(0).getId(), m.get(0).getId());
            loanService.borrow(b.get(27).getId(), m.get(0).getId());
            loanService.borrow(b.get(20).getId(), m.get(1).getId());
            loanService.borrow(b.get(54).getId(), m.get(2).getId());
            loanService.borrow(b.get(35).getId(), m.get(3).getId());
            loanService.borrow(b.get(44).getId(), m.get(5).getId());
            loanService.borrow(b.get(52).getId(), m.get(6).getId());

            // Overdue loans (backdated)
            backdate(loans, loanService.borrow(b.get(7).getId(), m.get(1).getId()), today.minusDays(21));
            backdate(loans, loanService.borrow(b.get(32).getId(), m.get(4).getId()), today.minusDays(18));
            backdate(loans, loanService.borrow(b.get(59).getId(), m.get(7).getId()), today.minusDays(25));

            // Returned loans — one on time, two late (fines are calculated on return)
            loanService.returnBook(backdate(loans, loanService.borrow(b.get(4).getId(), m.get(0).getId()), today.minusDays(10)).getId());
            loanService.returnBook(backdate(loans, loanService.borrow(b.get(21).getId(), m.get(2).getId()), today.minusDays(20)).getId());
            loanService.returnBook(backdate(loans, loanService.borrow(b.get(40).getId(), m.get(8).getId()), today.minusDays(30)).getId());
        };
    }

    private static Loan backdate(LoanRepository loans, Loan loan, LocalDate borrowed) {
        loan.setBorrowDate(borrowed);
        loan.setDueDate(borrowed.plusDays(14));
        return loans.save(loan);
    }

    private static List<Member> sampleMembers() {
        return List.of(
                new Member("Alice Johnson", "alice@example.com", "555-0101"),
                new Member("Benedict Hale", "benedict@example.com", "555-0102"),
                new Member("Clara Montgomery", "clara@example.com", "555-0103"),
                new Member("Darius Okafor", "darius@example.com", "555-0104"),
                new Member("Eleanor Vance", "eleanor@example.com", "555-0105"),
                new Member("Felix Romano", "felix@example.com", "555-0106"),
                new Member("Genevieve Laurent", "genevieve@example.com", "555-0107"),
                new Member("Hiroshi Tanaka", "hiroshi@example.com", "555-0108"),
                new Member("Isabella Cruz", "isabella@example.com", "555-0109"),
                new Member("Julian Whitmore", "julian@example.com", "555-0110"));
    }

    private static List<Book> sampleBooks() {
        List<Book> list = new ArrayList<>();
        // Classics
        add(list, "Pride and Prejudice", "Jane Austen", "9780141439518", "Classics", 1813, 3,
                "Elizabeth Bennet spars with the proud Mr. Darcy in a sharp comedy of manners, marriage and first impressions.");
        add(list, "Jane Eyre", "Charlotte Brontë", "9780141441146", "Classics", 1847, 2,
                "An orphaned governess finds love and a dark secret at Thornfield Hall.");
        add(list, "Wuthering Heights", "Emily Brontë", "9780141439556", "Classics", 1847, 2,
                "A wild, consuming passion between Catherine and Heathcliff echoes across two generations on the Yorkshire moors.");
        add(list, "Great Expectations", "Charles Dickens", "9780141439563", "Classics", 1861, 2,
                "The orphan Pip rises from the marshes of Kent into London society, guided by a mysterious benefactor.");
        add(list, "Moby-Dick", "Herman Melville", "9780142437247", "Classics", 1851, 2,
                "Captain Ahab's obsessive hunt for the white whale, narrated by the sailor Ishmael.");
        add(list, "Crime and Punishment", "Fyodor Dostoevsky", "9780143058144", "Classics", 1866, 2,
                "A destitute student commits a murder and is tormented by guilt in St. Petersburg.");
        add(list, "Anna Karenina", "Leo Tolstoy", "9780143035008", "Classics", 1878, 2,
                "A sweeping portrait of Russian society through a doomed love affair and a search for meaning.");
        add(list, "The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", "Classics", 1925, 3,
                "The mysterious Jay Gatsby pursues a lost love amid the glitter of the Jazz Age.");
        add(list, "Frankenstein", "Mary Shelley", "9780141439471", "Classics", 1818, 2,
                "A young scientist creates life and must face the consequences of his ambition.");
        add(list, "The Picture of Dorian Gray", "Oscar Wilde", "9780141439570", "Classics", 1890, 2,
                "A beautiful young man stays ageless while his portrait records every sin.");
        // Fiction
        add(list, "To Kill a Mockingbird", "Harper Lee", "9780061120084", "Fiction", 1960, 4,
                "Scout Finch watches her father defend a Black man falsely accused in 1930s Alabama.");
        add(list, "1984", "George Orwell", "9780451524935", "Fiction", 1949, 3,
                "Winston Smith rebels against an all-seeing Party that rewrites truth itself.");
        add(list, "Brave New World", "Aldous Huxley", "9780060850524", "Fiction", 1932, 2,
                "An engineered utopia where pleasure and conformity replace freedom.");
        add(list, "The Catcher in the Rye", "J.D. Salinger", "9780316769488", "Fiction", 1951, 2,
                "Holden Caulfield's restless days in New York after leaving prep school.");
        add(list, "One Hundred Years of Solitude", "Gabriel García Márquez", "9780060883287", "Fiction", 1967, 2,
                "Seven generations of the Buendía family in the magical town of Macondo.");
        add(list, "Beloved", "Toni Morrison", "9781400033416", "Fiction", 1987, 2,
                "A formerly enslaved woman is haunted by the past in post-Civil War Ohio.");
        add(list, "The Kite Runner", "Khaled Hosseini", "9781594631931", "Fiction", 2003, 3,
                "A story of friendship, betrayal and redemption between two boys in Kabul.");
        add(list, "The Road", "Cormac McCarthy", "9780307387899", "Fiction", 2006, 2,
                "A father and son walk through a burned America, carrying the fire.");
        add(list, "Never Let Me Go", "Kazuo Ishiguro", "9781400078776", "Fiction", 2005, 2,
                "Former students of an English boarding school slowly learn the truth about their purpose.");
        add(list, "The Remains of the Day", "Kazuo Ishiguro", "9780679731726", "Fiction", 1989, 1,
                "An aging English butler reflects on duty, dignity and a life of restraint.");
        // Fantasy
        add(list, "The Hobbit", "J.R.R. Tolkien", "9780547928227", "Fantasy", 1937, 2,
                "Bilbo Baggins is swept into a quest to reclaim a dwarf kingdom from a dragon.");
        add(list, "The Fellowship of the Ring", "J.R.R. Tolkien", "9780547928210", "Fantasy", 1954, 2,
                "Frodo sets out from the Shire to destroy the One Ring.");
        add(list, "A Game of Thrones", "George R.R. Martin", "9780553593716", "Fantasy", 1996, 3,
                "Noble houses scheme for the Iron Throne while winter approaches.");
        add(list, "The Name of the Wind", "Patrick Rothfuss", "9780756404741", "Fantasy", 2007, 2,
                "Kvothe recounts his journey from orphan to legendary arcanist.");
        add(list, "Harry Potter and the Sorcerer's Stone", "J.K. Rowling", "9780590353427", "Fantasy", 1997, 4,
                "An orphan discovers he is a wizard and begins his first year at Hogwarts.");
        add(list, "The Lion, the Witch and the Wardrobe", "C.S. Lewis", "9780064404990", "Fantasy", 1950, 2,
                "Four children step through a wardrobe into the enchanted land of Narnia.");
        add(list, "Mistborn: The Final Empire", "Brandon Sanderson", "9780765350381", "Fantasy", 2006, 2,
                "A street thief joins a crew planning to overthrow an immortal emperor.");
        // Science Fiction
        add(list, "Dune", "Frank Herbert", "9780441172719", "Science Fiction", 1965, 3,
                "Paul Atreides navigates politics, prophecy and survival on the desert planet Arrakis.");
        add(list, "Foundation", "Isaac Asimov", "9780553293357", "Science Fiction", 1951, 2,
                "A mathematician plans to shorten the dark age after a galactic empire falls.");
        add(list, "Neuromancer", "William Gibson", "9780441569595", "Science Fiction", 1984, 1,
                "A washed-up hacker is hired for one last job in cyberspace.");
        add(list, "The Left Hand of Darkness", "Ursula K. Le Guin", "9780441478125", "Science Fiction", 1969, 1,
                "An envoy on the icy world of Gethen confronts a society without fixed gender.");
        add(list, "Fahrenheit 451", "Ray Bradbury", "9781451673319", "Science Fiction", 1953, 3,
                "A fireman whose job is burning books begins to question everything.");
        add(list, "The Martian", "Andy Weir", "9780553418026", "Science Fiction", 2014, 2,
                "A stranded astronaut must science his way to survival on Mars.");
        add(list, "Project Hail Mary", "Andy Weir", "9780593135204", "Science Fiction", 2021, 2,
                "A lone astronaut wakes with no memory on a mission to save Earth.");
        // Mystery
        add(list, "The Hound of the Baskervilles", "Arthur Conan Doyle", "9780451528018", "Mystery", 1902, 2,
                "Sherlock Holmes investigates a legendary hound on the moors of Devon.");
        add(list, "And Then There Were None", "Agatha Christie", "9780062073488", "Mystery", 1939, 3,
                "Ten strangers on an island are accused of murder and begin to die one by one.");
        add(list, "Murder on the Orient Express", "Agatha Christie", "9780062693662", "Mystery", 1934, 2,
                "Hercule Poirot solves a murder aboard a snowbound luxury train.");
        add(list, "Gone Girl", "Gillian Flynn", "9780307588371", "Mystery", 2012, 2,
                "On their fifth anniversary, Amy Dunne vanishes and her husband becomes the suspect.");
        add(list, "The Girl with the Dragon Tattoo", "Stieg Larsson", "9780307454546", "Mystery", 2005, 2,
                "A journalist and a hacker investigate a decades-old disappearance.");
        add(list, "The Big Sleep", "Raymond Chandler", "9780394758282", "Mystery", 1939, 1,
                "Private eye Philip Marlowe is pulled into blackmail and murder in Los Angeles.");
        // History
        add(list, "Sapiens", "Yuval Noah Harari", "9780062316097", "History", 2011, 3,
                "A brief history of humankind from the Stone Age to the modern era.");
        add(list, "Guns, Germs, and Steel", "Jared Diamond", "9780393354324", "History", 1997, 2,
                "Why some societies came to dominate others, traced through geography and biology.");
        add(list, "The Guns of August", "Barbara W. Tuchman", "9780345476098", "History", 1962, 1,
                "The first month of the First World War and the decisions that shaped it.");
        add(list, "SPQR", "Mary Beard", "9781631492228", "History", 2015, 2,
                "A history of ancient Rome from its mythical founding to the third century.");
        // Science
        add(list, "A Brief History of Time", "Stephen Hawking", "9780553380163", "Science", 1988, 3,
                "Black holes, the Big Bang and the nature of time, explained for everyone.");
        add(list, "The Selfish Gene", "Richard Dawkins", "9780198788607", "Science", 1976, 2,
                "Evolution seen from the point of view of the gene.");
        add(list, "Cosmos", "Carl Sagan", "9780345539434", "Science", 1980, 2,
                "A journey through the universe and humanity's place within it.");
        add(list, "The Gene: An Intimate History", "Siddhartha Mukherjee", "9781476733524", "Science", 2016, 1,
                "The story of genetics, from Mendel's peas to gene editing.");
        // Philosophy
        add(list, "Meditations", "Marcus Aurelius", "9780812968255", "Philosophy", 180, 3,
                "Private reflections of a Roman emperor on Stoic virtue and self-discipline.");
        add(list, "Thus Spoke Zarathustra", "Friedrich Nietzsche", "9780140441185", "Philosophy", 1883, 1,
                "A prophet descends from the mountains to proclaim the Übermensch.");
        add(list, "The Republic", "Plato", "9780140455113", "Philosophy", null, 2,
                "Socrates and his companions debate justice and design an ideal city.");
        // Business
        add(list, "Thinking, Fast and Slow", "Daniel Kahneman", "9780374533557", "Business", 2011, 3,
                "The two systems that drive how we think and the biases that mislead us.");
        add(list, "Atomic Habits", "James Clear", "9780735211292", "Business", 2018, 4,
                "Small changes that compound into remarkable results.");
        add(list, "Good to Great", "Jim Collins", "9780066620992", "Business", 2001, 2,
                "Why some companies make the leap to lasting greatness and others don't.");
        // Software
        add(list, "Clean Code", "Robert C. Martin", "9780132350884", "Software", 2008, 3,
                "Principles and practices for writing readable, maintainable code.");
        add(list, "The Pragmatic Programmer", "Andrew Hunt, David Thomas", "9780135957059", "Software", 2019, 2,
                "Timeless advice on the craft of software development.");
        add(list, "Effective Java", "Joshua Bloch", "9780134685991", "Software", 2018, 2,
                "Best practices for the Java platform, item by item.");
        add(list, "Design Patterns", "Gamma, Helm, Johnson, Vlissides", "9780201633610", "Software", 1994, 1,
                "The classic catalogue of reusable object-oriented design patterns.");
        add(list, "Refactoring", "Martin Fowler", "9780134757599", "Software", 2018, 2,
                "Improving the design of existing code through small, safe steps.");
        // Biography
        add(list, "Steve Jobs", "Walter Isaacson", "9781451648539", "Biography", 2011, 2,
                "The life of Apple's co-founder, based on more than forty interviews with him.");
        add(list, "The Diary of a Young Girl", "Anne Frank", "9780553296983", "Biography", 1947, 2,
                "A Jewish girl's diary written while hiding from the Nazis in Amsterdam.");
        add(list, "Long Walk to Freedom", "Nelson Mandela", "9780316548182", "Biography", 1994, 1,
                "Mandela's own account of his life, imprisonment and the end of apartheid.");
        // Poetry
        add(list, "Leaves of Grass", "Walt Whitman", "9780486456768", "Poetry", 1855, 1,
                "Whitman's lifelong celebration of the self, democracy and America.");
        add(list, "The Odyssey", "Homer (trans. Emily Wilson)", "9780393356250", "Poetry", 2018, 2,
                "Odysseus's ten-year voyage home from Troy, in a modern verse translation.");
        return list;
    }

    private static void add(List<Book> list, String title, String author, String isbn, String genre,
                            Integer year, int copies, String description) {
        list.add(new Book(title, author, isbn, genre, year, copies, description));
    }
}
