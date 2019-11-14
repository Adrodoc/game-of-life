package de.adrodoc.game.of.life;

public class Grid {
  private final int width;
  private final int height;
  private final boolean[] data;

  public Grid(int width, int height) {
    this.width = width;
    this.height = height;
    data = new boolean[width * height];
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean isAlive(int x, int y) {
    if (isXInRange(x) && isYInRange(y)) {
      int index = calculateIndex(x, y);
      return data[index];
    } else {
      return false;
    }
  }

  private boolean isXInRange(int x) {
    return 0 <= x && x < width;
  }

  private boolean isYInRange(int y) {
    return 0 <= y && y < height;
  }

  public void setAlive(int x, int y, boolean alive) {
    int index = calculateIndex(x, y);
    data[index] = alive;
  }

  private int calculateIndex(int x, int y) {
    return x + y * width;
  }

  public void updateAlive(int x, int y, Grid previous) {
    boolean alive = previous.calculateWillBeAlive(x, y);
    setAlive(x, y, alive);
  }

  private boolean calculateWillBeAlive(int x, int y) {
    int neighbours = getNeighbourCount(x, y);
    if (isAlive(x, y)) {
      return neighbours == 2 || neighbours == 3;
    } else {
      return neighbours == 3;
    }
  }

  private int getNeighbourCount(int x, int y) {
    int neighbours = 0;
    for (int xNeighbour = x - 1; xNeighbour <= x + 1; xNeighbour++) {
      for (int yNeighbour = y - 1; yNeighbour <= y + 1; yNeighbour++) {
        if (xNeighbour != x || yNeighbour != y) {
          if (isAlive(xNeighbour, yNeighbour)) {
            neighbours++;
          }
        }
      }
    }
    return neighbours;
  }
}
