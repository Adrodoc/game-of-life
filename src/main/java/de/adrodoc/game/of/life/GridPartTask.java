package de.adrodoc.game.of.life;

import static java.util.Objects.requireNonNull;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

public class GridPartTask implements Callable<Void> {
  private final CyclicBarrier barrier;
  private AtomicReference<Grid> grid;

  public GridPartTask(CyclicBarrier barrier, AtomicReference<Grid> grid) {
    this.barrier = requireNonNull(barrier, "barrier");
    this.grid = requireNonNull(grid, "grid");
  }

  @Override
  public Void call() throws Exception {
    int parties = barrier.getParties();
    Grid oldGrid = grid.get();
    while (true) {
      int index = barrier.await();
      Grid newGrid = grid.get();
      int partHeight = newGrid.getHeight();
      int width = newGrid.getWidth();
      int partWidth = width / parties;
      int widthRemainder = width % parties;
      int startX = index * partWidth + Math.min(index, widthRemainder);
      int endX = startX + partWidth + (index < widthRemainder ? 1 : 0);
      for (int x = startX; x < endX; x++) {
        for (int y = 0; y < partHeight; y++) {
          newGrid.updateAlive(x, y, oldGrid);
        }
      }
      oldGrid = newGrid;
    }
  }
}
