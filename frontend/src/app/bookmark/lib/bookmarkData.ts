export type Bookmark = {
    id: number;
    memberId: number;
    bookId: number;
    book: BookmarkBookDetail;
    readState: string;
    readPage: number;
    readingRate: number;
    date: string;
}
export type BookmarkBookDetail = {
    id: number;
    isbn13: string;
    title: string;
    imageUrl: string;
    publisher: string;
    totalPage: number;
    avgRate: number;
    categoryName: string;
    publishDate: string;
    author: string[];
}

export type BookmarkDetail = {
    bookmark: Bookmark;
    createdAt: string;
    startReadDate: string;
    endReadDate: string;
    readingDuration: number;
    review: BookmarkReviewDetail;
    notes: BookmarkNoteDetail[];
}

export type BookmarkReviewDetail = {
    id: number;
    content: string;
    rate: number;
    date: string;
}

export type BookmarkNoteDetail = {
    id: number;
    title: string;
    content: string;
    createDate: string;
    modifiedDate: string;
}

export type BookmarkPage = {
    content: Bookmark[];
    pageNumber: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
    isLast: boolean;
}

export type BookmarkCreate = {
    bookId: number;
}

export type BookmarkUpdate = {
    readState: string;
    startReadDate: string;
    endReadDate: string;
    readPage: number;
}
