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
import android.database.Cursor;
import com.nextcloud.client.account.MockUser;
import com.nextcloud.client.core.Clock;
import com.nextcloud.client.core.ClockImpl;
import com.nextcloud.client.preferences.AppPreferences;
import com.owncloud.android.AbstractIT;
import com.owncloud.android.MainApp;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.fail;

public class FilesSyncHelperTest extends AbstractIT{
    @Mock private AppPreferences preferences;
    private Clock clock;

    @Before
    public void init(){
        this.clock = new ClockImpl();
    }
    @SuppressWarnings("ResultOfMethodCallIgnored") //file creation / deletion
    @Test
    public void testInsertAllDBEntries(){ //Warning: this only tests for non-media folders
        List<String> neededPaths = new LinkedList<>();

        String fileRoot = targetContext.getFilesDir() + "/nextcloudTest"; //create a subfolder in file root for files
        try{
            File root = new File(fileRoot);
            root.mkdir();
            new File(fileRoot+"/images").mkdir();
            new File(fileRoot+"/videos").mkdir();
            new File(fileRoot+"/other").mkdir();
            neededPaths.add(fileRoot+"/other");
            //add sample elements to file
            File image1 = new File(fileRoot+"/images/img1.png");
            image1.createNewFile();
            File video1 = new File(fileRoot+"/videos/vid1.mp4");
            video1.createNewFile();
            File other1 = new File(fileRoot+"/other/other1.txt");
            other1.createNewFile();
            neededPaths.add(fileRoot+"/other/other1.txt");
            StringBuilder nestedPath = new StringBuilder(fileRoot);
            nestedPath.append("/other");
            for(int i = 0; i < 10; i++){
                File add = new File(nestedPath+"/file"+i);
                add.createNewFile();
                neededPaths.add(add.getAbsolutePath());
                File directory = new File(nestedPath+"/d"+i);
                neededPaths.add(directory.getAbsolutePath());
                directory.mkdir();
                nestedPath.append("/d").append(i);
            }
        }catch (IOException e){
            fail("Unable to create Test files: "+e);
        }

        /*SyncedFolder images = new SyncedFolder(
            fileRoot+"/nextcloudTest/images",
            "/testing/images",
            false,
            false,
            true,
            false,
            TAG,
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
            TAG,
            FileUploader.LOCAL_BEHAVIOUR_FORGET,
            NameCollisionPolicy.OVERWRITE.serialize(),
            true,
            0L,
            MediaFolderType.VIDEO,
            false
        );*/
        String TAG = "FilesSyncHelperTest";
        SyncedFolder other = new SyncedFolder(
            fileRoot+"/nextcloudTest/other",
            "/testing/images",
            false,
            false,
            true,
            false,
            TAG,
            FileUploader.LOCAL_BEHAVIOUR_FORGET,
            NameCollisionPolicy.OVERWRITE.serialize(),
            true,
            0L,
            MediaFolderType.CUSTOM,
            false
        );
        SyncedFolderProvider provider = new SyncedFolderProvider(MainApp.getAppContext().getContentResolver(),preferences,clock);
        //provider.storeSyncedFolder(images);
        //provider.storeSyncedFolder(videos);
        provider.storeSyncedFolder(other);

        //Execute Test
        FilesSyncHelper.insertAllDBEntries(preferences,clock,false);

        //got from filesystemdataprovider to get all synced folders
        Set<String> filesInDB = new HashSet<>();
        ContentResolver resolver = MainApp.getAppContext().getContentResolver();
        Cursor cursor = resolver.query(
            ProviderMeta.ProviderTableMeta.CONTENT_URI_FILESYSTEM,
            null,
            null,
            null,
            null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String value = cursor.getString(cursor.getColumnIndexOrThrow(
                        ProviderMeta.ProviderTableMeta.FILESYSTEM_FILE_LOCAL_PATH));
                    if (value == null) {
                        fail("Cannot get local path");
                    } else {
                        File file = new File(value);
                        if (!file.exists()) {
                            Log_OC.d(TAG, "Ignoring file for upload (doesn't exist): " + value);
                        } else if (!SyncedFolderUtils.isQualifiedFolder(file.getParent())) {
                            Log_OC.d(TAG, "Ignoring file for upload (unqualified folder): " + value);
                        } else if (!SyncedFolderUtils.isFileNameQualifiedForAutoUpload(file.getName())) {
                            Log_OC.d(TAG, "Ignoring file for upload (unqualified file): " + value);
                        } else {
                            filesInDB.add(value);
                        }
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        List<String> missingFiles = new LinkedList<>();
        if(!filesInDB.containsAll(neededPaths)){
            for(String needed: neededPaths){
                if(!filesInDB.contains(needed)){
                    missingFiles.add("File "+needed+ " not found in database");
                }
            }
            fail("Not all files from synced folder are correctly in database" + missingFiles.stream().reduce(String::concat));
        }

        //cleanup (I don't know if it is done automatically, so I do it myself)
        provider.deleteSyncFoldersForAccount(new MockUser(TAG, MockUser.DEFAULT_MOCK_ACCOUNT_TYPE));
        Collections.reverse(neededPaths); //delete in reverse
        for(String path: neededPaths){
            new File(path).delete();
        }
        new File(fileRoot+"/nextcloudTest/images/img1.png").delete();
        new File(fileRoot+"/nextcloudTest/videos/vid1.mp4").delete();
        new File(fileRoot+"/nextcloudTest/other/other1.txt").delete();
        new File(fileRoot+"/nextcloudTest/images").delete();
        new File(fileRoot+"/nextcloudTest/videos").delete();
        new File(fileRoot+"/nextcloudTest/other").delete();
        new File(fileRoot+"/nextcloudTest").delete();
    }
}