package com.crazymerger;

import java.io.File;
import java.io.FileNotFoundException;

public class Analyzer {
  private File[] folders;
  private File destination;


  public static void main(String[] args) throws Exception {
    if (args.length == 1) {
      System.out.println("Usage: crazy-merger <folder_1> <folder_2> <folder_n> <destination>");
    }
    if (args.length == 2) {
      System.out.println("Missing destination");
    } else {
      File[] folders = new File[args.length - 1];
      for (int i = 0; i < args.length - 1; i++) {
        folders[i] = new File(args[i]);
      }

      File destination = new File(args[args.length - 1]);
      new Analyzer(folders, destination).run();
    }
  }

  public Analyzer(File[] folders, File destination) {
    this.folders = folders;
    this.destination = destination;
  }

  public void run() throws IllegalArgumentException, FileNotFoundException {
    checkFolders();
    checkDestination();
  }

  private void checkFolders() throws IllegalArgumentException {
    for (File folder : folders) {
      if (!folder.exists()) {
        throw new IllegalArgumentException("Folder " + folder + " does not exist");
      }
      if (!folder.isDirectory()) {
        throw new IllegalArgumentException(folder + " is not a directory");
      }
      
      System.out.println("Checked " + folder.getAbsolutePath());
    }
  }

  private void checkDestination() throws FileNotFoundException {
    if (destination.exists()) {
      if (!destination.isDirectory()) {
        throw new IllegalArgumentException(destination + " is not a directory");
      }
    }
    else {
      boolean mkdirSuccess = destination.mkdir();

      if (!mkdirSuccess) {
        throw new FileNotFoundException("Destination folder " + destination.getAbsolutePath() + " does not exist and cannot be created");
      }
    }

    System.out.println("Checked " + destination.getAbsolutePath());
  }
}
