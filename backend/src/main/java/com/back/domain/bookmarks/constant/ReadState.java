package com.back.domain.bookmarks.constant;

public enum ReadState {
    BEFORE_READING, READING, FINISHED;

    public ReadState getState(String state) {
        if(state.equals("BEFORE_READING")){ return BEFORE_READING; }
        if(state.equals("READING")){ return READING; }
        if(state.equals("FINISHED")){ return FINISHED; }
        return null;
    }
}
