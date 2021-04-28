package com.example.myapplication.ui.map;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.BuildConfig;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.heatmaps.*;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment implements GoogleMap.OnInfoWindowClickListener {

    MapView mMapView;
    private GoogleMap googleMap;
    private int maxCases;
    private int minCases;
    List<City> cities = new ArrayList<>();
    List<TestSite> testSites = new ArrayList<>();
    Bitmap bitmap;
    ArrayList<Integer> allCases = new ArrayList<>();
    List<Marker> cityMarkers = new ArrayList<>();
    List<Marker> siteMarkers = new ArrayList<>();
    boolean isSites = false;

    PinClicked mCallBack;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public interface PinClicked {
        void sendCity(String city);
    }

    public void sendCity(String city) {
        mCallBack.sendCity(city);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Activity a = (Activity) context;

        try {
            mCallBack = (PinClicked) a;
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString()
                    + " must implement TextClicked");
        }
    }

    @Override
    public void onDetach() {
        mCallBack = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);
        setHasOptionsMenu(true);

        this.maxCases = 0;
        this.minCases = 1000000;

        mMapView = rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        verifyStoragePermission(getActivity());
        View view = rootView.findViewById(R.id.legend);
        FloatingActionButton shareButton = rootView.findViewById(R.id.share_button);
        shareButton.setOnClickListener(v -> {
            takeScreenshot(view);
        });

        FloatingActionButton testSitesButton = rootView.findViewById(R.id.testSites);
        testSitesButton.setOnClickListener(v -> {
            changePins(rootView);
        });

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.getTag() == "city") {
                            sendCity(marker.getTitle());
                        }
                        return false;
                    }
                });

                // For dropping a marker at a point on the Map
                LatLng losAngeles = new LatLng(34.021338007781054, -118.28794802694372);
                //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(losAngeles).zoom(10).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                addHeatMap();
                createLegend(rootView);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void addHeatMap() {
        List<WeightedLatLng> latLngs = new ArrayList<>();

        // Get the data: latitude/longitude positions of police stations.
        try {
            latLngs = readItems();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        createMarkers();

        /*HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .weightedData(latLngs)
                .radius(50)
                .maxIntensity(50000.0d)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        TileOverlay overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));*/
    }

    private List<WeightedLatLng> readItems() throws JSONException {
        List<WeightedLatLng> result = new ArrayList<>();

        BufferedReader reader = null;
        BufferedReader readerCases = null;
        BufferedReader readerSites = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open("City_Locations.csv"), StandardCharsets.UTF_8));
            readerCases = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open("covidcases-deaths.csv"), StandardCharsets.UTF_8));
            readerSites = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open("testing_locations.csv"), StandardCharsets.UTF_8));
        } catch (IOException e) {
            //log the exception
        }

        String line = "";
        try {
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split(", ");
                cities.add(new City(lines[0].trim(), lines[1], lines[2]));
            }

            line = readerSites.readLine();
            while ((line = readerSites.readLine()) != null) {
                String[] lines = line.split(";");
                testSites.add(new TestSite(lines[0].trim(), lines[1].trim(), lines[2].trim(), lines[3], lines[4]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            line = "";
            while ((line = readerCases.readLine()) != null) {
                String[] lines = line.split(", ");
                if (lines.length < 2) {
                    continue;
                }

                String name = lines[0];
                name.trim();

                if (name.contains("City of ")) {
                    name = name.substring(9);
                } else if (name.contains("Los Angeles -")) {
                    name = name.substring(15);
                } else if (name.contains("Unincorporated")) {
                    name = name.substring(17);
                }

                City city = null;
                for (int i = 0; i < cities.size(); i++) {
                    if (cities.get(i).getName().trim().equals(name.trim())) {
                        System.out.println(name.trim());
                        city = cities.get(i);
                        city.addCases(lines[1]);
                        city.addDeaths(lines[3]);
                        break;
                    }
                }

                String cases = lines[1];
                if (Integer.parseInt(cases) > this.maxCases) {
                    this.maxCases = Integer.parseInt(cases);
                }
                if (Integer.parseInt(cases) < this.minCases && Integer.parseInt(cases) != 0)
                    this.minCases = Integer.parseInt(cases);

                allCases.add(Integer.parseInt(cases));

                if (city != null) {
                    WeightedLatLng coord = new WeightedLatLng(city.getCoordinates(), Double.parseDouble(cases));
                    result.add(coord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private void changePins(View view) {
        FloatingActionButton button = view.findViewById(R.id.testSites);

        for (Marker city : cityMarkers) {
            if (isSites) { city.setVisible(true); }
            else { city.setVisible(false); }
        }

        for (Marker site : siteMarkers) {
            if (isSites) { site.setVisible(false); }
            else { site.setVisible(true); }
        }

        if (isSites) {
            button.setImageResource(R.drawable.ic_baseline_content_paste_24);
            isSites = false;
        } else {
            button.setImageResource(R.drawable.ic_baseline_location_on_24);
            isSites = true;
        }
    }

    public void createMarkers() {
        Marker tempMarker;
        BitmapDescriptor bitmapDescriptor;
        Collections.sort(allCases);
        int size = allCases.size();

        for (City city : cities) {
            if (Double.parseDouble(city.getCases()) < allCases.get(size / 5)) {
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            }

            else if (Double.parseDouble(city.cases) < allCases.get(size * 2 / 5)) {
                bitmapDescriptor = getMarkerIcon("#DAF7A6");
            }

            else if (Double.parseDouble(city.cases) < allCases.get(size * 3 / 5)) {
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            }

            else if (Double.parseDouble(city.cases) < allCases.get(size * 4 / 5)) {
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            }

            else {
                bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }

            tempMarker = googleMap.addMarker(new MarkerOptions()
                    .position(city.getCoordinates())
                    .title(city.getName())
                    .snippet("Cases: " + city.getCases())
                    .icon(bitmapDescriptor));

            tempMarker.setTag("city");
            cityMarkers.add(tempMarker);
        }

        for (TestSite site : testSites) {
            tempMarker = googleMap.addMarker(new MarkerOptions()
                    .position(site.getCoordinates())
                    .title(site.getName())
                    .snippet("Address: " + site.getAddress() + "\nPhone: " + site.getPhone())
                    .visible(false)
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(getBitmapFromVectorDrawable(R.drawable.ic_testing_site))));

            tempMarker.setTag("testSite");
            siteMarkers.add(tempMarker);
        }

        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                LinearLayout info = new LinearLayout(getContext());
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(getContext());
                title.setTextColor(Color.BLACK);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(getContext());
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
    }

    public void createLegend(View view) {
        TextView textView = view.findViewById(R.id.map_legend_text1);
        textView.setText("" + minCases);
        textView = view.findViewById(R.id.map_legend_text2);
        textView.setText("" + maxCases);
    }

    private void takeScreenshot(View view) {

        googleMap.setOnMapLoadedCallback(() -> googleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                //This is used to provide file name with Date a format
                Date date = new Date();
                CharSequence format = DateFormat.format("MM-dd-yyyy_hh:mm:ss", date);

                //It will make sure to store file to given below Directory and If the file Directory doesn't exist then it will create it.
                try {
                    File mainDir = new File(
                            getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "FileShare");
                    if (!mainDir.exists()) {
                        boolean mkdir = mainDir.mkdir();
                    }

                    //Providing file name along with Bitmap to capture screen view
                    String path = mainDir + "/" + "MapCovid" + "-" + format + ".jpeg";

                    bitmap = snapshot;

                    view.setDrawingCacheEnabled(true);
                    Bitmap legend = Bitmap.createBitmap(view.getDrawingCache());
                    view.setDrawingCacheEnabled(false);

                    Bitmap combined = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
                    Canvas canvas = new Canvas(combined);
                    canvas.drawBitmap(bitmap, 0, 0, null);
                    canvas.drawBitmap(legend, 0, bitmap.getHeight() - legend.getHeight(), null);

//This logic is used to save file at given location with the given filename and compress the Image Quality.
                    File imageFile = new File(path);
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    combined.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                    fileOutputStream.flush();
                    fileOutputStream.close();

//Create New Method to take ScreenShot with the imageFile.
                    shareScreenshot(imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    private void shareScreenshot(File imageFile) {
        Uri uri = FileProvider.getUriForFile(
                requireContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                imageFile
        );

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_TEXT, "Shared from MapCovid");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        try {
            Intent chooser = Intent.createChooser(intent, "Share with");
            List<ResolveInfo> resInfoList = requireActivity().getPackageManager().queryIntentActivities(
                    chooser, PackageManager.MATCH_DEFAULT_ONLY);

            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                requireActivity().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Not able to share", Toast.LENGTH_LONG).show();
        }
    }

    public static void verifyStoragePermission(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_STORAGE,
                    REQUEST_EXTERNAL_STORAGE);
        }
    }

    public BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    public Bitmap getBitmapFromVectorDrawable(int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), drawableId);

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    class City {
        private Double latitude;
        private Double longitude;
        private String name;
        private String cases;
        private String deaths;

        public City(String name, String latitude, String longitude) {
            this.latitude = Double.parseDouble(latitude);
            this.longitude = Double.parseDouble(longitude);
            this.name = name;

        }

        public void addCases(String cases)
        {
            this.cases = cases;
        }

        public void addDeaths(String deaths)
        {
            this.deaths = deaths;
        }

        public String getCases()
        {
            return this.cases;
        }

        public String getDeaths()
        {
            return this.deaths;
        }

        public String getName() {
            return name;
        }

        public LatLng getCoordinates() {
            return new LatLng(this.latitude, this.longitude);
        }
    }

    class TestSite {
        private final Double latitude;
        private final Double longitude;
        private final String name;
        private final String address;
        private final String phone;

        public TestSite(String name, String address,String phone, String latitude, String longitude) {
            this.latitude = Double.parseDouble(latitude);
            this.longitude = Double.parseDouble(longitude);
            this.name = name;
            this.address = address;
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getPhone() {
            return phone;
        }

        public LatLng getCoordinates() {
            return new LatLng(this.latitude, this.longitude);
        }
    }
}


