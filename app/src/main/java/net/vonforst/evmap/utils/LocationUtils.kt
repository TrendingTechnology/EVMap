package net.vonforst.evmap.utils

import android.location.Location
import kotlin.math.*

/**
 * Adds a certain distance in meters to a location. Approximate calculation.
 */
fun Location.plusMeters(dx: Double, dy: Double): Pair<Double, Double> {
    val lat = this.latitude + (180 / Math.PI) * (dx / 6378137.0)
    val lon = this.longitude + (180 / Math.PI) * (dy / 6378137.0) / cos(Math.toRadians(lat))
    return Pair(lat, lon)
}

const val earthRadiusM = 6378137.0

/**
 * Calculates the distance between two points on Earth in meters.
 * Latitude and longitude should be given in degrees.
 */
fun distanceBetween(
    startLatitude: Double, startLongitude: Double,
    endLatitude: Double, endLongitude: Double
): Double {
    // see https://rosettacode.org/wiki/Haversine_formula#Java
    val dLat = Math.toRadians(endLatitude - startLatitude)
    val dLon = Math.toRadians(endLongitude - startLongitude)
    val originLat = Math.toRadians(startLatitude)
    val destinationLat = Math.toRadians(endLatitude)

    val a = sin(dLat / 2).pow(2.0) + sin(dLon / 2).pow(2.0) * cos(originLat) * cos(destinationLat)
    val c = 2 * asin(sqrt(a))
    return earthRadiusM * c
}