package de.adrodoc.game.of.life;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameOfLife extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  private static final KeyCodeCombination TOGGLE_FULLSCREEN = new KeyCodeCombination(KeyCode.F11);

  private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
    private final ThreadFactory delegate = Executors.defaultThreadFactory();

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = delegate.newThread(r);
      thread.setDaemon(true);
      return thread;
    }
  });
  private final Queue<Grid> gridCache = new ArrayDeque<>();
  private final Canvas canvas;

  public GameOfLife() {
    canvas = new Canvas();
  }

  private int getHeight() {
    return (int) canvas.getHeight();
  }

  private int getWidth() {
    return (int) canvas.getWidth();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Pane root = new Pane(canvas);
    canvas.heightProperty().bind(root.heightProperty());
    canvas.widthProperty().bind(root.widthProperty());
    primaryStage.setScene(new Scene(root, 500, 500));
    primaryStage.setFullScreen(true);
    primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
      if (TOGGLE_FULLSCREEN.match(event)) {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
      }
    });
    primaryStage.show();

    BlockingQueue<Grid> queue = new ArrayBlockingQueue<>(5);

    // AtomicLong time = new AtomicLong();
    Grid initialGrid = getInitialGrid();
    AtomicReference<Grid> gridRef = new AtomicReference<>(initialGrid);
    int threadCount = 8;
    CyclicBarrier barrier = new CyclicBarrier(threadCount, () -> {
      Grid newGrid = provideGrid(getWidth(), getHeight());
      Grid oldGrid = gridRef.getAndSet(newGrid);

      // System.out.println("queue size: " + queue.size());
      try {
        queue.put(oldGrid);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      // long now = System.currentTimeMillis();
      // long start = time.getAndSet(now);
      // System.out.println("tick took " + (now - start) + "ms");
    });
    for (int i = 0; i < threadCount; i++) {
      executor.submit(new GridPartTask(barrier, gridRef));
    }

    // Rendering
    executor.submit(() -> {
      Semaphore semaphore = new Semaphore(0);
      while (true) {
        Grid grid = queue.take();
        // long start = System.currentTimeMillis();
        Platform.runLater(() -> {
          try {
            paintGrid(grid);
          } finally {
            semaphore.release();
          }
        });
        semaphore.acquire();
        gridCache.add(grid);
        // System.out.println("rendering took " + (System.currentTimeMillis() - start) + "ms");
      }
    });
  }

  private Grid provideGrid(int width, int height) {
    while (!gridCache.isEmpty()) {
      Grid grid = gridCache.remove();
      if (grid.getWidth() == width && grid.getHeight() == height) {
        return grid;
      }
    }
    return new Grid(width, height);
  }

  private Grid getInitialGrid() {
    int width = getWidth();
    int height = getHeight();
    Grid initialGrid = new Grid(width, height);
    for (int x = 0; x < width - 2; x++) {
      for (int y = 0; y < height - 2; y++) {
        if (x % 3 == 0 ^ y % 3 == 0) {
          initialGrid.setAlive(x, y, true);
        }
      }
    }
    int xCenter = width / 2;
    int yCenter = height / 2;
    int r = 1;
    for (int x = xCenter - r; x <= xCenter + r; x++) {
      for (int y = yCenter - r; y <= yCenter + r; y++) {
        initialGrid.setAlive(x, y, true);
      }
    }
    return initialGrid;
  }

  private void paintGrid(Grid grid) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(Color.BLACK);
    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    PixelWriter pixelWriter = gc.getPixelWriter();
    int totalGeneration = grid.getTotalGeneration();
    int width = grid.getWidth();
    int height = grid.getHeight();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        int generation = grid.getGeneration(x, y);
        if (generation < totalGeneration - 5) {
          if (grid.isAlive(x, y)) {
            double hue = (double) generation / 10;
            double saturation = 1;
            double brightness = (double) generation / totalGeneration;
            Color color = Color.hsb(hue, saturation, brightness);
            pixelWriter.setColor(x, y, color);
          }
        }
      }
    }
  }
}
