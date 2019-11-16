package de.adrodoc.game.of.life;

public class Grid {
  private final int width;
  private final int height;
  private final boolean[] alive;
  private final int[] generation;
  private int totalGeneration;

  public Grid(int width, int height) {
    this.width = width;
    this.height = height;
    alive = new boolean[width * height];
    generation = new int[width * height];
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getTotalGeneration() {
    return totalGeneration;
  }

  public boolean isAlive(int x, int y) {
    if (isXInRange(x) && isYInRange(y)) {
      int index = calculateIndex(x, y);
      return alive[index];
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

  private int calculateIndex(int x, int y) {
    return x * height + y;
  }

  public void setAlive(int x, int y, boolean alive) {
    int index = calculateIndex(x, y);
    this.alive[index] = alive;
  }

  public int getGeneration(int x, int y) {
    if (isXInRange(x) && isYInRange(y)) {
      int index = calculateIndex(x, y);
      return generation[index];
    } else {
      return 0;
    }
  }

  public void setGeneration(int x, int y, int generation) {
    int index = calculateIndex(x, y);
    this.generation[index] = generation;
  }

  public void updateAlive(int x, int y, Grid previous) {
    totalGeneration = previous.totalGeneration + 1;
    boolean wasAlive = previous.isAlive(x, y);
    int neighbours = previous.getNeighbourCount(x, y);
    boolean alive = willBeAlive(wasAlive, neighbours);
    setAlive(x, y, alive);
    if (wasAlive && alive) {
      setGeneration(x, y, previous.getGeneration(x, y) + 1);
    } else {
      setGeneration(x, y, 0);
    }
  }

  private boolean willBeAlive(boolean wasAlive, int neighbours) {
    boolean alive;
    if (wasAlive) {
      // alive = neighbours == 2 || neighbours == 3;
      alive = neighbours == 3 || neighbours == 4 || neighbours == 6 || neighbours == 7
          || neighbours == 8;
    } else {
      // alive = neighbours == 3;
      alive = neighbours == 3 || neighbours == 6 || neighbours == 7 || neighbours == 8;
    }
    return alive;
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
