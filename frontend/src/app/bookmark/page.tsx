'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { getBookmarks, updateBookmark, deleteBookmark, getBookmarkReadStates } from '../../types/bookmarkAPI';
import { BookmarkPage, Bookmark, BookmarkReadStates, UpdateBookmark } from '../../types/bookmarkData';
import { BookOpen, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Label } from '@/components/ui/label';
import { useAuth } from "../_hooks/auth-context";
import { getCategories, Category } from '@/types/category';
import { BookmarkCard } from './_components/bookmarkCard';
import { BookmarkStats } from './_components/bookmarkStats';
import { BookmarkFilters } from './_components/bookmarkFilters';
import { PaginationControls } from './_components/paginationControls';
import { useDebounce } from '../_hooks/useDebounce';


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
  const [filteredReadState, setFilteredReadState] = useState<BookmarkReadStates>();
  const [categories, setCategories] = useState<Category[]>([]);

  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editBookmark, setEditBookmark] = useState<Bookmark | null>(null);

  const debouncedSearchKeyword = useDebounce(searchKeyword, 500);

  const onNavigate = (path: string) => {
    router.push(path);
  };

  useEffect(() => {
    if (!isAuthLoading && !isLoggedIn) {
      onNavigate('/login');
    }
  }, [isAuthLoading, isLoggedIn, onNavigate]);

  const fetchBookmarks = useCallback(async () => {
    if (!isLoggedIn) return;

    setIsLoading(true);
    setError('');
    try {
      const response = await getBookmarks({
        page: currentPage,
        size: 9,
        sort: "createDate,desc",
        category: selectedCategory,
        readState: selectedReadState,
        keyword: debouncedSearchKeyword,
      });
      setBookmarks(response.data);
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
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
  }, [isLoggedIn, currentPage, selectedCategory, selectedReadState, debouncedSearchKeyword]);


  const fetchInitialData = useCallback(async () => {
    if (!isLoggedIn) return;

    try {
      const [statsResponse, categoriesResponse] = await Promise.all([
        getBookmarkReadStates({
        category: null,
        readState: null,
        keyword: null,
        }),
        getCategories(),
      ]);

      setBookmarkReadStates(statsResponse.data);
      setCategories(categoriesResponse.data);
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
      setError(error instanceof Error ? error.message : '초기 데이터 로딩에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  }, [isLoggedIn]);

  const fetchFilteredReadState = useCallback(async () => {
    if (!isLoggedIn) return;
    try {
      const response = await getBookmarkReadStates({
        category: selectedCategory,
        readState: selectedReadState,
        keyword: debouncedSearchKeyword,
      });
      setFilteredReadState(response.data);
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
      setError(error instanceof Error ? error.message : '초기 데이터 로딩에 실패했습니다.');
      setFilteredReadState({
        totalCount: 0,
        avgRate: 0,
        readState: {
          WISH: 0,
          READING: 0,
          READ: 0,
        },
      });
    } finally {
      setIsLoading(false);
    }
  }, [isLoggedIn, selectedCategory, selectedReadState, debouncedSearchKeyword]);

  // 카테고리 또는 검색어가 변경되면 페이지를 처음으로 되돌림
  useEffect(() => {
    setCurrentPage(0);
  }, [selectedCategory, selectedReadState, debouncedSearchKeyword]);

  // 책 목록 불러오기
  useEffect(() => {
    fetchBookmarks();
    fetchFilteredReadState();
  }, [fetchBookmarks]);

  // 초기 데이터 로딩
  useEffect(() => {
    fetchInitialData();
  }, [fetchInitialData]);

  const filteredBookmarks = useMemo(() => {
    if (!bookmarks?.data) return [];
    if (selectedReadState === 'all') return bookmarks.data;
    return bookmarks.data.filter(bookmark => bookmark.readState === selectedReadState);
  }, [bookmarks, selectedReadState]);

  const handleEditClick = (bookmark: Bookmark) => {
    setEditBookmark(bookmark);
    setIsEditDialogOpen(true);
  };
  const handleCancelEdit = () => {
    setEditBookmark(null);
    setIsEditDialogOpen(false);
  };
  const handleSaveBookmark = async (updateData: UpdateBookmark) => {
    if (!editBookmark) return;
    try {
      await updateBookmark(editBookmark.id, updateData);
      setIsEditDialogOpen(false);
      setEditBookmark(null);
      await fetchInitialData();
      await fetchBookmarks();
    } catch (error) {
      console.error('❌ 에러 데이터:', (error as any).data);
      setError(error instanceof Error ? error.message : '북마크 업데이트가 실패했습니다.');
    }
  };

  const handleDeleteBookmark = async (bookmarkId: number) => {
    if (window.confirm("이 북마크를 삭제하시겠습니까?")) {
      try {
        await deleteBookmark(bookmarkId);
        await fetchInitialData();
        await fetchBookmarks();
      } catch (error) {
        console.error('❌ 에러 데이터:', (error as any).data);
        setError(error instanceof Error ? error.message : '북마크 삭제에 실패했습니다.');
      }
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
      <BookmarkStats stats={bookmarkReadStates} />
      {/* 책 목록 검색 필터 */}
      <BookmarkFilters searchKeyword={searchKeyword} onSearchKeywordChange={setSearchKeyword}
        selectedCategory={selectedCategory} onCategoryChange={setSelectedCategory}
        categories={categories} selectedReadState={selectedReadState}
        onReadStateChange={setSelectedReadState} readStates={['READ', 'READING', 'WISH']}
        setCurrentPage={setCurrentPage}
      />

      {/* 책 목록 테이블 */}
      <Tabs value={selectedReadState} onValueChange={(value) => {
        setSelectedReadState(value);
        setCurrentPage(0);
      }} className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="all">모든 상태 ({filteredReadState?.totalCount})</TabsTrigger>
          <TabsTrigger value="READ">읽은 책 ({filteredReadState?.readState.READ || 0})</TabsTrigger>
          <TabsTrigger value="READING">읽고 있는 책 ({filteredReadState?.readState.READING || 0})</TabsTrigger>
          <TabsTrigger value="WISH">읽고 싶은 책 ({filteredReadState?.readState.WISH || 0})</TabsTrigger>
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
                <BookmarkCard
                  key={bookmark.id}
                  bookmark={bookmark}
                  onNavigate={onNavigate}
                  onEditClick={handleEditClick}
                  onDeleteClick={handleDeleteBookmark}
                />
              ))}
            </div>
          )}
        </div>
      </Tabs>
      {/* 페이지  */}
      <PaginationControls currentPage={currentPage} totalPages={bookmarks?.totalPages || 0}
        onPrevious={() => setCurrentPage(p => Math.max(p - 1, 0))}
        onNext={() => setCurrentPage(p => p + 1)}
      />

      <Dialog open={isEditDialogOpen} onOpenChange={setIsEditDialogOpen}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>북마크 편집</DialogTitle>
            <DialogDescription>{editBookmark?.book.title}의 정보를 수정합니다.</DialogDescription>
          </DialogHeader>
          {editBookmark && (
            <BookmarkEditForm
              bookmark={editBookmark}
              onSave={handleSaveBookmark}
              onCancel={handleCancelEdit}
            />
          )}
        </DialogContent>
      </Dialog>
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
    if (newPage > totalPage) {
      newPage = totalPage;
    }
    if (newPage < 0) {
      newPage = 0;
    }

    if (newPage === totalPage) {
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

  const isFormValid = useMemo(() => {
    if (formData.readState === 'READING') {
      return !!formData.startReadDate && !!formData.readPage;
    }
    if (formData.readState === 'READ') {
      return !!formData.startReadDate && !!formData.endReadDate && formData.endReadDate >= formData.startReadDate;
    }
    return true;
  }, [formData]);

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
        <Button type="submit" disabled={!isFormValid}>
          저장
        </Button>
      </div>
    </form>
  );
}