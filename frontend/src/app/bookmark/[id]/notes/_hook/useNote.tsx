"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { useSearchParams } from "next/navigation";


const NEXT_PUBLIC_API_BASE_URL = "http://localhost:8080";

type Note = {
    id: number;
    createDate: string;
    modifyDate: string;
    title: string;
    content: string;
    page: string;
};

type BookInfo = {
    title: string;
    imageUrl: string;
};

export const useNote = (bookmarkId: string) => {
    const [note, setNote] = useState<Note | null>(null);
    const [bookInfo, setBookInfo] = useState<BookInfo>({ title: "", imageUrl: "" });

    useEffect(() => {
        const fetchNote = async () => {
            try {
                const res = await fetch(`${NEXT_PUBLIC_API_BASE_URL}/bookmarks/${bookmarkId}/notes`, {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    // credentials: "include",
                });

                if (!res.ok) {
                    throw new Error("서버 응답 실패");
                }

                const data = await res.json();
                setNote(data.notes);

                setBookInfo({
                    title: data.title,
                    imageUrl: data.imageUrl || "",
                });

                console.log(data);
            } catch (error) {
                console.error("API 요청 실패", error);
            }
        };

        fetchNote();
    }, []);

    return {
        note,
        bookInfo
    };
}