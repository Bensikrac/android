/*
 * Nextcloud Android client application
 *
 * @author Benedek Major
 * Copyright (C) 2023 Benedek Major
 * Copyright (C) 2023 Nextcloud
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import com.owncloud.android.R;
import com.owncloud.android.databinding.BidirectionalFilesSyncLayoutBinding;

/**
 * Activity allowing creation, editing and deleting bidirectionally synced folders. Also shows sync log
 * //TODO add content manager for this, since this is only gui
 */
public class BiDirectionalFileSyncActivity extends DrawerActivity {

    private static final String TAG = BiDirectionalFileSyncActivity.class.getSimpleName();
    private BidirectionalFilesSyncLayoutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) { //handles element setup for GUI
        super.onCreate(savedInstanceState);


        //setup view: Add binding first
        binding = BidirectionalFilesSyncLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        //then setup toolbar (handled by super class)
        setupToolbar();
        //Toolbar title
        updateActionBarTitleAndHomeButtonByString(getString(R.string.bidirectional_sync_menu_option));
        //left drawer
        setupDrawer(R.id.nav_bidirectional);
        //finally all content
        setupContent();
    }

    @Override
    protected void onResume() { //when app is resumed or reopened
        super.onResume();
        setDrawerMenuItemChecked(R.id.nav_bidirectional);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //handles hamburger button on left
        boolean retval = true;
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (isDrawerOpen()) {
                closeDrawer();
            } else {
                openDrawer();
            }
        } else {
            retval = super.onOptionsItemSelected(item);
        }
        return retval;
    }

    private void setupContent(){

    }


}
