import { apiFetch } from "@/lib/apiFetch";
import { ApiResponse } from "@/types/auth";


export const useReview = (bookId:number) =>{
    
    const createReview = async ({ bookId, rating, review } : {bookId:number, rating:number, review:string}) => {
        await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
            },
            body:JSON.stringify({"content":review, "rate":rating})
          });
    }
    return {
        createReview
    }
}