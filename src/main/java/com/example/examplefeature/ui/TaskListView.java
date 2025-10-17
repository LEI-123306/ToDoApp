package com.example.examplefeature.ui;

import static com.vaadin.flow.spring.data.VaadinSpringDataHelpers.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Optional;

import com.example.base.ui.component.ViewToolbar;
import com.example.examplefeature.Task;
import com.example.examplefeature.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Route("")
@PageTitle("Task List")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Task List")
class TaskListView extends Main {

    private final TaskService taskService;

    private final TextField description;
    private final DatePicker dueDate;
    private final Button createBtn;
    private final Grid<Task> taskGrid;

    TaskListView(TaskService taskService) {
        this.taskService = taskService;

        // Task description input
        description = new TextField();
        description.setPlaceholder("What do you want to do?");
        description.setAriaLabel("Task description");
        description.setMaxLength(Task.DESCRIPTION_MAX_LENGTH);
        description.setMinWidth("20em");

        // Due date input
        dueDate = new DatePicker();
        dueDate.setPlaceholder("Due date");
        dueDate.setAriaLabel("Due date");

        // Create task button
        createBtn = new Button("Create", event -> createTask());
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Sort by priority button
        Button sortBtn = new Button("Sort by Priority", event -> sortByPriority());
        sortBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // Date formatters
        var dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(getLocale())
                .withZone(ZoneId.systemDefault());
        var dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(getLocale());

        // Task grid
        taskGrid = new Grid<>();
        taskGrid.setItems(query -> taskService.list(toSpringPageRequest(query)).stream());
        taskGrid.addColumn(Task::getDescription).setHeader("Description");
        taskGrid.addColumn(task -> Optional.ofNullable(task.getDueDate()).map(dateFormatter::format).orElse("Never"))
                .setHeader("Due Date");
        taskGrid.addColumn(task -> dateTimeFormatter.format(task.getCreationDate())).setHeader("Creation Date");

        // Done checkbox column
        taskGrid.addComponentColumn(task -> {
            Checkbox checkbox = new Checkbox(task.isDone());
            checkbox.addValueChangeListener(event -> {
                taskService.updateTaskDone(task.getId(), event.getValue());
                Notification.show("Task updated", 1000, Notification.Position.BOTTOM_END)
                        .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
            });
            return checkbox;
        }).setHeader("Done");

        // Priority badge column
        taskGrid.addComponentColumn(task -> {
            Span priorityBadge = new Span(task.getPriority().getDisplayName());
            priorityBadge.addClassNames(
                    LumoUtility.FontWeight.BOLD,
                    LumoUtility.BorderRadius.MEDIUM,
                    LumoUtility.Padding.Horizontal.SMALL,
                    LumoUtility.TextAlignment.CENTER
            );
            switch (task.getPriority()) {
                case HIGH -> priorityBadge.addClassNames("priority-high");
                case MEDIUM -> priorityBadge.addClassNames("priority-medium");
                case LOW -> priorityBadge.addClassNames("priority-low");
            }
            return priorityBadge;
        }).setHeader("Priority");

        // Row background color
        taskGrid.setClassNameGenerator(task -> "custom-row-" + task.getColor().substring(1));
        taskGrid.setSizeFull();

        setSizeFull();
        addClassNames(LumoUtility.BoxSizing.BORDER, LumoUtility.Display.FLEX, LumoUtility.FlexDirection.COLUMN,
                LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);

        // Toolbar
        var exportBtn = new Button(VaadinIcon.DOWNLOAD_ALT.create(), event -> exportTasks());
        exportBtn.setText("Export to Calendar");
        exportBtn.setAriaLabel("Export tasks to calendar");
        exportBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add(new ViewToolbar("Task List", ViewToolbar.group(description, dueDate, createBtn, sortBtn, exportBtn)));
        add(taskGrid);
    }

    private void createTask() {
        taskService.createTask(description.getValue(), dueDate.getValue());
        taskGrid.getDataProvider().refreshAll();
        description.clear();
        dueDate.clear();
        Notification.show("Task added", 3000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void sortByPriority() {
        taskGrid.setItems(query -> taskService.listSortedByPriority(toSpringPageRequest(query)).stream());
        Notification.show("Tasks sorted by priority", 2000, Notification.Position.BOTTOM_END)
                .addThemeVariants(NotificationVariant.LUMO_PRIMARY);
    }

    private void exportTasks() {
        try {
            String icsContent = taskService.exportTasksToIcs();

            getUI().ifPresent(ui -> {
                String js = """
                const blob = new Blob([new TextEncoder().encode($0)], { type: 'text/calendar;charset=utf-8' });
                const a = document.createElement('a');
                a.href = URL.createObjectURL(blob);
                a.download = 'tasks.ics';
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
            """;
                ui.getPage().executeJs(js, icsContent);
            });

            Notification.show("Tasks exported to calendar", 3000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception e) {
            Notification.show("Error exporting tasks: " + e.getMessage(), 5000, Notification.Position.BOTTOM_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

}
