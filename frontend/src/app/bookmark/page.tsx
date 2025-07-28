'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { getBookmarks, createBookmark, updateBookmark, deleteBookmark } from './lib/bookmarkAPI';
import { BookmarkPage, Bookmark } from './lib/bookmarkData';
import { BookOpen, Plus, Search } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ImageWithFallback } from '@/components/ImageWithFallback';
import { Badge } from '@/components/ui/badge';


export default function Page() {
  const router = useRouter();
  const [bookmarks, setBookmarks] = useState<BookmarkPage>();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedReadState, setSelectedReadState] = useState('all');

  const [categories, setCategories] = useState<string[]>([]);
  const [readStates, setReadStates] = useState<string[]>(['READ', 'READING', 'WISH']);

  /*
  const fetchBookmarks = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const response = await getBookmarks({
        page: currentPage,
        size: 10,
        category: selectedCategory,
        readState: selectedReadState,
        keyword: searchKeyword,
      });
      setBookmarks(response.data);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (error) {
      setError(error instanceof Error ? error.message : '북마크 목록을 가져올 수 없습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, selectedCategory, selectedReadState, searchKeyword]);

  useEffect(() => {
    fetchBookmarks();
  }, [fetchBookmarks]);
*/
  // Page.tsx 파일 상단 (import 다음, Page 컴포넌트 이전)

  // 임시 북마크 데이터
  const mockBookmarkPage = {
    content: [
      {
        id: 1,
        memberId: 1,
        bookId: 1,
        readState: 'READ',
        readPage: 464,
        readingRate: 100,
        date: '2024-07-20',
        book: {
          id: 1,
          isbn13: "9788960777330",
          publisher: "프로그래밍 인사이트",
          avgRate: 4.5,
          publishDate: "2013-01-01",
          title: "클린 코드(Clean Code)",
          author: ["로버트 C. 마틴"],
          imageUrl: "https://image.aladin.co.kr/product/19/24/cover/8966262130_1.jpg",
          category: "프로그래밍",
          totalPage: 464,
        }
      },
      {
        id: 2,
        memberId: 1,
        bookId: 2,
        readState: 'READING',
        readPage: 150,
        readingRate: 27,
        date: '2025-07-10',
        book: {
          id: 2,
          isbn13: "9788960777330",
          publisher: "프로그래밍 인사이트",
          avgRate: 4.5,
          publishDate: "2013-01-01",
          title: "리팩터링 2판",
          author: ["마틴 파울러"],
          imageUrl: "https://image.aladin.co.kr/product/216/23/cover/8966262378_1.jpg",
          category: "프로그래밍",
          totalPage: 550,
        }
      },
      {
        id: 3,
        memberId: 1,
        bookId: 1,
        readState: 'WISH',
        readPage: 0,
        readingRate: 0,
        date: '2025-06-30',
        book: {
          id: 3,
          isbn13: "9788960777330",
          publisher: "프로그래밍 인사이트",
          avgRate: 4.5,
          publishDate: "2013-01-01",
          title: "Sapiens: A Brief History of Humankind",
          author: ["유발 하라리"],
          imageUrl: "https://image.aladin.co.kr/product/74/26/cover/8934910911_2.jpg",
          category: "역사",
          totalPage: 512,
        }
      },
      {
        id: 4,
        memberId: 1,
        bookId: 4,
        readState: 'READ',
        readPage: 396,
        readingRate: 100,
        date: '2025-05-15',
        book: {
          id: 4,
          isbn13: "9788960777330",
          publisher: "프로그래밍 인사이트",
          avgRate: 4.5,
          publishDate: "2013-01-01",
          title: "코스모스",
          author: ["칼 세이건"],
          imageUrl: "https://image.aladin.co.kr/product/21/11/cover/8983719213_2.jpg",
          category: "과학",
          totalPage: 396,
        }
      },
    ],
    totalPages: 1,
    totalElements: 4,
    pageNumber: 0,
    pageSize: 10,
    isLast: true,
  };
  useEffect(() => {
    // <<<< API 연동 대신 임시 데이터를 사용하도록 수정 >>>>
    console.log("임시 데이터로 렌더링합니다.");
    setIsLoading(true);

    // 실제 로딩처럼 보이게 0.5초 딜레이를 줍니다.
    setTimeout(() => {
      setBookmarks(mockBookmarkPage);
      setTotalPages(mockBookmarkPage.totalPages);
      setTotalElements(mockBookmarkPage.totalElements);
      setIsLoading(false);
    }, 500);

    /*
    // <<<< 실제 API 연동 시 이 부분을 다시 활성화하세요 >>>>
    // fetchBookmarks();
    */
  }, []); // [] 로 변경하여 컴포넌트가 처음 마운트될 때 한 번만 실행되도록 합니다.
  const onNavigate = (path: string) => {
    router.push(path);
  }

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

  const getReadStateCount = (readState: string) => {
    if (readState === '모든 상태') {
      return bookmarks?.totalElements || 0;
    }
    return bookmarks?.content.filter(bookmark => bookmark.readState === readState).length || 0;
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

  const filteredBookmarks = useMemo(() => {
    if (!bookmarks?.content) return [];
    if (selectedReadState === 'all') return bookmarks.content;
    return bookmarks.content.filter(bookmark => bookmark.readState === selectedReadState);
  }, [bookmarks, selectedReadState]);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl mb-2">내 책 목록</h1>
          <p className="text-muted-foreground">
            {totalElements}권의 책을 등록했습니다.
          </p>
        </div>

        <Button onClick={() => onNavigate('books')}>
          <Plus className="mr-2 h-4 w-4" />
          새 책 추가하기
        </Button>
      </div>

      {/* 내 책 목록 통계 */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl">{getReadStateCount('모든 상태')}</div>
            <p className="text-sm text-muted-foreground">총 책 수</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-green-600">{getReadStateCount('READ')}</div>
            <p className="text-sm text-muted-foreground">읽은 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-blue-600">{getReadStateCount('READING')}</div>
            <p className="text-sm text-muted-foreground">읽고 있는 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-gray-600">{getReadStateCount('WISH')}</div>
            <p className="text-sm text-muted-foreground">읽고 싶은 책</p>
          </CardContent>
        </Card>
      </div>
      {/* 책 목록 검색 필터 */}
      <div className="mb-8 space-y-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
          <Input
            placeholder="책 제목 또는 저자 검색..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            className="pl-10"
          />
          <Select value={selectedCategory} onValueChange={setSelectedCategory}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="카테고리 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 카테고리</SelectItem>
              {categories.map((category) => (
                <SelectItem key={category} value={category}>{category}</SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedReadState} onValueChange={setSelectedReadState}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="읽기 상태 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 상태</SelectItem>
              {readStates.map((readState) => (
                <SelectItem key={readState} value={readState}>{getReadState(readState)}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      {/* 책 목록 테이블 */}
      <Tabs defaultValue={selectedReadState} onValueChange={setSelectedReadState} className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="all">모든 상태 ({bookmarks?.totalElements})</TabsTrigger>
          <TabsTrigger value="READ">읽은 책 ({getReadStateCount('READ')})</TabsTrigger>
          <TabsTrigger value="READING">읽고 있는 책 ({getReadStateCount('READING')})</TabsTrigger>
          <TabsTrigger value="WISH">읽고 싶은 책 ({getReadStateCount('WISH')})</TabsTrigger>
        </TabsList>
        <div className="mt-6">
          {isLoading ? (
            <p className="text-center py-12">내 책 목록을 불러오는 중입니다...</p>
          ) : error ? (
            <p className="text-center py-12 text-red-500">{error}</p>
          ) : filteredBookmarks.length === 0 ? (
            <div className="text-center py-12">
              <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground mb-4">표시할 책이 없습니다.</p>
              <Button onClick={() => onNavigate('books')}>새 책 추가하기</Button>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
              {filteredBookmarks.map((bookmark) => (
                <Card key={bookmark.id} className="h-full cursor-pointer hover:shadow-lg transition-shadow overflow-hidden" onClick={() => onNavigate(`/bookmark/${bookmark.id}`)}>
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="line-clamp-2">{bookmark.book.title}</CardTitle>
                        <CardDescription>{bookmark.book.author.join(', ')}</CardDescription>
                        <Badge className={`mt-2 ${getReadStateColor(bookmark.readState)}`}>
                          {getReadState(bookmark.readState)}
                        </Badge>
                      </div>
                      <ImageWithFallback
                        src={bookmark.book.imageUrl}
                        alt={bookmark.book.title}
                        className="w-16 h-24 object-cover rounded ml-4"
                      />
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-4">
                      <div className="flex justify-between text-sm text-muted-foreground">
                        <span>카테고리: {bookmark.book.category}</span>
                        <span>{bookmark.book.totalPage} 쪽</span>
                      </div>
                      {bookmark.readState === 'READING' && bookmark.readPage && (
                        <div className="space-y-1">
                          <div className="flex justify-between text-sm">
                            <span>{bookmark.readPage}쪽 / {bookmark.book.totalPage}쪽</span>
                            <span>{bookmark.readingRate}%</span>
                          </div>
                          <div className="w-full bg-gray-200 rounded-full h-2">
                            <div className="bg-blue-600 h-2 rounded-full" style={{ width: `${bookmark.readingRate}%` }}></div>
                          </div>
                        </div>
                      )}
                    </div>
                    {/* 카드 평점 */}

                    {/* 날짜 정보 */}
                    <div className="text-xs text-muted-foreground">
                      {bookmark.readState === 'READ' ? '완독' : bookmark.readState === 'READING' ? '시작' : '추가'} : {bookmark.date}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      </Tabs>
    </div>
  );
}