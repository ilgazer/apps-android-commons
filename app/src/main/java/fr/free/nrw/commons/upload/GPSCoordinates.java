package fr.free.nrw.commons.upload;

import android.content.ContentResolver;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;

import timber.log.Timber;

/**
 * Extracts geolocation to be passed to API for category suggestions. If a picture with geolocation
 * is uploaded, extract latitude and longitude from EXIF data of image.
 */
public class GPSCoordinates {

    public static final GPSCoordinates DUMMY = new GPSCoordinates();
    public final boolean isEmpty;
    public final double decLatitude;
    public final double decLongitude;
    public final String latitude;
    public final String longitude;
    public final String latitudeRef;
    public final String longitudeRef;
    public final String decimalCoords;

    public static GPSCoordinates fromEXIF(@NonNull ExifInterface exif) {
        if (exif != null && exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null) {
            //If image has EXIF data, extract image coords
            Timber.d("EXIF data has location info");

            String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String latitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String longitudeRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

            return new GPSCoordinates(latitude, longitude, latitudeRef, longitudeRef);
        }
        return DUMMY;
    }

    public static GPSCoordinates fromFilepath(String filepath) {
        try {
            ExifInterface exif = new ExifInterface(filepath);
            return fromEXIF(exif);
        } catch (Exception e) {
            e.printStackTrace();
            return DUMMY;
        }
    }

    public static GPSCoordinates fromFile(File file, ContentResolver contentResolver) {
        try {
            ExifInterface exif = new ExifInterface(contentResolver.openInputStream(Uri.fromFile(file)));
            return fromEXIF(exif);
        } catch (Exception e) {
            e.printStackTrace();
            return fromFile(file);
        }
    }

    public static GPSCoordinates fromFile(File file) {
        return fromFilepath(file.getAbsolutePath());
    }

    /**
     * Dummy constructor
     */
    private GPSCoordinates() {
        decLatitude = 0;
        decLongitude = 0;

        latitude = "";
        longitude = "";

        latitudeRef = "";
        longitudeRef = "";

        decimalCoords = "";

        isEmpty = true;
    }

    /**
     * Constructor from EXIF-style coordinates
     *
     * @param latitude
     * @param longitude
     * @param latitudeRef
     * @param longitudeRef
     */
    private GPSCoordinates(String latitude, String longitude, String latitudeRef, String longitudeRef) {
        this.latitude = latitude;
        this.longitude = longitude;

        this.latitudeRef = latitudeRef;
        this.longitudeRef = longitudeRef;

        if (latitudeRef.equals("N")) {
            decLatitude = convertToDegree(latitude);
        } else {
            decLatitude = 0 - convertToDegree(latitude);
        }

        if (longitudeRef.equals("E")) {
            decLongitude = convertToDegree(longitude);
        } else {
            decLongitude = 0 - convertToDegree(longitude);
        }

        decimalCoords = decLatitude + "|" + decLongitude;
        Timber.d("Latitude and Longitude are %s", decimalCoords);

        isEmpty = false;
    }

    private static double convertToDegree(String stringDMS) {
        double result;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        double d0 = Double.parseDouble(stringD[0]);
        double d1 = Double.parseDouble(stringD[1]);
        double degrees = d0 / d1;

        String[] stringM = DMS[1].split("/", 2);
        double m0 = Double.parseDouble(stringM[0]);
        double m1 = Double.parseDouble(stringM[1]);
        double minutes = m0 / m1;

        String[] stringS = DMS[2].split("/", 2);
        double s0 = Double.parseDouble(stringS[0]);
        double s1 = Double.parseDouble(stringS[1]);
        double seconds = s0 / s1;

        result = degrees + (minutes / 60) + (seconds / 3600);
        return result;
    }
}
