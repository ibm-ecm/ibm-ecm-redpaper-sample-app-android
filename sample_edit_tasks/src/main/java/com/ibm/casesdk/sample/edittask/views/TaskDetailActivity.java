package com.ibm.casesdk.sample.edittask.views;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ibm.casemanagersdk.sdk.interfaces.ICMProperty;
import com.ibm.casemanagersdk.sdk.interfaces.ICMRole;
import com.ibm.casemanagersdk.sdk.interfaces.ICMSolution;
import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;
import com.ibm.casesdk.sample.edittask.R;
import com.ibm.casesdk.sample.edittask.TaskManagerApp;
import com.ibm.casesdk.sample.edittask.controllers.TaskController;
import com.ibm.casesdk.sample.edittask.utils.Constants;
import com.ibm.casesdk.sample.edittask.utils.TaskDisplayHelper;
import com.ibm.casesdk.sample.edittask.utils.Utils;
import com.ibm.casesdk.sample.edittask.viewmodels.TaskViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by stelian on 27/10/2015.
 */
public class TaskDetailActivity extends BaseActivity implements TaskViewModel, TaskDisplayHelper.PropertyChangeListener {

    @Bind(R.id.task_status)
    ImageView mTaskStatus;

    @Bind(R.id.task_name)
    TextView mTaskName;

    @Bind(R.id.task_subject)
    TextView mTaskSubject;

    @Bind(R.id.task_properties_container)
    LinearLayout mPropertiesContainer;

    private ICMTask mTask;
    private TaskController mTaskController;
    private TaskDisplayHelper mTaskDisplayHelper;
    private int mChangedPropertiesCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        ButterKnife.bind(this);

        setupToolbar(true);

        //  get task from intent extras
        mTask = (ICMTask) getIntent().getSerializableExtra(Constants.EXTRA_TASK);

        // get task controller from app
        TaskController tempController = ((TaskManagerApp) getApplication()).getTaskController();
        mTaskController = new TaskController(this, this);
        mTaskController.duplicateContext(tempController);

