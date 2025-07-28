"use client"

import withLogin from "@/app/_hooks/withLogin";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from "@radix-ui/react-select";
import { ArrowLeft, BookOpen, Calendar, Edit, FileText, PenTool, Star } from "lucide-react";
import { usePathname, useRouter } from "next/navigation";
import { use } from "react";

interface MyBookDetailPageProps {
    bookId: number | null;
    onNavigate: (page: string) => void;
    onWriteReview: (bookId: number) => void;
    onManageNotes: (bookId: number) => void;
  }
  
  interface MyBook {
    id: number;
    title: string;
    author: string;
    category: string;
    description: string;
    status: '읽은 책' | '읽고 있는 책' | '읽고 싶은 책';
    rating?: number;
    review?: string;
    notes?: string;
    dateAdded: string;
    dateStarted?: string;
    dateFinished?: string;
    currentPage?: number;
    totalPages: number;
    publisher: string;
    publishedDate: string;
    isbn: string;
  }
  
  interface Note {
    id: number;
    bookId: number;
    title: string;
    content: string;
    page?: number;
    createdDate: string;
  }

export default withLogin(function page({params}:{params:Promise<{id:string}>}){
  const {id:bookIdStr} = use(params);
  const bookId = parseInt(bookIdStr);
  console.log(bookId);
  const router = useRouter()
  const onNavigate = (e:string)=>{
    router.push(e)
  }
  const pathName = usePathname()
  const onWriteReview = (e:number)=>{
    router.push(`${pathName}/review`)
  }
  const onManageNotes = (e:number) => {
    router.push(`${pathName}/notes`)
  }

    // 내 책 데이터
  const myBooks: MyBook[] = [
    {
      id: 1,
      title: "클린 코드",
      author: "로버트 C. 마틴",
      category: "프로그래밍",
      description: "애자일 소프트웨어 장인 정신. 읽기 좋은 코드를 작성하는 방법을 설명하는 프로그래밍 필독서입니다.",
      status: "읽은 책",
      rating: 4.5,
      review: "코드 품질에 대한 훌륭한 인사이트를 제공합니다. 특히 변수명과 함수명의 중요성에 대해 깊이 깨달았습니다.",
      notes: "챕터 2의 의미 있는 이름 부분이 가장 인상적이었음",
      dateAdded: "2024-01-15",
      dateStarted: "2024-01-15",
      dateFinished: "2024-02-20",
      totalPages: 464,
      publisher: "인사이트",
      publishedDate: "2008-08-01",
      isbn: "9780132350884"
    },
    {
      id: 2,
      title: "리팩터링",
      author: "마틴 파울러",
      category: "프로그래밍",
      description: "기존 코드를 개선하는 체계적인 방법론을 제시하는 소프트웨어 개발의 고전입니다.",
      status: "읽고 있는 책",
      rating: 4.0,
      notes: "현재 3장까지 읽었음. 매우 실용적인 내용들이 많음",
      dateAdded: "2024-02-01",
      dateStarted: "2024-02-10",
      currentPage: 150,
      totalPages: 550,
      publisher: "한빛미디어",
      publishedDate: "2019-11-25",
      isbn: "9791162242742"
    }
  ];

  // 책 노트 데이터
  const bookNotes: Note[] = [
    {
      id: 1,
      bookId: 1,
      title: "의미 있는 이름",
      content: "변수명, 함수명, 클래스명은 존재 이유, 수행 기능, 사용 방법이 드러나야 한다. 주석이 필요하다면 의미를 명확히 드러내지 못했다는 말이다.",
      page: 22,
      createdDate: "2024-01-20"
    },
    {
      id: 2,
      bookId: 1,
      title: "함수 작성 규칙",
      content: "함수는 작게 만들어야 한다. 함수가 하는 일은 하나여야 한다. 함수 당 추상화 수준은 하나로 제한한다.",
      page: 42,
      createdDate: "2024-01-25"
    },
    {
      id: 3,
      bookId: 1,
      title: "주석 사용법",
      content: "나쁜 코드에 주석을 달지 마라. 새로 짜라. 코드로 의도를 표현하지 못해 실패를 만회하기 위해 주석을 사용한다.",
      page: 68,
      createdDate: "2024-02-02"
    }
  ];

  const book = myBooks.find(b => b.id === bookId);
  const notes = bookNotes.filter(note => note.bookId === bookId);

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

  const getStatusColor = (status: string) => {
    switch (status) {
      case '읽은 책': return 'bg-green-100 text-green-800';
      case '읽고 있는 책': return 'bg-blue-100 text-blue-800';
      case '읽고 싶은 책': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const renderStars = (rating?: number) => {
    if (!rating) return null;
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

  const getReadingProgress = () => {
    if (book.status === '읽고 있는 책' && book.currentPage && book.totalPages) {
      return Math.round((book.currentPage / book.totalPages) * 100);
    }
    return null;
  };

  const getDaysReading = () => {
    if (book.dateStarted) {
      const startDate = new Date(book.dateStarted);
      const endDate = book.dateFinished ? new Date(book.dateFinished) : new Date();
      const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays;
    }
    return null;
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 뒤로가기 버튼 */}
      <Button 
        variant="ghost" 
        onClick={() => onNavigate('/bookmark')}
        className="mb-6"
      >
        <ArrowLeft className="h-4 w-4 mr-2" />
        내 책 목록으로 돌아가기
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
                
                <Badge className={`mb-4 ${getStatusColor(book.status)}`}>
                  {book.status}
                </Badge>

                {book.rating && (
                  <div className="flex items-center justify-center space-x-1 mb-4">
                    {renderStars(book.rating)}
                    <span className="text-lg ml-2">{book.rating}</span>
                  </div>
                )}
              </div>

              <Separator className="mb-6" />

              {/* 읽기 진도 */}
              {book.status === '읽고 있는 책' && book.currentPage && (
                <div className="mb-6">
                  <div className="flex justify-between text-sm mb-2">
                    <span>읽기 진도</span>
                    <span>{getReadingProgress()}%</span>
                  </div>
                  <Progress value={getReadingProgress()} className="mb-2" />
                  <div className="flex justify-between text-sm text-muted-foreground">
                    <span>{book.currentPage}쪽</span>
                    <span>{book.totalPages}쪽</span>
                  </div>
                </div>
              )}

              {/* 독서 정보 */}
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">카테고리</span>
                  <span className="text-sm">{book.category}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">추가일</span>
                  <span className="text-sm flex items-center">
                    <Calendar className="h-4 w-4 mr-1" />
                    {book.dateAdded}
                  </span>
                </div>
                {book.dateStarted && (
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">시작일</span>
                    <span className="text-sm">{book.dateStarted}</span>
                  </div>
                )}
                {book.dateFinished && (
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">완독일</span>
                    <span className="text-sm">{book.dateFinished}</span>
                  </div>
                )}
                {getDaysReading() && (
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">
                      {book.status === '읽은 책' ? '독서 기간' : '독서 중'}
                    </span>
                    <span className="text-sm">{getDaysReading()}일</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">페이지</span>
                  <span className="text-sm flex items-center">
                    <BookOpen className="h-4 w-4 mr-1" />
                    {book.totalPages}쪽
                  </span>
                </div>
              </div>

              <Separator className="my-6" />

              <div className="space-y-3">
                {book.status === '읽은 책' && !book.review && (
                  <Button 
                    className="w-full" 
                    onClick={() => onWriteReview(book.id)}
                  >
                    <PenTool className="h-4 w-4 mr-2" />
                    리뷰 작성하기
                  </Button>
                )}
                {book.status === '읽은 책' && book.review && (
                  <Button 
                    variant="outline" 
                    className="w-full" 
                    onClick={() => onWriteReview(book.id)}
                  >
                    <Edit className="h-4 w-4 mr-2" />
                    리뷰 수정하기
                  </Button>
                )}
                <Button 
                  variant="outline" 
                  className="w-full"
                  onClick={() => onManageNotes(book.id)}
                >
                  <FileText className="h-4 w-4 mr-2" />
                  노트 관리 ({notes.length})
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 상세 정보 */}
        <div className="lg:col-span-2">
          <Tabs defaultValue="info" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="info">기본 정보</TabsTrigger>
              <TabsTrigger value="review">내 리뷰</TabsTrigger>
              <TabsTrigger value="notes">노트 ({notes.length})</TabsTrigger>
            </TabsList>
            
            <TabsContent value="info" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>책 정보</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div>
                    <h4 className="font-medium mb-2">책 소개</h4>
                    <p className="text-muted-foreground leading-relaxed">
                      {book.description}
                    </p>
                  </div>
                  
                  <Separator />
                  
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-sm text-muted-foreground">출판사</span>
                      <p className="font-medium">{book.publisher}</p>
                    </div>
                    <div>
                      <span className="text-sm text-muted-foreground">출간일</span>
                      <p className="font-medium">{book.publishedDate}</p>
                    </div>
                    <div>
                      <span className="text-sm text-muted-foreground">ISBN</span>
                      <p className="font-medium">{book.isbn}</p>
                    </div>
                    <div>
                      <span className="text-sm text-muted-foreground">페이지</span>
                      <p className="font-medium">{book.totalPages}쪽</p>
                    </div>
                  </div>

                  {book.notes && (
                    <>
                      <Separator />
                      <div>
                        <h4 className="font-medium mb-2">개인 메모</h4>
                        <p className="text-muted-foreground leading-relaxed">
                          {book.notes}
                        </p>
                      </div>
                    </>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="review" className="mt-6">
              <Card>
                <CardHeader>
                  <div className="flex justify-between items-center">
                    <CardTitle>내 리뷰</CardTitle>
                    {book.status === '읽은 책' && (
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => onWriteReview(book.id)}
                      >
                        <Edit className="h-4 w-4 mr-2" />
                        {book.review ? '수정' : '작성'}
                      </Button>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  {book.review ? (
                    <div>
                      {book.rating && (
                        <div className="flex items-center space-x-1 mb-3">
                          {renderStars(book.rating)}
                          <span className="ml-2">{book.rating}</span>
                        </div>
                      )}
                      <p className="text-muted-foreground leading-relaxed">
                        {book.review}
                      </p>
                    </div>
                  ) : (
                    <div className="text-center py-8">
                      <PenTool className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground mb-4">
                        {book.status === '읽은 책' 
                          ? '아직 리뷰를 작성하지 않았습니다.' 
                          : '책을 다 읽은 후 리뷰를 작성할 수 있습니다.'
                        }
                      </p>
                      {book.status === '읽은 책' && (
                        <Button onClick={() => onWriteReview(book.id)}>
                          리뷰 작성하기
                        </Button>
                      )}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>
            
            <TabsContent value="notes" className="mt-6">
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <h3 className="text-lg font-medium">독서 노트</h3>
                  <Button 
                    variant="outline" 
                    onClick={() => onManageNotes(book.id)}
                  >
                    <FileText className="h-4 w-4 mr-2" />
                    노트 관리
                  </Button>
                </div>
                
                {notes.length > 0 ? (
                  <div className="space-y-4">
                    {notes.slice(0, 3).map((note) => (
                      <Card key={note.id}>
                        <CardContent className="p-4">
                          <div className="flex justify-between items-start mb-2">
                            <h4 className="font-medium">{note.title}</h4>
                            {note.page && (
                              <Badge variant="secondary">{note.page}쪽</Badge>
                            )}
                          </div>
                          <p className="text-sm text-muted-foreground mb-2 line-clamp-2">
                            {note.content}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            {note.createdDate}
                          </p>
                        </CardContent>
                      </Card>
                    ))}
                    {notes.length > 3 && (
                      <Button 
                        variant="outline" 
                        className="w-full"
                        onClick={() => onManageNotes(book.id)}
                      >
                        모든 노트 보기 ({notes.length}개)
                      </Button>
                    )}
                  </div>
                ) : (
                  <Card>
                    <CardContent className="p-8 text-center">
                      <FileText className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground mb-4">
                        아직 작성한 노트가 없습니다.
                      </p>
                      <Button onClick={() => onManageNotes(book.id)}>
                        첫 번째 노트 작성하기
                      </Button>
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
})