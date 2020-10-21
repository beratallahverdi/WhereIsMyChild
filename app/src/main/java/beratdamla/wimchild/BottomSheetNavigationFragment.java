package beratdamla.wimchild;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class BottomSheetNavigationFragment extends BottomSheetDialogFragment {
    GoogleSignInOptions gso;
    GoogleSignInClient mGoogleSignInClient;

    public static BottomSheetNavigationFragment newInstance() {

        Bundle args = new Bundle();

        BottomSheetNavigationFragment fragment = new BottomSheetNavigationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //Bottom Sheet Callback
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            //check the slide offset and change the visibility of close button
            if (slideOffset > 0.5) {
                closeButton.setVisibility(View.VISIBLE);
            } else {
                closeButton.setVisibility(View.GONE);
            }
        }
    };

    private ImageView closeButton;
    private TextView bottom_email;
    private TextView bottom_name;
    private CircleImageView bottom_image;

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        //Get the content View
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        final View contentView = View.inflate(getContext(), R.layout.bottom_navigation_drawer, null);
        mGoogleSignInClient = GoogleSignIn.getClient(getContext().getApplicationContext(), gso);
        dialog.setContentView(contentView);

        NavigationView navigationView = contentView.findViewById(R.id.navigation_view);
        //((HomepageActivity)getActivity()).getUser().getName()
        //((HomepageActivity)getActivity()).getUser().getEmail()
        //((HomepageActivity)getActivity()).getUser().getImageUri()
        Picasso.with(getContext()).load(R.mipmap.logo).into((CircleImageView)contentView.findViewById(R.id.bottom_nav_person_image));
        if(((HomepageActivity)getActivity()).getUser().getImageUri()!=null)
            Picasso.with(getContext()).load(((HomepageActivity)getActivity()).getUser().getImageUri()).into((CircleImageView)contentView.findViewById(R.id.bottom_nav_person_image));
        ((TextView)contentView.findViewById(R.id.bottom_nav_person_name)).setText(((HomepageActivity)getActivity()).getUser().getDisplayName());
        ((TextView)contentView.findViewById(R.id.bottom_nav_person_email)).setText(((HomepageActivity)getActivity()).getUser().getEmail());

        //implement navigation menu item click event
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_nav_info:
                        Intent intent = new Intent(getContext().getApplicationContext(), InfoActivity.class);
                        Bundle bundle = new Bundle();
                        intent.putExtra(HomepageActivity.USER_ACCOUNT,((HomepageActivity)getActivity()).getUser());
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    case R.id.bottom_nav_settings:
                        startActivity(new Intent(getContext().getApplicationContext(),SettingsActivity.class));
                        break;
                    case R.id.bottom_nav_signout:
                        SharedPreferences settings = getContext().getSharedPreferences(HomepageActivity.PREFS, Context.MODE_PRIVATE);
                        settings.edit().clear().commit();
                        startActivity(new Intent(getContext().getApplicationContext(),LoginActivity.class));
                        getActivity().stopService(new Intent(getContext().getApplicationContext(), WimChildService.class));
                        getActivity().finish();
                        break;

                }
                return false;
            }
        });
        closeButton = contentView.findViewById(R.id.close_image_view);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss bottom sheet
                dismiss();
            }
        });

        //Set the coordinator layout behavior
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        //Set callback
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}