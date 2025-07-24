"use client"
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@radix-ui/react-select";
import { ArrowLeft, BookOpen, Building, Calendar, Globe, Heart, Plus, Star } from "lucide-react";
import { useState } from "react";

interface BookDetailPageProps {
    bookId: number | null;
    onNavigate: (page: string) => void;
    onAddToMyBooks: (bookId: number) => void;
  }
  
  interface Book {
    id: number;
    title: string;
    author: string;
    category: string;
    description: string;
    publishedDate: string;
    pages: number;
    isbn: string;
    averageRating: number;
    ratingsCount: number;
    language: string;
    publisher: string;
  }
  
  interface Review {
    id: number;
    userId: number;
    userName: string;
    userAvatar?: string;
    rating: number;
    review: string;
    reviewDate: string;
    likes: number;
  }

export default function page(){
    const [isInMyBooks, setIsInMyBooks] = useState(false);

    const bookId = 1;
    const onNavigate = (e:string)=>{}
    const onAddToMyBooks = (e:number)=>{}
  // 전체 책 데이터 (BooksPage와 동일)
  const allBooks: Book[] = [
    {
      id: 1,
      title: "클린 코드",
      author: "로버트 C. 마틴",
      category: "프로그래밍",
      description: "애자일 소프트웨어 장인 정신. 읽기 좋은 코드를 작성하는 방법을 설명하는 프로그래밍 필독서입니다. 이 책은 단순히 작동하는 코드가 아닌, 읽기 쉽고 유지보수가 가능한 코드를 작성하는 방법에 대해 다룹니다. 저자는 수십 년간의 경험을 바탕으로 실용적인 조언과 원칙들을 제시합니다.",
      publishedDate: "2008-08-01",
      pages: 464,
      isbn: "9780132350884",
      averageRating: 4.4,
      ratingsCount: 1250,
      language: "한국어",
      publisher: "인사이트"
    },
    {
      id: 2,
      title: "리팩터링",
      author: "마틴 파울러",
      category: "프로그래밍",
      description: "기존 코드를 개선하는 체계적인 방법론을 제시하는 소프트웨어 개발의 고전입니다. 코드의 외부 행동은 그대로 유지하면서 내부 구조를 개선하는 방법에 대해 자세히 설명합니다. 실무에서 바로 적용할 수 있는 구체적인 리팩터링 기법들을 다양한 예제와 함께 소개합니다.",
      publishedDate: "2019-11-25",
      pages: 550,
      isbn: "9791162242742",
      averageRating: 4.6,
      ratingsCount: 890,
      language: "한국어",
      publisher: "한빛미디어"
    },
    {
      id: 3,
      title: "디자인 패턴",
      author: "GoF",
      category: "프로그래밍",
      description: "객체지향 소프트웨어 설계의 핵심 패턴들을 정리한 소프트웨어 공학의 바이블입니다. 23가지 디자인 패턴을 생성, 구조, 행위 패턴으로 분류하여 체계적으로 설명합니다. 각 패턴의 의도, 구조, 결과를 명확히 제시하여 실무에서 올바른 패턴 선택을 도와줍니다.",
      publishedDate: "1994-10-21",
      pages: 395,
      isbn: "9780201633610",
      averageRating: 4.2,
      ratingsCount: 2100,
      language: "한국어",
      publisher: "피어슨"
    },
    {
      id: 4,
      title: "사피엔스",
      author: "유발 하라리",
      category: "역사",
      description: "호모 사피엔스의 역사를 통해 인류 문명의 발전 과정을 흥미롭게 서술한 베스트셀러입니다. 인지혁명, 농업혁명, 과학혁명이라는 세 가지 주요 혁명을 통해 인류가 어떻게 지구의 지배자가 되었는지를 설명합니다. 역사, 생물학, 철학을 아우르는 통합적 시각을 제공합니다.",
      publishedDate: "2014-09-04",
      pages: 512,
      isbn: "9788934972464",
      averageRating: 4.8,
      ratingsCount: 3200,
      language: "한국어",
      publisher: "김영사"
    },
    {
      id: 5,
      title: "코스모스",
      author: "칼 세이건",
      category: "과학",
      description: "우주에 대한 경이로움과 과학적 사고의 중요성을 일깨워주는 과학 교양서의 고전입니다. 우주의 탄생부터 생명의 진화, 그리고 인류의 미래까지를 아름다운 문체로 풀어냅니다. 복잡한 과학적 개념을 일반인도 이해할 수 있도록 쉽고 감동적으로 설명합니다.",
      publishedDate: "1980-09-28",
      pages: 396,
      isbn: "9788983711892",
      averageRating: 4.7,
      ratingsCount: 1800,
      language: "한국어",
      publisher: "사이언스북스"
    },
    {
      id: 6,
      title: "1984",
      author: "조지 오웰",
      category: "소설",
      description: "전체주의 사회의 공포를 그린 디스토피아 소설의 걸작으로, 현대에도 큰 울림을 주는 작품입니다. 빅 브라더의 감시 사회에서 살아가는 윈스턴의 이야기를 통해 자유와 진실의 소중함을 일깨워줍니다. 정치적 프로파간다와 언어 조작의 위험성을 경고하는 예언적 작품입니다.",
      publishedDate: "1949-06-08",
      pages: 328,
      isbn: "9788937460777",
      averageRating: 4.5,
      ratingsCount: 5600,
      language: "한국어",
      publisher: "민음사"
    },
    {
      id: 7,
      title: "데일 카네기 인간관계론",
      author: "데일 카네기",
      category: "자기계발",
      description: "인간관계의 기본 원칙들을 제시하며 전 세계적으로 사랑받고 있는 자기계발서의 고전입니다. 사람을 대하는 기본적인 방법부터 상대방을 설득하는 기술까지, 실생활에서 바로 적용할 수 있는 구체적인 방법들을 제시합니다. 수많은 실제 사례를 통해 효과적인 커뮤니케이션의 핵심을 전달합니다.",
      publishedDate: "1936-10-12",
      pages: 352,
      isbn: "9788936804589",
      averageRating: 4.3,
      ratingsCount: 2800,
      language: "한국어",
      publisher: "창해"
    },
    {
      id: 8,
      title: "해리 포터와 마법사의 돌",
      author: "J.K. 롤링",
      category: "소설",
      description: "전 세계를 매혹시킨 판타지 소설의 시작. 마법사 해리 포터의 모험이 펼쳐집니다. 평범한 소년이 자신의 정체성을 발견하고 성장해가는 과정을 그린 현대 판타지의 걸작입니다. 마법 세계의 풍부한 설정과 매력적인 캐릭터들이 독자들을 사로잡습니다.",
      publishedDate: "1997-06-26",
      pages: 309,
      isbn: "9788983920775",
      averageRating: 4.9,
      ratingsCount: 8900,
      language: "한국어",
      publisher: "문학수첩"
    },
    {
      id: 9,
      title: "호모 데우스",
      author: "유발 하라리",
      category: "역사",
      description: "인류의 미래에 대한 도발적인 질문들을 던지며 기술 발전이 가져올 변화를 예측합니다. 인공지능, 생명공학, 알고리즘이 지배하는 미래 사회에서 인간의 위치는 어떻게 될 것인가? 저자는 역사적 관점에서 인류의 미래를 통찰력 있게 분석합니다.",
      publishedDate: "2015-02-10",
      pages: 543,
      isbn: "9788934985594",
      averageRating: 4.4,
      ratingsCount: 2100,
      language: "한국어",
      publisher: "김영사"
    },
    {
      id: 10,
      title: "아토믹 해빗",
      author: "제임스 클리어",
      category: "자기계발",
      description: "작은 변화가 만들어내는 큰 성과에 대한 실용적인 가이드를 제시하는 습관 형성 가이드북입니다. 1% 개선의 힘을 통해 어떻게 놀라운 결과를 만들어낼 수 있는지를 과학적 근거와 실제 사례를 바탕으로 설명합니다. 실행하기 쉬운 구체적인 전략들을 제공합니다.",
      publishedDate: "2018-10-16",
      pages: 392,
      isbn: "9791164050208",
      averageRating: 4.6,
      ratingsCount: 4200,
      language: "한국어",
      publisher: "비즈니스북스"
    }
  ];

  // 다양한 책에 대한 사용자 리뷰 데이터
  const getAllReviews = (): Review[] => {
    const reviews: Review[] = [
      // 클린 코드 리뷰
      {
        id: 1,
        userId: 1,
        userName: "김개발자",
        userAvatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=40&h=40&fit=crop&crop=face",
        rating: 5,
        review: "프로그래머라면 반드시 읽어야 할 책입니다. 코드 품질에 대한 인식이 완전히 바뀌었어요. 특히 함수와 변수명을 짓는 방법에 대한 부분이 가장 도움이 되었습니다. 실무에서 바로 적용할 수 있는 실용적인 내용들이 많아서 정말 좋았습니다.",
        reviewDate: "2024-02-15",
        likes: 23
      },
      {
        id: 2,
        userId: 2,
        userName: "박소프트웨어",
        userAvatar: "https://images.unsplash.com/photo-1494790108755-2616b612b5bc?w=40&h=40&fit=crop&crop=face",
        rating: 4,
        review: "코드 리뷰를 할 때 많은 도움이 되는 책입니다. 다만 일부 내용은 현재 개발 환경과 맞지 않는 부분도 있어요. 그래도 기본 원칙들은 여전히 유효하고 매우 유용합니다.",
        reviewDate: "2024-02-10",
        likes: 15
      },
      // 사피엔스 리뷰
      {
        id: 3,
        userId: 3,
        userName: "이역사학",
        userAvatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=40&h=40&fit=crop&crop=face",
        rating: 5,
        review: "인류 역사를 완전히 새로운 관점에서 바라볼 수 있게 해준 책입니다. 농업혁명이 인류에게 축복이 아닐 수도 있다는 관점은 정말 충격적이었어요. 쉬운 문체로 복잡한 역사를 설명하는 저자의 능력이 탁월합니다.",
        reviewDate: "2024-02-12",
        likes: 45
      },
      {
        id: 4,
        userId: 4,
        userName: "최독서가",
        userAvatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=40&h=40&fit=crop&crop=face",
        rating: 4.5,
        review: "방대한 스케일의 역사서임에도 불구하고 지루하지 않게 읽을 수 있었습니다. 특히 화폐와 종교가 인류 협력을 가능하게 했다는 분석이 인상적이었어요.",
        reviewDate: "2024-02-08",
        likes: 32
      },
      // 1984 리뷰
      {
        id: 5,
        userId: 5,
        userName: "정문학",
        userAvatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=40&h=40&fit=crop&crop=face",
        rating: 5,
        review: "현재 우리 사회와 너무나 유사한 부분들이 많아서 소름이 돋았습니다. 70년 전에 쓰인 소설이지만 예언서처럼 느껴져요. 빅 브라더의 감시 사회는 이제 현실이 되었습니다.",
        reviewDate: "2024-02-14",
        likes: 67
      },
      {
        id: 6,
        userId: 6,
        userName: "윤사회학",
        userAvatar: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=40&h=40&fit=crop&crop=face",
        rating: 4.5,
        review: "언어의 힘과 위험성에 대해 깊이 생각해볼 수 있었습니다. 신어(Newspeak)의 개념은 정말 무서우면서도 현실적으로 느껴졌어요. 모든 시민이 읽어야 할 필독서입니다.",
        reviewDate: "2024-02-11",
        likes: 28
      },
      // 아토믹 해빗 리뷰
      {
        id: 7,
        userId: 7,
        userName: "강자기계발",
        userAvatar: "https://images.unsplash.com/photo-1559548331-f9cb98001426?w=40&h=40&fit=crop&crop=face",
        rating: 4.5,
        review: "실제로 적용 가능한 구체적인 방법들이 많아서 좋았습니다. 1% 개선의 복리 효과에 대한 설명이 특히 인상깊었어요. 읽고 나서 실제로 몇 가지 습관을 만들어가고 있습니다.",
        reviewDate: "2024-02-09",
        likes: 39
      },
      {
        id: 8,
        userId: 8,
        userName: "서습관왕",
        userAvatar: "https://images.unsplash.com/photo-1489424731084-a5d8b219a5bb?w=40&h=40&fit=crop&crop=face",
        rating: 4,
        review: "습관 형성에 대한 과학적 접근이 매우 흥미로웠습니다. 환경 디자인의 중요성을 새롭게 깨달았어요. 다만 일부 내용은 이미 알고 있던 것들도 있었습니다.",
        reviewDate: "2024-02-06",
        likes: 22
      },
      // 해리포터 리뷰
      {
        id: 9,
        userId: 9,
        userName: "홍판타지",
        userAvatar: "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=40&h=40&fit=crop&crop=face",
        rating: 5,
        review: "어른이 되어서 다시 읽어도 여전히 재미있는 마법 같은 책입니다. 호그와트에서의 첫 해가 이렇게 생생하게 그려질 줄 몰랐어요. 아이와 함께 읽으면서 동심을 되찾을 수 있었습니다.",
        reviewDate: "2024-02-13",
        likes: 56
      },
      {
        id: 10,
        userId: 10,
        userName: "임동화",
        userAvatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=40&h=40&fit=crop&crop=face",
        rating: 4.5,
        review: "판타지 소설의 새로운 기준을 제시한 작품이라고 생각합니다. 마법 세계의 설정이 정말 세밀하고, 캐릭터들도 모두 매력적이에요. 시리즈 전체를 읽게 만드는 중독성이 있습니다.",
        reviewDate: "2024-02-07",
        likes: 43
      }
    ];

    return reviews;
  };

  const book = allBooks.find(b => b.id === bookId);
  const allReviews = getAllReviews();
  const bookReviews = allReviews.filter(review => {
    // 각 책별로 해당하는 리뷰들을 매핑
    switch (bookId) {
      case 1: return [1, 2].includes(review.id); // 클린 코드
    //   case 4: return [3, 4].includes(review.id); // 사피엔스
    //   case 6: return [5, 6].includes(review.id); // 1984
    //   case 10: return [7, 8].includes(review.id); // 아토믹 해빗
    //   case 8: return [9, 10].includes(review.id); // 해리포터
      default: 
        // 기본적으로 해당 책에 대한 샘플 리뷰를 생성
        return [{
          id: 99,
          userId: 99,
          userName: "독서애호가",
          userAvatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=40&h=40&fit=crop&crop=face",
          rating: 4,
          review: "좋은 책이었습니다. 많은 것을 배울 수 있었어요.",
          reviewDate: "2024-02-01",
          likes: 5
        }];
    }
  });

  if (!book) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p>책을 찾을 수 없습니다.</p>
          <Button onClick={() => onNavigate('books')} className="mt-4">
            책 목록으로 돌아가기
          </Button>
        </div>
      </div>
    );
  }

  const renderStars = (rating: number) => {
    return [...Array(5)].map((_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${
          i < Math.floor(rating) 
            ? 'fill-yellow-400 text-yellow-400' 
            : 'text-gray-300'
        }`}
      />
    ));
  };

  const handleAddToMyBooks = () => {
    setIsInMyBooks(true);
    onAddToMyBooks(book.id);
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button 
        variant="ghost" 
        onClick={() => onNavigate('books')}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        책 목록으로 돌아가기
      </Button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* 책 기본 정보 */}
        <div className="lg:col-span-1">
          <Card>
            <CardContent className="p-6">
              <div className="text-center mb-6">
                <ImageWithFallback
                  src={`https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=200&h=300&fit=crop&crop=center&sig=${book.id}`}
                  alt={book.title}
                  className="w-48 h-72 object-cover rounded mx-auto mb-4"
                />
                <h1 className="text-2xl mb-2">{book.title}</h1>
                <p className="text-lg text-muted-foreground mb-4">{book.author}</p>
                
                <div className="flex items-center justify-center space-x-1 mb-2">
                  {renderStars(book.averageRating)}
                  <span className="text-lg ml-2">{book.averageRating}</span>
                </div>
                <p className="text-sm text-muted-foreground mb-6">
                  {book.ratingsCount}명이 평가
                </p>

                <Badge className="mb-4">{book.category}</Badge>
              </div>

              <Separator className="mb-6" />

              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">출간일</span>
                  <span className="text-sm flex items-center">
                    <Calendar className="h-4 w-4 mr-1" />
                    {book.publishedDate}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">페이지</span>
                  <span className="text-sm flex items-center">
                    <BookOpen className="h-4 w-4 mr-1" />
                    {book.pages}쪽
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">언어</span>
                  <span className="text-sm flex items-center">
                    <Globe className="h-4 w-4 mr-1" />
                    {book.language}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">출판사</span>
                  <span className="text-sm flex items-center">
                    <Building className="h-4 w-4 mr-1" />
                    {book.publisher}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">ISBN</span>
                  <span className="text-sm">{book.isbn}</span>
                </div>
              </div>

              <Separator className="my-6" />

              <div className="space-y-3">
                {isInMyBooks ? (
                  <Button className="w-full" disabled>
                    내 목록에 추가됨
                  </Button>
                ) : (
                  <Button className="w-full" onClick={handleAddToMyBooks}>
                    <Plus className="h-4 w-4 mr-2" />
                    내 목록에 추가
                  </Button>
                )}
                <Button variant="outline" className="w-full">
                  <Heart className="h-4 w-4 mr-2" />
                  관심 목록
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 책 상세 정보 및 리뷰 */}
        <div className="lg:col-span-2">
          <Tabs defaultValue="description" className="w-full">
            <TabsList className="grid w-full grid-cols-2">
              <TabsTrigger value="description">책 소개</TabsTrigger>
              <TabsTrigger value="reviews">리뷰 ({bookReviews.length})</TabsTrigger>
            </TabsList>
            
            <TabsContent value="description" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>책 소개</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground leading-relaxed">
                    {book.description}
                  </p>
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="reviews" className="mt-6">
              <div className="space-y-6">
                {bookReviews.map((review) => (
                  <Card key={review.id}>
                    <CardContent className="p-6">
                      <div className="flex items-start space-x-4">
                        <Avatar>
                          <AvatarImage src={review.userAvatar} />
                          <AvatarFallback>{review.userName.charAt(0)}</AvatarFallback>
                        </Avatar>
                        <div className="flex-1">
                          <div className="flex items-center space-x-2 mb-2">
                            <span className="font-medium">{review.userName}</span>
                            <div className="flex items-center space-x-1">
                              {renderStars(review.rating)}
                            </div>
                            <span className="text-sm text-muted-foreground">
                              {review.reviewDate}
                            </span>
                          </div>
                          <p className="text-muted-foreground mb-3 leading-relaxed">
                            {review.review}
                          </p>
                          <div className="flex items-center space-x-4">
                            <Button variant="ghost" size="sm">
                              <Heart className="h-4 w-4 mr-1" />
                              도움됨 {review.likes}
                            </Button>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
                
                {bookReviews.length === 0 && (
                  <Card>
                    <CardContent className="p-12 text-center">
                      <h3 className="text-lg mb-2">아직 리뷰가 없습니다</h3>
                      <p className="text-muted-foreground">
                        이 책을 읽으신 분이라면 첫 번째 리뷰를 작성해보세요!
                      </p>
                    </CardContent>
                  </Card>
                )}
              </div>
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
}