        // if we don't have a task notify the user and finish the activity
        if (mTask == null) {
            Snackbar.make(Utils.getContentView(this),
                    getString(R.string.err_no_task_parameter),
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(getString(R.string.action_ok), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TaskDetailActivity.this.finish();
                        }
                    }).show();
        } else {
            mTaskDisplayHelper = new TaskDisplayHelper(this, this);
            mTaskController.loadTaskDetails(mTask);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showIndeterminateProgress();

        // show task name and subject before details are loaded
        if (mTask != null) {
            mTaskName.setText(mTask.getStepName());
            mTaskSubject.setText(mTask.getSubject());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // add a small delay to allow the indeterminate progress to disappear without glitches
        Utils.getContentView(this).postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        //  check if we need to show save option
                        menu.findItem(R.id.action_save).setVisible(mChangedPropertiesCounter > 0);

                        // determine if we need to show lock/unlock option
                        boolean locked = mTask.getLockedUser() != null;

                        menu.findItem(R.id.action_lock).setVisible(!locked);
                        menu.findItem(R.id.action_unlock).setVisible(locked);
                    }
                }, 200);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveTaskProperties();
                return true;
            case R.id.action_complete:
                showCompleteTaskConfirmation();
                return true;
            case R.id.action_lock:
                lockTask();
                return true;
            case R.id.action_unlock:
                unlockTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onSessionInitiated() {
        // Not needed
    }

    @Override
    public void onSolutionFound(ICMSolution solution) {
        // Not needed
    }

    @Override
    public void onSolutionDetailsLoaded(ICMSolution solution) {
        // Not needed
    }

    @Override
    public void onRoleFound(ICMRole role) {
        // Not needed
    }

    @Override
    public void onTasksFound(List<ICMTask> icmTasks) {
        // Not needed
    }

    @Override
    public void onTaskDetailsLoaded(ICMTask task) {
        hideIndeterminateProgress();

        mTask = task;

        // update status icon
        updateTaskStatusIcon();

        // generate layout
        mTaskDisplayHelper.displayTask(mTask, mPropertiesContainer, true);
    }

    @Override
    public void onTaskLocked(ICMTask task) {
        hideIndeterminateProgress();

        mTask = task;

        //refresh menu state
        invalidateOptionsMenu();

        // udpate icon state
        updateTaskStatusIcon();

        finish();
    }

    @Override
    public void onTaskUnlocked(ICMTask task) {
        hideIndeterminateProgress();

        mTask = task;

        //refresh menu state
        invalidateOptionsMenu();

        // udpate icon state
        updateTaskStatusIcon();
    }

    @Override
    public void onTaskUpdated(ICMTask task) {
        hideIndeterminateProgress();

        // save the new task
        mTask = task;

        // reset change counter and refresh menu state
        mChangedPropertiesCounter = 0;
        updateMenuState(false);

        // notify the display helper that we have a new task
        mTaskDisplayHelper.clearTaskLayout(mPropertiesContainer);
        mTaskDisplayHelper.displayTask(mTask, mPropertiesContainer, true);
    }

    @Override
    public void onTaskActionPerformed(ICMTask task, int actionIndex) {
        hideIndeterminateProgress();

        // task was completed - finish activity ?
        finish();
    }

    @Override
    public void onError(@Operations int operationCode, String error) {
        hideIndeterminateProgress();

        //display the error message
        Snackbar.make(Utils.getContentView(this), error,
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onPropertyChanged(ICMProperty property, String newValue) {
        // if the newValues is the same as the old one -  disable save option
        if (property.getValue() == null) {
            // have to check if value is null or empty because the EditText will return an empty
            // string when we delete all text
            if (TextUtils.isEmpty(newValue)) {
                // decrease counter
                updateMenuState(false);
            } else {
                // increase counter
                updateMenuState(true);
            }
        } else if (property.getValue().toString().equalsIgnoreCase(newValue)) {

            //decrease counter
            updateMenuState(false);
        } else {
            // if the newValues is different from the old one -> show "save" option
            updateMenuState(true);
        }

    }

    private void updateMenuState(boolean hasNewChange) {
        if (hasNewChange) {
            mChangedPropertiesCounter++;
        } else {
            if (mChangedPropertiesCounter > 0) {
                mChangedPropertiesCounter--;
            }
        }

        // redraw menu only if it has a different state
        if ((hasNewChange && mChangedPropertiesCounter == 1) ||
                !hasNewChange) {
            invalidateOptionsMenu();
        }
    }


    private void saveTaskProperties() {
        mTaskController.updateTask(mTask, mTaskDisplayHelper.getUpdatedProperties());
        showIndeterminateProgress();
    }

    private void showCompleteTaskConfirmation() {

        // this is an important operation so ask confirmation
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dlg_title_confirm))
                .setMessage(getString(R.string.msg_confirm_task_completion))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int taskCompleteIndex = findTaskCompleteIndex();
                        if (taskCompleteIndex == -1) {
                            Snackbar.make(Utils.getContentView(TaskDetailActivity.this),
                                    getString(R.string.err_no_complete_action), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(getString(R.string.action_ok), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // the Snackbar will dismiss automatically when the user
                                            // clicks the action button
                                        }
                                    }).show();
                        } else {
                            showIndeterminateProgress();
                            mTaskController.performTaskAction(mTask,
                                    mTaskDisplayHelper.getUpdatedProperties(), taskCompleteIndex);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private int findTaskCompleteIndex() {
        List<String> responses;
        int index = -1;
        try {
            responses = mTask.getResponses();

            if (responses.size() > 0) {
                for (int i = 0; i < responses.size(); i++) {
                    if (responses.get(0).equalsIgnoreCase("complete")) {
                        index = i;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // no need to do anything
        }
        return index;

    }

    private void lockTask() {
        showIndeterminateProgress();
        mTaskController.lockTask(mTask);
    }

    private void unlockTask() {
        showIndeterminateProgress();
        mTaskController.unlockTask(mTask);
    }

    private void updateTaskStatusIcon() {
        if (TextUtils.isEmpty(mTask.getLockedUser())) {
            // task is not locked
            mTaskStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_open));
        } else {
            mTaskStatus.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_lock_closed));
        }
    }
}
