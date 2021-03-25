package com.example.myapplication.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;

import androidx.fragment.app.Fragment;

import com.example.myapplication.R;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.opencsv.CSVReader;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            for(int i = 0; i < latLngs.size(); i++)
            {
                System.out.println(latLngs.get(i).getIntensity());
                System.out.println(latLngs.get(i).getPoint());
            }
        } catch (JSONException e) {
            //Toast.makeText(context, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
        }


//        latLngs.add(new LatLng(34.021338007781054, -118.28794802694372));
//        latLngs.add(new LatLng(33.9, -118.1));

//        for (int i = 0; i < 100; i++) {
//            latLngs.add(new LatLng(34.02133, -118.2879));
//        }

        // Create a heat map tile provider, passing it the latlngs of the police stations.
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
        List<city> cities = new ArrayList<>();


        BufferedReader reader = null;
        BufferedReader readerCases = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open("City_Locations.csv"), "UTF-8"));
            readerCases = new BufferedReader(
                    new InputStreamReader(getContext().getAssets().open("covidcases-deaths.csv"), "UTF-8"));
            }
        catch (IOException e) {
            //log the exception
        }

        String line = "";
        try {
            line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] lines = line.split(", ");
                cities.add(new city(lines[0].trim(), lines[1], lines[2]));
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        try {
            line = "";
            while((line = readerCases.readLine()) != null)
            {
                String[] lines = line.split(", ");
                if(lines.length < 2)
                {
                    continue;
                }


                String name = lines[0];
                name.trim();
                if(name.contains("City of "))
                {
                    name = name.substring(8, name.length());
                }
                else if(name.contains("Los Angeles -"))
                {
                    name= name.substring(15, name.length());
                }
                else if(name.contains("Unincorporated"))
                {
                    name = name.substring(17, name.length());
                }
                name.trim();
                city city = null;
                for(int i = 0; i < cities.size() / 10; i++)
                {
                    if(cities.get(i).getName().trim().equals(name.trim()))
                    {
//                        System.out.println(name.trim());
//                        System.out.println(lines[1]);
//                        System.out.println(cities.get(i).getCoordinates());
                        city = cities.get(i);
                        break;
                    }
                }

                String cases = lines[1];
                double numCases = Double.parseDouble(cases);
                if(city != null) {
                    //System.out.println(city.getName());
                    WeightedLatLng coord = new WeightedLatLng(city.getCoordinates(), 1);
                    result.add(coord);
                    System.out.println(coord);
//                    for (int i = 0; i < numCases; i++) {
//                        result.add(city.getCoordinates());
//                    }
                }

            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        return result;
    }

    class city {
        private Double latitiude;
        private Double longitude;
        private String name;

        public city(String name, String latitude, String longitude)
        {
            this.latitiude = Double.parseDouble(latitude);
            this.longitude = Double.parseDouble(longitude);
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public LatLng getCoordinates()
        {
            return new LatLng(this.latitiude, this.longitude);
        }
    }
}


