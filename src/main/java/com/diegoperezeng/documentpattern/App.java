package com.diegoperezeng.documentpattern;

import java.util.ArrayList;
import java.util.List;

public class App {

  public static void main(String[] args) {
    // Initialize the feature extractor
    FeatureExtractor featureExtractor = new FeatureExtractor();

    // Load the test data
    List<ImageMetadata> testData = loadTestData();

    // Train the model on the training data
    List<ImageMetadata> trainingData = loadTrainingData();
    ModelTrainer trainer = new ModelTrainer(trainingData);
    trainer.trainModel();

    // Evaluate the model on the test data
    ModelEvaluator evaluator = new ModelEvaluator(testData, trainer);
    double accuracy = evaluator.evaluateModel();
    System.out.println("Model accuracy: " + accuracy);

    // Use the trained model to classify a new image
    String imagePath = "path/to/image.png";
    String predictedLabel = classifyImage(imagePath, featureExtractor, trainer);
    System.out.println("Predicted label: " + predictedLabel);
  }

  // Helper method to load the test data
  private static List<ImageMetadata> loadTestData() {
    List<ImageMetadata> testData = new ArrayList<>();
    testData.add(new ImageMetadata("test_image_1.png", "pattern_1"));
    testData.add(new ImageMetadata("test_image_2.png", "pattern_2"));
    // Add more test images as needed
    return testData;
  }

  // Helper method to load the training data
  private static List<ImageMetadata> loadTrainingData() {
    List<ImageMetadata> trainingData = new ArrayList<>();
    trainingData.add(new ImageMetadata("training_image_1.png", "pattern_1"));
    trainingData.add(new ImageMetadata("training_image_2.png", "pattern_2"));
    // Add more training images as needed
    return trainingData;
  }

  // Helper method to classify a new image using the trained model
  private static String classifyImage(
    String imagePath,
    FeatureExtractor featureExtractor,
    ModelTrainer trainer
  ) {
    ImageMetadata imageMetadata = new ImageMetadata(imagePath, "");
    String predictedLabel = trainer.predictLabel(imageMetadata);
    return predictedLabel;
  }
}
