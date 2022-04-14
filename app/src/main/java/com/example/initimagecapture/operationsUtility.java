package com.example.initimagecapture;

import android.graphics.Bitmap;
import android.graphics.Color;

public class operationsUtility {

    public static double getCalculatedCO2Percentage(Bitmap bmp) {
        double redTotal = 0;

        for (int y = 0; y < bmp.getHeight(); y++) {           // Currently uses every pixel in the Bitmap
            for (int x = 0; x < bmp.getWidth(); x++) {
                int pixelColours = bmp.getPixel(x, y);
                redTotal += Color.red(pixelColours);
            }
        }

        double redAverage = redTotal / (bmp.getWidth() * bmp.getHeight());

        double redBy255 = redAverage / 255;
        double uncorrectedGamma;

        if (redBy255 > 0.04045) {
            uncorrectedGamma = Math.pow(((redBy255 + 0.055) / 1.055), 2.4);
        }
        else {
            uncorrectedGamma = redBy255 / 12.92;
        }

        double correctedRed = uncorrectedGamma * 255;

        double noToLog = (255 / correctedRed);

        double appAbsorbance = Math.log10(noToLog);     // Excel uses Log10 by default

        double percentageCO2 = (1.21 - appAbsorbance) / ((appAbsorbance - 0.14) * 16);

        return (double) Math.round(percentageCO2 * 100000d) / 100000d;
        // 1.21 was the apparent absorbance under 0% CO2
        // 0.14 was the apparent absorbance under 100% CO2
    }

}
