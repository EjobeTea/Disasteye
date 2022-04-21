package com.example.disasteye;
//package com.example.pull_google_news_from_firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.disasteye.databinding.ActivityMapsBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;




public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    public MapsActivity(){

    }
    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    SearchView searchView;

    Marker searchMarker = null;

    ArrayList<Event> eventArray = new ArrayList<>();
    ArrayList<Event> wildfireArray = new ArrayList<>();
    ArrayList<Event> volcanoesArray = new ArrayList<>();
    ArrayList<Event> seaLakeArray = new ArrayList<>();
    ArrayList<Event> stormArray = new ArrayList<>();

    private ConstraintLayout bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private LinearLayout headerLayout;
    private ImageView swiper;
    private ImageButton aboutUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setMaxHeight(1750);
        bottomSheetBehavior.setPeekHeight(200);
        bottomSheetBehavior.setHideable(false);
        headerLayout = findViewById(R.id.header_layout);


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


        //Request will receive a URL and gather data from the API!

        HTTPRequest request = new HTTPRequest();
        try {
            request.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //R.id.idSearchView is found in the activity_maps.xml  -- Searches given location
        //More on searchView doc: https://abhiandroid.com/ui/searchview#SearchView_Methods_In_Android
        searchView = (SearchView) findViewById(R.id.idSearchView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        //Add the coordinates for all our markers.
        this.eventArray = request.getEvents();

        //OnQueryTextListener() -- call backs to changed made in query text: https://developer.android.com/reference/android/widget/SearchView.OnQueryTextListener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String locationName = searchView.getQuery().toString();
                List<Address> addresses = null;

                if (locationName != null) {
                    // Create geocoder obj -- takes address and finds location: https://developer.android.com/reference/android/location/Geocoder
                    Geocoder geocoder = new Geocoder(MapsActivity.this);

                     //Given locationName, it will gecode the location on map, and adds to addressList
                    try {
                        addresses = geocoder.getFromLocationName(locationName, 1);
                    } catch (IOException except) {
                        except.printStackTrace();
                    }

                    //Get location, from the first position listed in addressList:
                   try{
                       Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        //Add marker, without creating a duplicate marker from previous search:
                       if(searchMarker != null) {
                           searchMarker.remove();
                       }
                       searchMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(locationName));

                        // Move to pos.
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }
                   catch(Exception except) {
                       Toast.makeText(MapsActivity.this, "Location not found. Try again.",
                               Toast.LENGTH_SHORT).show();
                       except.printStackTrace();
                    }
                }
                //Returns false to let search view perform default action:
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        final MaterialToolbar TopAppBar = (MaterialToolbar) findViewById(R.id.topAppBar);
        TopAppBar.setNavigationIcon(R.drawable.ic_menu_24);
        TopAppBar.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
                        drawer.openDrawer(Gravity.LEFT);
                    }
                }
        );
        //Working on navigation view checkboxes.
        NavigationView navigation = findViewById(R.id.navigationView);

        //menuItems of events in nav view
        ArrayList<MenuItem> menuItemsArray= new ArrayList<>();
        menuItemsArray.add(navigation.getMenu().findItem(R.id.WildFire));
        menuItemsArray.add(navigation.getMenu().findItem(R.id.Volcanoes));
        menuItemsArray.add(navigation.getMenu().findItem(R.id.Iceberg));
        menuItemsArray.add(navigation.getMenu().findItem(R.id.Tornadoes));

        //The buttons to those menu items
        CompoundButton fireBox =  (CompoundButton) menuItemsArray.get(0).getActionView();
        CompoundButton volcanoBox =  (CompoundButton) menuItemsArray.get(1).getActionView();
        CompoundButton iceBox =  (CompoundButton) menuItemsArray.get(2).getActionView();
        CompoundButton stormBox =  (CompoundButton) menuItemsArray.get(3).getActionView();

        //And when the buttons get clicked:
        fireBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
                toggleVisible(wildfireArray);
            }
        });
        volcanoBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
                toggleVisible(volcanoesArray);
            }
        });
        iceBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
                toggleVisible(seaLakeArray);
            }
        });
        stormBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
                toggleVisible(stormArray);
            }
        });

        //About us button
        aboutUs = (ImageButton) findViewById(R.id.aboutus);
        aboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAboutUs();
            }
        });


    }

    //Function to bring up the about us page
    public void openAboutUs(){
        Intent intent = new Intent(this, aboutus.class);
        startActivity(intent);
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(GoogleMap googleMap) {
        try {
        // Customise the styling of the base map using a JSON object defined
        // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

            if (!success) {
                Log.e(null, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
        mMap = googleMap;

        try {
            for (Event e : eventArray) {
                if (e.disasterType.toLowerCase().contains("wildfires"))
                {
                    Marker temp = mMap.addMarker(new MarkerOptions().position(e.coords).title(e.title).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_wild_fire)));
                    wildfireArray.add(new Event(e.coords, e.title, e.disasterType, e.date, temp));
                }
                else if (e.disasterType.toLowerCase().contains("volcanoes"))
                {
                    Marker temp = mMap.addMarker(new MarkerOptions().position(e.coords).title(e.title).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_volcano)));
                    volcanoesArray.add(new Event(e.coords, e.title, e.disasterType, e.date, temp));
                }
                else if(e.disasterType.toLowerCase().contains("sealakeice"))
                {
                    Marker temp = mMap.addMarker(new MarkerOptions().position(e.coords).title(e.title).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_ice)));
                    seaLakeArray.add(new Event(e.coords, e.title, e.disasterType, e.date, temp));
                }
                else if(e.disasterType.toLowerCase().contains("severestorms"))
                {
                    Marker temp = mMap.addMarker(new MarkerOptions().position(e.coords).title(e.title).icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_storm)));
                    stormArray.add(new Event(e.coords, e.title, e.disasterType, e.date, temp));
                }
            }
        }
        catch(Exception exception){
            exception.printStackTrace();
        }

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
//                System.out.println(mMap.getCameraPosition().target.latitude);
//                System.out.println(mMap.getCameraPosition().target.longitude);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                String markerName = marker.getTitle();
                LatLng markerCoords = marker.getPosition();
                TextView title = findViewById(R.id.bottomsheeteventtitle);
                TextView coordinates = findViewById(R.id.bottomsheeteventcoordinates);
                TextView disastertype = findViewById(R.id.bottomsheeteventtype);
                ImageView img = findViewById(R.id.bottomsheeteventlogo);

                Event e = null;
                for(int i =0;i<eventArray.size();i++){
                    e = eventArray.get(i);
                    if(markerName.equals(e.title)){
                        break;
                    }
                }

                //Parse title
                //Get the country name
                //If not country name , return  recent disaster
                String result = markerName.substring(markerName.lastIndexOf(',') + 1).trim();
                System.out.println(result+"\n");
                System.out.println(markerName);

                String key;

                //Parse the tile for querying it to the database. Currently , based on extracting name
                // out of title. For future , trying to map coordinate to place/state.

                if (result.equals("United States"))
                {
                    String[] arr = markerName.split(",");
                    try
                    {
                        key = arr[arr.length-2];
//                        System.out.println(arr[arr.length-2]);
                    }
                    catch (Exception err)
                    {
                        key = "RECENT";
//                        System.out.println("Error");
                    }
                }
                else if(result.contains(" - United States"))
                {
//                    System.out.println("Success");
                    String[] arr = markerName.split(",");
                    String last;
                    try
                    {
                        last = arr[arr.length-1];

                    }
                    catch (Exception err)
                    {
                        last = "Recent" ;
//                        System.out.println("Error");
                    }

                    String[] arr_2 = last.split("-");
                    key = arr_2[0];
                    key = key.substring(0,key.length()-1);
//                    System.out.println(key + "SUCCESS \n");

                }
                else
                {
                    String[] arr = markerName.split(",");
                    //IF BRIGHTON HOVE - uNITED kINGDOM
                    if(arr[arr.length-1].contains("-"))
                    {
                        //get value after -
                        try
                        {
                            String[] splitIntoTwo = arr[arr.length-1].split("-");
                            key = splitIntoTwo[splitIntoTwo.length-1];
//                            System.out.println(key + "SUCCESS");

                        }
                        catch (Exception err)
                        {
                            key = "Recent";
                        }


                    }
                    else
                    {

                        try
                        {
                            key = arr[arr.length-1];

                        }
                        catch (Exception err)
                        {
                            key = "Recent";
                        }

                    }



                }

                key = key  +" "+e.disasterType;
                key = key.substring(1,key.length());
                System.out.println("The key for the current class is" + key + "\n");




                //Code for pulling database values out. Ignore out.
                final ArrayList<String> news_headline  = new ArrayList<String>();
                final ArrayList<String> news_link = new ArrayList<String>();


                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child(key);

                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        String news; String link;
                        news_headline.clear();
                        news_link.clear();
                        for  (DataSnapshot unique_id : snapshot.getChildren()) //Iterate through the child node and the unique id
                        {
                            news = unique_id.child("News Headline").getValue(String.class); //Get news from the firebase
                            System.out.println(news);
                            news_headline.add(news);
                            link = unique_id.child("Link").getValue(String.class);
                            System.out.println(link);
                            news_link.add(link);

                        }
                        //note news_headline , news_link only exist inside this class.

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

