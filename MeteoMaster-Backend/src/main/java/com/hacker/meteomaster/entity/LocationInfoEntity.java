package com.hacker.meteomaster.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "location_info")
public class LocationInfoEntity {
    @Id
    @Column(nullable = false)
    private String location;
    @Column(nullable = false)
    private double latitude;
    @Column(nullable = false)
    private double longitude;
    @Column(nullable = false)
    private double elevation;
    @Column(nullable = false)
    private String timezone;
    @Column(name = "timezone_abbreviation", nullable = false)
    private String timezoneAbbreviation;
    @Column(name = "last_location_change", nullable = false)
    private Date lastLocationChange;
    @Column(name = "last_data_refresh", nullable = false)
    private Date lastDataRefresh;
}