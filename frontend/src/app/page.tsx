"use client";

import React from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { BookOpen, Plus, Star, TrendingUp } from 'lucide-react';

export default function HomePage() {
  const isLoggedIn = true; // For demonstration purposes

  // 가상의 데이터
  const recentBooks = [
    {
      id: 1,
      title: "클린 코드",
      author: "로버트 C. 마틴",
      rating: 4.5,
      status: "읽은 책"
    },
    {
      id: 2,
      title: "리팩터링",
      author: "마틴 파울러",
      rating: 4.8,
      status: "읽고 있는 책"
    },
    {
      id: 3,
      title: "디자인 패턴",
      author: "GoF",
      rating: 0,
      status: "읽고 싶은 책"
    }
  ];

  const stats = {
    totalBooks: 25,
    readBooks: 18,
    wantToReadBooks: 7,
    averageRating: 4.2
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* 헤로 섹션 */}
      <div className="text-center mb-12">
        <h1 className="text-4xl mb-4">나만의 독서 기록</h1>
        <p className="text-xl text-muted-foreground mb-8">
          다양한 책을 탐색하고, 읽은 책들을 기록하며, 리뷰를 남겨보세요
        </p>
        
        {isLoggedIn ? (
          <div className="flex justify-center space-x-4">
            <Link href="/my-books" passHref>
              <Button size="lg">
                <BookOpen className="mr-2 h-5 w-5" />
                내 책 보기
              </Button>
            </Link>
            <Link href="/books/add" passHref>
              <Button size="lg" variant="outline">
                <Plus className="mr-2 h-5 w-5" />
                책 추가하기
              </Button>
            </Link>
          </div>
        ) : (
          <Link href="/login" passHref>
            <Button size="lg">
              시작하기
            </Button>
          </Link>
        )}
      </div>

      {isLoggedIn && (
        <>
          {/* 통계 카드 */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">총 책 수</CardTitle>
                <BookOpen className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{stats.totalBooks}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">읽은 책</CardTitle>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{stats.readBooks}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">읽고 싶은 책</CardTitle>
                <Plus className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{stats.wantToReadBooks}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm">평균 평점</CardTitle>
                <Star className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl">{stats.averageRating}</div>
              </CardContent>
            </Card>
          </div>

          {/* 최근 책들 */}
          <div className="mb-12">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl">최근 활동</h2>
              <Link href="/my-books" passHref>
                <Button variant="outline">
                  내 책 관리
                </Button>
              </Link>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {recentBooks.map((book) => (
                <Card key={book.id}>
                  <CardHeader>
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <CardTitle className="text-lg">{book.title}</CardTitle>
                        <CardDescription>{book.author}</CardDescription>
                      </div>
                      <Image
                        src={`https://images.unsplash.com/photo-1481627834876-b7833e8f5570?w=60&h=90&fit=crop&crop=center`}
                        alt={book.title}
                        width={60}
                        height={90}
                        className="w-12 h-16 object-cover rounded"
                      />
                    </div>
                  </CardHeader>
                  <CardContent>
                    <div className="flex justify-between items-center">
                      <span className="text-sm bg-secondary px-2 py-1 rounded">
                        {book.status}
                      </span>
                      {book.rating > 0 && (
                        <div className="flex items-center">
                          <Star className="h-4 w-4 fill-yellow-400 text-yellow-400 mr-1" />
                          <span className="text-sm">{book.rating}</span>
                        </div>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        </>
      )}

      {/* 기능 소개 섹션 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        <Card>
          <CardHeader>
            <BookOpen className="h-8 w-8 text-primary mb-2" />
            <CardTitle>책 관리</CardTitle>
            <CardDescription>
              읽은 책, 읽고 있는 책, 읽고 싶은 책을 체계적으로 관리하세요
            </CardDescription>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader>
            <Star className="h-8 w-8 text-primary mb-2" />
            <CardTitle>리뷰 작성</CardTitle>
            <CardDescription>
              읽은 책에 대한 생각과 평점을 기록하고 공유하세요
            </CardDescription>
          </CardHeader>
        </Card>

        <Card>
          <CardHeader>
            <TrendingUp className="h-8 w-8 text-primary mb-2" />
            <CardTitle>독서 통계</CardTitle>
            <CardDescription>
              독서 패턴을 분석하고 목표를 설정해보세요
            </CardDescription>
          </CardHeader>
        </Card>
      </div>
    </div>
  );
}
