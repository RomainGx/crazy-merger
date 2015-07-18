package com.crazymerger;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Analysis of a folder consists in retaining its files and being able to
 * recognize duplicates thanks to their hashes.
 */
public class FolderAnalysis {
  /** Folder concerned by the analysis. */
  private File folder;
  /** Map of unique hashes with the first file that generated this hash. */
  private Map<String, File> folderMap;
  /** Map of all the files, including duplicates, with their hashes. */
  private Map<File, String> fileHashes;


  /**
   * Create a new container for folder analysis.
   * @param folder Folder analyzed.
   */
  public FolderAnalysis(File folder) {
    this.folder = folder;
    this.folderMap = new HashMap<>();
    this.fileHashes = new HashMap<>();
  }

  /**
   * Add result of a file analysis if and only if the file is not a duplicate element.
   * @param file File analyzed.
   * @param fileHash Hash of the file.
   * @return Any existing file that matches this hash in the folder, or <code>null</code> if there is no duplicate element.
   */
  public File addFileAnalysis(File file, String fileHash) {
    File duplicate = folderMap.get(fileHash);

    if (duplicate == null) {
      folderMap.put(fileHash, file);
    }
    fileHashes.put(file, fileHash);

    return duplicate;
  }

  /**
   * Return the file in {@link FolderAnalysis#folder} that matches the given hash.
   * @param hash Hash used to check if a file already exists in the folder.
   * @return A file if there is already one for the given <code>hash</code>, or <code>null</code> if no one matches.
   */
  public File contains(String hash) {
    return folderMap.get(hash);
  }

  /**
   * Return the hash of a given file.
   * @param file File for which to get the hash.
   * @return The hash of <code>file</code>, or <code>null</code> if it was not already analyzed.
   */
  public String getFileHash(File file) {
    return fileHashes.get(file);
  }

  /**
   * @return List files with unique hashes (no duplicate elements).
   */
  public Collection<File> getFiles() {
    return folderMap.values();
  }

  /**
   * @return The folder concerned by analysis.
   */
  public File getFolder() {
    return folder;
  }
}
