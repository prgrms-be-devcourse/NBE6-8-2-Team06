"use client";

import React, { useState } from 'react';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import BookCard from '@/components/BookCard';
import { Search, Heart, BookOpen, Star, Filter, Plus } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { ImageWithFallback } from '@/components/ImageWithFallback';
import { useRouter } from 'next/navigation';
import { usePathname } from 'next/navigation';

interface BooksPageProps {
  onNavigate: (page: string) => void;
  onBookClick: (bookId: number) => void;
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


export default function BooksPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [sortBy, setSortBy] = useState('title');
  const [userBookStatus, setUserBookStatus] = useState<{[key: number]: string}>({
    1: '읽은 책',
    2: '읽고 있는 책',
    5: '읽고 싶은 책'
  });
  const router = useRouter()
  const pathName = usePathname()
  const onBookClick = (id:number) => {
    router.push(`${pathName}/${id}`)
  }
  // DB의 모든 책 데이터 (실제로는 API에서 가져올 데이터)
  const allBooks: Book[] = [
    {
      id: 1,
      title: "클린 코드",
      author: "로버트 C. 마틴",
      category: "프로그래밍",
      description: "애자일 소프트웨어 장인 정신. 읽기 좋은 코드를 작성하는 방법을 설명하는 프로그래밍 필독서입니다.",
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
      description: "기존 코드를 개선하는 체계적인 방법론을 제시하는 소프트웨어 개발의 고전입니다.",
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
      description: "객체지향 소프트웨어 설계의 핵심 패턴들을 정리한 소프트웨어 공학의 바이블입니다.",
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
      description: "호모 사피엔스의 역사를 통해 인류 문명의 발전 과정을 흥미롭게 서술한 베스트셀러입니다.",
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
      description: "우주에 대한 경이로움과 과학적 사고의 중요성을 일깨워주는 과학 교양서의 고전입니다.",
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
      description: "전체주의 사회의 공포를 그린 디스토피아 소설의 걸작으로, 현대에도 큰 울림을 주는 작품입니다.",
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
      description: "인간관계의 기본 원칙들을 제시하며 전 세계적으로 사랑받고 있는 자기계발서의 고전입니다.",
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
      description: "전 세계를 매혹시킨 판타지 소설의 시작. 마법사 해리 포터의 모험이 펼쳐집니다.",
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
      description: "인류의 미래에 대한 도발적인 질문들을 던지며 기술 발전이 가져올 변화를 예측합니다.",
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
      description: "작은 변화가 만들어내는 큰 성과에 대한 실용적인 가이드를 제시하는 습관 형성 가이드북입니다.",
      publishedDate: "2018-10-16",
      pages: 392,
      isbn: "9791164050208",
      averageRating: 4.6,
      ratingsCount: 4200,
      language: "한국어",
      publisher: "비즈니스북스"
    }
  ];

  const categories = ['all', '프로그래밍', '역사', '과학', '소설', '자기계발'];
  const sortOptions = [
    { value: 'title', label: '제목순' },
    { value: 'author', label: '저자순' },
    { value: 'rating', label: '평점순' },
    { value: 'published', label: '출간일순' },
    { value: 'popularity', label: '인기순' }
  ];

  const filteredBooks = allBooks
    .filter(book => {
      const matchesSearch = book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           book.author.toLowerCase().includes(searchTerm.toLowerCase()) ||
                           book.description.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesCategory = selectedCategory === 'all' || book.category === selectedCategory;
      return matchesSearch && matchesCategory;
    })
    .sort((a, b) => {
      switch (sortBy) {
        case 'title':
          return a.title.localeCompare(b.title);
        case 'author':
          return a.author.localeCompare(b.author);
        case 'rating':
          return b.averageRating - a.averageRating;
        case 'published':
          return new Date(b.publishedDate).getTime() - new Date(a.publishedDate).getTime();
        case 'popularity':
          return b.ratingsCount - a.ratingsCount;
        default:
          return 0;
      }
    });

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

  const addToMyBooks = (bookId: number, status: string) => {
    setUserBookStatus(prev => ({
      ...prev,
      [bookId]: status
    }));
  };

