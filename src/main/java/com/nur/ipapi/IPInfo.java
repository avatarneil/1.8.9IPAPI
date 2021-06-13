package com.nur.ipapi;

import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class IPInfo {
    private Integer fraudScore;
    private Boolean proxy;
    private Boolean vpn;
    private Boolean tor;
    private Boolean activeVpn;
    private Boolean activeTor;
    private Double lat;
    private Double lng;
    private String countryCode;
    private String region;
    private String city;

    /**
     *
     * @param apiResponse
     * @return 0=good, 1=warn, 2=bad
     */
    private Integer determineRiskThreshold(JsonObject apiResponse) {
        fraudScore = apiResponse.get("fraud_score").getAsInt();
        proxy = apiResponse.get("proxy").getAsBoolean();
        vpn = apiResponse.get("vpn").getAsBoolean();
        tor = apiResponse.get("tor").getAsBoolean();
        activeVpn = apiResponse.get("active_vpn").getAsBoolean();
        activeTor = apiResponse.get("active_tor").getAsBoolean();

        // See fraud_score lookup table in https://www.ipqualityscore.com/documentation/proxy-detection/overview
        if (fraudScore >= 85) {
            return 2;
        }
        if (fraudScore >= 70) {
            return 1;
        }
        if (proxy || vpn || tor || activeVpn || activeTor) {
            return 2;
        }

        return 0;
    }

    /**
     * @param color Color styling for the chat component
     * @return Formatted lat/lng info
     */
    public ChatComponentText prepareCoordinatesForDisplay(EnumChatFormatting color) {
        return new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Lat/Lng: " + color + "(" + lat + ", " + lng + ")");
    }

    /**
     * @param color Color styling for the chat component
     * @return Formatted city/region/country info
     */
    public ChatComponentText prepareLocationForDisplay(EnumChatFormatting color) {
        return new ChatComponentText("" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Region: " + color+ city + ", " + region + " " + countryCode);
    }

    IPInfo(JsonObject raw) {
        lat = raw.get("latitude").getAsDouble();
        lng = raw.get("longitude").getAsDouble();
        countryCode = raw.get("country_code").getAsString();
        region = raw.get("region").getAsString();
        city = raw.get("city").getAsString();
    }
}
