package com.diegoperezeng.documentpattern;

import java.util.List;

public class ModelEvaluator {

  private List<ImageMetadata> testData;
  private ModelTrainer trainer;

  public ModelEvaluator(List<ImageMetadata> testData, ModelTrainer trainer) {
    this.testData = testData;
    this.trainer = trainer;
  }

  public double evaluateModel() {
    int numCorrect = 0;
    int numTotal = testData.size();

    // Evaluate the model on the test data
    for (ImageMetadata imageMetadata : testData) {
      String trueLabel = imageMetadata.getLabel();
      String predictedLabel = trainer.predictLabel(imageMetadata);

      if (trueLabel.equals(predictedLabel)) {
        numCorrect++;
      }
    }

    // Calculate and return the accuracy of the model
    return (double) numCorrect / numTotal;
  }
}
