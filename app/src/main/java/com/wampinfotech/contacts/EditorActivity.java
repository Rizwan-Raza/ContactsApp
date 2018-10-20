package com.wampinfotech.contacts;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.wampinfotech.contacts.data.ContactContract.ContactEntry;

/**
 * Allows user to create a new contact or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the contact data loader
     */
    private static final int EXISTING_CONTACT_LOADER = 1;

    /**
     * Content URI for the existing contact (null if it's a new contact)
     */
    private Uri _CurrentContactUri;

    /**
     * Boolean flag that keeps track of whether the contact has been edited (true) or not (false)
     */
    private boolean _ContactHasChanged = false;

    /**
     * EditText field to enter the pet's name
     */
    private EditText _NameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    private EditText _EmailEditText;

    /**
     * EditText field to enter the pet's weight
     */
    private EditText _NumberEditText;

    /**
     * EditText field to enter the pet's gender
     */
    private Spinner _GenderSpinner;

    /**
     * Gender of the pet. The possible valid values are in the PetContract.java file:
     * {@link ContactEntry.GENDER#MALE}, {@link ContactEntry.GENDER#MALE} or
     * {@link ContactEntry.GENDER#FEMALE}.
     */
    private ContactEntry.GENDER _Gender = ContactEntry.GENDER.UNKNOWN;

    private View.OnTouchListener _TouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            _ContactHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new pet or editing an existing one.
        Intent intent = getIntent();
        _CurrentContactUri = intent.getData();

        // If the intent DOES NOT contain a pet content URI, then we know that we are
        // creating a new contact
        if (_CurrentContactUri == null) {
            // This is a new contact, so change the app bar to say "Add a Contact"
            setTitle(getString(R.string.editor_activity_title_new_contact));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a contact that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing contact, so change app bar to say "Edit Contact"
            setTitle(getString(R.string.editor_activity_title_edit_contact));

            // Initialize a loader to read the contact data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_CONTACT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        _NameEditText = findViewById(R.id.edit_contact_name);
        _EmailEditText = findViewById(R.id.edit_contact_email);
        _NumberEditText = findViewById(R.id.edit_contact_number);
        _GenderSpinner = findViewById(R.id.spinner_gender);

        _NameEditText.setOnTouchListener(_TouchListener);
        _EmailEditText.setOnTouchListener(_TouchListener);
        _NumberEditText.setOnTouchListener(_TouchListener);
        _GenderSpinner.setOnTouchListener(_TouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        _GenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        _GenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        _Gender = ContactEntry.GENDER.MALE;
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        _Gender = ContactEntry.GENDER.FEMALE;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                _Gender = ContactEntry.GENDER.UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save new pet into database.
     */
    private void saveContact() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = _NameEditText.getText().toString().trim();
        String emailString = _EmailEditText.getText().toString().trim();
        String numberString = _NumberEditText.getText().toString().trim();

        // Check if this is supposed to be a new contact
        // and check if all the fields in the editor are blank
        if (_CurrentContactUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(emailString) &&
                TextUtils.isEmpty(numberString) && _Gender == ContactEntry.GENDER.UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new contact.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ContactEntry.COLUMN_CONTACT_NAME, nameString);
        values.put(ContactEntry.COLUMN_CONTACT_EMAIL, emailString);
        values.put(ContactEntry.COLUMN_CONTACT_GENDER, _Gender.ordinal());
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        String number = "unprovided";
        if (!TextUtils.isEmpty(numberString)) {
            number = numberString;
        }
        values.put(ContactEntry.COLUMN_CONTACT_NUMBER, number);

        // Determine if this is a new or existing contact by checking if _CurrentContactUri is null or not
        if (_CurrentContactUri == null) {
            // This is a NEW contact, so insert a new contact into the provider,
            // returning the content URI for the new contact.
            Uri newUri = getContentResolver().insert(ContactEntry.CONTENT_URI, values);


            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_contact_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_contact_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING contact, so update the pet with content URI: _CurrentContactUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because _CurrentContactUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(_CurrentContactUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_contact_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_contact_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (_CurrentContactUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save Contact to database
                saveContact();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the contact hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (_ContactHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                ContactEntry._ID,
                ContactEntry.COLUMN_CONTACT_NAME,
                ContactEntry.COLUMN_CONTACT_EMAIL,
                ContactEntry.COLUMN_CONTACT_GENDER,
                ContactEntry.COLUMN_CONTACT_NUMBER};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                _CurrentContactUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of pet attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_NAME);
            int emailColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_EMAIL);
            int genderColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_GENDER);
            int numberColumnIndex = cursor.getColumnIndex(ContactEntry.COLUMN_CONTACT_NUMBER);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            int gender = cursor.getInt(genderColumnIndex);
            String number = cursor.getString(numberColumnIndex);

            // Update the views on the screen with the values from the database
            _NameEditText.setText(name);
            _EmailEditText.setText(email);
            _NumberEditText.setText(number);

            // Gender is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            if (gender == ContactEntry.GENDER.MALE.ordinal()) {
                _GenderSpinner.setSelection(1);
            } else if (gender == ContactEntry.GENDER.FEMALE.ordinal()) {
                _GenderSpinner.setSelection(2);
            } else {
                _GenderSpinner.setSelection(0);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        _NameEditText.setText("");
        _EmailEditText.setText("");
        _NumberEditText.setText("");
        _GenderSpinner.setSelection(0); // Select "Unknown" gender
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!_ContactHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the contact in the database.
     */
    private void deletePet() {
        // Only perform the delete if this is an existing contact.
        if (_CurrentContactUri != null) {
            // Call the ContentResolver to delete the contact at the given content URI.
            // Pass in null for the selection and selection args because the _CurrentContactUri
            // content URI already identifies the contact that we want.
            int rowsDeleted = getContentResolver().delete(_CurrentContactUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_contact_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_contact_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}