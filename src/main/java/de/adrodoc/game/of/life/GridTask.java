package de.adrodoc.game.of.life;

import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

public class GridTask implements Callable<Void> {
  private Grid grid;
  private final IntSupplier widthSupplier;
  private final IntSupplier heightSupplier;
  private final Consumer<Grid> renderer;

  public GridTask(Grid grid, IntSupplier widthSupplier, IntSupplier heightSupplier,
      Consumer<Grid> renderer) {
    this.grid = requireNonNull(grid, "grid");
    this.widthSupplier = requireNonNull(widthSupplier, "widthSupplier");
    this.heightSupplier = requireNonNull(heightSupplier, "heightSupplier");
    this.renderer = requireNonNull(renderer, "renderer");
  }

  @Override
  public Void call() throws Exception {
    while (true) {
      long start = System.currentTimeMillis();
      renderer.accept(grid);
      int width = widthSupplier.getAsInt();
      int height = heightSupplier.getAsInt();
      Grid newGrid = new Grid(width, height);
      for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
          newGrid.updateAlive(x, y, grid);
        }
      }
      grid = newGrid;
      System.out.println("tick took " + (System.currentTimeMillis() - start) + "ms");
    }
  }
}
