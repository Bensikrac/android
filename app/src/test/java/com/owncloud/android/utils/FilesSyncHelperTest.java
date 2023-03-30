/*
 *
 * Nextcloud Android client application
 *
 * @author Benedek Major
 * Copyright (C) 2023 Benedek Major
 * Copyright (C) 2023 Nextcloud GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.owncloud.android.utils;

import android.content.Context;

import com.nextcloud.client.jobs.BackgroundJobManager;
import com.owncloud.android.MainApp;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class FilesSyncHelperTest extends TestCase {

    private Context context;

    @Before
    public void setup(){
        this.context = MainApp.getAppContext();
        assertNotNull("Main App context was null",this.context);
    }

    @Test
    public void testInsertAllDBEntries() {
        assertNotNull("Main App context was null",this.context);
    }

    @Test
    public void testRestartJobsIfNeeded() {
    }

    @Test
    public void testScheduleFilesSyncIfNeeded() {

    }
}