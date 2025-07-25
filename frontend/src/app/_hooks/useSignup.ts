import { useState } from "react";
import { apiFetch } from "@/lib/apiFetch";
import { SignupForm, ApiResponse } from "@/types/auth";

const validateSignupForm = (form: SignupForm): string | null => {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!form.name.trim()) return "이름을 입력해주세요.";
  if (form.name.trim().length < 2) return "이름은 2자 이상이어야 합니다.";

  if (!form.email.trim()) return "이메일을 입력해주세요.";
  if (!emailPattern.test(form.email))
    return "올바른 이메일 형식을 입력해주세요.";

  if (!form.password) return "비밀번호를 입력해주세요.";
  if (form.password.length < 8) return "비밀번호는 최소 8자 이상이어야 합니다.";

  if (!form.confirmPassword) return "비밀번호 확인을 입력해주세요.";
  if (form.password !== form.confirmPassword)
    return "비밀번호가 일치하지 않습니다.";

  return null;
};

export function useSignup() {
  const [isLoading, setIsLoading] = useState(false);

  const signup = async (
    form: SignupForm
  ): Promise<{ success: boolean; error?: string }> => {
    setIsLoading(true);

    const validationError = validateSignupForm(form);
    if (validationError) {
      setIsLoading(false);
      return { success: false, error: validationError };
    }

    try {
      // confirmPassword는 서버에 보내지 않음
      const { confirmPassword, ...signupData } = form;

      const res = await apiFetch<ApiResponse>("/user/signup", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(signupData),
      });

      return { success: true };
    } catch (err: any) {
      const errorMsg = err.message || "회원가입에 실패했습니다.";
      return { success: false, error: errorMsg };
    } finally {
      setIsLoading(false);
    }
  };

  return { signup, isLoading };
}
