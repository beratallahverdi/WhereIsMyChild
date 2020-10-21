package beratdamla.wimchild;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoDatabase;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class PersonsViewAdapter extends RecyclerView
        .Adapter<PersonsViewAdapter
        .PersonHolder> {

    StitchAppClient client;
    RemoteMongoClient mongoClient;
    RemoteMongoDatabase wimchildb;
    RemoteMongoCollection<Document> users;
    RemoteMongoCollection<Document> locations;

    private static String LOG_TAG = "PersonsViewAdapter";
    private ArrayList<User> mDataset;
    private static MyClickListener myClickListener;
    private User user;

    public static class PersonHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView personDisplayName;
        ImageView personImage;
        ImageButton personMessage;
        ImageButton personCall;
        CardView personCard;

        public PersonHolder(View itemView) {
            super(itemView);
            personImage = itemView.findViewById(R.id.person_image);
            personDisplayName = itemView.findViewById(R.id.person_displayname);
            personMessage = itemView.findViewById(R.id.person_message);
            personCall = itemView.findViewById(R.id.person_call);
            personCard = itemView.findViewById(R.id.card_view);
            Log.i(LOG_TAG, "Adding Listener");

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getAdapterPosition(), v);
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public PersonsViewAdapter(ArrayList<User> myDataset, User user) {
        mDataset = myDataset;
        client = Stitch.getDefaultAppClient();
        this.user=user;
    }

    @Override
    public PersonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_person, parent, false);

        PersonHolder dataObjectHolder = new PersonHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(final PersonHolder holder, final int position) {
        holder.personDisplayName.setText("Adi Soyadi:"+mDataset.get(position).getDisplayName()+"\nEmail:"+mDataset.get(position).getEmail()+"\nTelefon:"+mDataset.get(position).getCellphone());
        holder.personMessage.setTag(mDataset.get(position).getCellphone());
        if(mDataset.get(position).getMember() == false){
            holder.personDisplayName.setTextColor(Color.BLACK);
            holder.personCard.setBackgroundColor(Color.CYAN);
        }
        holder.personMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton btn = (ImageButton) v;
                holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", btn.getTag().toString(), null)));
            }
        });
        holder.personCall.setTag(mDataset.get(position).getCellphone());
        holder.personCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton btn = (ImageButton) v;
                holder.itemView.getContext().startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", btn.getTag().toString(), null)));
            }
        });
    }

    public void addItem(User dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        public void onItemClick(int position, View v);
    }
}