package com.library.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.exception.LibraryException;
import com.library.exception.NotFoundException;
import com.library.model.Member;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;

@Service
@Transactional
public class MemberService {

    private final MemberRepository members;
    private final LoanRepository loans;

    public MemberService(MemberRepository members, LoanRepository loans) {
        this.members = members;
        this.loans = loans;
    }

    @Transactional(readOnly = true)
    public List<Member> findAll() {
        return members.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Member> findActive() {
        return members.findByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {
        return members.findById(id).orElseThrow(() -> new NotFoundException("Member", id));
    }

    public Member create(Member member) {
        if (members.existsByEmailIgnoreCase(member.getEmail())) {
            throw new LibraryException("A member with email " + member.getEmail() + " already exists");
        }
        member.setId(null);
        return members.save(member);
    }

    public Member update(Long id, Member changes) {
        Member member = findById(id);
        if (members.existsByEmailIgnoreCaseAndIdNot(changes.getEmail(), id)) {
            throw new LibraryException("Another member already uses email " + changes.getEmail());
        }
        member.setName(changes.getName());
        member.setEmail(changes.getEmail());
        member.setPhone(changes.getPhone());
        member.setActive(changes.isActive());
        return member;
    }

    public void delete(Long id) {
        Member member = findById(id);
        if (loans.countByMemberIdAndReturnDateIsNull(id) > 0) {
            throw new LibraryException(member.getName() + " still has books on loan");
        }
        loans.deleteAll(loans.findByMemberIdOrderByBorrowDateDesc(id));
        members.delete(member);
    }
}
