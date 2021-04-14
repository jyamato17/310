package com.example.myapplication.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.BuildConfig;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    private int maxCases;
    private int minCases;
    Bitmap bitmap;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSION_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

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
        ImageView imageView = rootView.findViewById(R.id.screenshot_image);
        FloatingActionButton button = rootView.findViewById(R.id.share_button);
        button.setOnClickListener(v -> {
            takeScreenshot(mMapView);
        });

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For dropping a marker at a point on the Map
                LatLng sydney = new LatLng(34.021338007781054, -118.28794802694372);
                //googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));

                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sydney).zoom(10).build();
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
        mMapView.onDestroy();
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

        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .weightedData(latLngs)
                .radius(50)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        TileOverlay overlay = googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));


        try {
            readItems();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<WeightedLatLng> readItems() throws JSONException {
        List<WeightedLatLng> result = new ArrayList<>();
        List<City> cities = new ArrayList<>();

        BufferedReader reader = null;
        BufferedReader readerCases = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open("City_Locations.csv"), "UTF-8"));
            readerCases = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open("covidcases-deaths.csv"), "UTF-8"));
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
                    name = name.substring(8);
                } else if (name.contains("Los Angeles -")) {
                    name = name.substring(15);
                } else if (name.contains("Unincorporated")) {
                    name = name.substring(17);
                }
                City city = null;
                for (int i = 0; i < cities.size() / 2; i++) {
                    //System.out.println(cities.get(i).getName().trim() + " " + name.trim());
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

                if (city != null) {
                    WeightedLatLng coord = new WeightedLatLng(city.getCoordinates(), 1);
                    googleMap.addMarker(new MarkerOptions().position(city.getCoordinates()).title(city.getName())
                            .snippet("Cases: " + city.getCases())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    result.add(coord);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    public void createLegend(View view) {
        TextView textView = (TextView) view.findViewById(R.id.map_legend_text1);
        textView.setText("" + minCases);
        textView = (TextView) view.findViewById(R.id.map_legend_text2);
        textView.setText("" + maxCases);
    }

    private void takeScreenshot(MapView view) {

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

//This logic is used to save file at given location with the given filename and compress the Image Quality.
                    File imageFile = new File(path);
                    FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
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

    class City {
        private Double latitiude;
        private Double longitude;
        private String name;
        private String cases;
        private String deaths;

        public City(String name, String latitude, String longitude) {
            this.latitiude = Double.parseDouble(latitude);
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
            return new LatLng(this.latitiude, this.longitude);
        }
    }

}


