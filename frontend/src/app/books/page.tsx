"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import BookCard from "@/components/BookCard";
import { Search, Heart, BookOpen, Star, Filter, Plus } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { ImageWithFallback } from "@/components/ImageWithFallback";
import { useRouter } from "next/navigation";
import { usePathname } from "next/navigation";
import {
  BookSearchDto,
  ReadState,
  fetchBooks,
  BooksResponse,
} from "@/types/book";

interface BooksPageProps {
  onNavigate: (page: string) => void;
  onBookClick: (bookId: number) => void;
}

export default function BooksPage() {
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [sortBy, setSortBy] = useState("title");
  const [books, setBooks] = useState<BookSearchDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [userBookStatus, setUserBookStatus] = useState<{
    [key: number]: string;
  }>({
    1: "읽은 책",
    2: "읽고 있는 책",
    5: "읽고 싶은 책",
  });
  const router = useRouter();
  const pathName = usePathname();
  const onBookClick = (id: number) => {
    router.push(`${pathName}/${id}`);
  };

  const loadBooks = async (page: number = 0) => {
    try {
      setLoading(true);
      console.log(`🚀 books 페이지에서 API 호출 시작 - 페이지: ${page}`);
      const response = await fetchBooks(page);
      console.log("📚 받아온 응답:", response);
      setBooks(response.books);
      setCurrentPage(response.pageInfo.currentPage);
      setTotalPages(response.pageInfo.totalPages);
      setTotalElements(response.pageInfo.totalElements);
    } catch (err) {
      console.error("💥 에러 발생:", err);
      setError(
        err instanceof Error ? err.message : "책을 불러오는데 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBooks(0);
  }, []);

  // Get unique categories from books data
  const categories = [
    "all",
    ...Array.from(new Set(books.map((book) => book.categoryName))),
  ];

  // Helper function to get display text for read state
  const getReadStateText = (readState: ReadState) => {
    switch (readState) {
      case ReadState.READ:
        return "읽은 책";
      case ReadState.READING:
        return "읽고 있는 책";
      case ReadState.NOT_READ:
        return "읽고 싶은 책";
      default:
        return "";
    }
  };

  const filteredBooks = books
    .filter((book) => {
      const matchesSearch =
        book.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
        book.authors.some((author) =>
          author.toLowerCase().includes(searchTerm.toLowerCase())
        );
      const matchesCategory =
        selectedCategory === "all" || book.categoryName === selectedCategory;
      return matchesSearch && matchesCategory;
    })
    .sort((a, b) => {
      switch (sortBy) {
        case "title":
          return a.title.localeCompare(b.title);
        case "author":
          return a.authors[0]?.localeCompare(b.authors[0] || "") || 0;
        case "rating":
          return b.avgRate - a.avgRate;
        case "published":
          return (
            new Date(b.publishedDate).getTime() -
            new Date(a.publishedDate).getTime()
          );
        case "popularity":
          return b.avgRate - a.avgRate; // Using avgRate as popularity metric
        default:
          return 0;
      }
    });

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p>책을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="text-center">
          <p className="text-red-500">{error}</p>
          <Button onClick={() => window.location.reload()} className="mt-4">
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  const sortOptions = [
    { value: "title", label: "제목순" },
    { value: "author", label: "저자순" },
    { value: "rating", label: "평점순" },
    { value: "published", label: "출간일순" },
    { value: "popularity", label: "인기순" },
  ];

  const renderStars = (rating: number) => {
    return [...Array(5)].map((_, i) => (
      <Star
        key={i}
        className={`h-4 w-4 ${
          i < Math.floor(rating)
            ? "fill-yellow-400 text-yellow-400"
            : "text-gray-300"
        }`}
      />
    ));
  };

  const addToMyBooks = (bookId: number, status: string) => {
    setUserBookStatus((prev) => ({
      ...prev,
      [bookId]: status,
    }));
  };

  const removeFromMyBooks = (bookId: number) => {
    setUserBookStatus((prev) => {
      const newStatus = { ...prev };
      delete newStatus[bookId];
      return newStatus;
    });
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "읽은 책":
        return "bg-green-100 text-green-800";
      case "읽고 있는 책":
        return "bg-blue-100 text-blue-800";
      case "읽고 싶은 책":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="mb-8">
        <h1 className="text-3xl mb-2">책 탐색</h1>
        <p className="text-muted-foreground">
          총 {totalElements}권의 책이 등록되어 있습니다. 관심 있는 책을 찾아 내
          목록에 추가해보세요.
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
              {categories.map((category) => (
                <SelectItem key={category} value={category}>
                  {category === "all" ? "모든 카테고리" : category}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Select value={sortBy} onValueChange={setSortBy}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="정렬 기준" />
            </SelectTrigger>
            <SelectContent>
              {sortOptions.map((option) => (
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
          <p className="text-muted-foreground">
            검색 조건에 맞는 책이 없습니다.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredBooks.map((book) => (
            <Card
              key={book.id}
              className="h-full flex flex-col cursor-pointer hover:shadow-lg transition-shadow"
            >
              <CardHeader onClick={() => onBookClick(book.id)}>
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <CardTitle className="line-clamp-2">{book.title}</CardTitle>
                    <CardDescription>{book.authors.join(", ")}</CardDescription>
                    <div className="flex items-center gap-2 mt-2">
                      <Badge variant="secondary">{book.categoryName}</Badge>
                      {userBookStatus[book.id] && (
                        <Badge
                          className={getStatusColor(userBookStatus[book.id])}
                        >
                          {userBookStatus[book.id]}
                        </Badge>
                      )}
                    </div>
                  </div>
                  <ImageWithFallback
                    src={
                      book.imageUrl ||
                      `https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=80&h=120&fit=crop&crop=center&sig=${book.id}`
                    }
                    alt={book.title}
                    className="w-16 h-24 object-cover rounded ml-4"
                  />
                </div>
              </CardHeader>
              <CardContent
                className="flex-1 flex flex-col"
                onClick={() => onBookClick(book.id)}
              >
                <div className="flex-1 space-y-3">
                  <div className="flex items-center gap-2 mt-2">
                    <Badge
                      className={
                        getReadStateText(book.readState)
                          ? getStatusColor(getReadStateText(book.readState))
                          : "hidden"
                      }
                    >
                      {getReadStateText(book.readState)}
                    </Badge>
                  </div>

                  <div className="flex items-center justify-between text-sm text-muted-foreground">
                    <span>{book.totalPage}쪽</span>
                    <span>{new Date(book.publishedDate).getFullYear()}년</span>
                  </div>

                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-1">
                      {renderStars(book.avgRate)}
                      <span className="text-sm ml-2">
                        {book.avgRate.toFixed(1)}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {book.publisher}
                      </span>
                    </div>
                  </div>
                </div>

                <div
                  className="mt-4 pt-4 border-t"
                  onClick={(e) => e.stopPropagation()}
                >
                  {userBookStatus[book.id] ? (
                    <div className="flex gap-2">
                      <Select
                        value={userBookStatus[book.id]}
                        onValueChange={(status) =>
                          addToMyBooks(book.id, status)
                        }
                      >
                        <SelectTrigger className="flex-1">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="읽고 싶은 책">
                            읽고 싶은 책
                          </SelectItem>
                          <SelectItem value="읽고 있는 책">
                            읽고 있는 책
                          </SelectItem>
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
                        onClick={() => addToMyBooks(book.id, "읽고 싶은 책")}
                      >
                        <Plus className="h-4 w-4 mr-2" />내 목록에 추가
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => addToMyBooks(book.id, "읽고 싶은 책")}
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

      {/* 페이징 버튼 */}
      {totalPages > 1 && (
        <div className="mt-8 flex justify-center items-center space-x-2">
          <Button
            variant="outline"
            disabled={currentPage === 0}
            onClick={() => loadBooks(currentPage - 1)}
          >
            이전
          </Button>

          <div className="flex space-x-1">
            {Array.from({ length: Math.min(totalPages, 5) }, (_, index) => {
              let pageNum;
              if (totalPages <= 5) {
                pageNum = index;
              } else if (currentPage <= 2) {
                pageNum = index;
              } else if (currentPage >= totalPages - 3) {
                pageNum = totalPages - 5 + index;
              } else {
                pageNum = currentPage - 2 + index;
              }

              return (
                <Button
                  key={pageNum}
                  variant={currentPage === pageNum ? "default" : "outline"}
                  size="sm"
                  onClick={() => loadBooks(pageNum)}
                >
                  {pageNum + 1}
                </Button>
              );
            })}
          </div>

          <Button
            variant="outline"
            disabled={currentPage === totalPages - 1}
            onClick={() => loadBooks(currentPage + 1)}
          >
            다음
          </Button>
        </div>
      )}

      {/* 페이지 정보 */}
      <div className="mt-4 text-center text-sm text-muted-foreground">
        {totalElements > 0 && (
          <p>
            페이지 {currentPage + 1} / {totalPages}
            (총 {totalElements}개 중 {currentPage * 9 + 1}-
            {Math.min((currentPage + 1) * 9, totalElements)}개 표시)
          </p>
        )}
      </div>
    </div>
  );
}
