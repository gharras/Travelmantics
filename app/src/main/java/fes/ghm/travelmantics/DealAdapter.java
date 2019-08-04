package fes.ghm.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * this class will do the job of an adapter to link our data to the recyclerView
 * we need our class to extends the RecyclerView.Adapter class, and we pass to it the ViewHolder class
 * we just created
 * and finally import the abstract methods that exist on it
 * Created by @author = GHARRAS Mohammed
 * On 30/07/2019
 * Email med.gharras@gmail.com
 */
public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {

    private ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    /**
     * we need to show a thumbnail of our image in the listActivity
     */
    private ImageView imageDeal;

    /**
     * we create a default constructor where we will create our link to the database
     */
    public DealAdapter() {
//        FirebaseUtil.openFirebaseReference("traveldeals" );
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        //we populate the array of our deals from the deals we have in the FirebaseUtil class
        deals = FirebaseUtil.mDeals;
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
                // we Log the Deal title to know that it work
                Log.i("Deal : ", td.getTitle() );

                // we put the id of the deal to the push id that was generated by Firebase
                // so that we can read it easily later
                td.setId(dataSnapshot.getKey());

                // add the deal to the deals arrays
                deals.add(td);

                // we notify our recyclerView that we added a new item by passing the size of the
                // arrayList of our deals - 1, so that the User Interface can be updated
                notifyItemInserted(deals.size() - 1);
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

    }


    /**
     * this method is called once a new ViewHolder is created
     * @param parent
     * @param viewType
     * @return
     */

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // we call the context of the parent of the view Holder
        Context context = parent.getContext();
        // then we inflate the view we want to show as row in our RecyclerView
        View itemView = LayoutInflater.from(context).inflate(R.layout.rv_row, parent, false);

        // finally we return the ViewHolder passing it the itemView we just created
        return new DealViewHolder(itemView);
    }

    /**
     * this method called when we want to display data
     * @param holder
     * @param position
     */

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        holder.bind(deals.get(position));
    }

    /**
     * we count the items on the array list we created and return it's size
     * @return
     */
    @Override
    public int getItemCount() {
        return deals.size();
    }

    /**
     * this class will be called everytime a new row of the recyclerView is created
     * and to enable the click on our View holder we will implements an View.OnClickListener
     * and impliment the onCLick event method
     */
    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // the TextView we use for now to put the text on it
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;

        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            // get the imageDeal imageView
            imageDeal = itemView.findViewById(R.id.imageDeal);
            // add the setOnClickListener on our itemView so we can detect when it's clicked
            itemView.setOnClickListener(this);
        }

        /**
         * bind our data with the TextView in the recyclerView
         * @param deal
         */
        public void bind (TravelDeal deal) {
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            // when binding our data we will call our showImage method
            showImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            // first we need to get the position of the clicked View by using the method
            // getAdapterPosition()
            int position = getAdapterPosition();

            // Log the position to be sure that it's working
            Log.i("Click Position: ", String.valueOf(position));

            // now that we have the position we can get the travel deal selected thanks to our array
            TravelDeal selectedTravelDeal = deals.get(position);

            // now we need to pass this data to our DealAcitity and show the data in the textViews
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            // we pass our data as extra
            // and to pass an object we need to make our travelDeal object either serializable
            // or Parcelable, for more speed and efficiency it better to use Parcelable but it will need some extra code
            // so because our Deal is simple we will only use Serializable
            intent.putExtra("Deal", selectedTravelDeal);
            // we use the context of the view parameter to start our new activity
            // and we will need to do some change to be able to get the extra data we sent to it
            view.getContext().startActivity(intent);

        }
        /**
         * create a show image to show the thimbnail
         */
        private void showImage(String imageUrl){
            /**
             * test to get the size of the thumbnail depending on the content
             */

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .resize(180, 180)
                        .centerCrop()
                        .into(imageDeal);
            }

        }
    }
}
