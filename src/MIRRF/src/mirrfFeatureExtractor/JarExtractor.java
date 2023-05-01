package mirrfFeatureExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;

public class JarExtractor {
	
	public boolean extract(String resourceName, String destFolderName, String destFileName, boolean delete) {
		InputStream inpStream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
		if (inpStream != null) {
			try {
				File destFile = new File(destFolderName+destFileName);
				if (destFile.exists() && delete == false) {
					inpStream.close();
					return false;
				} else if (destFile.exists() && delete == true) {
					if (!destFile.delete()) {
						System.out.println("Warning: JarExtractor could not delete pre-existing file.");
						inpStream.close();
						return false;
					}
				}
				
				System.out.println(destFile.getPath());
				File destFolder = new File(destFolderName);
				if (destFolder.exists()) {
					File[] existingFiles = destFolder.listFiles();
					for (int i = 0; i < existingFiles.length; i++) {
						System.out.println(existingFiles[i].getName());
						if (existingFiles[i].isDirectory()) {
							try {
								FileUtils.deleteDirectory(existingFiles[i]);
							} catch (Exception e) {
								System.out.println("Warning: JarExtractor could not delete all pre-existing files in selected folder.");
								return false;
							}
						} else {
							if (!existingFiles[i].delete()) {
								System.out.println("Warning: JarExtractor could not delete all pre-existing files in selected folder.");
								return false;
							}
						}
					}
				} else {
					System.out.println("Warning: JarExtractor could not locate selected folder.");
					return false;
				}
			
				// Kudos: https://www.baeldung.com/convert-input-stream-to-a-file
				OutputStream outpStream = new FileOutputStream(destFile);
				byte[] buffer = new byte[8 * 1024];
			    int bytesRead;
			    while ((bytesRead = inpStream.read(buffer)) != -1) {
			        outpStream.write(buffer, 0, bytesRead);
			    }
			    inpStream.close();
			    outpStream.close();
			} catch (FileNotFoundException e) {
				System.out.println("Warning: JarExtractor could not find destination file.");
				return false;
			} catch (Exception e) {
				System.out.println("Warning: Exception caught in JarExtractor output stream.");
				return false;
			}
		    return true;
		} else {
			System.out.println("Warning: JarExtractor could not create input stream.");
			return false;
		}
	}
	
}