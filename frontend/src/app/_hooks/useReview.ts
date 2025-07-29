import { apiFetch } from "@/lib/apiFetch";
import { ApiResponse } from "@/types/auth";

export interface ReviewResponseDto{
  id:number,
  content:string,
  rate:string,
  memberName:string,
  memberId:number,
  likeCount:number,
  dislikeCount:number,
  createdDate:string,
  modifiedDate:string
};

export const useReviewRecommend = (reviewId:number) =>{
  const createReviewRecommend = async (isRecommend:boolean) => {
    const res = await apiFetch<ApiResponse>(`/reviewRecommend/${reviewId}/${isRecommend}`,{
      method:"POST",
      headers:{
        "Content-Type":"application/json"
      }});
  }

  const modifyReviewRecomend = async (isRecommend:boolean) => {
    const res = await apiFetch<ApiResponse>(`/reviewRecommend/${reviewId}/${isRecommend}`,{
      method:"PUT",
      headers:{
        "Content-Type":"application/json"
      }
    });
  }

  const deleteReviewRecommend = async () => {
    const res = await apiFetch<ApiResponse>(`/reviewRecommend/${reviewId}`, {
      method:"DELETE",
      headers:{
        "Content-Type":"application/json"
      }
    });
  }
  return {
    createReviewRecommend,
    modifyReviewRecomend,
    deleteReviewRecommend
  }
}

export const useReview = (bookId:number) =>{
  
  const getReview = async () =>{
    const res = await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
      method:"GET",
      headers:{
        "Content-Type":"application/json",
      }});
    const data:ReviewResponseDto = res.data;
    return data;
  }

  const createReview = async ({ rating, review } : {rating:number, review:string}) => {
      await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body:JSON.stringify({"content":review, "rate":rating})
        });
  }

  const editReview = async({rating, review}:{rating:Number, review:string}) => {
    await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
      method:"PUT",
      headers:{
        "Content-Type":"application/json",
      },
      body:JSON.stringify({"content":review, "rate":rating})
    });
  }

  const deleteReview = async() =>{
    await apiFetch<ApiResponse>(`/reviews/${bookId}`, {
      method:"DELETE",
      headers:{
        "Content-Type":"application/json",
      },
    })
  }

  return {
      createReview,
      editReview,
      deleteReview,
      getReview
  }
}