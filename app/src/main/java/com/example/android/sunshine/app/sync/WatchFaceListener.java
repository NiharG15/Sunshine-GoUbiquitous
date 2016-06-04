package com.example.android.sunshine.app.sync;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchFaceListener extends WearableListenerService {

    GoogleApiClient mGoogleApiClient;

    public WatchFaceListener() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.toString(), "onCreate: Service created");

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        Log.d(this.toString(), "On Data Changed");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem dataItem = event.getDataItem();
                if (dataItem.getUri().getPath().compareTo("/update_req") == 0) {
                    mGoogleApiClient = new GoogleApiClient.Builder(WatchFaceListener.this)
                            .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(@Nullable Bundle bundle) {
                                    String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

                                    String locationSetting = Utility.getPreferredLocation(WatchFaceListener.this);
                                    Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                                            locationSetting, System.currentTimeMillis());
                                    Cursor c = WatchFaceListener.this.getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);
                                    if (c != null && c.moveToFirst()) {
                                        double high = c.getDouble(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
                                        String highString = Utility.formatTemperature(WatchFaceListener.this, high);
                                        double low = c.getDouble(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
                                        String lowString = Utility.formatTemperature(WatchFaceListener.this, low);
                                        PutDataMapRequest dataMapRequest = PutDataMapRequest.create("/weather_data");
                                        DataMap mDataMap = dataMapRequest.getDataMap();
                                        mDataMap.putString(SunshineSyncAdapter.WATCH_DATA_HIGHTEMP, highString);
                                        mDataMap.putString(SunshineSyncAdapter.WATCH_DATA_LOWTEMP, lowString);
                                        Log.d("Weather cond", String.valueOf(c.getInt(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID))));
                                        mDataMap.putInt(SunshineSyncAdapter.WATCH_DATA_COND, c.getInt(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID)));
                                        mDataMap.putLong("time", System.currentTimeMillis());
                                        Wearable.DataApi.putDataItem(mGoogleApiClient, dataMapRequest.asPutDataRequest().setUrgent());
                                        //mGoogleApiClient.disconnect();
                                    }
                                    if (c != null) {
                                        c.close();
                                    }
                                }

                                @Override
                                public void onConnectionSuspended(int i) {

                                }
                            })
                            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                                }
                            })
                            .addApi(Wearable.API).build();
                    mGoogleApiClient.connect();
                }
            }
        }
    }
}
