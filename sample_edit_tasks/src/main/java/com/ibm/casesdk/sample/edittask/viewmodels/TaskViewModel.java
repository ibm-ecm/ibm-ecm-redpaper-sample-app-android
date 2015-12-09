package com.ibm.casesdk.sample.edittask.viewmodels;

import android.support.annotation.IntDef;

import com.ibm.casemanagersdk.sdk.interfaces.ICMRole;
import com.ibm.casemanagersdk.sdk.interfaces.ICMSolution;
import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by stelian on 26/10/2015.
 */
public interface TaskViewModel {

    int INITIATE_SESSION = 0;
    int FIND_SOLUTION = 1;
    int LOAD_SOLUTION_DETAILS = 2;
    int FIND_ROLE = 3;
    int FIND_NEARBY_TASKS = 4;
    int LOAD_TASK_DETAILS = 5;
    int LOCK_TASK = 6;
    int UNLOCK_TASK = 7;
    int TASK_ACTION = 8;

    /**
     * Define valid operations for the {@link TaskViewModel}
     */
    @IntDef({INITIATE_SESSION, FIND_NEARBY_TASKS, FIND_ROLE, FIND_SOLUTION, LOAD_SOLUTION_DETAILS,
            LOAD_TASK_DETAILS, LOCK_TASK, UNLOCK_TASK, TASK_ACTION})
    @Retention(RetentionPolicy.SOURCE)
    @interface Operations {
    }

    /**
     * Notify when a session has been initiated;
     */
    void onSessionInitiated();

    /**
     * Notify when a solution has been found.
     */
    void onSolutionFound(final ICMSolution solution);

    /**
     * Notify when a solution's details have been loaded.
     */
    void onSolutionDetailsLoaded(final ICMSolution solution);


    /**
     * Notify when a role has been found.
     */
    void onRoleFound(final ICMRole role);


    /**
     * Notify when tasks have been found.
     *
     * @param icmTasks
     */
    void onTasksFound(final List<ICMTask> icmTasks);

    /**
     * Notify when the details for a {@link ICMTask} have been loaded.
     *
     * @param task
     */
    void onTaskDetailsLoaded(final ICMTask task);

    /**
     * Notify when a task has been locked.
     *
     * @param task
     */
    void onTaskLocked(final ICMTask task);

    /**
     * Notify when a task has been unlocked.
     *
     * @param task
     */
    void onTaskUnlocked(final ICMTask task);

    /**
     * Notify when a task's properties have been updated.
     *
     * @param task
     */
    void onTaskUpdated(final ICMTask task);

    /**
     * Notify when an action has been performed on a task.
     *
     * @param task
     * @param actionIndex is the index for the {@code task.getResponses()} list
     */
    void onTaskActionPerformed(final ICMTask task, final int actionIndex);

    /**
     * Notify when an operation fails.
     *
     * @param operationCode the code of the operation that failed
     * @param error         the error message
     */
    void onError(@Operations final int operationCode, String error);
}
