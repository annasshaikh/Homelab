import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;  
import java.util.Date; 

public class Sync {
    public static void main(String[] args) {
        final String IP = "192.168.100.37"; //NAS IP
        final int Waittime = 1800000; // Waiting Time In Miliseconds
        final String src = "D:\\CodeSpace"; //Address of the Source Folder
        final String des = "Z:\\Share\\DATA\\CodeSpace"; 
        boolean reachable = false;
        //Wait for server to become reachable
        while(!reachable){

            try {
                System.out.printf("Connecting to NAS (%s)\n",IP);
                //Ping the Server.
                InetAddress inet = InetAddress.getByName(IP);
                reachable = inet.isReachable(5000);
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
            }

            if (reachable)
                System.out.println("Connection Established");
            else{
                System.out.println("NAS - Server Down \n" + IP + " Not Reachable\n" + "Retrying in " +  Waittime/(30*60*1000) + " Seconds");
                //Sleep for Waittime
                try {
                    Thread.sleep(Waittime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Syncing...");
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		Date date = new Date();  
		System.out.println(formatter.format(date)); 

        File src_file = new File(src);
        File des_file = new File(des);

        try (FileWriter logs = new FileWriter(new File(des + "\\log.txt"), true)) {
			logs.append("\n ---------------- \nSync At: " + formatter.format(date) + "\n ----------------\n");
            System.out.println("------------------------------------------");
            syncFolders(src_file, des_file, logs);
			logs.append("*************************** \n");
        } catch (IOException e) {
            System.out.println("Record Database Not Accessable");
            e.printStackTrace();
        }

        System.out.println("------------------------------------------");
        System.out.println("Synced");
		
		
		
		try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public static void syncFolders(File source, File destination, FileWriter logs) throws IOException{
        // Check if the source file is a directory
        if (!source.isDirectory()) {
            System.out.println("Error: Source is not a directory.");
            return;
        }

        // Check if the destination file is a directory
        if (!destination.isDirectory()) {
            System.out.println("Error: Destination is not a directory.");
            return;
        }

        // Loop over the files in the source directory
        for (File sourceFile : source.listFiles()) {
            // Create the corresponding file in the destination directory
            File destinationFile = new File(destination, sourceFile.getName());

            // If the file doesn't exist in the destination directory, or if it has been modified more recently
            if (!destinationFile.exists() || sourceFile.lastModified() > destinationFile.lastModified() || sourceFile.length() != destinationFile.length()) {
                // If the source file is a directory, recursively call the syncFolders method
                if (!destinationFile.exists()) {
                    if (sourceFile.isDirectory()) {
                        destinationFile.mkdirs();
                        try {
                            logs.append("copy," + destinationFile.toString() + ",a\n");
                        } catch (IOException e) {
                            System.out.println("Error #96: IOException Logs Not Appended");
                        }

                        syncFolders(sourceFile, destinationFile, logs);
                    } else {
                        destinationFile.getParentFile().mkdirs();
                        copyFile(sourceFile, destinationFile);
                        try {
                            logs.append("copy," + destinationFile.toString() + ",r\n");
                        } catch (IOException e) {
                            System.out.println("Error #106: IOException Logs Not Appended");
                        }
                    }
                }                
            }
        }

        //Loop over the files in the destination directory
        for (File destinationFile : destination.listFiles()) {
            // Create the corresponding file in the source directory
            File sourceFile = new File(source, destinationFile.getName());
            boolean exceptiofiles = false;
            for (String x : new String[]{"logs.txt","index.html","TempBin"})
                if (destination.getName().equals(x)) 
                    exceptiofiles = true;

            // If the file doesn't exist in the source directory, delete the file in destination
            if (!sourceFile.exists() && !exceptiofiles) {
                try {
                    logs.append("delete," + destinationFile.toString() + ",\n");
                } catch (IOException e) {
                    System.out.println("Error #123: IOException Logs Not Appended");
                }
                deleteDirectory(destinationFile);
            }
        }
    }

    private static void copyFile(File source, File destination) {
        try {
            // Use FileInputStream and FileOutputStream to copy the file
            java.nio.file.Files.copy(source.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File Copied: " + source + " to " + destination);
        } catch (Exception e) {
            System.out.println("Error copying file: " + e);
        }
    }
	public static void deleteDirectory(File directory){

        // if the file is directory or not
        if(directory.isDirectory()) {
          File[] files = directory.listFiles();
    
          // if the directory contains any file
          if(files != null) {
            for(File file : files) {
    
              // recursive call if the subdirectory is non-empty
              deleteDirectory(file);
            }
          }
        }
    
        directory.delete();
      }

}

