package com.example.letsgogolfing.utils;

import android.text.TextUtils;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

/**
 * Utility class providing commonly used formatters for numeric and date formatting.
 * <p>
 * This class includes a {@link DecimalFormat} for formatting decimal numbers with commas,
 * and a {@link SimpleDateFormat} for formatting dates in the "yyyy-MM-dd" format.
 * <p>
 * Usage:
 * - Decimal Format: {@code String formattedDecimal = Formatters.decimalFormat.format(decimalValue);}
 * - Date Format: {@code String formattedDate = Formatters.dateFormat.format(date);}
 */
public class Formatters {
    /**
     * Decimal format instance for formatting decimal numbers with commas.
     * <p>
     * Example usage:
     * {@code String formattedDecimal = Formatters.decimalFormat.format(decimalValue);}
     */
    public static final DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
    /**
     * Date format instance for formatting dates in the "yyyy-MM-dd" format.
     * <p>
     * Example usage:
     * {@code String formattedDate = Formatters.dateFormat.format(date);}
     */
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

}
