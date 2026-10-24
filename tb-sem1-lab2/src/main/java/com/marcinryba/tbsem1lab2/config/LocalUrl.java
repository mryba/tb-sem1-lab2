package com.marcinryba.tbsem1lab2.config;

public final class LocalUrl {

    private LocalUrl() {
    }

    public static boolean isLocal(String url) {
        if (url == null || url.isEmpty() || url.charAt(0) != '/') {
            return false;
        }
        if (url.length() > 1 && (url.charAt(1) == '/' || url.charAt(1) == '\\')) {
            return false;
        }
        return url.chars().noneMatch(Character::isISOControl);
    }
}
