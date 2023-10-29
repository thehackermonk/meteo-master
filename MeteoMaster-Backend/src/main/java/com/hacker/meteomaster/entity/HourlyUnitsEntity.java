package com.hacker.meteomaster.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hourly_units")
public class HourlyUnitsEntity {
    @Id
    @Column(nullable = false)
    private String parameter;
    @Column(nullable = false)
    private String value;
}