/*
 * Copyright (C) 2016 The Android Open Source Project
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
 * limitations under the License.
 */
package com.wampinfotech.contacts.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Contact app.
 */
public final class ContactContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.wampinfotech.contacts";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_CONTACTS = "contacts";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ContactContract() {
    }

    /**
     * Inner class that defines constant values for the pets database table.
     * Each entry in the table represents a single pet.
     */
    public static final class ContactEntry implements BaseColumns {

        /**
         * The content URI to access the contact data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of contacts.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single contact.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;


        /**
         * Name of database table for pets
         */
        public final static String TABLE_NAME = "contacts";

        /**
         * Unique ID number for the pet (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the pet.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_CONTACT_NAME = "name";

        /**
         * Breed of the pet.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_CONTACT_EMAIL = "email";

        /**
         * Gender of the pet.
         * <p>
         * The only possible values are {@link GENDER#UNKNOWN}, {@link GENDER#MALE},
         * or {@link GENDER#FEMALE}.
         * <p>
         * Type: GENDER
         */
        public final static String COLUMN_CONTACT_GENDER = "gender";

        /**
         * Weight of the pet.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_CONTACT_NUMBER = "mobile";

        /**
         * Returns whether or not the given gender is {@link GENDER#UNKNOWN}, {@link GENDER#MALE},
         * or {@link GENDER#FEMALE}.
         */
        static boolean isValidGender(int gender) {
            return gender == GENDER.UNKNOWN.ordinal() || gender == GENDER.MALE.ordinal() || gender == GENDER.FEMALE.ordinal();
        }

        /**
         * Possible values for the gender of the pet.
         */
//        public static final boolean GENDER_MALE = true;
//        public static final boolean GENDER_FEMALE = false;

        public enum GENDER {
            UNKNOWN,
            MALE,
            FEMALE
        }
    }

}

