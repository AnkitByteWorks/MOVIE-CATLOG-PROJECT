package com.movietracker.watchlog.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DurationFormatter Unit Tests")
class DurationFormatterTest {

    @Test
    @DisplayName("Should return 0m for 0 or negative minutes")
    void shouldHandleZeroOrNegative() {
        assertThat(DurationFormatter.formatMinutes(0L)).isEqualTo("0m");
        assertThat(DurationFormatter.formatMinutes(-10L)).isEqualTo("0m");
    }

    @Test
    @DisplayName("Should format minutes under 1 hour")
    void shouldFormatMinutesOnly() {
        assertThat(DurationFormatter.formatMinutes(45L)).isEqualTo("45m");
    }

    @Test
    @DisplayName("Should format hours and minutes")
    void shouldFormatHoursAndMinutes() {
        assertThat(DurationFormatter.formatMinutes(60L)).isEqualTo("1h 0m");
        assertThat(DurationFormatter.formatMinutes(135L)).isEqualTo("2h 15m");
    }

    @Test
    @DisplayName("Should format days, hours, and minutes")
    void shouldFormatDaysHoursAndMinutes() {
        // 1 day = 1440 min. 1440 + 120 + 30 = 1590 min (1d 2h 30m)
        assertThat(DurationFormatter.formatMinutes(1590L)).isEqualTo("1d 2h 30m");
        // 2 days = 2880 min
        assertThat(DurationFormatter.formatMinutes(2880L)).isEqualTo("2d 0h 0m");
    }
}
