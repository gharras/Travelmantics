package fes.ghm.travelmantics;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * because we need to use our Database connection in multiple activity,
 * rather than declaring them in each activity, we will create this utility class
 * that will be responsible to communicate with our Firebase database and we will
 * use statics methods to call to them whenever we need
 * <p>
 * <p>
 * Created by @author = GHARRAS Mohammed
 * On 29/07/2019
 * Email med.gharras@gmail.com
 */
public class FirebaseUtil {

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseReference;

    private static FirebaseUtil mFirebaseUtil;
    // creating a FirebaseAuth reference so we ca authenticate to our firebase database
    public static FirebaseAuth mFirebaseAuth;
    // create the FirebaseAuth.AuthStateListener so we can see if the user is already logged in or not
    public static FirebaseAuth.AuthStateListener mAuthStateListener;
    // to be able to connect to the Firebase Storage we will create new property
    public static FirebaseStorage mFirebaseStorage;
    // and like for the database we will also need a reference for our storage
    public static StorageReference mStorageReference;

    // we create a private activity that we will use as our caller activity
    // we change the caller from Activity to ListActivity to be able to call the showMenu method
    private static ListActivity caller;
    public static ArrayList<TravelDeal> mDeals;

    // add a boolean to see if the user is an administrators or not
    public static boolean isAdmin;

    private FirebaseUtil() {
    }

    // change the callerActivity from Activity to ListActivity
    public static void openFirebaseReference(String ref, final ListActivity callerActivity) {
        // if we don't have any instance of the FIrebaseUtil class we will instenciate one
        if (mFirebaseUtil == null) {
            mFirebaseUtil = new FirebaseUtil();
            mFirebaseDatabase = FirebaseDatabase.getInstance();

            // initialize our caller activity
            caller = callerActivity;

            // because we instanciate our array only if the mFirebaseUtil is null
            // we got a problem when deleting a node, and what more our data is duplicated
            // so to resolve the problem we need to instanciate the Array each time we call this method
            // so we will do it outside of the if statement
            // mDeals = new ArrayList<>();


            // initialize the FirebaseAuth object by using the getInstance method
            mFirebaseAuth = FirebaseAuth.getInstance();
            // initialize the mAuthStateListener and for that we will need to declare
            // the onAuthStateChanged method
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                /**
                 * in this method we will check whether the use is logged in or not
                 *
                 * @param firebaseAuth
                 */
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    // to only call the signIn method if there is no user connected
                    if (firebaseAuth.getCurrentUser() == null) {
                        FirebaseUtil.signIn();
                    } else {
                        // we retrieve the uid of the user to see if he is an admin or not
                        String userId = firebaseAuth.getUid();
                        checkAdmin(userId);
                    }
                    Toast.makeText(callerActivity.getBaseContext(), "Welcome Back!", Toast.LENGTH_LONG).show();
                }
            };
            // we make a call to our connectStorage method the moment we open the firebaseDatabase
            connectStorage();
        }
        mDeals = new ArrayList<>();
        mDatabaseReference = mFirebaseDatabase.getReference().child(ref);
    }

    private static void checkAdmin(String userId) {
        isAdmin = false;
        // create a reference to the administrators node and look if they have a child with userId
        DatabaseReference ref = mFirebaseDatabase.getReference().child("administrators")
                .child(userId);
        // add a listener
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //we set the isAdmin = true
                // because this method will only be triggered only if a child with the userId
                //of the current user was found in the administrators node
                isAdmin = true;
                // Log.i("Admin: " , "You are an administrator");
                // we call the showMenu method to change the visibility of our menu
                caller.showMenu();
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
        // add the listener to our ref
        ref.addChildEventListener(listener);
    }


    /**
     * we copy the code from guide line for sign in and will only use the one
     * with email and google
     */

    private static void signIn() {

        // Choose authentication providers
        // if you have problem in logging with your google account go to this link and save your app public name in there
        // https://console.developers.google.com/apis/credentials/consent
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder()
                        .build());
//                            new AuthUI.IdpConfig.PhoneBuilder().build(),
//                            new AuthUI.IdpConfig.FacebookBuilder().build(),
//                            new AuthUI.IdpConfig.TwitterBuilder().build());

        // Create and launch sign-in intent
        // we cannot start an activity from outside an activity
        // so we will add a new parameter to our openFirebaseReference
        // where will will put the caller Activity to use it for calling
        caller.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
//                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
        caller.showMenu();
    }


    // create the method that will attack our listener with the user once he is logged in
    public static void attachListener() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    // the method that will detach the listener once the user is logged out
    public static void dettachListener() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    /**
     * create the method we will use to connect with our storage
     */
    public static void connectStorage(){
        // instanciate firebase storage and the reference
        mFirebaseStorage = FirebaseStorage.getInstance();
        // we will get the folder that we created with the firebase console
        mStorageReference = mFirebaseStorage.getReference().child("deals_pictures");
    }

}
