package fes.ghm.travelmantics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

//        FileInputStream serviceAccount =
//                new FileInputStream("path/to/serviceAccountKey.json");
//
//        FirebaseOptions options = new FirebaseOptions.Builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .setDatabaseUrl("https://travelmantics-c4e2c.firebaseio.com")
//                .build();
//
//        FirebaseApp.initializeApp(options);
        /**
         * now that we have created the FirebaseUtil class let's use it rather than
         * creating instance directly
         */
//        mFirebaseDatabase = FirebaseDatabase.getInstance();
//        mDatabaseReference = mFirebaseDatabase.getReference().child("traveldeals");
        /**
         * now we will affect our object from the FirebaseUtil class to our object here
         */


    }

    /**
     * overriding this method to show the menu that we will use to add a new Deal
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_activity_menu, menu);

        // fine the insert_menu and make sure that if the user is an admninistrator, the insert menu will be visible
//        MenuItem insertMenuItem = menu.findItem(R.id.insert_menu);
//        insertMenuItem.setVisible(FirebaseUtil.isAdmin);
        return true;
    }

    /**
     * method called when we use invalidateOptionMenu
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // fine the insert_menu and make sure that if the user is an admninistrator, the insert menu will be visible
        MenuItem insertMenuItem = menu.findItem(R.id.insert_menu);
        insertMenuItem.setVisible(FirebaseUtil.isAdmin);
        return true;
    }

    /**
     * method responsible for the action to use when a menu item is selected
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.insert_menu :
                Intent intent = new Intent(this, DealActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout_menu :
                /**
                 * we copy the code for sign out from the official docs
                 */
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // we will log the logout for now
                                Log.i("LogOut: ", "User Logged Out");
                                // and to get back to the sign in page we will need
                                // to attach the listener because our signIn method can only be called from there
                                FirebaseUtil.attachListener();
                            }
                        });
                // then we detach the listener
                FirebaseUtil.dettachListener();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * we will override the onPause and onResume method to attach and dettach the AuthStateListener
     */

    @Override
    protected void onResume() {
        super.onResume();
        /**
         * we moved the code from onCreate method to on Resume because with log in he can't create
         * a recyclerView so when he call the activity it's already late to use it
         */
        // because we copied all our code to the RecyclerViewAdapter class
        // now we just comment this code because we don't need it now

        FirebaseUtil.openFirebaseReference("traveldeals", this);
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        // create the childEventListener who will be responsible for the communication
        // between our app and Firebase

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                TextView tvDeals = findViewById(R.id.tvDeals);

                // dataSnapshot instance contains data from a firebase database location
                // any time you read database data, you receive the data as a dataSnapshot
                // with the getValue method we are serializing the data and putting it
                // in our TravelDeal class
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);

//                tvDeals.setText(tvDeals.getText() + "\n" + td.getTitle());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.addChildEventListener(mChildEventListener);


        // create a reference to our recyclerViw
        RecyclerView rvDeals = findViewById(R.id.rvDeals);
        // we declare our adapter
        final DealAdapter adapter = new DealAdapter();
        // set the adapter on our recyclerView
        rvDeals.setAdapter(adapter);

        // finally we will declar a linearLayoutManager and apply it on our RecyclerView
        LinearLayoutManager dealLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        // finally we will set the layout of our recyclerView
        rvDeals.setLayoutManager(dealLayoutManager);

        /**
         * end copy from onCreate method
         */
        FirebaseUtil.attachListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUtil.dettachListener();
    }

    /**
     * this method will be responsible to show or hide the menu in listActivity
     * depending on if the user is an administrator or not
     */
    public void showMenu(){
        // we will call the invalidateOptionMenu() method
        // we use this method to tell the android studio that the content of the menu has changed
        // and the menu should be redrawn from calling onPrepareOptionsMenu

        invalidateOptionsMenu();
    }
}
