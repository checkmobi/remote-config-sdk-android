package com.checkmobi.sdk.model;

public class IndexedValue {
    private int index;
    private long value;
    
    public IndexedValue(int index, long value) {
        this.index = index;
        this.value = value;
    }
    
    public int getIndex() {
        return index;
    }
    
    public long getValue() {
        return value;
    }
}