  const removeFromMyBooks = (bookId: number) => {
    setUserBookStatus(prev => {
      const newStatus = { ...prev };
      delete newStatus[bookId];
      return newStatus;
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case '읽은 책': return 'bg-green-100 text-green-800';
      case '읽고 있는 책': return 'bg-blue-100 text-blue-800';
      case '읽고 싶은 책': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl mb-2">책 탐색</h1>
        <p className="text-muted-foreground">
          총 {allBooks.length}권의 책이 등록되어 있습니다. 관심 있는 책을 찾아 내 목록에 추가해보세요.
        </p>
      </div>

      {/* 검색 및 필터 */}
      <div className="mb-8 space-y-4">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
            <Input
              placeholder="책 제목, 저자, 내용으로 검색..."
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
          <Select value={sortBy} onValueChange={setSortBy}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="정렬 기준" />
            </SelectTrigger>
            <SelectContent>
              {sortOptions.map(option => (
                <SelectItem key={option.value} value={option.value}>
                  {option.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* 검색 결과 */}
      <div className="mb-6">
        <p className="text-sm text-muted-foreground">
          {filteredBooks.length}개의 책이 검색되었습니다
        </p>
      </div>

      {/* 책 목록 */}
      {filteredBooks.length === 0 ? (
        <div className="text-center py-12">
          <BookOpen className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
          <p className="text-muted-foreground">검색 조건에 맞는 책이 없습니다.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredBooks.map((book) => (
            <Card key={book.id} className="h-full flex flex-col cursor-pointer hover:shadow-lg transition-shadow">
              <CardHeader onClick={() => onBookClick(book.id)}>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <CardTitle className="line-clamp-2">{book.title}</CardTitle>
                    <CardDescription>{book.author}</CardDescription>
                    <div className="flex items-center gap-2 mt-2">
                      <Badge variant="secondary">{book.category}</Badge>
                      {userBookStatus[book.id] && (
                        <Badge className={getStatusColor(userBookStatus[book.id])}>
                          {userBookStatus[book.id]}
                        </Badge>
                      )}
                    </div>
                  </div>
                  <ImageWithFallback
                    src={`https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=80&h=120&fit=crop&crop=center&sig=${book.id}`}
                    alt={book.title}
                    className="w-16 h-24 object-cover rounded ml-4"
                  />
                </div>
              </CardHeader>
              <CardContent className="flex-1 flex flex-col" onClick={() => onBookClick(book.id)}>
                <div className="flex-1 space-y-3">
                  <p className="text-sm text-muted-foreground line-clamp-3">
                    {book.description}
                  </p>
                  
                  <div className="flex items-center justify-between text-sm text-muted-foreground">
                    <span>{book.pages}쪽</span>
                    <span>{book.publishedDate.split('-')[0]}년</span>
                  </div>
                  
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-1">
                      {renderStars(book.averageRating)}
                      <span className="text-sm ml-2">{book.averageRating}</span>
                      <span className="text-xs text-muted-foreground">({book.ratingsCount}명)</span>
                    </div>
                  </div>
                </div>
                
                <div className="mt-4 pt-4 border-t" onClick={(e) => e.stopPropagation()}>
                  {userBookStatus[book.id] ? (
                    <div className="flex gap-2">
                      <Select 
                        value={userBookStatus[book.id]} 
                        onValueChange={(status) => addToMyBooks(book.id, status)}
                      >
                        <SelectTrigger className="flex-1">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="읽고 싶은 책">읽고 싶은 책</SelectItem>
                          <SelectItem value="읽고 있는 책">읽고 있는 책</SelectItem>
                          <SelectItem value="읽은 책">읽은 책</SelectItem>
                        </SelectContent>
                      </Select>
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => removeFromMyBooks(book.id)}
                      >
                        제거
                      </Button>
                    </div>
                  ) : (
                    <div className="flex gap-2">
                      <Button 
                        className="flex-1"
                        onClick={() => addToMyBooks(book.id, '읽고 싶은 책')}
                      >
                        <Plus className="h-4 w-4 mr-2" />
                        내 목록에 추가
                      </Button>
                      <Button 
                        variant="outline" 
                        size="sm"
                        onClick={() => addToMyBooks(book.id, '읽고 싶은 책')}
                      >
                        <Heart className="h-4 w-4" />
                      </Button>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
};

