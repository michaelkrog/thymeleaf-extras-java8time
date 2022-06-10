/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.extras.java8time.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;
import java.util.Locale;
import org.thymeleaf.exceptions.TemplateProcessingException;
import static org.thymeleaf.extras.java8time.util.TemporalObjects.temporal;
import static org.thymeleaf.extras.java8time.util.TemporalObjects.zonedTime;
import org.thymeleaf.util.StringUtils;
import org.thymeleaf.util.Validate;

/**
 * Formatting utilities for Java 8 Time objects.
 *
 * @author Jos&eacute; Miguel Samper
 *
 * @since 2.1.0
 */
public final class TemporalFormattingUtils {

    // Even though Java comes with several patterns for ISO8601, we use the same pattern of Thymeleaf #dates utility.
    private static final DateTimeFormatter ISO8601_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ");
    
    private final Locale locale;
    private final ZoneId defaultZoneId;
    
    public TemporalFormattingUtils(final Locale locale, final ZoneId defaultZoneId) {
        super();
        Validate.notNull(locale, "Locale cannot be null");
        Validate.notNull(defaultZoneId, "ZoneId cannot be null");
        this.locale = locale;
        this.defaultZoneId = defaultZoneId;
    }

    public String format(final Object target) {
        return formatDate(target);
    }

    public String format(final Object target, final Locale locale) {
        Validate.notNull(locale, "Locale cannot be null");
        return formatDate(target, null, locale);
    }

    public String format(final Object target, final String pattern) {
        return format(target, pattern, null);
    }

    public String format(final Object target, final String pattern, final Locale locale) {
        Validate.notEmpty(pattern, "Pattern cannot be null or empty");
        return formatDate(target, pattern, locale);
    }

    public Integer day(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.DAY_OF_MONTH);
    }

    public Integer month(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.MONTH_OF_YEAR);
    }

    public String monthName(final Object target) {
        return format(target, "MMMM");
    }

    public String monthNameShort(final Object target) {
        return format(target, "MMM");
    }

    public Integer year(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.YEAR);
    }

    public Integer dayOfWeek(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.DAY_OF_WEEK);
    }

    public String dayOfWeekName(final Object target) {
        return format(target, "EEEE");
    }

    public String dayOfWeekNameShort(final Object target) {
        return format(target, "EEE");
    }

    public Integer hour(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.HOUR_OF_DAY);
    }

    public Integer minute(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.MINUTE_OF_HOUR);
    }

    public Integer second(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.SECOND_OF_MINUTE);
    }

    public Integer nanosecond(final Object target) {
        if (target == null) {
            return null;
        }
        final TemporalAccessor time = temporal(target);
        return time.get(ChronoField.NANO_OF_SECOND);
    }

    public String formatISO(final Object target) {
        if (target == null) {
            return null;
        } else if (target instanceof TemporalAccessor) {
            ChronoZonedDateTime time = zonedTime(target, defaultZoneId);
            return ISO8601_DATE_TIME_FORMATTER.withLocale(locale).format(time);
        } else {
            throw new IllegalArgumentException(
                "Cannot format object of class \"" + target.getClass().getName() + "\" as a date");
        }
    }

    private String formatDate(final Object target) {
        return formatDate(target, null, null);
    }

    private String formatDate(final Object target, final String pattern, final Locale localeOverride) {
        if (target == null) {
            return null;
        }
        Locale formattingLocale = localeOverride != null ? localeOverride : locale;
        try {
            DateTimeFormatter formatter;
            if (StringUtils.isEmptyOrWhitespace(pattern)) {
                formatter = TemporalObjects.formatterFor(target, formattingLocale);
                return formatter.format(temporal(target));
            } else {
                boolean hasDate = !(target instanceof LocalTime);
                boolean hasTime = !(target instanceof LocalDate);
                formatter = defaultOrPatternFormatted(pattern, formattingLocale, hasDate ,hasTime);
                return formatter.format(zonedTime(target, defaultZoneId));
            }
        } catch (final Exception e) {
            throw new TemplateProcessingException(
                "Error formatting date for locale " + formattingLocale, e);
        }
    }

    private DateTimeFormatter defaultOrPatternFormatted(final String pattern, final Locale locale, boolean hasDate, boolean hasTime) {
        switch (pattern) {
            case "SHORT"  : return buildFormatter(FormatStyle.SHORT, hasDate, hasTime).withLocale(locale);
            case "MEDIUM" : return buildFormatter(FormatStyle.MEDIUM, hasDate, hasTime).withLocale(locale);
            case "LONG"   : return buildFormatter(FormatStyle.LONG, hasDate, hasTime).withLocale(locale);
            case "FULL"   : return buildFormatter(FormatStyle.FULL, hasDate, hasTime).withLocale(locale);
            default       : return DateTimeFormatter.ofPattern(pattern, locale);
        }
    }

    private DateTimeFormatter buildFormatter(FormatStyle style, boolean hasDate, boolean hasTime) {
        if(!hasDate && hasTime) {
            return DateTimeFormatter.ofLocalizedTime(style);
        } else if(hasDate && !hasTime) {
            return DateTimeFormatter.ofLocalizedDate(style);
        } else {
            return DateTimeFormatter.ofLocalizedDateTime(style);
        }
    }

}
