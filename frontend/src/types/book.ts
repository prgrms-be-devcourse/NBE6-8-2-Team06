export enum ReadState {
  NOT_READ = 'NOT_READ',
  READING = 'READING',
  READ = 'READ'
}

export interface BookSearchDto {
  id: number;
  title: string;
  imageUrl: string;
  publisher: string;
  isbn13: string;
  totalPage: number;
  publishedDate: string; // LocalDateTime from backend will be serialized as string
  avgRate: number;
  categoryName: string;
  authors: string[];
  readState: ReadState;
}

// API 공통 응답 구조
interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

// PageResponseDto 타입 정의 (백엔드와 일치)
interface PageResponseDto<T> {
  data: T[];
  pageNumber: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  isLast: boolean;
}

export interface BooksResponse {
  books: BookSearchDto[];
  pageInfo: {
    currentPage: number;
    totalPages: number;
    totalElements: number;
    isLast: boolean;
  };
}

// 공통 응답 처리 함수
async function processApiResponse(response: ApiResponse<PageResponseDto<BookSearchDto>>): Promise<BooksResponse> {
  console.log('📦 백엔드 응답 원본:', response);
  console.log('📊 응답 타입:', typeof response);
  
  if (response) {
    console.log('📋 응답 키들:', Object.keys(response));
    console.log('✅ resultCode:', response.resultCode);
    console.log('💬 msg:', response.msg);
  }
  
  // API 공통 응답에서 data 필드 추출
  if (response && typeof response === 'object' && 'data' in response) {
    const pageData = response.data;
    console.log('📄 PageResponseDto 데이터:', pageData);
    
    if (pageData && typeof pageData === 'object' && 'data' in pageData) {
      console.log('📚 책 배열:', pageData.data);
      console.log('📊 총 원소 개수:', pageData.totalElements);
      console.log('📄 총 페이지 수:', pageData.totalPages);
      console.log('🔢 현재 페이지:', pageData.pageNumber);
      console.log('📏 페이지 크기:', pageData.pageSize);
      console.log('🔚 마지막 페이지인가?', pageData.isLast);
      
      if (Array.isArray(pageData.data)) {
        console.log('✅ 책 배열 추출 성공 - 첫 번째 책:', pageData.data[0]);
        console.log('📊 추출된 책 개수:', pageData.data.length);
        
        return {
          books: pageData.data,
          pageInfo: {
            currentPage: pageData.pageNumber,
            totalPages: pageData.totalPages,
            totalElements: pageData.totalElements,
            isLast: pageData.isLast
          }
        };
      }
    }
  }
  
  console.warn('⚠️ 예상하지 못한 응답 구조:', response);
  return {
    books: [],
    pageInfo: {
      currentPage: 0,
      totalPages: 0,
      totalElements: 0,
      isLast: true
    }
  };
}

export async function fetchBooks(page: number = 0, size: number = 9): Promise<BooksResponse> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    console.log(`🔍 API 호출 시작: /api/books?page=${page}&size=${size}`);
    const response = await apiFetch<ApiResponse<PageResponseDto<BookSearchDto>>>(`/api/books?page=${page}&size=${size}`);
    return await processApiResponse(response);
  } catch (error) {
    console.error('❌ API 호출 에러:', error);
    throw error;
  }
}

export async function searchBooks(query: string, page: number = 0, size: number = 9): Promise<BooksResponse> {
  const { apiFetch } = await import('@/lib/apiFetch');
  
  try {
    console.log(`🔍 검색 API 호출 시작: /api/books/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`);
    const response = await apiFetch<ApiResponse<BookSearchDto[]>>(`/api/books/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`);
    
    console.log('📦 검색 API 응답 원본:', response);
    console.log('📊 응답 타입:', typeof response);
    
    if (response) {
      console.log('📋 응답 키들:', Object.keys(response));
      console.log('✅ resultCode:', response.resultCode);
      console.log('💬 msg:', response.msg);
    }
    
    // 검색 API는 data 필드에 직접 배열이 들어있음
    if (response && typeof response === 'object' && 'data' in response) {
      const booksArray = response.data;
      console.log('📚 검색 결과 배열:', booksArray);
      
      if (Array.isArray(booksArray)) {
        console.log('✅ 검색 결과 추출 성공 - 첫 번째 책:', booksArray[0]);
        console.log('📊 검색된 책 개수:', booksArray.length);
        
        // 검색 API는 페이징 정보가 없으므로 계산해서 생성
        const totalElements = booksArray.length;
        const totalPages = Math.ceil(totalElements / size);
        const startIndex = page * size;
        const endIndex = startIndex + size;
        const pageBooks = booksArray.slice(startIndex, endIndex);
        
        return {
          books: pageBooks,
          pageInfo: {
            currentPage: page,
            totalPages: totalPages,
            totalElements: totalElements,
            isLast: page >= totalPages - 1
          }
        };
      }
    }
    
    console.warn('⚠️ 예상하지 못한 검색 응답 구조:', response);
    return {
      books: [],
      pageInfo: {
        currentPage: 0,
        totalPages: 0,
        totalElements: 0,
        isLast: true
      }
    };
  } catch (error) {
    console.error('❌ 검색 API 호출 에러:', error);
    throw error;
  }
}