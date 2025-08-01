import { apiFetch } from "@/lib/apiFetch";

export const getBookmarks = async ({ page, size, sort, category, readState, keyword }) => {
    const params = new URLSearchParams({
        page: String(page),
        size: String(size),
        sort: String(sort),
    });
    if(category && category !== 'all') params.append('category', category);
    if(readState && readState !== 'all') params.append('readState', readState);
    if(keyword) params.append('keyword', keyword);
    return apiFetch(`/api/bookmarks?${params.toString()}`);
};

export const getBookmark = async (id) => {
    return apiFetch(`/api/bookmarks/${id}`);
};

export const createBookmark = async (data) => {
    return apiFetch('/api/bookmarks', {
        method: 'POST',
        body: JSON.stringify(data),
    });
};

export const updateBookmark = async (id, data) => {
    return apiFetch(`/api/bookmarks/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
};

export const deleteBookmark = async (id) => {
    return apiFetch(`/api/bookmarks/${id}`, {
        method: 'DELETE',
    });
};

export const getBookmarkReadStates = async () => {
    return apiFetch('/api/bookmarks/read-states');
};
