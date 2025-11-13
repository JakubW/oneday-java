package com.oneday.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Table;

/**
 * Entity representing temperature offset for a given altitude range.
 * Used to adjust base temperature based on elevation above sea level.
 */
@Entity
@Table(name = "altitude_offset_range")
public class AltitudeOffsetRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_meters", nullable = false)
    private int fromMeters;

    @Column(name = "to_meters", nullable = false)
    private int toMeters;

    @Column(name = "offset_value", nullable = false)
    private double offset;

    public AltitudeOffsetRange() {}

    public AltitudeOffsetRange(int fromMeters, int toMeters, double offset) {
        this.fromMeters = fromMeters;
        this.toMeters = toMeters;
        this.offset = offset;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getFromMeters() {
        return fromMeters;
    }

    public void setFromMeters(int fromMeters) {
        this.fromMeters = fromMeters;
    }

    public int getToMeters() {
        return toMeters;
    }

    public void setToMeters(int toMeters) {
        this.toMeters = toMeters;
    }

    public double getOffset() {
        return offset;
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
}
