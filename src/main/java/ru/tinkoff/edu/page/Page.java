package ru.tinkoff.edu.page;

import java.util.List;

public class Page {
    private final String url;
    private final String content;
    private final List<String> relativeLinks;

    public Page(String url, String content, List<String> relativeLinks) {
        this.url = url;
        this.content = content;
        this.relativeLinks = relativeLinks;
    }

    public String getContent() {
        return content;
    }

    public List<String> getRelativeLinks() {
        return relativeLinks;
    }

    public String getUrl() {
        return url;
    }
}
