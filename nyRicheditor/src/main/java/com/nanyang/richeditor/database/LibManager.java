package com.nanyang.richeditor.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

public class LibManager {
    private Context context;

    public LibManager(Context context) {
        this.context = context;
    }

    // Create or open a secondary database
    public SQLiteDatabase getDatabase(String dbName) {
        return context.openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
    }

    // Other methods to interact with the secondary databases
    public void createTableInDatabase(String dbName, String tableName, String tableSchema) {
        SQLiteDatabase db = getDatabase(dbName);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " (" + tableSchema + ")");
        db.close();
    }

    public void insertIntoDatabase(String dbName, String tableName, ContentValues values) {
        SQLiteDatabase db = getDatabase(dbName);
        db.insert(tableName, null, values);
        db.close();
    }

    public Cursor queryDatabase(String dbName, String tableName, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        SQLiteDatabase db = getDatabase(dbName);
        return db.query(tableName, columns, selection, selectionArgs, groupBy, having, orderBy);
    }

    public void closeDatabase(SQLiteDatabase db) {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
}

    // Method to show the AlertDialog
  /*  public void showLibsDialog() {
        // Inflate the custom layout for the AlertDialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_libs, null);

        // Initialize the ListView
        ListView listView = dialogView.findViewById(R.id.listViewLibs);

        // Retrieve all DataLib entries from the database
        List<DataLib> dataLibs = getAllDataLibs();

        // Create an adapter for the ListView
        DataLibAdapter adapter = new DataLibAdapter(context, dataLibs);

        // Set the adapter to the ListView
        listView.setAdapter(adapter);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setTitle("Manage Libraries");

        // Set neutral button to add a new library
        builder.setNeutralButton("Add Library", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call method to show add library dialog
                showAddLibraryDialog();
            }
        });

        // Show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Method to show the dialog for adding a new library
    private void showAddLibraryDialog() {
        // Inflate the layout for the dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_library, null);

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        builder.setTitle("Add New Library");

        // Set positive button for adding the library
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Call method to handle addition of new library
              //  handleAddLibrary();
            }
        });

        // Set negative button for canceling
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Method to handle addition of a new library
   /* private void handleAddLibrary() {
        // Retrieve the values entered by the user for name, title, and order
        EditText editTextName = ((AlertDialog) dialog).findViewById(R.id.editTextName);
        EditText editTextTitle = ((AlertDialog) dialog).findViewById(R.id.editTextTitle);
        EditText editTextOrder = ((AlertDialog) dialog).findViewById(R.id.editTextOrder);

        String name = editTextName.getText().toString().trim();
        String title = editTextTitle.getText().toString().trim();
        int order = Integer.parseInt(editTextOrder.getText().toString());

        // Add the new library to the database
        addNewLibrary(name, title, order);

        // Refresh the library list dialog
        showLibsDialog();
    }*/

    // Method to retrieve all DataLib entries from the database
   /* private List<DataLib> getAllDataLibs() {
        // Implement this method to retrieve data from your database
    }

    // Method to add a new library to the database
    private void addNewLibrary(String name, String title, int order) {
        // Implement this method to add a new library to your database
    }


}*/

