package com.crazymerger;

import com.google.common.collect.FluentIterable;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Analyzer {
  private File[] folders;
  private File destination;
  private Logger logger;


  public static void main(String[] args) throws Exception {
    if (args.length == 1) {
      System.out.println("Usage: crazy-merger <folder_1> <folder_2> <folder_n> <destination>");
    }
    if (args.length == 2) {
      System.out.println("Missing destination");
    }
    else {
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

    logger = new Logger(destination.getParentFile());
    List<FolderAnalysis> folderAnalyses = analyzeFolders();
    FolderAnalysis destinationAnalysis = analyzeFolder(destination);

    mergeFoldersWithDestination(folderAnalyses);
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
      String[] content = destination.list();
      if (content != null && content.length > 0) {
        throw new IllegalArgumentException(destination + " is not empty");
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

  private List<FolderAnalysis> analyzeFolders() {
    List<FolderAnalysis> analysises = new LinkedList<>();

    for (File folder : folders) {
      analysises.add(analyzeFolder(folder));
    }

    return analysises;
  }

  private FolderAnalysis analyzeFolder(File folder) {
    FolderAnalysis folderAnalysis = new FolderAnalysis(folder);
    HashFunction hashFunction = Hashing.sha256();
    FluentIterable<File> itFiles = Files.fileTreeTraverser().preOrderTraversal(folder);


    for (File file : itFiles) {
      try {
        if (file.isFile()) {
          HashCode hashCode = Files.hash(file, hashFunction);
          File doublonInFolder = folderAnalysis.addFileAnalysis(file, hashCode.toString());
          logger.info(file.getAbsolutePath() + " : " + hashCode.toString());

          if (doublonInFolder != null) {
            logger.doublon(file, doublonInFolder);
          }
        }
      }
      catch (IOException ioe) {
        logger.error("Error hashing file " + file.getAbsolutePath(), ioe);
        ioe.printStackTrace();
      }
    }

    return folderAnalysis;
  }

  private void mergeFoldersWithDestination(List<FolderAnalysis> analyses) {
    FolderAnalysis destinationAnalysis;


    for (FolderAnalysis folderAnalysis : analyses) {
      destinationAnalysis = analyzeFolder(destination);
      mergeWithDestination(folderAnalysis, destinationAnalysis);
    }
  }

  private void mergeWithDestination(FolderAnalysis folderAnalysis, FolderAnalysis destinationAnalysis) {
    for (File file : folderAnalysis.getFiles()) {
      File fileInDestination = destinationAnalysis.contains(folderAnalysis.getFileHash(file));
      if (fileInDestination == null) {
        copyToDestination(folderAnalysis.getFolder(), file);
      }
      else {
        logger.doublon(file, fileInDestination);
      }
    }
  }

  private void copyToDestination(File folder, File file) {
    File destFile = new File(destination.getAbsolutePath() + File.separator + folder.toURI().relativize(file.toURI()).getPath());
    try {
      Files.copy(file, destFile);
    }
    catch (IOException ioe) {
      logger.error("Cannot copy file " + file.getAbsolutePath() + " to destination", ioe);
    }
  }

  private void mergeWithDestination(FolderAnalysis analysis) {
    for (File file : analysis.getFiles()) {
      File destFile = new File(destination.getAbsolutePath() + File.separator + analysis.getFolder().toURI().relativize(file.toURI()).getPath());
      try {
        Files.copy(file, destFile);
      }
      catch (IOException ioe) {
        logger.error("Cannot copy file " + file.getAbsolutePath() + " to destination", ioe);
      }
    }
  }
}
