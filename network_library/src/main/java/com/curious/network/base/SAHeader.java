package com.curious.network.base;

public class SAHeader extends SAKeyValue {
    private boolean isSetHeader = false;

    public SAHeader(String name, String value, boolean isSetHeader) {
        super(name, value);
        this.isSetHeader = isSetHeader;
    }

    public boolean isSetHeader() {
        return isSetHeader;
    }
}
