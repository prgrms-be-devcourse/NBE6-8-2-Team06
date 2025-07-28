"use client"
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { ArrowLeft, Save, Star, X } from "lucide-react";
import { useRouter } from "next/navigation";
import { use, useState } from "react";

interface WriteReviewPageProps {
    bookId: number | null;
    onNavigate: (page: string) => void;
  }
  
  interface MyBook {
    id: number;
    title: string;
    author: string;
    category: string;
    status: '읽은 책' | '읽고 있는 책' | '읽고 싶은 책';
    rating?: number;
    review?: string;
    totalPages: number;
  }

  export default function page({params}:{params:Promise<{bookId:string}>}){
    const {bookId:bookIdStr} = use(params);
    const bookId = parseInt(bookIdStr);
      
    const router = useRouter();
    const onNavigate = (e:string)=>{
      router.push(e);
    }

    // 내 책 데이터
  const myBooks: MyBook[] = [
    {
      id: 1,
      title: "클린 코드",
      author: "로버트 C. 마틴",
      category: "프로그래밍",
      status: "읽은 책",
      rating: 4.5,
      review: "코드 품질에 대한 훌륭한 인사이트를 제공합니다. 특히 변수명과 함수명의 중요성에 대해 깊이 깨달았습니다.",
      totalPages: 464
    },
    {
      id: 2,
      title: "리팩터링",
      author: "마틴 파울러",
      category: "프로그래밍",
      status: "읽은 책",
      totalPages: 550
    }
  ];

  const book = myBooks.find(b => b.id === bookId);
  const [rating, setRating] = useState(book?.rating || 0);
  const [review, setReview] = useState(book?.review || '');
  const [hoveredRating, setHoveredRating] = useState(0);

  if (!book) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p>책을 찾을 수 없습니다.</p>
          <Button onClick={() => onNavigate('/bookmark')} className="mt-4">
            내 책 목록으로 돌아가기
          </Button>
        </div>
      </div>
    );
  }

  const handleStarClick = (starRating: number) => {
    setRating(starRating === rating ? 0 : starRating);
  };

  const handleStarHover = (starRating: number) => {
    setHoveredRating(starRating);
  };

  const handleStarLeave = () => {
    setHoveredRating(0);
  };

  const handleSave = () => {
    // 여기서 실제로는 API 호출을 통해 리뷰를 저장
    console.log('리뷰 저장:', { bookId, rating, review });
    
    onNavigate(`/bookmark/${bookId}`);
  };

  const handleCancel = () => {
    onNavigate(`/bookmark/${bookId}`);
  };

  const renderStars = () => {
    return [...Array(5)].map((_, i) => {
      const starValue = i + 1;
      const isFilled = starValue <= (hoveredRating || rating);
      
      return (
        <button
          key={i}
          type="button"
          className="p-1"
          onClick={() => handleStarClick(starValue)}
          onMouseEnter={() => handleStarHover(starValue)}
          onMouseLeave={handleStarLeave}
        >
          <Star
            className={`h-8 w-8 transition-colors ${
              isFilled 
                ? 'fill-yellow-400 text-yellow-400' 
                : 'text-gray-300 hover:text-yellow-200'
            }`}
          />
        </button>
      );
    });
  };

  const getRatingText = (rating: number) => {
    switch (rating) {
      case 1: return '별로예요';
      case 2: return '그저 그래요';
      case 3: return '보통이에요';
      case 4: return '좋아요';
      case 5: return '최고예요';
      default: return '별점을 선택해주세요';
    }
  };

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button 
        variant="ghost" 
        onClick={handleCancel}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        돌아가기
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 책 정보 */}
        <div className="lg:col-span-1">
          <Card>
            <CardContent className="p-6">
              <div className="text-center">
                <ImageWithFallback
                  src={`https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=200&h=300&fit=crop&crop=center&sig=${book.id}`}
                  alt={book.title}
                  className="w-40 h-60 object-cover rounded mx-auto mb-4"
                />
                <h2 className="text-xl mb-2">{book.title}</h2>
                <p className="text-muted-foreground mb-2">{book.author}</p>
                <p className="text-sm text-muted-foreground">{book.category}</p>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 리뷰 작성 폼 */}
        <div className="lg:col-span-2">
          <Card>
            <CardHeader>
              <CardTitle>리뷰 작성</CardTitle>
              <CardDescription>
                이 책에 대한 솔직한 생각을 들려주세요
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* 별점 */}
              <div className="space-y-3">
                <Label>별점</Label>
                <div className="flex items-center space-x-2">
                  <div className="flex">
                    {renderStars()}
                  </div>
                  <span className="text-sm text-muted-foreground ml-4">
                    {getRatingText(hoveredRating || rating)}
                  </span>
                </div>
              </div>

              {/* 리뷰 작성 */}
              <div className="space-y-3">
                <Label htmlFor="review">리뷰</Label>
                <Textarea
                  id="review"
                  placeholder="이 책에 대한 생각을 자유롭게 작성해주세요. 어떤 점이 좋았는지, 아쉬웠는지, 누구에게 추천하고 싶은지 등을 포함하면 더욱 도움이 됩니다."
                  value={review}
                  onChange={(e) => setReview(e.target.value)}
                  rows={10}
                  className="resize-none"
                />
                <div className="flex justify-between text-sm text-muted-foreground">
                  <span>최소 10자 이상 작성해주세요</span>
                  <span>{review.length}자</span>
                </div>
              </div>

              {/* 리뷰 작성 팁 */}
              <div className="bg-muted p-4 rounded-lg">
                <h4 className="font-medium mb-2">💡 좋은 리뷰 작성 팁</h4>
                <ul className="text-sm text-muted-foreground space-y-1">
                  <li>• 구체적인 내용이나 인상 깊었던 부분을 언급해보세요</li>
                  <li>• 어떤 독자에게 추천하고 싶은지 적어보세요</li>
                  <li>• 개인적인 경험이나 느낀 점을 솔직하게 표현해보세요</li>
                  <li>• 스포일러는 피해주세요</li>
                </ul>
              </div>

              {/* 버튼 */}
              <div className="flex space-x-3 pt-4">
                <Button 
                  onClick={handleSave}
                  disabled={rating === 0 || review.trim().length < 10}
                  className="flex-1"
                >
                  <Save className="h-4 w-4 mr-2" />
                  저장하기
                </Button>
                <Button 
                  variant="outline"
                  onClick={handleCancel}
                >
                  <X className="h-4 w-4 mr-2" />
                  취소
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}