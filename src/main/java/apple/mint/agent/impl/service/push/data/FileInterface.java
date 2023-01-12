package apple.mint.agent.impl.service.push.data;

public class FileInterface {

    String interfaceId;
    String direction;
    String directory;
    String errorDirectory;
    int maxFileCountLimit;
    int fileTimeLimit;
    int errorFileDurLimit;
    int directoryCheckDelay;

    public FileInterface(String interfaceId, String direction, String directory, String errorDirectory,
            int maxFileCountLimit, int fileTimeLimit, int errorFileDurLimit, int directoryCheckDelay) {
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getErrorDirectory() {
        return errorDirectory;
    }

    public void setErrorDirectory(String errorDirectory) {
        this.errorDirectory = errorDirectory;
    }

    public int getMaxFileCountLimit() {
        return maxFileCountLimit;
    }

    public void setMaxFileCountLimit(int maxFileCountLimit) {
        this.maxFileCountLimit = maxFileCountLimit;
    }

    public int getFileTimeLimit() {
        return fileTimeLimit;
    }

    public void setFileTimeLimit(int fileTimeLimit) {
        this.fileTimeLimit = fileTimeLimit;
    }

    public int getErrorFileDurLimit() {
        return errorFileDurLimit;
    }

    public void setErrorFileDurLimit(int errorFileDurLimit) {
        this.errorFileDurLimit = errorFileDurLimit;
    }

    public int getDirectoryCheckDelay() {
        return directoryCheckDelay;
    }

    public void setDirectoryCheckDelay(int directoryCheckDelay) {
        this.directoryCheckDelay = directoryCheckDelay;
    }

    
    
}
