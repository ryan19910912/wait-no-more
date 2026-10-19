package com.waitnomore.common.utils;

/**
 * 處理地理座標轉換工具
 */
public class GeoUtils {

    // 將經緯度轉為 WKT (Well-Known Text) 格式，供 MySQL ST_GeomFromText 使用
    public static String toWktPoint(Double lng, Double latitude) {
        if (lng == null || latitude == null) return null;
        return String.format("POINT(%f %f)", lng, latitude);
    }

    // 計算兩點間的大致距離 (簡單勾股定理，精確計算建議用資料庫 ST_Distance_Sphere)
    public static double calculateApproxDistance(double lon1, double lat1, double lon2, double lat2) {
        return Math.sqrt(Math.pow(lon1 - lon2, 2) + Math.pow(lat1 - lat2, 2));
    }
}
