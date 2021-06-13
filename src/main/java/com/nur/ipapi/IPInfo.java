package com.nur.ipapi;

import com.google.common.base.Joiner;
import com.google.gson.JsonObject;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

enum Risk {
    Low,
    Medium,
    High
}

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
     * @return How risky a given IP is considered
     */
    public Risk risk() {
        // TODO: Add support for IPHubAPI risk assessment

        // See fraud_score lookup table in https://www.ipqualityscore.com/documentation/proxy-detection/overview
        if (fraudScore >= 85) {
            return Risk.High;
        }

        if (fraudScore >= 50) {
            return Risk.Medium;
        }

        return Risk.Low;
    }

    private EnumChatFormatting riskColor() {
        switch (risk()) {
            case Low:
                return EnumChatFormatting.GREEN;
            case Medium:
                return EnumChatFormatting.GOLD;
            case High:
                return EnumChatFormatting.RED;
            default:
                return EnumChatFormatting.RED;
        }
    }

    private String riskDetails() {
        String proxyStr = proxy ? "Proxy" : null;
        String vpnStr = vpn ? "VPN" : null;
        String torStr = tor ? "Tor" : null;
        String activeVpnStr = activeVpn ? "Active VPN" : null;
        String activeTorStr = activeTor ? "Active TOR" : null;

        String details = Joiner.on('/').skipNulls().join(proxyStr, vpnStr, torStr, activeVpnStr, activeTorStr);

        if (details == "") {
            return "None";
        }

        return details;
    }

    public ChatComponentText prepareRiskForDisplay() {
        String typePrefix = "" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Risk(s): ";

        switch (risk()) {
            case Low:
                return new ChatComponentText(typePrefix + EnumChatFormatting.GREEN + riskDetails());
            case Medium:
                return new ChatComponentText(typePrefix + EnumChatFormatting.GOLD + riskDetails());
            case High:
                return new ChatComponentText(typePrefix + EnumChatFormatting.RED + riskDetails());
            default:
                return new ChatComponentText(typePrefix + EnumChatFormatting.RED + "Error assessing risk");
        }
    }

    /**
     * @return Formatted lat/lng info
     */
    public ChatComponentText prepareCoordinatesForDisplay() {
        String latLngPrefix = "" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Lat/Lng: ";
        return new ChatComponentText(latLngPrefix + riskColor() + "(" + lat + ", " + lng + ")");
    }

    /**
     * @return Formatted city/region/country info
     */
    public ChatComponentText prepareLocationForDisplay() {
        String locationPrefix = "" + EnumChatFormatting.WHITE + EnumChatFormatting.BOLD + " * " + EnumChatFormatting.GRAY + "Region: ";
        return new ChatComponentText(locationPrefix + riskColor() + city + ", " + region + " " + countryCode);
    }

    IPInfo(JsonObject raw) {
        fraudScore = raw.get("fraud_score").getAsInt();
        proxy = raw.get("proxy").getAsBoolean();
        vpn = raw.get("vpn").getAsBoolean();
        tor = raw.get("tor").getAsBoolean();
        activeVpn = raw.get("active_vpn").getAsBoolean();
        activeTor = raw.get("active_tor").getAsBoolean();
        lat = raw.get("latitude").getAsDouble();
        lng = raw.get("longitude").getAsDouble();
        countryCode = raw.get("country_code").getAsString();
        region = raw.get("region").getAsString();
        city = raw.get("city").getAsString();
    }
}
