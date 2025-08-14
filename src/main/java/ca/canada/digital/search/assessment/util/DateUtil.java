package ca.canada.digital.search.assessment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd";


    public static Date stringToDate(String dateString) {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(dateString);
        } catch (ParseException e) {
            LOG.error("The date provided is invalid.", e);
        }
        return null;
    }

    public static String dateToString(LocalDateTime date) {
        if (date == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

        return date.format(formatter);
    }

}
