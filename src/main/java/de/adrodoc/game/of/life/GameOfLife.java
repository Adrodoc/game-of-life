package de.adrodoc.game.of.life;

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
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GameOfLife extends Application {
  public static void main(String[] args) {
    launch(args);
  }

  private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
    private final ThreadFactory delegate = Executors.defaultThreadFactory();

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = delegate.newThread(r);
      thread.setDaemon(true);
      return thread;
    }
  });
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
    primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.F11) {
        primaryStage.setFullScreen(!primaryStage.isFullScreen());
      }
    });
    primaryStage.setFullScreen(true);
    primaryStage.show();

    BlockingQueue<Grid> queue = new ArrayBlockingQueue<>(5);

    // AtomicLong time = new AtomicLong();
    int width = getWidth();
    int height = getHeight();
    // Grid initialGrid = Grids.loadGosperGliderGun(width, height);
    Grid initialGrid = Grids.grid(width, height, 2);
    // Grid initialGrid = Grids.random(width, height);
    AtomicReference<Grid> gridRef = new AtomicReference<>(initialGrid);
    int threadCount = 8;
    CyclicBarrier barrier = new CyclicBarrier(threadCount, () -> {
      Grid newGrid = new Grid(getWidth(), getHeight());
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
        // System.out.println("rendering took " + (System.currentTimeMillis() - start) + "ms");
      }
    });
  }

  private void paintGrid(Grid grid) {
    GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    PixelWriter pixelWriter = gc.getPixelWriter();
    int width = grid.getWidth();
    int height = grid.getHeight();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (grid.isAlive(x, y)) {
          pixelWriter.setColor(x, y, Color.BLACK);
        }
      }
    }
  }
}
