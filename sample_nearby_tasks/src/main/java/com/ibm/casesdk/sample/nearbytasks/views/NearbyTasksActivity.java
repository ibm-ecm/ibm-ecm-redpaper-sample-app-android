package com.ibm.casesdk.sample.nearbytasks.views;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ibm.casemanagersdk.sdk.interfaces.ICMRole;
import com.ibm.casemanagersdk.sdk.interfaces.ICMSolution;
import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;
import com.ibm.casesdk.sample.nearbytasks.R;
import com.ibm.casesdk.sample.nearbytasks.controllers.CaseController;
import com.ibm.casesdk.sample.nearbytasks.utils.Constants;
import com.ibm.casesdk.sample.nearbytasks.utils.Utils;
import com.ibm.casesdk.sample.nearbytasks.viewmodel.NearbyTasksViewModel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;


public class NearbyTasksActivity extends BaseMapLocationActivity implements NearbyTasksViewModel {

    private CaseController mCaseController;
    private HashMap<Marker, ICMTask> mTaskMarkers = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator);

        ButterKnife.bind(this);

        setupToolbar();

        setupGoogleApiClient();

        setupMap();

        // init controller
        mCaseController = new CaseController(this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // login to ICM
        mCaseController.login(Constants.USER, Constants.PASS);
        showIndeterminateProgress();
    }


    @Override
    public void onSessionInitiated() {
        //  find custom solution
        mCaseController.findSolution(Constants.SOLUTION);
    }

    @Override
    public void onSolutionFound(ICMSolution solution) {
        // create a solution manager and load the solution details
        mCaseController.loadSolutionDetails(solution);
    }

    @Override
    public void onSolutionDetailsLoaded(ICMSolution solution) {
        // the solution details are loaded, we can now search for the role
        mCaseController.findRole(Constants.ROLE);
    }

    @Override
    public void onRoleFound(ICMRole role) {
        mCaseController.createRoleManager(role);

        // load all the workbaskets for the role - we need to do this because Inbaskets contain
        // a limited number of tasks until their details are loaded
        mCaseController.loadRoleInbaskets();
    }

    @Override
    public void onWorkbasketsLoaded() {
        // all the inbaskets have been loaded, we can now udpate cases
        updateCases();
    }

    @Override
    public void onNearbyTasksFound(List<ICMTask> icmTasks) {
        // create bounds for the new task markers
        final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

        // clear current task markers
        clearCurrentMarkers();

        // display new markers
        for (ICMTask task : icmTasks) {

            // check that we have a valid location
            if (Utils.hasLocation(task)) {
                // create custom marker and show it on the map
                final Marker marker = mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_task_marker))
                                .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
                                .position(new LatLng(task.getLatitude(), task.getLongitude()))
                                .title(task.getSubject())
                                .snippet(task.getStepName())
                );

                // save marker refference
                mTaskMarkers.put(marker, task);

                // add the marker in bounds
                boundsBuilder.include(marker.getPosition());
            }
        }

        hideIndeterminateProgress();

        // zoom out so we can see all markers
        final CameraUpdate update = CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 200);
        mMap.animateCamera(update);
    }

    @Override
    public void onTaskDetailsLoaded(ICMTask task) {
        hideIndeterminateProgress();
        showTaskDetailsDialog(task);
    }

    @Override
    public void onTaskLocked(ICMTask task) {
        Snackbar.make(Utils.getContentView(this), getString(R.string.msg_task_locked),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onTaskUnlocked(ICMTask task) {
        Snackbar.make(Utils.getContentView(this), getString(R.string.msg_task_unlocked),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onTaskActionPerformed(ICMTask task, int actionIndex) {
        // check that the task returned from the server is not null
        String action = getString(R.string.action_complete);
        if (task != null) {
            action = task.getResponses().get(actionIndex);
        }
        Snackbar.make(Utils.getContentView(this),
                getString(R.string.msg_task_action_performed, action),
                Snackbar.LENGTH_LONG).show();

        // reload context
        showIndeterminateProgress();
        mCaseController.loadSolutionDetails(mCaseController.getSolution());
    }

    @Override
    public void onError(@Operations int operationCode, String error) {

        // hide the indeterminate progress
        hideIndeterminateProgress();

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
    public void onLocationChanged(Location location) {
        // update map location
        if (location != null) {
            //  check if it's a better location
            if (Utils.isBetterLocation(location, mLastLocation)) {
                // remove the old marker if it exists
                if (mLastLocationMarker != null) {
                    mLastLocationMarker.remove();
                }

                // Add a marker to current location and move the camera
                mLastLocation = location;
                showMyLocationOnMap(14);
                updateCases();
            }
        }
    }

    @Override
    protected void onMarkerInfoClicked(Marker marker) {
        final ICMTask markerTask = mTaskMarkers.get(marker);

        // show details only for tasks - not for last location
        if (markerTask != null) {
            showIndeterminateProgress();
            mCaseController.loadTaskDetails(markerTask);
        }

    }

    private void showTaskDetailsDialog(final ICMTask markerTask) {
        final View view = LayoutInflater.from(this).inflate(R.layout.marker_layout, null);

        final AlertDialog dlg = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        ((TextView) view.findViewById(R.id.marker_task)).setText(markerTask.getStepName());
        ((TextView) view.findViewById(R.id.marker_subject)).setText(markerTask.getSubject());

        final Button lockBtn = (Button) view.findViewById(R.id.btn_lock);
        final boolean isTaskLocked = markerTask.getLockedUser() != null;

        if (isTaskLocked) {
            lockBtn.setText(getString(R.string.action_unlock_task));
        }

        lockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);

                // lock or unlock the task
                if (isTaskLocked) {
                    mCaseController.unlockTask(markerTask);
                } else {
                    mCaseController.lockTask(markerTask);
                }

                dlg.dismiss();
            }
        });

        Button actionBtn = (Button) view.findViewById(R.id.btn_finish);
        actionBtn.setText(markerTask.getResponses().get(Constants.DEFAULT_TASK_ACTION_INDEX));

        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                mCaseController.performTaskAction(markerTask, Constants.DEFAULT_TASK_ACTION_INDEX);
                dlg.dismiss();
            }
        });


        dlg.show();
    }

    private void updateCases() {
        // check if we have a case controller and also that we have a role manager that can get the tasks
        if (mCaseController != null && mCaseController.getRoleManager() != null) {

            // check that we actually have a location
            if (mLastLocation != null) {
                mCaseController.getNearbyTasks(mLastLocation, Constants.DEFAULT_RADIUS);
            }
        }
    }

    private void clearCurrentMarkers() {
        for (Map.Entry<Marker, ICMTask> next : mTaskMarkers.entrySet()) {
            next.getKey().remove();
        }

        mTaskMarkers.clear();
    }
}
