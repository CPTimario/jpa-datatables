final class Order {
    private int column;
    private String dir;

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        if (!"asc".equalsIgnoreCase(dir) && !"desc".equalsIgnoreCase(dir))
            throw new IllegalArgumentException(dir + " not a valid direction.");
        this.dir = dir;
    }
}
