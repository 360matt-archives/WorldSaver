package fr.ulity.worldsaver.exceptions;

public class BackupNotFoundException extends Exception {

    public BackupNotFoundException (String filename) {
        super("Backup file " + filename + ".wsaver not exist");
    }

}
