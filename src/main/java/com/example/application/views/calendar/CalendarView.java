package com.example.application.views.calendar;

import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.Entry;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Route(value = "calendar", layout = com.example.base.ui.MainLayout.class)
@PageTitle("Calendar | Todo App")
class CalendarView extends Main {

    private final TaskService taskService;

    CalendarView(TaskService taskService) {
        this.taskService = taskService;
        var calendar = new FullCalendar();
        var entryProvider = calendar.getEntryProvider().asInMemory();

        // Load tasks and add to calendar
        List<Task> tasks = taskService.findAll();
        for (Task task : tasks) {
            if (task.getDueDate() != null) {
                Entry entry = new Entry();
                entry.setTitle(task.getDescription());
                var start = task.getDueDate().atStartOfDay();
                entry.setStart(start);
                entry.setEnd(start.plusDays(1));
                entry.setAllDay(true);
                if (task.isDone()) {
                    entry.setColor("#28a745"); // Green for done
                } else {
                    entry.setColor("#6c757d"); // Gray for pending
                }
                entryProvider.addEntries(entry);
            }
        }
        entryProvider.refreshAll();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
        add(calendar);
    }
}
