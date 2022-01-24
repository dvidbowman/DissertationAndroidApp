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
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageAnalysis extends AppCompatActivity {
    // Controls
    private Button discard_btn, save_btn;
    private ImageView preview_imgv, detection_imgv;
    private Bitmap detection;

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
            detection = findRectangle(bitmap);
            detection_imgv.setImageBitmap(detection);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // OnClickListener for Save button
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                detection.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                User.setCroppedImageByteArray(baos.toByteArray());
                openGetDyeAreaActivity();

                /*
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    detection.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bytesToSave = baos.toByteArray();
                    save(bytesToSave);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                 */

            }
        });

        // OnClickListener for Discard button
        discard_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageCaptureActivity();
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

        MatOfPoint2f approxCurve;

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
                    MatOfPoint2f temp2f = new MatOfPoint2f(contour.toArray());
                    double area = Imgproc.contourArea(contour);
                    approxCurve = new MatOfPoint2f();
                    Imgproc.approxPolyDP(temp2f, approxCurve, Imgproc.arcLength(temp2f, true) * 0.02, true);

                    if (approxCurve.total() == 4 && area >= maxArea) {
                        double maxCosine = 0;

                        List<Point> curves = approxCurve.toList();
                        for (int k = 2; k < 5; k++) {
                            double cosine = Math.abs(angle(curves.get(k % 4),
                                    curves.get(k - 2), curves.get(k - 1)));
                            maxCosine = Math.max(maxCosine, cosine);
                        }

                        if (maxCosine < 0.3) {
                            maxArea = area;
                            maxId = contours.indexOf(contour);
                        }
                    }
                }
            }
        }

        if (maxId >= 0) {
            Rect rect = Imgproc.boundingRect(contours.get(maxId));
            Rect rec = new Rect(rect.x, rect.y, rect.width, rect.height);
            last = first.submat(rec);
        }

        Bitmap bmp;
        bmp = Bitmap.createBitmap(last.cols(), last.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(last, bmp);
        return bmp;
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

    private void openImageCaptureActivity() {
        Intent intent = new Intent(this, ImageCapture.class);
        startActivity(intent);
    }

    private void openGetDyeAreaActivity() {
        Intent intent = new Intent(this, GetDyeArea.class);
        startActivity(intent);
    }

}