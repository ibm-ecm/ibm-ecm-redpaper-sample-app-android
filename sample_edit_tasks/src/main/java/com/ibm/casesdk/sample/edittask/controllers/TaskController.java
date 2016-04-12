package com.ibm.casesdk.sample.edittask.controllers;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.ibm.casemanagersdk.sdk.interfaces.ICMCallback;
import com.ibm.casemanagersdk.sdk.interfaces.ICMInBasket;
import com.ibm.casemanagersdk.sdk.interfaces.ICMProperty;
import com.ibm.casemanagersdk.sdk.interfaces.ICMRole;
import com.ibm.casemanagersdk.sdk.interfaces.ICMSolution;
import com.ibm.casemanagersdk.sdk.interfaces.ICMTask;
import com.ibm.casemanagersdk.sdk.manager.InBasketManager;
import com.ibm.casemanagersdk.sdk.manager.RoleManager;
import com.ibm.casemanagersdk.sdk.manager.SessionManager;
import com.ibm.casemanagersdk.sdk.manager.SolutionManager;
import com.ibm.casemanagersdk.sdk.manager.TaskManager;
import com.ibm.casesdk.sample.edittask.R;
import com.ibm.casesdk.sample.edittask.utils.Constants;
import com.ibm.casesdk.sample.edittask.viewmodels.TaskViewModel;

import java.util.List;
import java.util.Map;

/**
 * Created by stelian on 26/10/2015.
 */
public class TaskController {

    private Activity mCallingActivity;
    private TaskViewModel mViewModel;

    private SessionManager mSessionManager;
    private SolutionManager mSolutionManager;
    private RoleManager mRoleManager;
    private ICMSolution mSolution;
    private ICMInBasket mCurrentInbasket;


