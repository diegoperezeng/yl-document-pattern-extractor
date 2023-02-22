package com.diegoperezeng.documentpattern;

import com.diegoperezeng.documentpattern.utils.ImageTypeUtil;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

public class FeatureExtractor {

  private ORB detector;

  public FeatureExtractor() {
    this.detector = ORB.create();
  }

  public Mat extractFeatures(ImageMetadata imageMetadata) {
    Mat image = ImageTypeUtil.bufferedImageToMat(imageMetadata.getImage());
    Mat gray = new Mat();
    Mat edges = new Mat();
    MatOfKeyPoint keypoints = new MatOfKeyPoint();

    // Convert the image to grayscale
    Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

    // Detect edges using Canny algorithm
    Imgproc.Canny(gray, edges, 100, 200);

    // Detect keypoints using ORB feature detector
    detector.detect(gray, keypoints);

    // Draw keypoints on the image
    Mat output = new Mat();
    Features2d.drawKeypoints(image, keypoints, output);

    return output;
  }
}
