package com.example.initimagecapture;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class detectionUtility {

    public static Bitmap findRectangle(Bitmap image) throws Exception {
        Mat first = new Mat();
        Mat temp = new Mat();
        Mat src = new Mat();

        Utils.bitmapToMat(image, temp);
        Utils.bitmapToMat(image, first);

        Imgproc.cvtColor(temp, src, Imgproc.COLOR_BGR2RGB);

        Mat blurred = src.clone();
        Imgproc.medianBlur(src, blurred,9);

        Mat gray0 = new Mat(blurred.size(), CvType.CV_8U), gray = new Mat();

        List<MatOfPoint> contours = new ArrayList<>();
        List<Mat> blurredChannel = new ArrayList<>();
        blurredChannel.add(blurred);
        List<Mat> gray0channel = new ArrayList<>();
        gray0channel.add(gray0);

        MatOfPoint2f approxCurve = new MatOfPoint2f();

        List<Point> source = new ArrayList<Point>();

        double maxArea = 0;
        int maxId = -1;

        for (int i = 0; i < 3; i++) {
            int ch[] = {i, 0 };
            Core.mixChannels(blurredChannel, gray0channel, new MatOfInt(ch));

            int thresholdLevel = 1;
            for (int j = 0; j < thresholdLevel; j++) {
                if (j == 0) {
                    Imgproc.Canny(gray0, gray, 10, 20, 3, true);
                    Imgproc.dilate(gray, gray, new Mat(), new Point(-1, -1), 1);
                }
                else {
                    Imgproc.adaptiveThreshold(gray0, gray, thresholdLevel,
                            Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                            Imgproc.THRESH_BINARY,
                            (src.width() + src.height()) / 200, j);
                }

                Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

                for (MatOfPoint contour : contours) {
                    MatOfPoint2f temp_contour = new MatOfPoint2f(contour.toArray());
                    double area = Imgproc.contourArea(contour);
                    MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp_contour, approxCurve_temp, Imgproc.arcLength(temp_contour, true) * 0.02, true);

                    if (approxCurve_temp.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<Point> curves = approxCurve_temp.toList();
                        for (int k = 2; k < 5; k++) {
                            double cosine = Math.abs(angle(curves.get(k % 4),
                                    curves.get(k - 2), curves.get(k - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                            approxCurve = approxCurve_temp;
                        }
                    }
                }

            }
        }

        if (maxId >= 0) {
            double[] temp_double;
            List<Point> unorderedPoints = new ArrayList<Point>();
            temp_double = approxCurve.get(0, 0);
            Point unorderedP1 = new Point(temp_double[0], temp_double[1]);

            temp_double = approxCurve.get(1, 0);
            Point unorderedP2 = new Point(temp_double[0], temp_double[1]);

            temp_double = approxCurve.get(2, 0);
            Point unorderedP3 = new Point(temp_double[0], temp_double[1]);

            temp_double = approxCurve.get(3, 0);
            Point unorderedP4 = new Point(temp_double[0], temp_double[1]);

            unorderedPoints.add(unorderedP1);
            unorderedPoints.add(unorderedP2);
            unorderedPoints.add(unorderedP3);
            unorderedPoints.add(unorderedP4);

            Point P1 = unorderedP1;
            // Point 1 will have the lowest x + y value
            for (Point p : unorderedPoints) {
                if ((p.x + p.y) <= (P1.x + P1.y)) {
                    P1 = p;
                }
            }
            unorderedPoints.remove(P1);     // Object referencing is used to remove the now known Point 1 from list of possible points

            Point P4 = unorderedP4;
            // Point 4 will have the greatest x + y value
            for (Point p : unorderedPoints) {
                if ((p.x + p.y) >= (P4.x + P4.y)) {
                    P4 = p;
                }
            }
            unorderedPoints.remove(P4);     // Known Point 4 is removed

            Point P2 = unorderedPoints.get(0);  // Last two points are assigned
            Point P3 = unorderedPoints.get(1);

            if (P2.x < P3.x) {                  // Point 2 always has larger x than Point 3, so if it turns out to be lower
                P2 = unorderedPoints.get(1);    // the values are swapped around
                P3 = unorderedPoints.get(0);
            }

            source.add(P1);
            source.add(P3);
            source.add(P4);
            source.add(P2);

            Mat startM = Converters.vector_Point2f_to_Mat(source);
            Mat sourceImage = new Mat();
            Utils.bitmapToMat(image, sourceImage);

            if (ImageManipulation.detectionCounter == 0) {                      // First pass detects bandage outline
                ImageManipulation.initResult = warp(sourceImage, startM);
            }
            else if (ImageManipulation.detectionCounter == 1) {                 // Second pass detects reactive patch
                ImageManipulation.mainResult = warp(sourceImage, startM);
            }
            else {
                ImageManipulation.constantResult = warp(sourceImage, startM);   // Last pass detects non-reactive patch
            }

            ImageManipulation.detectionCounter++;

            Imgproc.circle(src, unorderedP1, 15, new Scalar(255, 0, 0), 3);
            Imgproc.circle(src, unorderedP2, 30, new Scalar(255, 0, 0), 3);
            Imgproc.circle(src, unorderedP3, 50, new Scalar(255, 0, 0), 3);
            Imgproc.circle(src, unorderedP4, 70, new Scalar(255, 0, 0), 3);

            Imgproc.circle(src, P1, 10, new Scalar(0, 0, 255), 3);
            Imgproc.circle(src, P2, 20, new Scalar(0, 0, 255), 3);
            Imgproc.circle(src, P3, 40, new Scalar(0, 0, 255), 3);
            Imgproc.circle(src, P4, 60, new Scalar(0, 0, 255), 3);
        }

        Bitmap boundedSrc;
        boundedSrc = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, boundedSrc);
        return boundedSrc;
    }

    private static double angle(Point p1, Point p2, Point p0) {
        double dx1 = p1.x - p0.x;
        double dy1 = p1.y - p0.y;
        double dx2 = p2.x - p0.x;
        double dy2 = p2.y - p0.y;
        return (dx1 * dx2 + dy1 * dy2)
                / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2)
                + 1e-10);
    }

    private static Bitmap warp(Mat inputMat, Mat startM) {
        int resultHeight;
        int resultWidth;

        if (ImageManipulation.detectionCounter == 0) {
            resultHeight = 800;
            resultWidth = 500;
        }
        else {
            resultHeight = 200;
            resultWidth = 200;
        }

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        Point outTL = new Point(0, 0);
        Point outBL = new Point(0, resultHeight);
        Point outBR = new Point(resultWidth, resultHeight);
        Point outTR = new Point(resultWidth, 0);

        List<Point> dest = new ArrayList<Point>();
        dest.add(outTL);
        dest.add(outBL);
        dest.add(outBR);
        dest.add(outTR);

        Mat endM = Converters.vector_Point2f_to_Mat(dest);
        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat, outputMat, perspectiveTransform,
                new Size(resultWidth, resultHeight), Imgproc.INTER_CUBIC);

        Bitmap bmp;
        bmp = Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputMat, bmp);
        return bmp;
    }

}
