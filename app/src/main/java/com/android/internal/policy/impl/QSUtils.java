/*
 * Copyright (C) 2015 The CyanogenMod Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.internal.policy.impl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;

import java.util.Iterator;
import java.util.List;

public class QSUtils {
    private static boolean sAvailableTilesFiltered;

    private QSUtils() {
    }

    public static List<String> getAvailableTiles(Context context) {
        filterTiles(context);
        return QSConstants.TILES_AVAILABLE;
    }

    public static List<String> getDefaultTiles(Context context) {
        filterTiles(context);
        return QSConstants.TILES_DEFAULT;
    }

    public static String getDefaultTilesAsString(Context context) {
        List<String> list = getDefaultTiles(context);
        return TextUtils.join(",", list);
    }

    private static void filterTiles(Context context) {
        if (!sAvailableTilesFiltered) {

            // Tiles that need conditional filtering
            Iterator<String> iterator = QSConstants.TILES_AVAILABLE.iterator();
            while (iterator.hasNext()) {
                String tileKey = iterator.next();
                boolean removeTile = false;
                switch (tileKey) {
                    case QSConstants.TILE_VISUALIZER:
                        removeTile = isV4AInstalled(context);
                        break;
                    case QSConstants.TILE_LTE:
                    case QSConstants.TILE_PROFILES:
                    case QSConstants.TILE_PERFORMANCE:
                    case QSConstants.TILE_LOCKSCREEN:
                    case QSConstants.TILE_APPCIRCLEBAR:
                    case QSConstants.TILE_EXPANDED_DESKTOP:
                    case QSConstants.TILE_HEADS_UP:
                    case QSConstants.TILE_NAVBAR:
                    case QSConstants.TILE_NOTIFICATIONS:
                    case QSConstants.TILE_POWERMENU:
                    case QSConstants.TILE_REBOOT:
                    case QSConstants.TILE_SYNC:
                        removeTile = true;
                        break;
                }
                if (removeTile) {
                    iterator.remove();
                    QSConstants.TILES_DEFAULT.remove(tileKey);
                }
            }

            sAvailableTilesFiltered = true;
        }
    }

//    private static boolean isAudioFXInstalled(Context context) {
//        PackageManager pm = context.getPackageManager();
//        try {
//            pm.getPackageInfo("org.cyanogenmod.audiofx", PackageManager.GET_ACTIVITIES);
//            return true;
//        } catch (NameNotFoundException e) {
//            return false;
//        }
//    }

    private static boolean isV4AInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.vipercn.viper4android_v2", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
