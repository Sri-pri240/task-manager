package service;

import model.Task;

public class KeywordSearchFilter implements TaskFilter {
    private final String keyword;

    public KeywordSearchFilter(String keyword) {
        this.keyword = keyword == null ? "" : keyword.toLowerCase();
    }

    @Override
    public boolean matches(Task task) {
        return task.getTitle().toLowerCase().contains(keyword)
            || task.getDescription().toLowerCase().contains(keyword);
    }
}

