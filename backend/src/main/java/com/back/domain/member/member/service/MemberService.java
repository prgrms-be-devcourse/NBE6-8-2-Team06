package com.back.domain.member.member.service;

import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    
    public Member join(String name, String email, String password){
        Member member = new Member(name, email, password);
        return memberRepository.save(member);
    }
    
    public Optional<Member> findByEmail(String email){ 
        return memberRepository.findByEmail(email); 
    }
}
