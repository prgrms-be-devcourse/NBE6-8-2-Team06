const API_URL = 'http://localhost:8080/api';

const handleResponse = async (response) => {
    if (!response.ok || !response.created) {
        const error = await response.json();
        throw new Error(error.msg || 'API 요청에 실패하였습니다.');
    }
    return response.json();
}

export const getBookmarks = async ({ page, size, category, readState, keyword }) => {
    const params = new URLSearchParams({
        page,
        size
    });
    if(category && category !== 'all') params.append('category', category);
    if(readState && readState !== 'all') params.append('readState', readState);
    if(keyword) params.append('keyword', keyword);
    const response = await fetch(`${API_URL}/bookmarks?${params.toString()}`);
    return handleResponse(response);
}

export const createBookmark = async (data) => {
    const response = await fetch(`${API_URL}/bookmarks`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    })
    return handleResponse(response);
}

export const updateBookmark = async (id, data) => {
    const response = await fetch(`${API_URL}/bookmarks/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    })
    return handleResponse(response);
}

export const deleteBookmark = async (id) => {
    const response = await fetch(`${API_URL}/bookmarks/${id}`, {
        method: 'DELETE',
    })
    return handleResponse(response);
}

export const getBookmarkReadStates = async () => {
    const response = await fetch(`${API_URL}/bookmarks/read-states`);
    return handleResponse(response);
}