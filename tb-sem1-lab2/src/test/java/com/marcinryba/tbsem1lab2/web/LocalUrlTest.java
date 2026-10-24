package com.marcinryba.tbsem1lab2.web;

import com.marcinryba.tbsem1lab2.config.LocalUrl;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalUrlTest {

    @Test
    void acceptsLocalPaths() {
        assertThat(LocalUrl.isLocal("/")).isTrue();
        assertThat(LocalUrl.isLocal("/Account/CheckLogin")).isTrue();
        assertThat(LocalUrl.isLocal("/a?b=c")).isTrue();
    }

    @Test
    void rejectsExternalAndMalformedTargets() {
        assertThat(LocalUrl.isLocal(null)).isFalse();
        assertThat(LocalUrl.isLocal("")).isFalse();
        assertThat(LocalUrl.isLocal("https://evil.example")).isFalse();
        assertThat(LocalUrl.isLocal("//evil.example")).isFalse();
        assertThat(LocalUrl.isLocal("/\\evil.example")).isFalse();
        assertThat(LocalUrl.isLocal("relative/path")).isFalse();
        assertThat(LocalUrl.isLocal("/a\r\nSet-Cookie: x=y")).isFalse();
    }
}
