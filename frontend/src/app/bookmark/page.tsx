'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { getBookmarks, updateBookmark, deleteBookmark, getBookmarkReadStates } from '../../types/bookmarkAPI';
import { BookmarkPage, Bookmark, BookmarkReadStates, UpdateBookmark } from '../../types/bookmarkData';
import { BookOpen, Plus, Search, Trash2, Edit, Star } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription, CardFooter } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { ImageWithFallback } from '@/components/ImageWithFallback';
import { Badge } from '@/components/ui/badge';
import { Label } from '@/components/ui/label';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger } from '@/components/ui/dialog';
import { useAuth } from "../_hooks/auth-context";


export default function Page() {
  const router = useRouter();
  const { isLoggedIn, isLoading: isAuthLoading } = useAuth();
  const [isLoading, setIsLoading] = useState(true);
  const [bookmarks, setBookmarks] = useState<BookmarkPage>();
  const [error, setError] = useState<string | null>(null);

  const [bookmarkReadStates, setBookmarkReadStates] = useState<BookmarkReadStates>();
  const [currentPage, setCurrentPage] = useState(0);

  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedReadState, setSelectedReadState] = useState('all');

  const [categories, setCategories] = useState<string[]>([]);
  const [readStates, setReadStates] = useState<string[]>(['READ', 'READING', 'WISH']);

  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editBookmark, setEditBookmark] = useState<Bookmark | null>(null);

  const onNavigate = (path: string) => {
    router.push(path);
  };

  useEffect(() => {
    if (!isAuthLoading && !isLoggedIn) {
      onNavigate('/login');
    }
  }, [isAuthLoading, isLoggedIn, onNavigate]);

  const fetchBookmarks = useCallback(async (searchKeyword) => {
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
    } catch (error) {
      if (error instanceof Error && error.message.includes("데이터가 없습니다")) {
        setBookmarks({
          data: [],
          totalPages: 0,
          totalElements: 0,
          pageNumber: 0,
          pageSize: 0,
          isLast: true
        });
      } else {
        setError(error instanceof Error ? error.message : '북마크 목록을 가져올 수 없습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, selectedCategory, selectedReadState]);

  const fetchBookmarkReadStates = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const response = await getBookmarkReadStates();
      setBookmarkReadStates(response.data);
    } catch (error) {
      setError(error instanceof Error ? error.message : '북마크 읽기 상태 데이터를 가져올 수 없습니다.');
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      fetchBookmarks(searchKeyword);
    }, 500);
    return () => {
      clearTimeout(timer);
    };
  },[searchKeyword, fetchBookmarks]);
  useEffect(() => {
    if (!isAuthLoading && isLoggedIn) {
      fetchBookmarkReadStates();
    }
  }, [isAuthLoading, isLoggedIn, fetchBookmarkReadStates]);

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

  const filteredBookmarks = useMemo(() => {
    if (!bookmarks?.data) return [];
    if (selectedReadState === 'all') return bookmarks.data;
    return bookmarks.data.filter(bookmark => bookmark.readState === selectedReadState);
  }, [bookmarks, selectedReadState]);

  const handleSaveBookmark = async (updateData: UpdateBookmark) => {
    if (!editBookmark) return;
    try {
      await updateBookmark(editBookmark.id, updateData);
      setIsEditDialogOpen(false);
      setEditBookmark(null);
      await fetchBookmarkReadStates();
      await fetchBookmarks(searchKeyword);
    } catch (error) {
      setError(error instanceof Error ? error.message : '북마크 업데이트가 실패했습니다.');
    }
  };

  const handleDeleteBookmark = async (bookmarkId: number) => {
    if (window.confirm("이 북마크를 삭제하시겠습니까?")) {
      try {
        await deleteBookmark(bookmarkId);
        await fetchBookmarkReadStates();
        await fetchBookmarks(searchKeyword);
      } catch (error) {
        setError(error instanceof Error ? error.message : '북마크 삭제에 실패했습니다.');
      }
    }
  };

  const handlePreviousPage = () => {
    setCurrentPage(prev => Math.max(prev-1, 0));
  };
  const handleNextPage = () =>{
    if(bookmarks && !bookmarks.isLast) {
      setCurrentPage(prev => prev +1);
    }
  };

  if (isAuthLoading) {
    return <div className="flex justify-center items-center h-screen">로딩 중...</div>;
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl mb-2">내 책 목록</h1>
          <p className="text-muted-foreground">
            {bookmarkReadStates?.totalCount || 0} 권의 책을 등록했습니다.
          </p>
        </div>

        <Button onClick={() => onNavigate('books')}>
          <Plus className="mr-2 h-4 w-4" />
          새 책 추가하기
        </Button>
      </div>

      {/* 내 책 목록 통계 */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl">{bookmarkReadStates?.totalCount || 0}</div>
            <p className="text-sm text-muted-foreground">총 책 수</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-green-600">{bookmarkReadStates?.readState.READ || 0}</div>
            <p className="text-sm text-muted-foreground">읽은 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-blue-600">{bookmarkReadStates?.readState.READING || 0}</div>
            <p className="text-sm text-muted-foreground">읽고 있는 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-gray-600">{bookmarkReadStates?.readState.WISH || 0}</div>
            <p className="text-sm text-muted-foreground">읽고 싶은 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-yellow-600">{(bookmarkReadStates?.avgRate ?? 0).toFixed(1)}</div>
            <p className="text-sm text-muted-foreground">평균 평점</p>
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
          <TabsTrigger value="all">모든 상태 ({bookmarkReadStates?.totalCount})</TabsTrigger>
          <TabsTrigger value="READ">읽은 책 ({bookmarkReadStates?.readState.READ || 0})</TabsTrigger>
          <TabsTrigger value="READING">읽고 있는 책 ({bookmarkReadStates?.readState.READING || 0})</TabsTrigger>
          <TabsTrigger value="WISH">읽고 싶은 책 ({bookmarkReadStates?.readState.WISH || 0})</TabsTrigger>
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
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredBookmarks.map((bookmark) => (
                <Card key={bookmark.id} className="h-full cursor-pointer hover:shadow-lg transition-shadow overflow-hidden" onClick={() => onNavigate(`/bookmark/${bookmark.id}`)}>
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="line-clamp-2">{bookmark.book.title}</CardTitle>
                        <CardDescription>{bookmark.book?.authors?.join(', ') || '저자 정보 없음'}</CardDescription>
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
                  <CardContent className="flex-grow">
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
                    {bookmark.review?.rate > 0 && (
                      <p className="flex items-center space-x-1 py-2">
                        {renderStars(bookmark.review.rate)}
                        <span className="text-sm ml-2">{bookmark.review.rate}</span>
                      </p>
                    )}
                    {bookmark.review?.content && (
                      <p className="text-sm text-muted-foreground line-clamp-2 gap-2 py-2">
                        {bookmark.review.content}
                      </p>
                    )}
                    {/* 날짜 정보 */}
                    <div className="text-xs text-muted-foreground gap-2 py-2">
                      {bookmark.readState === 'READ' ? `완독 : ${bookmark.endReadDate?.substring(0, 10)}` : bookmark.readState === 'READING' ? `시작 : ${bookmark.startReadDate?.substring(0, 10)}` : `추가 : ${bookmark.createDate?.substring(0, 10)}`}
                    </div>
                  </CardContent>
                  <CardFooter className="flex justify-end space-x-2 py-3">
                    {/* 북마크 편집 버튼 */}
                      <div className="flex space-x-2" onClick={(e) => e.stopPropagation()}>
                        <Dialog open={isEditDialogOpen && editBookmark?.id === bookmark.id} onOpenChange={setIsEditDialogOpen}>
                          <DialogTrigger asChild>
                            <Button variant="ghost" size="sm" onClick={(e) => {
                              e.stopPropagation();
                              setEditBookmark(bookmark);
                              setIsEditDialogOpen(true);
                            }}
                            >
                              <Edit className="h-4 w-4" />
                            </Button>
                          </DialogTrigger>
                          <DialogContent className="max-w-2xl">
                            <DialogHeader>
                              <DialogTitle>북마크 편집</DialogTitle>
                              <DialogDescription>{bookmark.book.title}의 정보를 수정하세요.</DialogDescription>
                            </DialogHeader>
                            {editBookmark && (
                              <BookmarkEditForm
                                bookmark={editBookmark} onSave={handleSaveBookmark} onCancel={() => {
                                  setEditBookmark(null);
                                  setIsEditDialogOpen(false);
                                }}
                              />
                            )}
                          </DialogContent>
                        </Dialog>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteBookmark(bookmark.id);
                          }}
                        >
                          <Trash2 className='h-4 w-4' />
                        </Button>
                      </div>
                  </CardFooter>
                </Card>
              ))}
            </div>
          )}
        </div>
      </Tabs>
      {/* 페이지  */}
      {bookmarks && bookmarks.totalPages >1 && (
        <div className="flex justify-center items-center mt-8 space-x-4">
          <Button
            onClick={handlePreviousPage}
            disabled={currentPage === 0}
            variant="outline"
            >
              이전
            </Button>
            <span className="test-sm">
              {currentPage + 1} / {bookmarks?.totalPages}
            </span>
            <Button
            onClick={handleNextPage}
            disabled={bookmarks?.isLast}
            variant="outline"
            >
              다음
            </Button>
        </div>)}
    </div>
  );
}
// 책 편집 폼 컴포넌트
interface BookmarkEditFormProps {
  bookmark: Bookmark;
  onSave: (updateData: UpdateBookmark) => void;
  onCancel: () => void;
}

