package framework.models;

public class MapFile {

    private String name;
    private String extension;
    private byte[] content;

    public MapFile() {
    }

    public MapFile(String name, String extension, byte[] content) {
        this.name = name;
        this.extension = extension;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public long getSize() {
        return content == null ? 0 : content.length;
    }

    @Override
    public String toString() {
        return "MapFile{name=" + name + ", extension=" + extension + ", size=" + getSize() + "}";
    }
}
