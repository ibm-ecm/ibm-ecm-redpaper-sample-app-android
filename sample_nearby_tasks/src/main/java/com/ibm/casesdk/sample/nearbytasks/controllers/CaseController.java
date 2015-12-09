package com.ibm.casesdk.sample.nearbytasks.controllers;

import android.app.Activity;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ibm.casemanagersdk.sdk.interfaces.ICMCallback;
import com.ibm.casemanagersdk.sdk.interfaces.ICMInBasket;
import com.ibm.casemanagersdk.sdk.interfaces.ICMRole;
import com.ibm.casemanagersdk.sdk.interfaces.ICMSolution;
import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;
import com.ibm.casemanagersdk.sdk.manager.InBasketManager;
import com.ibm.casemanagersdk.sdk.manager.RoleManager;
import com.ibm.casemanagersdk.sdk.manager.SessionManager;
import com.ibm.casemanagersdk.sdk.manager.SolutionManager;
import com.ibm.casemanagersdk.sdk.manager.TaskManager;
import com.ibm.casesdk.sample.nearbytasks.R;
import com.ibm.casesdk.sample.nearbytasks.utils.Constants;
import com.ibm.casesdk.sample.nearbytasks.viewmodel.NearbyTasksViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class will handle creating a session and getting the cases from a specific solution.
 * <p>
 * Created by stelian on 20/10/2015.
 */
public class CaseController {

    private Activity mCallingActivity;
    private NearbyTasksViewModel mViewModel;

    private SessionManager mSessionManager;
    private SolutionManager mSolutionManager;
    private RoleManager mRoleManager;
    private ICMSolution mSolution;
    private HashMap<ICMRole, List<ICMInBasket>> mRoleInbasketsMap;
    private boolean mLoadingRoles;

    public CaseController(@NonNull Activity callingActivity, @NonNull NearbyTasksViewModel model) {
        mCallingActivity = callingActivity;
        mViewModel = model;
        mRoleInbasketsMap = new LinkedHashMap<>();
    }

    /**
     * Perform authentication with the default credentials stored in {@link Constants}.
     * <p>
     * If the operation is successful, the {@link NearbyTasksViewModel} passed in the constructor
     * will be notified in it's {@code onSessionInitiated()} method.
     */
    public void login(@NonNull String user, @NonNull String password) {
        // set session manager
        SessionManager.initSession(mCallingActivity, Constants.ENDPOINT, user, password,
                new ICMCallback<SessionManager>() {
                    @Override
                    public void onSuccess(SessionManager ret) {
                        mSessionManager = ret;
                        mViewModel.onSessionInitiated();
                    }

                    @Override
                    public void onError(String error) {
                        mViewModel.onError(NearbyTasksViewModel.INITIATE_SESSION, error);
                    }
                });
    }

    /**
     * Obtain a list of all the available solutions from the SDK and search for the one with the
     * given name.
     * <p>
     * If there is no solution with the searched name we show an error message on the calling
     * activity.
     */
    public void findSolution(@NonNull final String solutionName) {
        // get all available solutions and search for the one we need
        mSessionManager.getSolutions(new ICMCallback<List<ICMSolution>>() {
            @Override
            public void onSuccess(List<ICMSolution> ret) {
                boolean found = false;
                for (ICMSolution solution : ret) {
                    if (solution.getName().equalsIgnoreCase(solutionName)) {
                        mViewModel.onSolutionFound(solution);
                        found = true;
                        break;
                    }
                }

                // show error message if we didn't find any solution
                if (!found) {
                    mViewModel.onError(NearbyTasksViewModel.FIND_SOLUTION,
                            mCallingActivity.getString(R.string.err_solution_not_found));
                }
            }

            @Override
            public void onError(String error) {
                mViewModel.onError(NearbyTasksViewModel.FIND_SOLUTION, error);
            }
        });
    }

