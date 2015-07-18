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

/**
 * Analyze content of several folders and merge it without duplication in
 * the destination directory.
 */
public class Analyzer {
  /** Folders to merge. */
  private File[] folders;
  /** Directory where to merge {@link Analyzer#folders}. */
  private File destination;
  /** Events logger. */
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

  /**
   * Create a new object for merging content of <code>folders</code> in <code>destination</code>.
   * @param folders Folders to merge without duplicate elements.
   * @param destination Directory where to merge folders' content.
   */
  public Analyzer(File[] folders, File destination) {
    this.folders = folders;
    this.destination = destination;
  }

  /**
   * Start merging of folders.
   * @throws IllegalArgumentException If there are problems with folders or destination.
   * @throws FileNotFoundException If there are problems with folders or destination.
   */
  public void run() throws IllegalArgumentException, FileNotFoundException {
    checkFolders();
    checkDestination();

    logger = new Logger(destination.getParentFile());
    List<FolderAnalysis> folderAnalyses = analyzeFolders();

    mergeFoldersWithDestination(folderAnalyses);
  }

  /**
   * Check validity of folders to merge.
   * @throws IllegalArgumentException If there are problems with folders.
   */
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

  /**
   * Check validity of destination.
   * @throws FileNotFoundException If there are problems with destination.
   */
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

  /**
   * Analyze content of all folders.
   * @return List of analysis results.
   */
  private List<FolderAnalysis> analyzeFolders() {
    List<FolderAnalysis> analyses = new LinkedList<>();

    for (File folder : folders) {
      analyses.add(analyzeFolder(folder));
    }

    return analyses;
  }

  /**
   * Analyze files of a given folder. Duplicate files are ignored: only one of them will be merged.
   * @param folder Folder to analyze.
   * @return Result of the analyze.
   */
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
            logger.conflict(file, doublonInFolder);
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

  /**
   * Merge each folder with destination, one by one.
   * @param analyses Folder analyses.
   */
  private void mergeFoldersWithDestination(List<FolderAnalysis> analyses) {
    FolderAnalysis destinationAnalysis;


    for (FolderAnalysis folderAnalysis : analyses) {
      destinationAnalysis = analyzeFolder(destination);
      mergeWithDestination(folderAnalysis, destinationAnalysis);
    }
  }

  /**
   * Merge one folder with destination.
   * @param folderAnalysis Analysis of the folder to merge.
   * @param destinationAnalysis Analysis of the destination.
   */
  private void mergeWithDestination(FolderAnalysis folderAnalysis, FolderAnalysis destinationAnalysis) {
    for (File file : folderAnalysis.getFiles()) {
      File fileInDestination = destinationAnalysis.contains(folderAnalysis.getFileHash(file));

      if (fileInDestination == null) {
        copyToDestination(folderAnalysis.getFolder(), file);
      }
      else {
        logger.conflict(file, fileInDestination);
      }
    }
  }

  /**
   * Copy a <code>file</code> of a <code>folder</code> into destination.
   * @param folder Folder that contains <code>file</code>.
   * @param file File to copy to destination.
   */
  private void copyToDestination(File folder, File file) {
    try {
      String fileRelativePath = folder.toURI().relativize(file.toURI()).getPath();
      File destFile = new File(destination.getAbsolutePath() + File.separator + fileRelativePath);
      Files.copy(file, destFile);
    }
    catch (IOException ioe) {
      logger.error("Cannot copy file " + file.getAbsolutePath() + " to destination", ioe);
    }
  }
}
