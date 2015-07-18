package com.crazymerger;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FolderAnalysis {
  private File folder;
  private Map<String, File> folderMap;
  private Map<File, String> fileHashes;


  public FolderAnalysis(File folder) {
    this.folder = folder;
    this.folderMap = new HashMap<>();
    this.fileHashes = new HashMap<>();
  }

  public File addFileAnalysis(File file, String fileHash) {
    File doublon = folderMap.get(fileHash);

    if (doublon == null) {
      folderMap.put(fileHash, file);
    }
    fileHashes.put(file, fileHash);

    return doublon;
  }

  public File contains(String hash) {
    return folderMap.get(hash);
  }

  public String getFileHash(File file) {
    return fileHashes.get(file);
  }

  public Collection<File> getFiles() {
    return folderMap.values();
  }

  public File getFolder() {
    return folder;
  }
}
