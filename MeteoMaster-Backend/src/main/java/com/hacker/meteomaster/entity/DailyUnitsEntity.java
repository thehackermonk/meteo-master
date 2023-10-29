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
@Table(name = "daily_units")
public class DailyUnitsEntity {
    @Id
    @Column(nullable = false)
    private String parameter;
    @Column(nullable = false)
    private String value;
}