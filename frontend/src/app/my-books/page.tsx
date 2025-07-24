"use client";

import React, { useState } from 'react';
import Link from 'next/link';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Search, Plus, Star, Edit, Trash2, BookOpen } from 'lucide-react';
import {ImageWithFallback} from '@/components/ImageWithFallback'
import { useRouter } from 'next/navigation';

interface MyBooksPageProps {
  onNavigate: (page: String) => void;
  onBookClick: (bookId: number) => void;
}

interface MyBook {
  id: number;
  title: string;
  author: string;
  category: string;
  status: '읽은 책' | '읽고 있는 책' | '읽고 싶은 책';
  rating?: number;
  review?: string;
  notes?: string;
  dateAdded: string;
  dateStarted?: string;
  dateFinished?: string;
  currentPage?: number;
  totalPages: number;
}

export default function MyBooksPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [editingBook, setEditingBook] = useState<MyBook | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const router = useRouter();

  const onNavigate = (e:string) => {
    router.push(e)
  }

  const onBookClick = (e:number) =>{
    router.push(`/books/${e}`)
  }

  // 내가 추가한 책들 데이터
  const [myBooks, setMyBooks] = useState<MyBook[]>([
    {
      id: 1,
      title: "클린 코드",
      author: "로버트 C. 마틴",
      category: "프로그래밍",
      status: "읽은 책",
      rating: 4.5,
      review: "코드 품질에 대한 훌륭한 인사이트를 제공합니다. 특히 변수명과 함수명의 중요성에 대해 깊이 깨달았습니다.",
      notes: "챕터 2의 의미 있는 이름 부분이 가장 인상적이었음",
      dateAdded: "2024-01-15",
      dateStarted: "2024-01-15",
      dateFinished: "2024-02-20",
      totalPages: 464
    },
    {
      id: 2,
      title: "리팩터링",
      author: "마틴 파울러",
      category: "프로그래밍",
      status: "읽고 있는 책",
      rating: 4.0,
      notes: "현재 3장까지 읽었음. 매우 실용적인 내용들이 많음",
      dateAdded: "2024-02-01",
      dateStarted: "2024-02-10",
      currentPage: 150,
      totalPages: 550
    },
    {
      id: 3,
      title: "디자인 패턴",
      author: "GoF",
      category: "프로그래밍",
      status: "읽고 싶은 책",
      dateAdded: "2024-02-10",
      totalPages: 395
    },
    {
      id: 4,
      title: "사피엔스",
      author: "유발 하라리",
      category: "역사",
      status: "읽은 책",
      rating: 4.8,
      review: "인류 역사에 대한 새로운 관점을 제시합니다. 특히 농업혁명에 대한 해석이 인상적이었습니다.",
      notes: "인지혁명, 농업혁명, 과학혁명의 3단계 구분이 흥미로움",
      dateAdded: "2023-12-01",
      dateStarted: "2023-12-05",
      dateFinished: "2024-01-05",
      totalPages: 512
    },
    {
      id: 5,
      title: "코스모스",
      author: "칼 세이건",
      category: "과학",
      status: "읽고 싶은 책",
      dateAdded: "2024-02-15",
      totalPages: 396
    }
  ]);

  const categories = ['all', '프로그래밍', '역사', '과학', '소설', '자기계발'];
  const statuses = ['all', '읽은 책', '읽고 있는 책', '읽고 싶은 책'];

  const filteredBooks = myBooks.filter(book => {
    const matchesSearch = book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         book.author.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesCategory = selectedCategory === 'all' || book.category === selectedCategory;
    const matchesStatus = selectedStatus === 'all' || book.status === selectedStatus;
    
    return matchesSearch && matchesCategory && matchesStatus;
  });

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

  const booksByStatus = {
    'all': filteredBooks,
    '읽은 책': filteredBooks.filter(book => book.status === '읽은 책'),
    '읽고 있는 책': filteredBooks.filter(book => book.status === '읽고 있는 책'),
    '읽고 싶은 책': filteredBooks.filter(book => book.status === '읽고 싶은 책')
  };

  const handleSaveBook = (updatedBook: MyBook) => {
    setMyBooks(prevBooks => 
      prevBooks.map(book => 
        book.id === updatedBook.id ? updatedBook : book
      )
    );
    setEditingBook(null);
    setIsDialogOpen(false);
  };

  const handleDeleteBook = (bookId: number) => {
    setMyBooks(prevBooks => prevBooks.filter(book => book.id !== bookId));
  };

  const getReadingProgress = (book: MyBook) => {
    if (book.status === '읽고 있는 책' && book.currentPage && book.totalPages) {
      return Math.round((book.currentPage / book.totalPages) * 100);
    }
    return null;
  };

  const stats = {
    total: myBooks.length,
    read: myBooks.filter(book => book.status === '읽은 책').length,
    reading: myBooks.filter(book => book.status === '읽고 있는 책').length,
    wantToRead: myBooks.filter(book => book.status === '읽고 싶은 책').length,
    averageRating: myBooks.filter(book => book.rating).reduce((sum, book) => sum + (book.rating || 0), 0) / myBooks.filter(book => book.rating).length || 0
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl mb-2">내 책 목록</h1>
          <p className="text-muted-foreground">
            총 {stats.total}권의 책을 관리하고 있습니다
          </p>
        </div>
        <Button onClick={() => onNavigate('books')}>
          <Plus className="mr-2 h-4 w-4" />
          새 책 추가하기
        </Button>
      </div>

      {/* 통계 */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-4 mb-8">
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl">{stats.total}</div>
            <p className="text-sm text-muted-foreground">총 책 수</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-green-600">{stats.read}</div>
            <p className="text-sm text-muted-foreground">읽은 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-blue-600">{stats.reading}</div>
            <p className="text-sm text-muted-foreground">읽고 있는 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-gray-600">{stats.wantToRead}</div>
            <p className="text-sm text-muted-foreground">읽고 싶은 책</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="p-4">
            <div className="text-2xl text-yellow-600">
              {stats.averageRating ? stats.averageRating.toFixed(1) : '0'}
            </div>
            <p className="text-sm text-muted-foreground">평균 평점</p>
          </CardContent>
        </Card>
      </div>

      {/* 검색 및 필터 */}
      <div className="mb-8 space-y-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder="책 제목이나 저자로 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Select value={selectedCategory} onValueChange={setSelectedCategory}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="카테고리 선택" />
            </SelectTrigger>
            <SelectContent>
              {categories.map(category => (
                <SelectItem key={category} value={category}>
                  {category === 'all' ? '모든 카테고리' : category}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={selectedStatus} onValueChange={setSelectedStatus}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="상태 선택" />
            </SelectTrigger>
            <SelectContent>
              {statuses.map(status => (
                <SelectItem key={status} value={status}>
                  {status === 'all' ? '모든 상태' : status}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* 책 목록 탭 */}
      <Tabs defaultValue="all" className="w-full">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="all">모든 책 ({booksByStatus['all'].length})</TabsTrigger>
          <TabsTrigger value="읽은 책">읽은 책 ({booksByStatus['읽은 책'].length})</TabsTrigger>
          <TabsTrigger value="읽고 있는 책">읽고 있는 책 ({booksByStatus['읽고 있는 책'].length})</TabsTrigger>
          <TabsTrigger value="읽고 싶은 책">읽고 싶은 책 ({booksByStatus['읽고 싶은 책'].length})</TabsTrigger>
        </TabsList>

        {Object.entries(booksByStatus).map(([status, books]) => (
          <TabsContent key={status} value={status} className="mt-6">
            {books.length === 0 ? (
              <div className="text-center py-12">
                <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
                <p className="text-muted-foreground mb-4">
                  {status === 'all' ? '아직 추가한 책이 없습니다.' : `${status}이 없습니다.`}
                </p>
                <Button onClick={() => onNavigate('books')}>
                  새 책 추가하기
                </Button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {books.map((book) => (
                  <Card key={book.id} className="h-full cursor-pointer hover:shadow-lg transition-shadow" onClick={() => onBookClick(book.id)}>
                    <CardHeader>
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <CardTitle className="line-clamp-2">{book.title}</CardTitle>
                          <CardDescription>{book.author}</CardDescription>
                          <Badge className={`mt-2 ${getStatusColor(book.status)}`}>
                            {book.status}
                          </Badge>
                        </div>
                        <ImageWithFallback
                          src={`https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=80&h=120&fit=crop&crop=center&sig=${book.id}`}
                          alt={book.title}
                          className="w-16 h-24 object-cover rounded ml-4"
                        />
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="space-y-3">
                        <div className="flex justify-between text-sm text-muted-foreground">
                          <span>카테고리: {book.category}</span>
                          <span>{book.totalPages}쪽</span>
                        </div>
                        
                        {/* 읽기 진도 */}
                        {book.status === '읽고 있는 책' && book.currentPage && (
                          <div className="space-y-1">
                            <div className="flex justify-between text-sm">
                              <span>{book.currentPage}쪽 / {book.totalPages}쪽</span>
                              <span>{getReadingProgress(book)}%</span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-2">
                              <div 
                                className="bg-blue-600 h-2 rounded-full"
                                style={{ width: `${getReadingProgress(book)}%` }}
                              />
                            </div>
                          </div>
                        )}
                        
                        {/* 평점 */}
                        {book.rating && (
                          <div className="flex items-center space-x-1">
                            {renderStars(book.rating)}
                            <span className="text-sm ml-2">{book.rating}</span>
                          </div>
                        )}
                        
                        {/* 리뷰 */}
                        {book.review && (
                          <p className="text-sm text-muted-foreground line-clamp-2">
                            {book.review}
                          </p>
                        )}
                        
                        {/* 날짜 정보 */}
                        <div className="text-xs text-muted-foreground">
                          {book.dateFinished && `완독: ${book.dateFinished}`}
                          {book.dateStarted && !book.dateFinished && `시작: ${book.dateStarted}`}
                          {!book.dateStarted && `추가: ${book.dateAdded}`}
                        </div>
                        
                        <div className="flex justify-between items-center pt-2">
                          <div className="flex space-x-2" onClick={(e) => e.stopPropagation()}>
                            <Dialog open={isDialogOpen && editingBook?.id === book.id} onOpenChange={setIsDialogOpen}>
                              <DialogTrigger asChild>
                                <Button 
                                  variant="ghost" 
                                  size="sm"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    setEditingBook(book);
                                    setIsDialogOpen(true);
                                  }}
                                >
                                  <Edit className="h-4 w-4" />
                                </Button>
                              </DialogTrigger>
                              <DialogContent className="max-w-2xl">
                                <DialogHeader>
                                  <DialogTitle>책 정보 수정</DialogTitle>
                                  <DialogDescription>
                                    {book.title}의 정보를 수정하세요.
                                  </DialogDescription>
                                </DialogHeader>
                                <BookEditForm 
                                  book={editingBook} 
                                  onSave={handleSaveBook}
                                  onCancel={() => {
                                    setEditingBook(null);
                                    setIsDialogOpen(false);
                                  }}
                                />
                              </DialogContent>
                            </Dialog>
                            <Button 
                              variant="ghost" 
                              size="sm"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleDeleteBook(book.id);
                              }}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            )}
          </TabsContent>
        ))}
      </Tabs>
    </div>
  );
}

// 책 편집 폼 컴포넌트
interface BookEditFormProps {
  book: MyBook | null;
  onSave: (book: MyBook) => void;
  onCancel: () => void;
}

function BookEditForm({ book, onSave, onCancel }: BookEditFormProps) {
  const [formData, setFormData] = useState<MyBook>(
    book || {
      id: 0,
      title: '',
      author: '',
      category: '',
      status: '읽고 싶은 책',
      dateAdded: new Date().toISOString().split('T')[0],
      totalPages: 0
    }
  );

  if (!book) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave(formData);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="status">읽기 상태</Label>
          <Select value={formData.status} onValueChange={(value: any) => setFormData({...formData, status: value})}>
            <SelectTrigger>
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="읽고 싶은 책">읽고 싶은 책</SelectItem>
              <SelectItem value="읽고 있는 책">읽고 있는 책</SelectItem>
              <SelectItem value="읽은 책">읽은 책</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {formData.status === '읽고 있는 책' && (
          <div className="space-y-2">
            <Label htmlFor="currentPage">현재 페이지</Label>
            <Input
              id="currentPage"
              type="number"
              value={formData.currentPage || ''}
              onChange={(e) => setFormData({...formData, currentPage: parseInt(e.target.value) || undefined})}
              placeholder="현재 읽고 있는 페이지"
            />
          </div>
        )}

        {formData.status === '읽은 책' && (
          <>
            <div className="space-y-2">
              <Label htmlFor="rating">평점</Label>
              <Select value={formData.rating?.toString() || ''} onValueChange={(value) => setFormData({...formData, rating: value ? parseFloat(value) : undefined})}>
                <SelectTrigger>
                  <SelectValue placeholder="평점 선택" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">1점</SelectItem>
                  <SelectItem value="1.5">1.5점</SelectItem>
                  <SelectItem value="2">2점</SelectItem>
                  <SelectItem value="2.5">2.5점</SelectItem>
                  <SelectItem value="3">3점</SelectItem>
                  <SelectItem value="3.5">3.5점</SelectItem>
                  <SelectItem value="4">4점</SelectItem>
                  <SelectItem value="4.5">4.5점</SelectItem>
                  <SelectItem value="5">5점</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="dateFinished">완독일</Label>
              <Input
                id="dateFinished"
                type="date"
                value={formData.dateFinished || ''}
                onChange={(e) => setFormData({...formData, dateFinished: e.target.value})}
              />
            </div>
          </>
        )}
      </div>

      {(formData.status === '읽고 있는 책' || formData.status === '읽은 책') && (
        <div className="space-y-2">
          <Label htmlFor="dateStarted">시작일</Label>
          <Input
            id="dateStarted"
            type="date"
            value={formData.dateStarted || ''}
            onChange={(e) => setFormData({...formData, dateStarted: e.target.value})}
          />
        </div>
      )}

      {formData.status === '읽은 책' && (
        <div className="space-y-2">
          <Label htmlFor="review">리뷰</Label>
          <Textarea
            id="review"
            value={formData.review || ''}
            onChange={(e) => setFormData({...formData, review: e.target.value})}
            placeholder="이 책에 대한 리��를 작성해주세요..."
            rows={3}
          />
        </div>
      )}

      <div className="space-y-2">
        <Label htmlFor="notes">메모</Label>
        <Textarea
          id="notes"
          value={formData.notes || ''}
          onChange={(e) => setFormData({...formData, notes: e.target.value})}
          placeholder="개인적인 메모를 작성해주세요..."
          rows={2}
        />
      </div>

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