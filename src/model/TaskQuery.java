package model;

import service.SortStrategy;
import service.TaskFilter;
import java.util.ArrayList;
import java.util.List;

public class TaskQuery {
    private final List<TaskFilter> filters;
    private final SortStrategy sortStrategy;
    private final int page;
    private final int pageSize;

    private TaskQuery(Builder builder) {
        this.filters = builder.filters;
        this.sortStrategy = builder.sortStrategy;
        this.page = builder.page;
        this.pageSize = builder.pageSize;
    }

    public List<TaskFilter> getFilters() { return filters; }
    public SortStrategy getSortStrategy() { return sortStrategy; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }

    public static class Builder {
        private final List<TaskFilter> filters = new ArrayList<>();
        private SortStrategy sortStrategy;
        private int page = 1;
        private int pageSize = 10;

        public Builder addFilter(TaskFilter filter) {
            if (filter != null) this.filters.add(filter);
            return this;
        }

        public Builder sortBy(SortStrategy sortStrategy) {
            this.sortStrategy = sortStrategy;
            return this;
        }

        public Builder page(int page) {
            this.page = Math.max(1, page);
            return this;
        }

        public Builder pageSize(int pageSize) {
            this.pageSize = Math.max(1, pageSize);
            return this;
        }

        public TaskQuery build() {
            return new TaskQuery(this);
        }
    }
}
