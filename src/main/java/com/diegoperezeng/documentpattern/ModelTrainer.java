package com.diegoperezeng.documentpattern;

import com.diegoperezeng.documentpattern.db.DatabaseConnector;
import com.diegoperezeng.documentpattern.utils.ImageTypeUtil;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelTrainer {

  private List<ImageMetadata> trainingData;
  private KNearest knn;
  private DatabaseConnector dbConnector;
  private ORB detector;
  private static String SQL_EXCEPTION = "Error executing SQL query:";
  private static String SQL_QUERY =
    "INSERT INTO training_data (features, label) VALUES (";

  private static final Logger logger = LoggerFactory.getLogger(
    ModelTrainer.class
  );

  public ModelTrainer(List<ImageMetadata> trainingData) {
    this.trainingData = trainingData;
    this.detector = ORB.create();
    this.knn = KNearest.create();
    try {
      this.dbConnector =
        new DatabaseConnector(
          "jdbc:mysql://localhost:3306/mydatabase",
          "username",
          "password"
        );
    } catch (SQLException e) {
      logger.error(SQL_EXCEPTION + e.getMessage(), e);
    }
  }

  public void trainModel() {
    List<Mat> featureVectors = new ArrayList<>();

    // Extract features from training images and store as feature vectors
    for (ImageMetadata imageMetadata : trainingData) {
      List<Mat> augmentedImages = augmentImage(imageMetadata);

      for (Mat augmentedImage : augmentedImages) {
        Mat features = extractFeatures(augmentedImage);
        featureVectors.add(features);

        // Insert feature vector and label into MySQL database
        String insertQuery =
          SQL_QUERY + features.dump() + ", '" + imageMetadata.getLabel() + "')";
        try {
          dbConnector.executeUpdate(insertQuery);
        } catch (SQLException e) {
          logger.error(SQL_EXCEPTION + e.getMessage(), e);
        }
      }
    }

    // Convert the feature vectors to a training data matrix
    int numFeatures = featureVectors.get(0).cols();
    Mat trainingDataMat = new Mat(
      featureVectors.size(),
      numFeatures,
      featureVectors.get(0).type()
    );
    for (int i = 0; i < featureVectors.size(); i++) {
      featureVectors.get(i).copyTo(trainingDataMat.row(i));
    }

    // Create the training labels array
    Mat labelsMat = new Mat(trainingData.size() * 3, 1, CvType.CV_32SC1);
    int index = 0;
    for (int i = 0; i < trainingData.size(); i++) {
      labelsMat.put(index++, 0, i);
      labelsMat.put(index++, 0, i);
      labelsMat.put(index++, 0, i);
    }

    // Train the KNN model
    knn.train(trainingDataMat, Ml.ROW_SAMPLE, labelsMat);
  }

  public String predictLabel(ImageMetadata imageMetadata) {
    Mat features = extractFeatures(
      ImageTypeUtil.bufferedImageToMat(imageMetadata.getImage())
    );

    // Use the KNN model to predict the label of the input image
    Mat response = new Mat();
    Mat neighborResponses = new Mat();
    Mat dist = new Mat();
    int k = 1;
    knn.findNearest(features, k, response, neighborResponses, dist);

    int labelIndex = (int) response.get(0, 0)[0];
    String label = trainingData.get(labelIndex).getLabel();

    return label;
  }

  private List<Mat> augmentImage(ImageMetadata imageMetadata) {
    List<Mat> augmentedImages = new ArrayList<>();
    List<Mat> featureVectors = new ArrayList<>();
    int numAugmentations = 2;

    // Extract features for the original image
    Mat image = ImageTypeUtil.bufferedImageToMat(imageMetadata.getImage());
    augmentedImages.add(image);

    // Augment and extract features for rotated images
    for (int i = 0; i < numAugmentations; i++) {
      Mat imageRotated = new Mat();
      Core.rotate(image, imageRotated, Core.ROTATE_90_CLOCKWISE);
      Mat featuresRotated = extractFeatures(imageRotated);

      // Insert the feature vector for the rotated image into the database
      String insertQueryRotated =
        SQL_QUERY +
        featuresRotated.dump() +
        ", '" +
        imageMetadata.getLabel() +
        "_rotated_" +
        i +
        "')";
      try {
        dbConnector.executeUpdate(insertQueryRotated);
      } catch (SQLException e) {
        logger.error(SQL_EXCEPTION + e.getMessage(), e);
      }

      featureVectors.add(featuresRotated);
      augmentedImages.add(imageRotated);
    }

    // Augment and extract features for flip vertical images
    for (int i = 0; i < numAugmentations; i++) {
      Mat imageFlipped = new Mat();
      Core.flip(
        ImageTypeUtil.bufferedImageToMat(imageMetadata.getImage()),
        imageFlipped,
        0
      );
      Mat featuresFlipped = extractFeatures(imageFlipped);

      // Insert the feature vector for the flipped image into the database
      String insertQueryFlipped =
        SQL_QUERY +
        featuresFlipped.dump() +
        ", '" +
        imageMetadata.getLabel() +
        "_flipped_" +
        i +
        "')";
      try {
        dbConnector.executeUpdate(insertQueryFlipped);
      } catch (SQLException e) {
        logger.error(SQL_EXCEPTION + e.getMessage(), e);
      }

      featureVectors.add(featuresFlipped);
      augmentedImages.add(imageFlipped);
    }

    // Augment and extract features for flip horizontal images
    for (int i = 0; i < numAugmentations; i++) {
      Mat imageFlipped = new Mat();
      Core.flip(
        ImageTypeUtil.bufferedImageToMat(imageMetadata.getImage()),
        imageFlipped,
        1
      );
      Mat featuresFlipped = extractFeatures(imageFlipped);

      // Insert the feature vector for the flipped image into the database
      String insertQueryFlipped =
        SQL_QUERY +
        featuresFlipped.dump() +
        ", '" +
        imageMetadata.getLabel() +
        "_flipped_h_" +
        i +
        "')";
      try {
        dbConnector.executeUpdate(insertQueryFlipped);
      } catch (SQLException e) {
        logger.error(SQL_EXCEPTION + e.getMessage(), e);
      }

      featureVectors.add(featuresFlipped);
      augmentedImages.add(imageFlipped);
    }

    return augmentedImages;
  }

  private Mat extractFeatures(Mat image) {
    Mat gray = new Mat();
    Mat edges = new Mat();
    MatOfKeyPoint keypoints = new MatOfKeyPoint();
    MatOfFloat descriptors = new MatOfFloat();
    int numFeatures = 50;
    ORB descriptor = ORB.create();

    // Convert the image to grayscale
    Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);

    // Detect edges using Canny algorithm
    Imgproc.Canny(gray, edges, 100, 200);

    // Detect keypoints using ORB feature detector
    detector.detect(gray, keypoints);

    // Extract feature descriptors using ORB feature extractor
    Features2d.drawKeypoints(image, keypoints, image);
    Mat descriptorsMat = new Mat();
    descriptor.compute(gray, keypoints, descriptorsMat);

    // Reduce the number of features to a fixed size
    Mat descriptorsMatReduced = new Mat(
      numFeatures,
      descriptorsMat.cols(),
      descriptorsMat.type()
    );
    for (int i = 0; i < numFeatures; i++) {
      descriptorsMat.row(i).copyTo(descriptorsMatReduced.row(i));
    }

    // Normalize the feature vectors
    Core.normalize(descriptorsMatReduced, descriptors);

    return descriptors;
  }
}
