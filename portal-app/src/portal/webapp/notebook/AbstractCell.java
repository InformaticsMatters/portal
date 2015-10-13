package portal.webapp.notebook;

public abstract class AbstractCell implements Cell {

    private String name;
    private int positionLeft;
    private int positionTop;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getPositionLeft() {
        return positionLeft;
    }

    public void setPositionLeft(int x) {
        this.positionLeft = x;
    }

    @Override
    public int getPositionTop() {
        return positionTop;
    }

    public void setPositionTop(int y) {
        this.positionTop = y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !o.getClass().equals(getClass())) {
            return false;
        }
        return ((Cell)o).getName().equals(getName());
    }

}
