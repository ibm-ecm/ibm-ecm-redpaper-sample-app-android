package com.ibm.casesdk.sample.edittask.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.ibm.casemanagersdk.sdk.interfaces.ICMRole;
import com.ibm.casemanagersdk.sdk.interfaces.ICMSolution;
import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;
import com.ibm.casesdk.sample.edittask.R;
import com.ibm.casesdk.sample.edittask.TaskManagerApp;
import com.ibm.casesdk.sample.edittask.adapters.ClickkableRecyclerAdapter;
import com.ibm.casesdk.sample.edittask.adapters.TaskListAdapter;
import com.ibm.casesdk.sample.edittask.controllers.TaskController;
import com.ibm.casesdk.sample.edittask.utils.Constants;
import com.ibm.casesdk.sample.edittask.utils.Utils;
import com.ibm.casesdk.sample.edittask.viewmodels.TaskViewModel;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class TaskManagerActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener,
        TaskViewModel, ClickkableRecyclerAdapter.RecyclerViewClickListener {

    @Bind(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipelayout;

    private TaskListAdapter mTaskListAdapter;
    private TaskController mTaskController;

    private boolean mAuthenticating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_manager);

        ButterKnife.bind(this);

        setupToolbar();

        setupRecyclerView();

        setupSwipeRefresh();

        mTaskController = new TaskController(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // login to ICM if we don't already have a session started
        final TaskManagerApp app = (TaskManagerApp) getApplication();
        if (app.getTaskController() == null) {
            login();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // automatically refresh tasks
        onRefresh();
    }

    @Override
    public void onRefresh() {
        // refresh task list or login if necessary
        if (mTaskController.isSessionInitiated()) {
            // user already authenticated which means we already have a solution and a role

            // we need to reload the solution from the server to get the updated tasks
            setLayoutRefreshing(true);
            mTaskController.loadSolutionDetails(mTaskController.getSolution());
        } else {
            login();
        }
    }

    @Override
    public void onSessionInitiated() {
        mAuthenticating = false;

        // save at Application level
        final TaskManagerApp app = (TaskManagerApp) getApplication();
        app.setTaskController(mTaskController);

        //  find custom solution
        mTaskController.findSolution(Constants.SOLUTION);
    }

    @Override
    public void onSolutionFound(ICMSolution solution) {
        // load solution details
        mTaskController.loadSolutionDetails(solution);
    }

    @Override
    public void onSolutionDetailsLoaded(ICMSolution solution) {
        // search for the role
        mTaskController.findRole(Constants.ROLE);
    }

    @Override
    public void onRoleFound(ICMRole role) {
        // after we found the role, we ca create the RoleManager and get the tasks
        mTaskController.createRoleManager(role);

        // get tasks from the first InBasket
        mTaskController.getTasksFromBasket(role, 0);
    }

    @Override
    public void onTasksFound(List<ICMTask> icmTasks) {
        setLayoutRefreshing(false);

        if (!icmTasks.isEmpty()) {
            // update the recycler view
            mTaskListAdapter.updateTasks(icmTasks);
            mSwipelayout.setRefreshing(false);
            mTaskListAdapter.notifyDataSetChanged();
        } else {
            Snackbar.make(Utils.getContentView(this),
                    getString(R.string.err_no_tasks_in_basket),
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskDetailsLoaded(ICMTask task) {
        // Not needed here
    }

    @Override
    public void onTaskLocked(ICMTask task) {
        // Not needed here
    }

    @Override
    public void onTaskUnlocked(ICMTask task) {
        // Not needed here
    }

    @Override
    public void onTaskUpdated(ICMTask task) {
        // Not needed here
    }

    @Override
    public void onTaskActionPerformed(ICMTask task, int actionIndex) {
        // Not needed here
    }

    @Override
    public void onError(@Operations int operationCode, String error) {
        // hide the indeterminate progress
        hideIndeterminateProgress();
        mSwipelayout.setRefreshing(false);

        // perform special actions based on the operation that failed
        switch (operationCode) {
            case INITIATE_SESSION:
                break;
            case FIND_SOLUTION:
                break;
            case LOAD_SOLUTION_DETAILS:
                break;
            case FIND_ROLE:
                break;
            case FIND_NEARBY_TASKS:
                break;
            case LOAD_TASK_DETAILS:
                break;
            case LOCK_TASK:
                break;
            case UNLOCK_TASK:
                break;
            case TASK_ACTION:
                break;
        }

        //display the error message
        Snackbar.make(Utils.getContentView(this), error,
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onRecyclerViewItemClicked(int position) {
        // get icm task
        final ICMTask task = mTaskListAdapter.getItem(position);
        if (task != null) {
            // launch detail activity
            final Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra(Constants.EXTRA_TASK, task);
            startActivity(intent);

            // TODO : animations ?
        }

    }

    private void setupRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.seton

        // create empty adapter
        mTaskListAdapter = new TaskListAdapter(this);
        mTaskListAdapter.setRecyclerViewClickListener(this);
        mRecyclerView.setAdapter(mTaskListAdapter);
    }

    private void setupSwipeRefresh() {
        if (mSwipelayout != null) {
            mSwipelayout.setOnRefreshListener(this);
        }
    }

    private void login() {
        if (!mAuthenticating) {
            setLayoutRefreshing(true);
            mTaskController.login(Constants.USER, Constants.PASS);
        }
    }

    private void setLayoutRefreshing(@NonNull final boolean refreshing) {
        if (mSwipelayout != null) {
            mSwipelayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipelayout.setRefreshing(refreshing);
                }
            });
        }
    }

}
