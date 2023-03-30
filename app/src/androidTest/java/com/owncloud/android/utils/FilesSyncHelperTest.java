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
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.nextcloud.client.account.MockUser;
import com.nextcloud.client.account.User;
import com.nextcloud.client.core.Clock;
import com.nextcloud.client.core.ClockImpl;
import com.nextcloud.client.preferences.AppPreferences;
import com.nextcloud.client.preferences.AppPreferencesImpl;
import com.owncloud.android.AbstractIT;
import com.owncloud.android.MainApp;
import com.owncloud.android.datamodel.FileSystemDataSet;
import com.owncloud.android.datamodel.FilesystemDataProvider;
import com.owncloud.android.datamodel.MediaFolderType;
import com.owncloud.android.datamodel.SyncedFolder;
import com.owncloud.android.datamodel.SyncedFolderProvider;
import com.owncloud.android.db.ProviderMeta;
import com.owncloud.android.files.services.FileUploader;
import com.owncloud.android.files.services.NameCollisionPolicy;
import com.owncloud.android.lib.common.utils.Log_OC;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.fail;

public class FilesSyncHelperTest extends AbstractIT{

    @Mock AppPreferences preferences;
    @Mock Clock clock;

/*
    @Mock
    AppPreferences preferences;
    @Mock
    Clock clock;
    @Mock FilesystemDataProvider provider;
    @Mock ContentResolver resolver;
    @Mock Context appContext;
*/
    @Before
    public void init(){
        this.clock = Mockito.mock(Clock.class);
        Mockito.when(clock.getCurrentTime()).thenReturn(1000L);
        /*
        appContext = Mockito.mock(Context.class);
        resolver = Mockito.mock(ContentResolver.class);
        provider = new FilesystemDataProvider(resolver);
        clock = Mockito.mock(Clock.class);
        Mockito.when(clock.getCurrentTime()).thenReturn(10000L);*/
    }

    @Test
    public void testInsertAllDBEntries(){
        String fileRoot = "/storage/emulated/0"; //replace this if test don't work
        try{
            File root = new File(fileRoot+"/nextcloudTest");
            root.mkdir();
            new File(fileRoot+"/nextcloudTest/images").mkdir();
            new File(fileRoot+"/nextcloudTest/videos").mkdir();
            new File(fileRoot+"/nextcloudTest/other").mkdir();
            //add sample elements to file
            File image1 = new File(fileRoot+"/nextcloudTest/images/img1.png");
            image1.createNewFile();
            new FileWriter(image1).write("Test");
            File video1 = new File(fileRoot+"/nextcloudTest/videos/vid1.mp4");
            video1.createNewFile();
            new FileWriter(video1).write("Test");
            File other1 = new File(fileRoot+"/nextcloudTest/other/other1.txt");
            new FileWriter(other1).write("Test");
        }catch (IOException e){
            fail("Unable to create Test files: "+e);
        }

        SyncedFolder images = new SyncedFolder(
            fileRoot+"/nextcloudTest/images",
            "/testing/images",
            false,
            false,
            true,
            false,
            "FilesSyncHelperTest",
            FileUploader.LOCAL_BEHAVIOUR_FORGET,
            NameCollisionPolicy.OVERWRITE.serialize(),
            true,
            0L,
            MediaFolderType.IMAGE,
            false
        );
        SyncedFolder videos = new SyncedFolder(
            fileRoot+"/nextcloudTest/videos",
            "/testing/images",
            false,
            false,
            true,
            false,
            "FilesSyncHelperTest",
            FileUploader.LOCAL_BEHAVIOUR_FORGET,
            NameCollisionPolicy.OVERWRITE.serialize(),
            true,
            0L,
            MediaFolderType.VIDEO,
            false
        );
        SyncedFolder other = new SyncedFolder(
            fileRoot+"/nextcloudTest/other",
            "/testing/images",
            false,
            false,
            true,
            false,
            "FilesSyncHelperTest",
            FileUploader.LOCAL_BEHAVIOUR_FORGET,
            NameCollisionPolicy.OVERWRITE.serialize(),
            true,
            0L,
            MediaFolderType.CUSTOM,
            false
        );
        SyncedFolderProvider provider = new SyncedFolderProvider(MainApp.getAppContext().getContentResolver(),preferences,clock);
        provider.storeSyncedFolder(images);
        provider.storeSyncedFolder(videos);
        provider.storeSyncedFolder(other);

        //Set Up mock query

        FilesSyncHelper.insertAllDBEntries(preferences,clock,false);
        FilesystemDataProvider fileprovider = new FilesystemDataProvider(MainApp.getAppContext().getContentResolver());
        Log.d("FilesSyncHelperTest",fileprovider.getFilesForUpload("%",images.getId()+"").size()+"sized");


        //cleanup (I don't know if it is done automatically, so I do it myself)
        provider.deleteSyncFoldersForAccount(new MockUser("FilesSyncHelperTest",MockUser.DEFAULT_MOCK_ACCOUNT_TYPE));
        boolean deleted = true;
        new File(fileRoot+"/nextcloudTest/images/img1.png").delete();
        new File(fileRoot+"/nextcloudTest/videos/vid1.mp4").delete();
        new File(fileRoot+"/nextcloudTest/other/other1.txt").delete();
        new File(fileRoot+"/nextcloudTest/images").delete();
        new File(fileRoot+"/nextcloudTest/videos").delete();
        new File(fileRoot+"/nextcloudTest/other").delete();
        new File(fileRoot+"/nextcloudTest").delete();

    }
}