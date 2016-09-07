package de.example.helge.shoppinglist;

/**
 * Created by Administrator on 06.09.2016.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private ShoppingMemoDataSource dataSource;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ShoppingMemo testMemo = new ShoppingMemo("Golf", 5,105);
        Log.d(LOG_TAG, "Inhalt Main" + testMemo.toString());
        dataSource = new ShoppingMemoDataSource(this);
//        Log.d(LOG_TAG,"Quelle wird geoeffnet");
//        dataSource.open();
        activateAddButton();
        initializeContextualActionBar();
//        Log.d(LOG_TAG,"Quelle wird geschlossen");
//        dataSource.close();
    }

    private void initializeContextualActionBar() {
        final ListView shoppingMemoListView = (ListView)findViewById(R.id.listview_shopping_memos);
        shoppingMemoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        shoppingMemoListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                getMenuInflater().inflate(R.menu.menu_contextual_menu_bar, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.delete:
                        SparseBooleanArray touchedMemoPosition = shoppingMemoListView.getCheckedItemPositions();
                        for(int i=0;i<touchedMemoPosition.size();i++) {
                            boolean isCheckd = touchedMemoPosition.valueAt(i);
                            if(isCheckd) {
                                int posInListView = touchedMemoPosition.keyAt(i);
                                ShoppingMemo memo = (ShoppingMemo) shoppingMemoListView.
                                        getItemAtPosition(posInListView);
                                dataSource.deleteShoppingMemo(memo);
                            }
                        }
                        showAllListEntries();
                        actionMode.finish();
                        return true;
                    default:
                        return false;
                }

            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }

            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        dataSource.open();
    }
    @Override
    protected void onStop() {
        super.onStop();
        dataSource.close();
    }
    @Override
    protected void onPause() {
        super.onPause();
        dataSource.close();
    }
    @Override
    protected void onResume() {
        super.onResume();
        dataSource.open();
    }

    private void activateAddButton(){
        Button button = (Button)findViewById(R.id.button_add_product);
        final EditText editQuantity = (EditText)findViewById(R.id.editText_quantity);
        final EditText editProduct = (EditText)findViewById(R.id.editText_product);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = editQuantity.getText().toString();
                String product = editProduct.getText().toString();
                if(TextUtils.isEmpty(quantity)){
                    editQuantity.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                if(TextUtils.isEmpty(product)){
                    editProduct.setError(getString(R.string.editText_errorMessage));
                    return;
                }
                int quant = Integer.parseInt(quantity);
                editQuantity.setText("");
                editProduct.setText("");

                dataSource.createShoppingMemo(product, quant);

                InputMethodManager inputMethodManager;
                inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if(getCurrentFocus() != null){
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
                }
                showAllListEntries();
//                dataSource.close();
            }
        });
    }

    private void showAllListEntries(){
        List<ShoppingMemo> list = dataSource.getAllShoppingMemos();
        ArrayAdapter<ShoppingMemo> adapter = new ArrayAdapter<ShoppingMemo>(this,android.R.layout.
        simple_list_item_multiple_choice, list);
        ListView listView = (ListView)findViewById(R.id.listview_shopping_memos);
        listView.setAdapter(adapter);
    }


    @Override

    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;

    }


    @Override

    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private AlertDialog createEditShoppingMenuDialog(final ShoppingMemo memo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogsView = inflater.inflate(R.layout.dialog_edit_shopping_memo, null);

        final EditText editTextQuantity = (EditText) dialogsView.findViewById(R.id.editText_quantity);
        editTextQuantity.setText(String.valueOf(memo.getQuantity()));

        final EditText editTextProduct = (EditText) dialogsView.findViewById(R.id.editText_product);
        editTextProduct.setText(String.valueOf(memo.getProduct()));

        builder.setView(dialogsView).setTitle(R.string.dialog_title).setPositiveButton(R.string.dialog_button_positive
                , new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String quantityString = editTextQuantity.getText().toString();
                        String product = editTextProduct.getText().toString();
                        if (TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(product)) {
                            return;
                        }
                        int quantity = Integer.parseInt(quantityString);
                        ShoppingMemo sMemo = dataSource.updateShoppingMemo(memo.getId(),
                                product, quantity);
                        showAllListEntries();
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(R.string.dialog_button_negative, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
    return builder.create();
    }
}