//                //Print news value
//                System.out.println("Trying to print stuff");
//                for (int i=0; i<news_headline.size();i++)
//                {
//                    System.out.println(news_headline.get(i));
//                    System.out.println(news_link.get(i));
//                }


















                title.setText(markerName);
                coordinates.setText(markerCoords.toString());
                disastertype.setText(e.disasterType);

                if(e.disasterType.equals("wildfires")){
                    img.setImageResource(R.drawable.ic_wild_fire);
                }
                else if(e.disasterType.equals("volcanoes")){
                    img.setImageResource(R.drawable.ic_volcano);
                }
                else if(e.disasterType.equals("seaLakeIce")){
                    img.setImageResource(R.drawable.ic_ice);
                }
                else if(e.disasterType.equals("severeStorms")){
                    img.setImageResource(R.drawable.ic_storm);
                }

                return false;
            }
        });
    }

    public void toggleVisible(ArrayList<Event> events)
    {
       for(Event e: events)
       {
           if(e.marker.isVisible()){
               e.marker.setVisible(false);
           }
           else{
               e.marker.setVisible(true);
           }
       }
    }

    //Documentation: https://developers.google.com/android/reference/com/google/android/gms/maps/model/package-summary
    //BitmapDescriptor defines our bitmap image.
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId){
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}