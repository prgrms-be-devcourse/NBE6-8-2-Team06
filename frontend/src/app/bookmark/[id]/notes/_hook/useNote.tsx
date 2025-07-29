"use client";

import { useState, useEffect } from "react";

const NEXT_PUBLIC_API_BASE_URL = "http://localhost:8080";

// 요청 DTO
export interface NoteRequest {
    title: string;
    content: string;
    page?: string | null; // optional
}

// 응답 DTO
export interface NoteResponse {
    id: number;
    title: string;
    content: string;
    page?: string | null;
    createDate: string; // ISO 형식 날짜 문자열
    modifyDate: string;
}

// 책 정보(수정 필요)
type BookInfo = {
    title: string;
    imageUrl: string;
};

// 경로 수정 필요
export function useNote(bookmarkId: number) {
    const [notes, setNotes] = useState<NoteResponse[]>([]);
    const [bookInfo, setBookInfo] = useState<BookInfo>({ title: "", imageUrl: "" });

    // 1. 전체 노트 가져오기
    const fetchNotes = async () => {
        const res = await fetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/1/notes`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
            // credentials: "include",
        });

        const data = await res.json();
        setNotes(data.notes);
        setBookInfo({
            title: data.title,
            imageUrl: data.imageUrl || "",
        });
        console.log(data);
    };

    // 2. 노트 추가
    const addNote = async (newNote: Partial<NoteRequest>) => {
        await fetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/1/notes`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(newNote),
        });

        await fetchNotes(); // 🔁 추가 후 리스트 다시 불러오기
    };

    // 3. 노트 수정
    const updateNote = async (noteId: number, updatedNote: Partial<NoteRequest>) => {
        await fetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/1/notes/${noteId}`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(updatedNote),
        });

        await fetchNotes(); // 🔁 수정 후 리스트 다시 불러오기
    };

    // 4. 노트 삭제
    const deleteNote = async (noteId: number) => {
        await fetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/1/notes/${noteId}`, {
            method: "DELETE",
        });

        await fetchNotes(); // 🔁 삭제 후 리스트 다시 불러오기
    };

    useEffect(() => {
        fetchNotes(); // 페이지 최초 진입 시 로딩
    }, [bookmarkId]);

    return {
        notes,
        bookInfo,
        addNote,
        updateNote,
        deleteNote,
    };
}