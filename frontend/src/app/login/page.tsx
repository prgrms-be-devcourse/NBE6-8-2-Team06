"use client";

import React, { useState } from "react";
import { useRouter } from "next/navigation";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Checkbox } from "@/components/ui/checkbox";
import { BookOpen, Eye, EyeOff } from "lucide-react";
import { useLogin } from "../_hooks/useLogin";
import { useSignup } from "../_hooks/useSignup";
import { LoginForm, SignupForm } from "@/types/auth";
import { toast } from "@/lib/toast";

export default function LoginPage() {
  const router = useRouter();
  const { login, isLoading: isLoginLoading } = useLogin();
  const { signup, isLoading: isSignupLoading } = useSignup();
  const [currentTab, setCurrentTab] = useState("login");

  // 폼 상태
  const [loginForm, setLoginForm] = useState<LoginForm>({
    email: "",
    password: "",
  });
  const [signupForm, setSignupForm] = useState<SignupForm>({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  // UI 상태
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [showSignupPassword, setShowSignupPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [agreeToTerms, setAgreeToTerms] = useState(false);

  // 에러 상태
  const [loginErrors, setLoginErrors] = useState<{ [key: string]: string }>({});
  const [signupErrors, setSignupErrors] = useState<{ [key: string]: string }>(
    {}
  );

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();

    const result = await login(loginForm);

    if (result.success) {
      toast.success("로그인되었습니다!");
      router.push("/"); // 홈으로 이동
    } else {
      toast.error(result.error || "로그인에 실패했습니다.");
    }
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();

    // 이용약관 동의 체크
    if (!agreeToTerms) {
      toast.error("이용약관에 동의해주세요.");
      return;
    }

    const result = await signup(signupForm);

    if (result.success) {
      toast.success("회원가입이 완료되었습니다! 로그인해주세요.");
      // 회원가입 성공 후 로그인 탭으로 이동
      setSignupForm({ name: "", email: "", password: "", confirmPassword: "" });
      setAgreeToTerms(false);
      setCurrentTab("login");
    } else {
      toast.error(result.error || "회원가입에 실패했습니다.");
    }
  };

  const handleLoginInputChange = (field: keyof LoginForm, value: string) => {
    setLoginForm((prev) => ({ ...prev, [field]: value }));
    // 에러 메시지 클리어
    if (loginErrors[field]) {
      setLoginErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  const handleSignupInputChange = (field: keyof SignupForm, value: string) => {
    setSignupForm((prev) => ({ ...prev, [field]: value }));
    // 에러 메시지 클리어
    if (signupErrors[field]) {
      setSignupErrors((prev) => ({ ...prev, [field]: "" }));
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <BookOpen className="h-12 w-12 text-primary mx-auto mb-4" />
          <h1 className="text-2xl mb-2">책 관리 시스템</h1>
          <p className="text-muted-foreground">독서 기록을 시작해보세요</p>
        </div>

        <Tabs
          value={currentTab}
          onValueChange={setCurrentTab}
          className="w-full"
        >
          <TabsList className="grid w-full grid-cols-2">
            <TabsTrigger value="login">로그인</TabsTrigger>
            <TabsTrigger value="signup">회원가입</TabsTrigger>
          </TabsList>

          <TabsContent value="login">
            <Card>
              <CardHeader>
                <CardTitle>로그인</CardTitle>
                <CardDescription>
                  계정에 로그인하여 독서 기록을 확인하세요
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleLogin} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="login-email">이메일</Label>
                    <Input
                      id="login-email"
                      type="email"
                      placeholder="이메일을 입력하세요"
                      value={loginForm.email}
                      onChange={(e) =>
                        handleLoginInputChange("email", e.target.value)
                      }
                      className={loginErrors.email ? "border-destructive" : ""}
                      disabled={isLoginLoading}
                    />
                    {loginErrors.email && (
                      <p className="text-sm text-destructive">
                        {loginErrors.email}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="login-password">비밀번호</Label>
                    <div className="relative">
                      <Input
                        id="login-password"
                        type={showLoginPassword ? "text" : "password"}
                        placeholder="비밀번호를 입력하세요"
                        value={loginForm.password}
                        onChange={(e) =>
                          handleLoginInputChange("password", e.target.value)
                        }
                        className={
                          loginErrors.password
                            ? "border-destructive pr-10"
                            : "pr-10"
                        }
                        disabled={isLoginLoading}
                      />
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                        onClick={() => setShowLoginPassword(!showLoginPassword)}
                        disabled={isLoginLoading}
                      >
                        {showLoginPassword ? (
                          <EyeOff className="h-4 w-4 text-muted-foreground" />
                        ) : (
                          <Eye className="h-4 w-4 text-muted-foreground" />
                        )}
                      </Button>
                    </div>
                    {loginErrors.password && (
                      <p className="text-sm text-destructive">
                        {loginErrors.password}
                      </p>
                    )}
                  </div>
                  <div className="text-right">
                    <button
                      type="button"
                      className="text-sm text-primary underline hover:text-primary/80"
                    >
                      비밀번호를 잊으셨나요?
                    </button>
                  </div>
                  <Button
                    type="submit"
                    className="w-full"
                    disabled={isLoginLoading}
                  >
                    {isLoginLoading ? "로그인 중..." : "로그인"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>

          <TabsContent value="signup">
            <Card>
              <CardHeader>
                <CardTitle>회원가입</CardTitle>
                <CardDescription>
                  새 계정을 만들어 독서 여정을 시작하세요
                </CardDescription>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSignup} className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="signup-name">이름 *</Label>
                    <Input
                      id="signup-name"
                      type="text"
                      placeholder="이름을 입력하세요"
                      value={signupForm.name}
                      onChange={(e) =>
                        handleSignupInputChange("name", e.target.value)
                      }
                      className={signupErrors.name ? "border-destructive" : ""}
                      disabled={isSignupLoading}
                    />
                    {signupErrors.name && (
                      <p className="text-sm text-destructive">
                        {signupErrors.name}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="signup-email">이메일 *</Label>
                    <Input
                      id="signup-email"
                      type="email"
                      placeholder="이메일을 입력하세요"
                      value={signupForm.email}
                      onChange={(e) =>
                        handleSignupInputChange("email", e.target.value)
                      }
                      className={signupErrors.email ? "border-destructive" : ""}
                      disabled={isSignupLoading}
                    />
                    {signupErrors.email && (
                      <p className="text-sm text-destructive">
                        {signupErrors.email}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="signup-password">비밀번호 *</Label>
                    <div className="relative">
                      <Input
                        id="signup-password"
                        type={showSignupPassword ? "text" : "password"}
                        placeholder="최소 8자 이상"
                        value={signupForm.password}
                        onChange={(e) =>
                          handleSignupInputChange("password", e.target.value)
                        }
                        className={
                          signupErrors.password
                            ? "border-destructive pr-10"
                            : "pr-10"
                        }
                        disabled={isSignupLoading}
                      />
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                        onClick={() =>
                          setShowSignupPassword(!showSignupPassword)
                        }
                        disabled={isSignupLoading}
                      >
                        {showSignupPassword ? (
                          <EyeOff className="h-4 w-4 text-muted-foreground" />
                        ) : (
                          <Eye className="h-4 w-4 text-muted-foreground" />
                        )}
                      </Button>
                    </div>
                    {signupErrors.password && (
                      <p className="text-sm text-destructive">
                        {signupErrors.password}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="confirm-password">비밀번호 확인 *</Label>
                    <div className="relative">
                      <Input
                        id="confirm-password"
                        type={showConfirmPassword ? "text" : "password"}
                        placeholder="비밀번호를 다시 입력하세요"
                        value={signupForm.confirmPassword}
                        onChange={(e) =>
                          handleSignupInputChange(
                            "confirmPassword",
                            e.target.value
                          )
                        }
                        className={
                          signupErrors.confirmPassword
                            ? "border-destructive pr-10"
                            : "pr-10"
                        }
                        disabled={isSignupLoading}
                      />
                      <Button
                        type="button"
                        variant="ghost"
                        size="sm"
                        className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                        onClick={() =>
                          setShowConfirmPassword(!showConfirmPassword)
                        }
                        disabled={isSignupLoading}
                      >
                        {showConfirmPassword ? (
                          <EyeOff className="h-4 w-4 text-muted-foreground" />
                        ) : (
                          <Eye className="h-4 w-4 text-muted-foreground" />
                        )}
                      </Button>
                    </div>
                    {signupErrors.confirmPassword && (
                      <p className="text-sm text-destructive">
                        {signupErrors.confirmPassword}
                      </p>
                    )}
                  </div>
                  <div className="space-y-2">
                    <div className="flex items-center space-x-2">
                      <Checkbox
                        id="terms"
                        checked={agreeToTerms}
                        onCheckedChange={(checked: boolean) => {
                          setAgreeToTerms(checked);
                          if (signupErrors.terms) {
                            setSignupErrors((prev) => ({ ...prev, terms: "" }));
                          }
                        }}
                        disabled={isSignupLoading}
                      />
                      <Label
                        htmlFor="terms"
                        className="text-sm leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
                      >
                        <span className="text-muted-foreground">
                          <button
                            type="button"
                            className="text-primary underline"
                          >
                            이용약관
                          </button>{" "}
                          및{" "}
                          <button
                            type="button"
                            className="text-primary underline"
                          >
                            개인정보처리방침
                          </button>
                          에 동의합니다 *
                        </span>
                      </Label>
                    </div>
                    {signupErrors.terms && (
                      <p className="text-sm text-destructive">
                        {signupErrors.terms}
                      </p>
                    )}
                  </div>
                  <Button
                    type="submit"
                    className="w-full"
                    disabled={isSignupLoading}
                  >
                    {isSignupLoading ? "회원가입 중..." : "회원가입"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>

        <div className="text-center mt-6">
          <Button variant="ghost" onClick={() => router.push("/")}>
            홈으로 돌아가기
          </Button>
        </div>
      </div>
    </div>
  );
}
