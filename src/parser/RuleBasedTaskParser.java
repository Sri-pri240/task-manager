package parser;

import model.ParsedTask;
import java.time.LocalDate;

public class RuleBasedTaskParser implements TaskParser {
    @Override
    public ParsedTask parse(String text, String today) {
        if (text == null || text.isBlank()) {
            return new ParsedTask("", "", null, null);
        }
        String lower = text.toLowerCase();
        String dueDate = null;
        String recurrence = null;
        LocalDate base = LocalDate.parse(today);

        if (lower.contains("tomorrow")) {
            dueDate = base.plusDays(1).toString();
        } else if (lower.contains("1st")) {
            dueDate = base.withDayOfMonth(1).plusMonths(1).toString();
        }

        if (lower.contains("every month")) {
            recurrence = "MONTHLY";
        } else if (lower.contains("every week")) {
            recurrence = "WEEKLY";
        }
        return new ParsedTask(text.trim(), "Task: " + text.trim(), dueDate, recurrence);
    }
}
