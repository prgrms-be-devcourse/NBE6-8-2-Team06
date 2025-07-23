package com.back.domain.user.user.controller;

import com.back.domain.user.user.dto.UserDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.service.UserService;
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
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    record UserJoinReqBody(
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
    public RsData<UserDto> join(
            @Valid @RequestBody UserJoinReqBody reqBody
    ){
        userService.findByEmail(reqBody.email).ifPresent(user -> {
            throw new RuntimeException("같은 이메일이 존재합니다. 다른 이메일로 가입해주세요.");
        });
        User user = userService.join(
                reqBody.email(),
                reqBody.name,
                passwordEncoder.encode(reqBody.password)
        );
        return new RsData<>(
                "201-1",
                "%s님 환영합니다. Bookers 회원가입이 완료되었습니다.".formatted(user.getName()),
                new UserDto(user)
        );
    }



}
