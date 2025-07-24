package com.back.domain.member.member.controller;

import com.back.domain.member.member.dto.MemberDto;
import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.repository.MemberRepository;
import com.back.domain.member.member.service.MemberService;
import com.back.global.exception.ServiceException;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final Rq rq;

    record MemberJoinReqBody(
        @NotBlank
        @Size(min = 2, max = 30)
        String email,
        @NotBlank
        @Size(min = 2, max = 20)
        String name,
        @NotBlank
        @Size(min = 2, max = 20)
        String password
    ) {}

    @PostMapping("/signup")
    @Transactional
    public RsData<MemberDto> join(
            @Valid @RequestBody MemberJoinReqBody reqBody
    ){
        memberService.findByEmail(reqBody.email()).ifPresent(member -> {
            throw new ServiceException("409","이미 존재하는 이메일 입니다. 다시 입력해주세요.");
        });
        Member member = memberService.join(
                reqBody.name(),
                reqBody.email(),
                passwordEncoder.encode(reqBody.password)
        );
        return new RsData<>(
                "201-1",
                "%s님 환영합니다. Bookers 회원가입이 완료되었습니다.".formatted(member.getName()),
                new MemberDto(member)
        );
    }

    record MemberLoginReqBody(
            @NotBlank
            @Size(min = 2, max = 30)
            String email,
            @NotBlank
            @Size(min = 2, max = 50)
            String password
    ){}

    record MemberLoginResBody(
            MemberDto memDto,
            String accessToken
    ) {
    }

    @PostMapping("/login")
    @Transactional
    public RsData<MemberLoginResBody> login(
            @Valid @RequestBody MemberLoginReqBody reqBody
    ){
        Member member =memberService.findByEmail(reqBody.email())
                .orElseThrow(()->new ServiceException("401-1", "존재하지 않는 아이디입니다."));

        memberService.checkPassword(member,reqBody.password);

        String accessToken =memberService.geneAccessToken(member);

        rq.setCookie("accessToken",accessToken);
        return new RsData<>(
                "200-1",
                "%s님 환영합니다.".formatted(member.getEmail()),
                new MemberLoginResBody(
                        new MemberDto(member),
                        accessToken
                )
        );
    }

}
