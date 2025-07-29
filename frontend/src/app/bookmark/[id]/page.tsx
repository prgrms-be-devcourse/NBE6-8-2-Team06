"use client"

import React, { useState, useEffect, useCallback } from "react";
import { useParams, useRouter } from "next/navigation";
import { getBookmark } from "../../../types/bookmarkAPI";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Separator } from '@/components/ui/separator';
import { ArrowLeft, BookOpen, Calendar, Edit, FileText, PenTool, Star } from "lucide-react";
import { BookmarkDetail } from "../../../types/bookmarkData";
import { useAuth } from "../../_hooks/auth-context";


export default function Page() {
  const router = useRouter();
  const params = useParams();
  const { isLoggedIn, isLoading: isAuthLoading } = useAuth();
  const bookmarkId = parseInt(params.id as string);
  const [bookmark, setBookmark] = useState<BookmarkDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');

  const onNavigate = (path: string) => {
    router.push(path);
  };

  useEffect(() => {
    if (!isAuthLoading && !isLoggedIn) {
      onNavigate('/login');
    }
  }, [isAuthLoading, isLoggedIn, router]);

  const fetchBookmark = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const response = await getBookmark(bookmarkId);
      setBookmark(response.data);
    } catch (error) {
      setError(error instanceof Error ? error.message : '내 책 정보를 불러올 수 없습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [bookmarkId]);

  useEffect(() => {
    if (!isAuthLoading && isLoggedIn) {
      if (!bookmarkId) return;

      fetchBookmark();
    }
  }, [isAuthLoading, isLoggedIn, bookmarkId, fetchBookmark]);

  const getReadState = (readState: string) => {
    switch (readState) {
      case 'READ':
        return '읽은 책';
      case 'READING':
        return '읽고 있는 책';
      case 'WISH':
        return '읽고 싶은 책';
      default:
        return '모든 상태';
    }
  };

  const getReadStateColor = (readState: string) => {
    switch (readState) {
      case 'READ':
        return 'bg-green-100 text-green-800';
      case 'READING':
        return 'bg-blue-100 text-blue-800';
      case 'WISH':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const renderStars = (rating?: number) => {
    if (!rating) return null;
    return [...Array(5)].map((_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${i < Math.floor(rating)
            ? 'fill-yellow-400 text-yellow-400'
            : 'text-gray-300'
          }`}
      />
    ));
  };

  if (isAuthLoading) {
    return <div className="flex justify-center items-center h-screen">로딩 중...</div>;
  };

  if (isLoading) {
    return <div className="text-center py-20">데이터를 불러오는 중입니다...</div>;
  };

  if (error) {
    return <div className="text-center py-20 text-red-500">{error}</div>;
  };

  if (!bookmark) {
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
                  src={bookmark.book.imageUrl}
                  alt={bookmark.book.title}
                  className="w-48 h-72 object-cover rounded mx-auto mb-4"
                />
                <h1 className="text-2xl mb-2">{bookmark.book.title}</h1>
                <p className="text-lg text-muted-foreground mb-4">{bookmark.book?.authors?.join(', ') || '저자 정보 없음'}</p>

                <Badge className={`mb-4 ${getReadStateColor(bookmark?.readState)}`}>
                  {getReadState(bookmark?.readState)}
                </Badge>
                
                {bookmark.book.avgRate > 0 && (
                  <div className="flex items-center justify-center space-x-1 mb-4">
                    {renderStars(bookmark?.book.avgRate)}
                    <span className="text-lg ml-2">{bookmark.book.avgRate.toFixed(1)}</span>
                  </div>
                )}
              </div>

              <Separator className="mb-6" />

              {/* 읽기 진도 */}
              {bookmark?.readState === 'READING' && bookmark.readPage && (
                <div className="mb-6">
                  <div className="flex justify-between text-sm mb-2">
                    <span>읽기 진도</span>
                    <span>{bookmark?.readingRate}%</span>
                  </div>
                  <Progress value={bookmark.readingRate} className="mb-2" />
                  <div className="flex justify-between text-sm text-muted-foreground">
                    <span>{bookmark.readPage}쪽</span>
                    <span>{bookmark.book.totalPage}쪽</span>
                  </div>
                </div>
              )}

              {/* 독서 정보 */}
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">카테고리</span>
                  <span className="text-sm">{bookmark.book.category}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">추가일</span>
                  <span className="text-sm flex items-center">
                    <Calendar className="h-4 w-4 mr-1" />
                    {bookmark.createDate?.substring(0,10)}
                  </span>
                </div>
                {bookmark?.startReadDate && (
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">시작일</span>
                    <span className="text-sm">{bookmark.startReadDate?.substring(0,10)}</span>
                  </div>
                )}
                {bookmark?.endReadDate && (
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">완독일</span>
                    <span className="text-sm">{bookmark.endReadDate?.substring(0,10)}</span>
                  </div>
                )}
                {bookmark?.readingDuration >0 && (
                  <div className="flex justify-between">
                    <span className="text-sm text-muted-foreground">
                      {bookmark?.readState === 'READ' ? '독서 기간' : '독서 중'}
                    </span>
                    <span className="text-sm">{bookmark.readingDuration}일</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-sm text-muted-foreground">페이지</span>
                  <span className="text-sm flex items-center">
                    <BookOpen className="h-4 w-4 mr-1" />
                    {bookmark.book.totalPage}쪽
                  </span>
                </div>
              </div>

              <Separator className="my-6" />

              <div className="space-y-3">
                {bookmark?.readState === 'READ' && !bookmark.review && (
                  <Button
                    className="w-full"
                    onClick={() => onNavigate(`/bookmark/${bookmarkId}/review`)}
                  >
                    <PenTool className="h-4 w-4 mr-2" />
                    리뷰 작성하기
                  </Button>
                )}
                {bookmark?.readState === 'READ' && bookmark.review && (
                  <Button
                    variant="outline"
                    className="w-full"
                    onClick={() => onNavigate(`/bookmark/${bookmarkId}/review`)}
                  >
                    <Edit className="h-4 w-4 mr-2" />
                    리뷰 수정하기
                  </Button>
                )}
                <Button
                  variant="outline"
                  className="w-full"
                  onClick={() => onNavigate(`/bookmark/${bookmarkId}/notes`)}
                >
                  <FileText className="h-4 w-4 mr-2" />
                  노트 관리 ({bookmark.notes.length})
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
              <TabsTrigger value="notes">노트 ({bookmark.notes.length})</TabsTrigger>
            </TabsList>

            <TabsContent value="info" className="mt-6">
              <Card>
                <CardHeader>
                  <CardTitle>책 정보</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  {bookmark.book.description && (
                  <div>
                    <h4 className="font-medium mb-2">책 소개</h4>
                    <p className="text-muted-foreground leading-relaxed">
                      {bookmark.book.description}
                    </p>
                  </div>
                  )}
                  <Separator />

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-sm text-muted-foreground">출판사</span>
                      <p className="font-medium">{bookmark.book.publisher}</p>
                    </div>
                    <div>
                      <span className="text-sm text-muted-foreground">출간일</span>
                      <p className="font-medium">{bookmark?.book.publishDate?.substring(0,10)}</p>
                    </div>
                    <div>
                      <span className="text-sm text-muted-foreground">ISBN</span>
                      <p className="font-medium">{bookmark.book.isbn13}</p>
                    </div>
                    <div>
                      <span className="text-sm text-muted-foreground">페이지</span>
                      <p className="font-medium">{bookmark.book.totalPage}쪽</p>
                    </div>
                  </div>

                  {/* 개인 메모는 작성을 어디서 하는가 
                  {bookmark.notes && (
                    <>
                      <Separator />
                      <div>
                        <h4 className="font-medium mb-2">개인 메모</h4>
                        <p className="text-muted-foreground leading-relaxed">
                          {bookmark.notes}
                        </p>
                      </div>
                    </>
                  )}*/}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="review" className="mt-6">
              <Card>
                <CardHeader>
                  <div className="flex justify-between items-center">
                    <CardTitle>내 리뷰</CardTitle>
                    {bookmark?.readState === 'READ' && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => onNavigate(`/bookmark/${bookmarkId}/review`)}
                      >
                        <Edit className="h-4 w-4 mr-2" />
                        {bookmark.review ? '수정' : '작성'}
                      </Button>
                    )}
                  </div>
                </CardHeader>
                <CardContent>
                  {bookmark.review ? (
                    <div>
                      {bookmark.review.rate && (
                        <div className="flex items-center space-x-1 mb-3">
                          {renderStars(bookmark.review.rate)}
                          <span className="ml-2">{bookmark.review.rate}</span>
                        </div>
                      )}
                      <p className="text-muted-foreground leading-relaxed">
                        {bookmark.review.content}
                      </p>
                    </div>
                  ) : (
                    <div className="text-center py-8">
                      <PenTool className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                      <p className="text-muted-foreground mb-4">
                        {bookmark?.readState === 'READ'
                          ? '아직 리뷰를 작성하지 않았습니다.'
                          : '책을 다 읽은 후 리뷰를 작성할 수 있습니다.'
                        }
                      </p>
                      {bookmark?.readState === 'READ' && (
                        <Button onClick={() => onNavigate(`/bookmark/${bookmarkId}/review`)}>
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
                    onClick={() => onNavigate(`/bookmark/${bookmarkId}/notes`)}
                  >
                    <FileText className="h-4 w-4 mr-2" />
                    노트 관리
                  </Button>
                </div>

                {bookmark.notes.length > 0 ? (
                  <div className="space-y-4">
                    {bookmark.notes.slice(0, 3).map((note) => (
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
                            {note.modifiedDate ? note.modifiedDate : note.createDate}
                          </p>
                        </CardContent>
                      </Card>
                    ))}
                    {bookmark.notes.length > 3 && (
                      <Button
                        variant="outline"
                        className="w-full"
                        onClick={() => onNavigate(`/bookmark/${bookmarkId}/notes`)}
                      >
                        모든 노트 보기 ({bookmark.notes.length}개)
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
                      <Button onClick={() => onNavigate(`/bookmark/${bookmarkId}/notes`)}>
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
}