function BookmarkEditForm({ bookmark, onSave, onCancel }: BookmarkEditFormProps) {
  const [formData, setFormData] = useState({
    readState: bookmark.readState,
    startReadDate: bookmark.startReadDate?.substring(0, 10) || '',
    endReadDate: bookmark.endReadDate?.substring(0, 10) || '',
    readPage: bookmark.readPage || 0,
  });

  useEffect(() => {
    setFormData({
      readState: bookmark.readState,
      startReadDate: bookmark.startReadDate?.substring(0, 10) || '',
      endReadDate: bookmark.endReadDate?.substring(0, 10) || '',
      readPage: bookmark.readPage || 0,
    });
  }, [bookmark]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const dataToSend = {
      ...formData,
      startReadDate: formData.startReadDate ? `${formData.startReadDate}T00:00:00` : null,
      endReadDate: formData.endReadDate ? `${formData.endReadDate}T00:00:00` : null,
    };
    onSave(dataToSend);
  };

  const handleValueChange = (field: keyof typeof formData, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handlePageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const totalPage = bookmark.book.totalPage;
    let newPage = parseInt(e.target.value.trim()) || 0;
    if(newPage > totalPage) {
      newPage = totalPage;
    }
    if(newPage <0) {
      newPage = 0;
    }

    if(newPage === totalPage) {
      setFormData(prev => ({
        ...prev,
        readState: 'READ',
        readPage: newPage,
        endReadDate: new Date().toISOString().split('T')[0],
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        readPage: newPage,
      }));
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="status">읽기 상태</Label>
          <Select value={formData.readState} onValueChange={(value) => handleValueChange('readState', value)}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="WISH">읽고 싶은 책</SelectItem>
              <SelectItem value="READING">읽고 있는 책</SelectItem>
              <SelectItem value="READ">읽은 책</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {formData.readState === 'READING' && (
          <div className="space-y-2">
            <Label htmlFor="currentPage">현재 페이지</Label>
            <Input
              id="currentPage"
              type="number"
              value={formData.readPage || ''}
              onChange={handlePageChange}
              placeholder="현재 읽고 있는 페이지"
            />
          </div>
        )}

        {formData.readState === 'READ' && (
          <>
            <div className="space-y-2">
              <Label htmlFor="dateFinished">완독일</Label>
              <Input
                id="endReadDate"
                type="date"
                value={formData.endReadDate || ''}
                onChange={(e) => handleValueChange('endReadDate', e.target.value)}
                min={formData.startReadDate || undefined}
              />
            </div>
          </>
        )}
      </div>

      {(formData.readState === 'READING' || formData.readState === 'READ') && (
        <div className="space-y-2">
          <Label htmlFor="dateStarted">시작일</Label>
          <Input
            id="startReadDate"
            type="date"
            value={formData.startReadDate || ''}
            onChange={(e) => handleValueChange('startReadDate', e.target.value)}
            max={formData.endReadDate || undefined}
          />
        </div>
      )}

      <div className="flex justify-end space-x-2 pt-4">
        <Button type="button" variant="outline" onClick={onCancel}>
          취소
        </Button>
        <Button type="submit">
          저장
        </Button>
      </div>
    </form>
  );
}