    public TaskController(@NonNull Activity callingActivity, @NonNull TaskViewModel model) {
        mCallingActivity = callingActivity;
        mViewModel = model;
    }

    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.mSessionManager = sessionManager;
    }

    public SolutionManager getSolutionManager() {
        return mSolutionManager;
    }

    public void setSolutionManager(SolutionManager solutionManager) {
        this.mSolutionManager = solutionManager;
    }

    public RoleManager getRoleManager() {
        return mRoleManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.mRoleManager = roleManager;
    }

    public ICMSolution getSolution() {
        return mSolution;
    }

    public void setSolution(ICMSolution solution) {
        this.mSolution = solution;
    }

    /**
     * Copy context from a different {@link TaskController} object.
     *
     * @param oldTaskController
     */
    public void duplicateContext(@NonNull TaskController oldTaskController) {
        setSessionManager(oldTaskController.getSessionManager());
        setSolutionManager(oldTaskController.getSolutionManager());
        setRoleManager(oldTaskController.getRoleManager());
        setSolution(oldTaskController.getSolution());
        mCurrentInbasket = oldTaskController.getCurrentInbasket();
    }

    /**
     * Perform authentication with the default credentials stored in {@link Constants}.
     * <p/>
     * If the operation is successful, the {@link TaskViewModel} passed in the constructor
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
                        mViewModel.onError(TaskViewModel.INITIATE_SESSION, error);
                    }
                });
    }

    /**
     * Obtain a list of all the available solutions from the SDK and search for the one with the
     * given name.
     * <p/>
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
                    mViewModel.onError(TaskViewModel.FIND_SOLUTION,
                            mCallingActivity.getString(R.string.err_solution_not_found));
                }
            }

            @Override
            public void onError(String error) {
                mViewModel.onError(TaskViewModel.FIND_SOLUTION, error);
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
        mCurrentInbasket = null;
        mSolutionManager = mSessionManager.getSolutionManager(solution);

        mSolutionManager.getSolutionDetails(new ICMCallback<ICMSolution>() {
            @Override
            public void onSuccess(ICMSolution icmSolution) {
                mSolution = icmSolution;
                mViewModel.onSolutionDetailsLoaded(icmSolution);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(TaskViewModel.LOAD_SOLUTION_DETAILS, s);
            }
        });
    }

    /**
     * Find a role with the given name in the current {@link ICMSolution}.
     * <p/>
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
            mViewModel.onError(TaskViewModel.FIND_ROLE,
                    mCallingActivity.getString(R.string.err_role_not_found));
        }
    }

    /**
     * Create a {@link RoleManager} for the given {@link ICMRole}/
     *
     * @param role
     */
    public void createRoleManager(@NonNull ICMRole role) {
        // reset current role manager
        mRoleManager = null;
        mRoleManager = mSolutionManager.getRoleManager(role);
    }

    /**
     * Load the details for the given {@link ICMTask}.
     *
     * @param task
     */
    public void loadTaskDetails(@NonNull ICMTask task) {
        if (mCurrentInbasket != null) {
            final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(mCurrentInbasket);
            final TaskManager taskManager = inbasketManager.getTaskManager(task);

            taskManager.getTaskDetails(new ICMCallback<ICMTask>() {
                @Override
                public void onSuccess(ICMTask icmTask) {
                    mViewModel.onTaskDetailsLoaded(icmTask);
                }

                @Override
                public void onError(String s) {
                    mViewModel.onError(TaskViewModel.LOAD_TASK_DETAILS, s);
                }
            });
        } else {
            // notify activity that we can't load details
            mViewModel.onError(TaskViewModel.LOAD_TASK_DETAILS,
                    mCallingActivity.getString(R.string.err_no_inbasket));
        }
    }

    /**
     * Lock the given task.
     *
     * @param task
     */
    public void lockTask(@NonNull ICMTask task) {
        final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(mCurrentInbasket);
        final TaskManager taskManager = inbasketManager.getTaskManager(task);

        taskManager.lockTask(new ICMCallback<ICMTask>() {
            @Override
            public void onSuccess(ICMTask icmTask) {
                mViewModel.onTaskLocked(icmTask);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(TaskViewModel.LOCK_TASK, s);
            }
        });
    }

    /**
     * Unlock the given task.
     *
     * @param task
     */
    public void unlockTask(@NonNull ICMTask task) {
        final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(mCurrentInbasket);
        final TaskManager taskManager = inbasketManager.getTaskManager(task);

        taskManager.unlockTask(new ICMCallback<ICMTask>() {
            @Override
            public void onSuccess(ICMTask icmTask) {
                mViewModel.onTaskUnlocked(icmTask);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(TaskViewModel.UNLOCK_TASK, s);
            }
        });
    }

    /**
     * Update the given {@link ICMTask}.
     *
     * @param task              the task to perform the operation on
     * @param updatedProperties the map holding the new property values -  the map key is
     *                          {@link ICMProperty#getSymbolicName()}
     */
    public void updateTask(@NonNull ICMTask task, final Map<String, String> updatedProperties) {
        final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(mCurrentInbasket);
        final TaskManager taskManager = inbasketManager.getTaskManager(task);
        taskManager.updateTask(updatedProperties, new ICMCallback<ICMTask>() {
            @Override
            public void onSuccess(ICMTask icmTask) {
                mViewModel.onTaskUpdated(icmTask);
            }

            @Override
            public void onError(String s) {
                mViewModel.onError(TaskViewModel.TASK_ACTION, s);
            }
        });
    }

    /**
     * Perform an operation on the given {@link ICMTask}.
     *
     * @param task        the task to perform the operation on
     * @param actionIndex the index of the action to be performed - MUST be a valid index for the {@code task.getResponses()} list
     */
    public void performTaskAction(@NonNull final ICMTask task, @NonNull Map<String, String> properties, final int actionIndex) {
        final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(mCurrentInbasket);
        final TaskManager taskManager = inbasketManager.getTaskManager(task);
        taskManager.completeTask(task.getResponses().get(actionIndex),
                new Gson().toJson(properties), new ICMCallback<Object>() {
                    @Override
                    public void onSuccess(Object object) {
                        mViewModel.onTaskActionPerformed(task, actionIndex);
                    }

                    @Override
                    public void onError(String s) {
                        mViewModel.onError(TaskViewModel.TASK_ACTION, s);
                    }
                });
    }

    /**
     * Get all the tasks from the {@link ICMInBasket} found at the given index from the
     * available baskets for the current role.
     *
     * @param inBasketIndex
     */
    public void getTasksFromBasket(@NonNull ICMRole role, @NonNull int inBasketIndex) {
        final List<ICMInBasket> workbaskets = role.getWorkbaskets();

        if (!workbaskets.isEmpty()) {
            if (inBasketIndex <= workbaskets.size()) {

                // load inbasket details
                final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(workbaskets.get(inBasketIndex));
                inbasketManager.getInBasketDetails(new ICMCallback<ICMInBasket>() {
                    @Override
                    public void onSuccess(ICMInBasket icmInBasket) {
                        mCurrentInbasket = icmInBasket;
                        mViewModel.onTasksFound(icmInBasket.getTasks());
                    }

                    @Override
                    public void onError(String s) {
                        // there are no work baskets for the current role
                        mViewModel.onError(TaskViewModel.FIND_NEARBY_TASKS,
                                mCallingActivity.getString(R.string.err_workbasket_details));
                    }
                });

            } else {
                // the index of the work basket is not in the valid range
                mViewModel.onError(TaskViewModel.FIND_NEARBY_TASKS,
                        mCallingActivity.getString(R.string.err_no_workbaskets_for_index));
            }
        } else {
            // there are no work baskets for the current role
            mViewModel.onError(TaskViewModel.FIND_NEARBY_TASKS,
                    mCallingActivity.getString(R.string.err_no_workbaskets_for_role));
        }
    }

    /**
     * Search a {@link ICMInBasket} from the ones assigned to the current {@link ICMRole}
     * that contains the given {@link ICMTask}.
     *
     * @param task
     * @return a {@link ICMInBasket} that contains the given task or {@code null}
     */
    private ICMInBasket findTaskBasket(final ICMTask task) {
        final List<ICMInBasket> workBaskets = mRoleManager.getRole().getWorkbaskets();
        ICMInBasket taskBasket = null;

        // search through all the baskets available for this role
//        for (ICMInBasket basket : workBaskets) {
//
//            // load inbasket details
//            final InBasketManager inbasketManager = mSolutionManager.getInbasketManager(basket);
//            inbasketManager.getInBasketDetails(new ICMCallback<ICMInBasket>() {
//                @Override
//                public void onSuccess(ICMInBasket icmInBasket) {
//                    // search through all the tasks defined in a basket
//                    for (ICMTask tk : icmInBasket.getTasks()) {
//
//                        // if the basket contains our task -> break the loops
//                        if (tk.getId().equalsIgnoreCase(task.getId())) {
//                            taskBasket = icmInBasket;
//                            break;
//                        }
//                    }
//                }
//
//                @Override
//                public void onError(String s) {
//                }
//            });
//
//
//            if (taskBasket != null) {
//                break;
//            }
//        }

        return taskBasket;
    }


    /**
     * Determine if a {@link SessionManager} exists.
     *
     * @return @{code true} if a user successfully authenticated and a {@link SessionManager} was
     * created, {@code false} otherwise.
     */
    public boolean isSessionInitiated() {
        return mSessionManager != null;
    }

    public ICMInBasket getCurrentInbasket() {
        return mCurrentInbasket;
    }
}
