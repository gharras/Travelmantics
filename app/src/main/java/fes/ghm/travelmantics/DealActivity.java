package fes.ghm.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

/**
 * because our DealActivity will also contain features to edit and delete activity
 * we will rename it to DealActivity
 */
public class DealActivity extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private EditText etTitle;
    private EditText etPrice;
    private EditText etDescription;
    private TravelDeal mDeal;
    /**
     * we create a constant that will hold the constant that we passed as second parameter
     * in startActivityForResult that we will use as a handle
     */
    private static final int PICTURE_RESULT = 42;

    /**
     * create the imageView property to show our image
     */
    private ImageView image;

    /**
     * we will add another button to upload an image and save it into our Firebase Storage
     * and we will also rename the activity xml file from activity_deal to activity_deal
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        // getting the instance of FirebaseDatabase si we can work with it
//        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // getting a reference to the database we want to work on
//        mDatabaseReference = mFirebaseDatabase.getReference().child("traveldeals");

        /**
         * now we will affect our object from the FirebaseUtil class to our object here
         */
        FirebaseUtil.openFirebaseReference("traveldeals", (ListActivity) this.getParent());
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        // getting reference to our view in the insert layout
        etTitle = findViewById(R.id.ptTitle);
        etPrice = findViewById(R.id.ptPrice);
        etDescription = findViewById(R.id.ptDescription);
        image = findViewById(R.id.image);

        // now we will get the intent in case it has any extra data that need to show up in the activity
        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        // if the deal is null we will create a new deal to pass it to the deal property of the class
        if (deal == null) {
            deal = new TravelDeal();
        }

        this.mDeal = deal;
        // then set the text for each textView
        etTitle.setText(deal.getTitle());
        etPrice.setText(deal.getPrice());
        etDescription.setText(deal.getDescription());
        showImage(mDeal.getImageUrl());

        Button btnImage = findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent.ACTION_GET_CONTENT allow the user to select a particular kind of data
                // and return it
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // we set the type of the intent into image/jpeg
                intent.setType("image/jpeg");
                // we specify that we will only allow local data images on the particular device
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

                // we call the startActivityForResult calling the createChooser method as first
                // parameter because we are awaiting an image file to be selected
                // in our createChooser we will pass the created intent and a Tag that we will use
                // as a title and we will pass a code as a handle to identify the sender as second
                // parameter of startActivityForResult

                startActivityForResult(Intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });

    }


    /**
     * we will override the onActivityResult method to manage our upload picture
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check the requestCode if he is our sender and if the resultCode is OK
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK) {
            /**
             * to upload a file in FirebaseStorage we need 3 steps:
             *  1- locate the file you want to upload and put the location in a URI object
             *  2- we need the storage reference where we want to upload the data
             *  3- we use the putFile method on the reference this method will return an
             *  UploadTask object who is an async task and we can listener to success or failure
             *  of our operation in it
             */

            final Uri imageUri = data.getData();
            // and we put the imageUril as parameter for the child method on our reference
            // so that the file can be saved there
            final StorageReference storageReference = FirebaseUtil.mStorageReference
                    .child(imageUri.getLastPathSegment());
            //now we upload the picture to the FirebaseStorage
            // because our putFile method return an asynchronous upload task
            // we want to listen to the result of the task so that we can take action
            // depending on the result
            // so we add an addOnSuccessListener we pass the activity as first parameter
            // and as for the second parameter we pass OnSuccessListener object
            storageReference.putFile(imageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                            // to register the link of our stored object (image in this project) we will need
                            // to call the getDownloadUrl() method from the storageReference and add
                            // the addOnSuccessListener to it, we will need to override a OnSuccess method
                            // that has a Uri as parameter who will content the link we need
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // we register the name of the image file we just saved
                                    String imageName = taskSnapshot.getStorage().getName();
                                    mDeal.setImageName(imageName);
                                    mDeal.setImageUrl(uri.toString());
                                    showImage(uri.toString());
                                }
                            });
                        }
                    });
        }
    }

    /**
     * to be able to use our menu we need to override the onCreateOptionsMenu
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater is the object who can create Menu from xml resources
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        return true;
    }

    /**
     * we make our delete menu invisible if the user is not an admin
     * and also enable or disable the change on the deal
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.delete_menu).setVisible(FirebaseUtil.isAdmin);
        menu.findItem(R.id.save_menu).setVisible(FirebaseUtil.isAdmin);
        findViewById(R.id.btnImage).setEnabled(FirebaseUtil.isAdmin);
        setEnableEditText(FirebaseUtil.isAdmin);
        return true;
    }

    /**
     * to send our data to firebase after we click on save we need to override this method
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // we will use switch in case we need to add more options to our menu

        switch (item.getItemId()) {
            case R.id.save_menu:
                // method to save the deal in the firebase database
                saveDeal();
                // using a Toast to show the user that his object was saved
                Toast.makeText(this, "Deal Saved", Toast.LENGTH_LONG).show();
                // clear all the edit text content so we can add new deals
                clean();
                // return to the ListActivity activity
                backToList();
                return true;

            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this, "Deal Deleted", Toast.LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void clean() {
        etTitle.setText("");
        etPrice.setText("");
        etDescription.setText("");

        etTitle.requestFocus();
    }

    /**
     * method responsible to save our deal in the firebase database
     * we change the text we have to the deal object
     */
    private void saveDeal() {
        mDeal.setTitle(etTitle.getText().toString());
        mDeal.setPrice(etPrice.getText().toString());
        mDeal.setDescription(etDescription.getText().toString());

        // now we need to knew if the object is new or old by seeing if the ID already exist or not
        // we do that by watching if the ID is null or not
        if (mDeal.getId() == null) {
            // we create the object we will send to the database
            // we want need to put an id for the deal because the push() method will take care of it
            mDatabaseReference.push().setValue(mDeal);
        } else {
            // editing the data if the deal already exist
            mDatabaseReference.child(mDeal.getId()).setValue(mDeal);
        }

    }

    /**
     * now we will create the Delete Deal method to delete a node
     */

    private void deleteDeal() {
        if (mDeal == null) {
            // we do an error message to the user by a Toast
            Toast.makeText(this, "Please save the Deal before deleting it", Toast.LENGTH_LONG);
            return;
        }
        mDatabaseReference.child(mDeal.getId()).removeValue();
        // now we add the code we need to delete our image file that we have stored before
        if(!mDeal.getImageName().isEmpty() && mDeal.getImageName() != null) {
            StorageReference storageReference = FirebaseUtil.mStorageReference
                    .child(mDeal.getImageName());
            storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Delete Image", e.getMessage());
                }
            });
        }
    }

    /**
     * we create a method that will move us to the ListActivity after saving editing or deleting a deal
     */

    private void backToList() {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void setEnableEditText(boolean isEnabled) {
        findViewById(R.id.ptTitle).setEnabled(isEnabled);
        findViewById(R.id.ptDescription).setEnabled(isEnabled);
        findViewById(R.id.ptPrice).setEnabled(isEnabled);
    }

    private void showImage(String imageUrl) {
        if (imageUrl != null &&!imageUrl.isEmpty()) {
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(imageUrl)
                    .resize(width, width * 2 / 3)
                    .centerCrop()
                    .into(image);
        }
    }
}
