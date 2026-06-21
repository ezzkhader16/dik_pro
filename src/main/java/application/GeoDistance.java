package application;

public final class GeoDistance {
    // Earth radius in kilometers, used by the Haversine formula.
    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoDistance() {
        // Private constructor because this class only has static methods.
    }

    public static double kilometers(City a, City b) {
        // Convert coordinates from degrees to radians because sin/cos use radians.
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double deltaLat = Math.toRadians(b.getLatitude() - a.getLatitude());
        double deltaLon = Math.toRadians(b.getLongitude() - a.getLongitude());

        // Apply the Haversine formula to calculate distance on the earth surface.
        double sinLat = Math.sin(deltaLat / 2.0);
        double sinLon = Math.sin(deltaLon / 2.0);
        double h = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;
        double c = 2.0 * Math.atan2(Math.sqrt(h), Math.sqrt(1.0 - h));
        // Distance equals earth radius multiplied by the angle.
        return EARTH_RADIUS_KM * c;
    }
}
