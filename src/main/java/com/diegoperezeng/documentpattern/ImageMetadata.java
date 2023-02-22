package com.diegoperezeng.documentpattern;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageMetadata {

  private String filePath;
  private String label;
  private BufferedImage image;

  public ImageMetadata(String filePath, String label) {
    this.filePath = filePath;
    this.label = label;
    this.image = loadImageFromResources(filePath);
  }

  public String getFilePath() {
    return filePath;
  }

  public String getLabel() {
    return label;
  }

  public BufferedImage getImage() {
    return image;
  }

  private BufferedImage loadImageFromResources(String filePath) {
    try {
      return ImageIO.read(new File("src/main/resources/images/" + filePath));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load the image: " + filePath, e);
    }
  }
}
