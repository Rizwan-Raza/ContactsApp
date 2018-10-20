package com.wampinfotech.contacts;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.wampinfotech.contacts.data.ContactContract.ContactEntry;

/**
 * {@link ContactCursorAdaptor} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of pet data in the {@link Cursor}.
 */
public class ContactCursorAdaptor extends CursorAdapter {

    /**
     * Constructs a new {@link ContactCursorAdaptor}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    ContactCursorAdaptor(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameView = view.findViewById(R.id.contact_name);
        TextView emailView = view.findViewById(R.id.contact_summary);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_CONTACT_NAME));
        String email = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_CONTACT_EMAIL));

        // If the contact email is empty string or null, then use some default text
        // that says "Unknown email", so the TextView isn't blank.
        if (TextUtils.isEmpty(email)) {
            email = context.getString(R.string.unknown_email);
        }

        // Update the TextViews with the attributes for the current contact
        nameView.setText(name);
        emailView.setText(email);
    }
}