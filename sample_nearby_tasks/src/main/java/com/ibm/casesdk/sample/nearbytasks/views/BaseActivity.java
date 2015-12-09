package com.ibm.casesdk.sample.nearbytasks.views;

import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.ibm.casesdk.sample.nearbytasks.R;

import butterknife.Bind;


/**
 * Base class that provides basic navigation and toolbar fields and setup methods.
 * <p/>
 * Created by stelian on 20/10/2015.
 */
public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Nullable
    @Bind(R.id.navigation_view)
    NavigationView mNavigationView;

    @Nullable
    @Bind(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @Nullable
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @Bind(R.id.toolbar_progress_bar)
    ProgressBar mIndeterminateProgress;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout != null) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {

        // we only set menu item checked if the menu entry will start a navigation activity
        // menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
           /* case R.id.drawer_cart:
                startActivity(new Intent(this, ShoppingCartActivity.class));
                break;
            case R.id.drawer_wishlist:
                startActivity(new Intent(this, WishlistActivity.class));
                break;*/
        }

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
        return true;
    }

    protected void setupToolbar() {
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setIcon(R.mipmap.ic_launcher);
            }
        }
    }

    protected void setupNavigationDrawer() {
        // setup navigation
        if (mNavigationView != null) {
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeButtonEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_action_menu);
                actionBar.setDisplayHomeAsUpEnabled(true);
                mNavigationView.setNavigationItemSelectedListener(this);
            }
        }
    }

    protected void showIndeterminateProgress() {
        // show loading progress bar
        if (mIndeterminateProgress != null) {
            mIndeterminateProgress.setVisibility(View.VISIBLE);
        }
    }

    protected void hideIndeterminateProgress() {
        // show loading progress bar
        if (mIndeterminateProgress != null) {
            mIndeterminateProgress.setVisibility(View.INVISIBLE);
        }
    }

}
