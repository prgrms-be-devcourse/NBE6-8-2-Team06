const API_URL = 'http://localhost:8080/api';

/**
 * 
 * @param {string} URL - api 경로
 * @param {object} options - method, body 등 
 * @returns {Promise<any}
 */
const apiRequest = async (URL, options = {}) => {
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
    }
    const mergedOptions = {
    ...defaultOptions,
    ...options,
    headers: {
        ...defaultOptions.headers,
        ...options.headers,
        },
    };

    const response = await fetch(`${API_URL}${URL}`, mergedOptions);
    if(!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.msg || 'API 요청에 실패했습니다.');
    }

    if(response.status ===204 || response.headers.get("content-length") === "0") {
        return null;
    }
    return response.json();
};

export const getBookmarks = async ({ page, size, category, readState, keyword }) => {
    const params = new URLSearchParams({
        page,
        size
    });
    if(category && category !== 'all') params.append('category', category);
    if(readState && readState !== 'all') params.append('readState', readState);
    if(keyword) params.append('keyword', keyword);
    return apiRequest(`/bookmarks?${params.toString()}`);
}

export const createBookmark = async (data) => {
    return apiRequest('/bookmarks', {
        method: 'POST',
        body: JSON.stringify(data),
    });
}

export const updateBookmark = async (id, data) => {
    return apiRequest(`/bookmarks/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
}

export const deleteBookmark = async (id) => {
    return apiRequest(`/bookmarks/${id}`, {
        method: 'DELETE',
    });
}

export const getBookmarkReadStates = async () => {
    return apiRequest('/bookmarks/read-states');
}