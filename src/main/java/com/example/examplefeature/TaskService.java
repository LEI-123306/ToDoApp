package com.example.examplefeature;

import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public void createTask(String description, @Nullable LocalDate dueDate) {
        if ("fail".equals(description)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        var task = new Task(description, Instant.now());
        task.setDueDate(dueDate);
        taskRepository.saveAndFlush(task);
    }

    @Transactional(readOnly = true)
    public List<Task> list(Pageable pageable) {
        return taskRepository.findAllBy(pageable).toList();
    }

    @Transactional(readOnly = true)
    public String exportTasksToIcs() throws IOException {
        List<Task> tasks = taskRepository.findAll();
        Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Tasks App//iCal4j 1.0//EN"));
        calendar.getProperties().add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
        calendar.getProperties().add(net.fortuna.ical4j.model.property.CalScale.GREGORIAN);

        for (Task task : tasks) {
            VEvent vEvent = new VEvent();

            // Set summary (description)
            vEvent.getProperties().add(new Summary(task.getDescription()));

            // Set start date to creation date
            vEvent.getProperties().add(
                    new net.fortuna.ical4j.model.property.DtStart(new DateTime(Date.from(task.getCreationDate()))));

            // Set due date if present, or use creation date + 1 day as end date
            if (task.getDueDate() != null) {
                java.util.Date dueDate = java.util.Date
                        .from(task.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                vEvent.getProperties().add(new net.fortuna.ical4j.model.property.DtEnd(new DateTime(dueDate)));
            } else {
                // Set end date to creation date + 1 day if no due date
                java.util.Date endDate = java.util.Date
                        .from(task.getCreationDate().plusSeconds(86400)); // Add 24 hours
                vEvent.getProperties().add(new net.fortuna.ical4j.model.property.DtEnd(new DateTime(endDate)));
            }

            // Generate unique ID
            var uid = new net.fortuna.ical4j.model.property.Uid(task.getId().toString());
            vEvent.getProperties().add(uid);

            calendar.getComponents().add(vEvent);
        }

        StringWriter writer = new StringWriter();
        net.fortuna.ical4j.data.CalendarOutputter outputter = new net.fortuna.ical4j.data.CalendarOutputter();
        outputter.output(calendar, writer);
        return writer.toString();
    }
}
