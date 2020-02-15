package fr.free.nrw.commons.upload.metadata;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Extracts geolocation to be passed to API for category suggestions. If a picture with geolocation
 * is uploaded, extract latitude and longitude from EXIF data of image.
 */
public class GPSCoordinates {

    public static final GPSCoordinates DUMMY = new GPSCoordinates();
    public final boolean hasCoords;
    public final double decLatitude;
    public final double decLongitude;
    public final String latitude;
    public final String longitude;
    public final String latitudeRef;
    public final String longitudeRef;
    public final String decimalCoords;

    /**
     * @param latitude
     * @param latitudeRef "N" or "S"
     * @param longitude
     * @param longitudeRef "E" or "W"
     */
    private GPSCoordinates(String latitude, String latitudeRef, String longitude, String longitudeRef) {
        this.hasCoords = true;

        this.latitude = latitude;
        this.latitudeRef = latitudeRef;

        this.longitude = longitude;
        this.longitudeRef = longitudeRef;

        decLatitude = getDecimalCoordinate(latitude, latitudeRef);
        decLongitude = getDecimalCoordinate(longitude, longitudeRef);

        decimalCoords = decLatitude + "|" + decLongitude;
        Timber.d("Latitude and Longitude are %s", decimalCoords);
    }

    /**
     * Dummy constructor
     */
    private GPSCoordinates() {
        hasCoords = false;

        latitude = "";
        latitudeRef = "";

        longitude = "";
        longitudeRef = "";

        decLatitude = 0.0;
        decLongitude = 0.0;

        decimalCoords = "";
    }

    /**
     * Construct from a stream.
     */
    public static GPSCoordinates from(@NonNull InputStream stream) throws IOException {
        ExifInterface exif = new ExifInterface(stream);
        return from(exif);
    }

    /**
     * Construct from the file path of the image.
     *
     * @param path file path of the image
     */
    public static GPSCoordinates from(@NonNull String path) {
        try {
            ExifInterface exif = new ExifInterface(path);
            return from(exif);
        } catch (IOException | IllegalArgumentException e) {
            Timber.w(e);
        }
        return DUMMY;
    }

    /**
     * Construct from the exif interface of the image.
     *
     * @param exif exif interface of the image
     */
    public static GPSCoordinates from(@NonNull ExifInterface exif) {
        //If image has no EXIF data and user has enabled GPS setting, get user's location
        //Always return null as a temporary fix for #1599
        if (exif != null && exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) != null)
            return new GPSCoordinates(
                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF),
                    exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                    exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)
            );
        else
            return DUMMY;
    }

    public static GPSCoordinates from(String latitude, String latitudeRef, String longitude, String longitudeRef) {
        return new GPSCoordinates(latitude,latitudeRef,longitude,longitudeRef);
    }


        /**
         * Converts format of geolocation into decimal coordinates as required by MediaWiki API
         *
         * @return the coordinates in decimals
         */
    private static double getDecimalCoordinate(String coordinate, String coordinateRef) {
        double decCoordinate;
        if (coordinateRef.equals("N") || coordinateRef.equals("E")) {
            decCoordinate = convertToDegree(coordinate);
        } else {
            decCoordinate = 0 - convertToDegree(coordinate);
        }

        return decCoordinate;
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
