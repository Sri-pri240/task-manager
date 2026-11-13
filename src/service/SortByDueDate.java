package service;

import model.Task;
import java.util.Comparator;

public class SortByDueDate implements SortStrategy {
    @Override
    public Comparator<Task> getComparator() {
        return Comparator.comparing(Task::getDueDate, Comparator.nullsLast(String::compareTo));
    }
}
