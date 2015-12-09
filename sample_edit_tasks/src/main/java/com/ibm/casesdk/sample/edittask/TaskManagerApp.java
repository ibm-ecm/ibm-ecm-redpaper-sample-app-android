package com.ibm.casesdk.sample.edittask;

import android.app.Application;

import com.ibm.casesdk.sample.edittask.controllers.TaskController;

/**
 * Created by stelian on 26/10/2015.
 */
public class TaskManagerApp extends Application {

    // Save the TaskController at Application level so it can be accessible in multiple parts of the app
    private TaskController taskController;

    public TaskController getTaskController() {
        return taskController;
    }

    public void setTaskController(TaskController taskController) {
        this.taskController = taskController;
    }
}
