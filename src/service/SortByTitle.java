package service;

import model.Task;
import java.util.Comparator;

public class SortByTitle implements SortStrategy {
    @Override
    public Comparator<Task> getComparator() {
        return Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
    }
}