    /**
     * Create a {@link SolutionManager} for the given {@link ICMSolution} and load the solution
     * details.
     *
     * @param solution
     */
    public void loadSolutionDetails(@NonNull ICMSolution solution) {
        mSolution = null;
        mSolutionManager = null;
        mSolutionManager = mSessionManager.getSolutionManager(solution);

        mSolutionManager.getSolutionDetails(new ICMCallback<ICMSolution>() {
            @Override
            public void onSuccess(ICMSolution icmSolution) {
                mSolution = icmSolution;
                mViewModel.onSolutionDetailsLoaded(icmSolution);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(NearbyTasksViewModel.LOAD_SOLUTION_DETAILS, s);
            }
        });
    }

    /**
     * Find a role with the given name in the current {@link ICMSolution}.
     * <p>
     * If no role is found, an error message will be displayed.
     *
     * @param roleName
     */
    public void findRole(@NonNull String roleName) {
        boolean found = false;

        if (mSolution != null) {
            try {
                // sdk throws NPE if there are no roles
                final List<ICMRole> roles = mSolution.getRoles();

                for (ICMRole role : roles) {
                    if (role.getName().equalsIgnoreCase(roleName)) {

                        // create new map entry
                        mRoleInbasketsMap.put(role, new ArrayList<ICMInBasket>());

                        // notify liteners
                        mViewModel.onRoleFound(role);
                        found = true;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // show error message if we didn't find any solution
        if (!found) {
            mViewModel.onError(NearbyTasksViewModel.FIND_ROLE,
                    mCallingActivity.getString(R.string.err_role_not_found));
        }
    }

    /**
     * Create a {@link RoleManager} for the given {@link ICMRole}/
     *
     * @param role
     */
    public void createRoleManager(@NonNull final ICMRole role) {
        // reset current role manager
        mRoleManager = null;
        mRoleManager = mSolutionManager.getRoleManager(role);
    }

    /**
     * Load all the {@link ICMInBasket} for the current {@link ICMRole}. The {@link ICMInBasket}
     * will be loaded with all their details.
     */
    public void loadRoleInbaskets() {
        if (mRoleManager != null) {
            ICMRole currentRole = mRoleManager.getRole();
            final List<ICMInBasket> workbaskets = currentRole.getWorkbaskets();
            final List<ICMInBasket> fullDetailBaskets = mRoleInbasketsMap.get(currentRole);

            for (final ICMInBasket basket : workbaskets) {
                // create inbasket manager
                final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(basket);
                inbasketManager.getInBasketDetails(new ICMCallback<ICMInBasket>() {
                    @Override
                    public void onSuccess(ICMInBasket icmInBasket) {
                        fullDetailBaskets.add(icmInBasket);

                        if (fullDetailBaskets.size() == workbaskets.size()) {
                            mViewModel.onWorkbasketsLoaded();
                        }
                    }

                    @Override
                    public void onError(String s) {
                        Log.d("CaseControlelr", s);

                        // we just have to save the incomplete basket
                        fullDetailBaskets.add(basket);
                    }
                });
            }
        }
    }

    /**
     * Find nearby tasks for the current {@link ICMSolution} and {@link ICMRole}.
     *
     * @param location
     * @param radius
     */
    public void getNearbyTasks(@NonNull Location location, @NonNull Double radius) {
        mRoleManager.getNearbyTasks(location.getLatitude(),
                location.getLongitude(), radius, new ICMCallback<List<ICMTask>>() {
                    @Override
                    public void onSuccess(List<ICMTask> icmTasks) {
                        mViewModel.onNearbyTasksFound(icmTasks);
                    }

                    @Override
                    public void onError(String s) {
                        mViewModel.onError(NearbyTasksViewModel.FIND_NEARBY_TASKS, s);
                    }
                });
    }

    /**
     * Load the details for the given {@link ICMTask}.
     *
     * @param task
     */
    public void loadTaskDetails(@NonNull ICMTask task) {

        // find the task basket for this task
        final ICMInBasket taskBasket = findTaskBasket(task);

        if (taskBasket != null) {
            final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(taskBasket);
            final TaskManager taskManager = inbasketManager.getTaskManager(task);

            taskManager.getTaskDetails(new ICMCallback<ICMTask>() {
                @Override
                public void onSuccess(ICMTask icmTask) {
                    mViewModel.onTaskDetailsLoaded(icmTask);
                }

                @Override
                public void onError(String s) {
                    mViewModel.onError(NearbyTasksViewModel.LOAD_TASK_DETAILS, s);
                }
            });
        } else {
            // notify activity that we can't load details
            mViewModel.onError(NearbyTasksViewModel.LOAD_TASK_DETAILS,
                    mCallingActivity.getString(R.string.err_no_inbasket));
        }
    }

    /**
     * Lock the given task.
     *
     * @param task
     */
    public void lockTask(@NonNull ICMTask task) {
        // find the task basket for this task
        final ICMInBasket taskBasket = findTaskBasket(task);

        final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(taskBasket);
        final TaskManager taskManager = inbasketManager.getTaskManager(task);

        taskManager.lockTask(new ICMCallback<ICMTask>() {
            @Override
            public void onSuccess(ICMTask icmTask) {
                mViewModel.onTaskLocked(icmTask);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(NearbyTasksViewModel.LOCK_TASK, s);
            }
        });
    }

    /**
     * Unlock the given task.
     *
     * @param task
     */
    public void unlockTask(@NonNull ICMTask task) {
        // find the task basket for this task
        final ICMInBasket taskBasket = findTaskBasket(task);

        final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(taskBasket);
        final TaskManager taskManager = inbasketManager.getTaskManager(task);

        taskManager.unlockTask(new ICMCallback<ICMTask>() {
            @Override
            public void onSuccess(ICMTask icmTask) {
                mViewModel.onTaskUnlocked(icmTask);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(NearbyTasksViewModel.UNLOCK_TASK, s);
            }
        });
    }

    /**
     * Perform an operation on the given {@link ICMTask}.
     *
     * @param task        the task to perform the operation on
     * @param actionIndex the index of the action to be performed - MUST be a valid index for the {@code task.getResponses()} list
     */
    public void performTaskAction(@NonNull ICMTask task, final int actionIndex) {
        final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(
                mRoleManager.getRole().getWorkbaskets().get(0));
        final TaskManager taskManager = inbasketManager.getTaskManager(task);
        taskManager.completeTask(task.getResponses().get(actionIndex), "", new ICMCallback<ICMTask>() {
            @Override
            public void onSuccess(ICMTask icmTask) {
                mViewModel.onTaskActionPerformed(icmTask, actionIndex);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(NearbyTasksViewModel.TASK_ACTION, s);
            }
        });
    }

    /**
     * Return the current {@link RoleManager}.
     *
     * @return
     */
    public RoleManager getRoleManager() {
        return mRoleManager;
    }

    /**
     * Search a {@link ICMInBasket} from the ones assigned to the current {@link ICMRole}
     * that contains the given {@link ICMTask}.
     *
     * @param task
     * @return a {@link ICMInBasket} that contains the given task or {@code null}
     */
    private ICMInBasket findTaskBasket(ICMTask task) {
        ICMInBasket taskBasket = null;

        // search through all the baskets available for this role
        for (ICMInBasket basket : mRoleInbasketsMap.get(mRoleManager.getRole())) {

            // search through all the tasks defined in a basket
            for (ICMTask tk : basket.getTasks()) {

                // if the basket contains our task -> break the loops
                if (tk.getId().equalsIgnoreCase(task.getId())) {
                    taskBasket = basket;
                    break;
                }
            }

            if (taskBasket != null) {
                break;
            }
        }

        return taskBasket;
    }


    public ICMSolution getSolution() {
        return mSolution;
    }
}
