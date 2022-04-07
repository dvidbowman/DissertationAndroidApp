package com.example.initimagecapture;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageAnalysis extends AppCompatActivity {
    // Controls
    private Button discard_btn, next_btn;
    private ImageView preview_imgv, detection_imgv, constant_imgv, main_imgv;
    private RadioButton saveText_rbtn;
    private static Bitmap initResult, mainResult, constantResult, mainCropped, constantCropped, boundSrc;
    private static int detectionCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        discard_btn = findViewById(R.id.button_discardImage);
        next_btn = findViewById(R.id.button_nextImageAnalysis);
        preview_imgv = findViewById(R.id.imageView_preview);
        detection_imgv = findViewById(R.id.imageView_detection);
        constant_imgv = findViewById(R.id.imageView_constantReact);
        main_imgv = findViewById(R.id.imageView_mainReact);
        saveText_rbtn = findViewById(R.id.radioButton_saveValues);

        // Sets Bitmap of preview ImageView using bytearray of image just taken
        Bitmap bitmap = BitmapFactory.decodeByteArray(User.getUserByteArray(), 0, User.getUserByteArray().length);
        preview_imgv.setImageBitmap(bitmap);

        // Automatic Rectangle Detection and Display of Cropped Images
        try {
            Bitmap initPass = findRectangle(bitmap);
            preview_imgv.setImageBitmap(initPass);
            detection_imgv.setImageBitmap(initResult);

            //
            Bitmap mainReactBmp = Bitmap.createBitmap(initResult, 10, 250, initResult.getWidth() - 10, initResult.getHeight() - 250);
            Bitmap mainPass =  findRectangle(mainReactBmp);
            main_imgv.setImageBitmap(mainResult);
            int reactiveCropStartX = mainResult.getWidth() / 4;
            int reactiveCropStartY = mainResult.getHeight() / 4;
            int reactiveCropWidth = mainResult.getWidth() / 2;
            int reactiveCropHeight = mainResult.getHeight() / 2;
            mainCropped = Bitmap.createBitmap(mainResult, reactiveCropStartX, reactiveCropStartY, reactiveCropWidth, reactiveCropHeight);

            Bitmap constantReactBmp = Bitmap.createBitmap(initResult, 10, 10, initResult.getWidth() - 20, initResult.getHeight() - 535);
            Bitmap constantPass = findRectangle(constantReactBmp);
            constant_imgv.setImageBitmap(constantResult);
            int nonreactiveCropStartX = constantResult.getWidth() / 4;
            int nonreactiveCropStartY = constantResult.getHeight() / 4;
            int nonreactiveCropWidth = constantResult.getWidth() / 2;
            int nonreactiveCropHeight = constantResult.getHeight() / 2;
            constantCropped = Bitmap.createBitmap(constantResult, nonreactiveCropStartX, nonreactiveCropStartY, nonreactiveCropWidth, nonreactiveCropHeight);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // OnClickListener for Next button
        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteArrayOutputStream reactivebaos = new ByteArrayOutputStream();
                mainCropped.compress(Bitmap.CompressFormat.JPEG, 100, reactivebaos);
                User.setCroppedReactiveByteArray(reactivebaos.toByteArray());

                ByteArrayOutputStream nonreactivebaos = new ByteArrayOutputStream();
                constantCropped.compress(Bitmap.CompressFormat.JPEG, 100, nonreactivebaos);
                User.setCroppedNonReactiveByteArray(nonreactivebaos.toByteArray());

                User.setSaveRGBValues(saveText_rbtn.isChecked());

                openGetDyeAreaActivity();
            }
        });

        // OnClickListener for Discard button
        discard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectionCounter = 0;

                if (User.getCameFromCamera()) {
                    openImageCaptureActivity();
                }
                else {
                    openImportImageActivity();
                }

            }
        });

    }

    // Saving cropped image in database
    private void save(byte[] bytes) throws IOException {
        String deviceManufacturer = Build.MANUFACTURER;
        String deviceModel = Build.MODEL;
        String deviceOs = Build.VERSION.RELEASE;

        makeRequest imageUploadRequest = new makeRequest("http://192.168.0.29/projectPHP/imageupload.php", "POST", "imageUpload", bytes, deviceManufacturer, deviceModel, deviceOs);
        if (imageUploadRequest.startRequest()) {
            if(imageUploadRequest.onComplete()) {

                try {
                    JSONObject obj = new JSONObject(imageUploadRequest.getResult());
                    if (obj.getString("message").equals("none")) {
                        Toast.makeText(getApplicationContext(), "Image Uploaded Successfully", Toast.LENGTH_LONG).show();
                        User.setUserImageNo(User.getUserImageNo() + 1);
                        openImageCaptureActivity();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), imageUploadRequest.getResult(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Rectangle Detection Code based on Project by dhananjay-91
    // at https://github.com/dhananjay-91/DetectRectangle
    private static Bitmap findRectangle(Bitmap image) throws Exception {
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

            // pointOneCoords = "Ordered P1 X: " + P1.x + ", Y: " + P1.y;
            // pointTwoCoords = "Unordered P2 X: " + unorderedP2.x + ", Y: " + unorderedP2.y;

            source.add(P1);
            source.add(P3);
            source.add(P4);
            source.add(P2);

            Mat startM = Converters.vector_Point2f_to_Mat(source);
            Mat sourceImage = new Mat();
            Utils.bitmapToMat(image, sourceImage);

            if (detectionCounter == 0) {
                initResult = warp(sourceImage, startM);
            }
            else if (detectionCounter == 1) {
                mainResult = warp(sourceImage, startM);
            }
            else {
                constantResult = warp(sourceImage, startM);
            }

            detectionCounter++;

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

        if (detectionCounter == 0) {
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

    private void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }

    private void openImportImageActivity() {
        Intent intent = new Intent(this, ImportImage.class);
        startActivity(intent);
    }

    private void openGetDyeAreaActivity() {
        Intent intent = new Intent(this, GetDyeArea.class);
        startActivity(intent);
    }

}