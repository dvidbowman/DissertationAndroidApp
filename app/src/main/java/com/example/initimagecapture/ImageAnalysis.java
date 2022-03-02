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
import org.opencv.core.Rect;
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
    private Button discard_btn, save_btn;
    private ImageView preview_imgv, detection_imgv;
    private static Bitmap result;
    private static Bitmap boundSrc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);

        discard_btn = findViewById(R.id.button_discardImage);
        save_btn = findViewById(R.id.button_saveImage);
        preview_imgv = findViewById(R.id.imageView_preview);
        detection_imgv = findViewById(R.id.imageView_detection);

        // Sets Bitmap of preview ImageView using bytearray of image just taken
        Bitmap bitmap = BitmapFactory.decodeByteArray(User.getUserByteArray(), 0, User.getUserByteArray().length);
        preview_imgv.setImageBitmap(bitmap);

        // Automatic Rectangle Detection and Display of Cropped Image
        try {
            findRectangle(bitmap);
            detection_imgv.setImageBitmap(result);
            preview_imgv.setImageBitmap(boundSrc);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // OnClickListener for Save button
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                result.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                User.setCroppedImageByteArray(baos.toByteArray());
                openGetDyeAreaActivity();
            }
        });

        // OnClickListener for Discard button
        discard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    private static void findRectangle(Bitmap image) throws Exception {
        Mat first = new Mat();
        Mat temp = new Mat();
        Mat src = new Mat();
        Mat last = new Mat();
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
            temp_double = approxCurve.get(0, 0);
            Point p1 = new Point(temp_double[0], temp_double[1]);

            temp_double = approxCurve.get(1, 0);
            Point p2 = new Point(temp_double[0], temp_double[1]);

            temp_double = approxCurve.get(2, 0);
            Point p3 = new Point(temp_double[0], temp_double[1]);

            temp_double = approxCurve.get(3, 0);
            Point p4 = new Point(temp_double[0], temp_double[1]);

            //List<Point> source = new ArrayList<Point>();
            source.add(p1);
            source.add(p2);
            source.add(p3);
            source.add(p4);

            Mat startM = Converters.vector_Point2f_to_Mat(source);
            Mat sourceImage = new Mat();
            Utils.bitmapToMat(image, sourceImage);
            result = warp(sourceImage, startM);

            Imgproc.circle(src, p1, 10, new Scalar(255, 0, 0), 3);
            Imgproc.circle(src, p2, 10, new Scalar(255, 0, 0), 3);
            Imgproc.circle(src, p3, 10, new Scalar(255, 0, 0), 3);
            Imgproc.circle(src, p4, 10, new Scalar(255, 0, 0), 3);
        }

        Bitmap boundedSrc;
        boundedSrc = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(src, boundedSrc);
        boundSrc = boundedSrc;
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
        int resultHeight = 350;
        int resultWidth = 350;
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