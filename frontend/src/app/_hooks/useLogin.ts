import { useState } from "react";
import { apiFetch } from "@/lib/apiFetch";
import { LoginForm, ApiResponse } from "@/types/auth";
import { useAuth } from "./auth-context";

const validateLoginForm = (form: LoginForm): string | null => {
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  if (!form.email.trim()) return "이메일을 입력해주세요.";
  if (!emailPattern.test(form.email))
    return "올바른 이메일 형식을 입력해주세요.";
  if (!form.password) return "비밀번호를 입력해주세요.";

  return null;
};

export function useLogin() {
  const [isLoading, setIsLoading] = useState(false);
  const { login: authLogin } = useAuth();

  const login = async (
    form: LoginForm
  ): Promise<{ success: boolean; error?: string }> => {
    setIsLoading(true);

    const validationError = validateLoginForm(form);
    if (validationError) {
      setIsLoading(false);
      return { success: false, error: validationError };
    }

    try {
      const res = await apiFetch<ApiResponse>("/user/login", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(form),
      });

      // AuthContext의 login을 호출하여 사용자 정보 갱신
      await authLogin();

      return { success: true };
    } catch (err: any) {
      const errorMsg = err.message || "로그인에 실패했습니다.";
      return { success: false, error: errorMsg };
    } finally {
      setIsLoading(false);
    }
  };

  return { login, isLoading };
}
