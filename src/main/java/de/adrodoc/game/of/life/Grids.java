package de.adrodoc.game.of.life;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

public class Grids {
  public static Grid random(int width, int height) {
    Random random = new Random();
    return create(width, height, (x, y) -> random.nextBoolean());
  }

  public static Grid striped(int width, int height, int stride) {
    return create(width, height, (x, y) -> y % stride == 0);
  }

  public static Grid grid(int width, int height, int stride) {
    return create(width, height, (x, y) -> x % stride == 0 ^ y % stride == 0);
  }

  public static Grid create(int width, int height, BiPredicate<Integer, Integer> predicate) {
    Grid grid = new Grid(width, height);
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        if (predicate.test(x, y)) {
          grid.setAlive(x, y, true);
        }
      }
    }
    return grid;
  }

  public static Grid loadGosperGliderGun(int width, int height) throws IOException {
    URL resource = Grids.class.getResource("gosper-glider-gun.txt");
    return loadResource(resource, width, height);
  }

  public static Grid loadResource(URL resource, int width, int height) throws IOException {
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
      Grid grid = new Grid(width, height);
      AtomicInteger y = new AtomicInteger();
      reader.lines().forEachOrdered(line -> {
        AtomicInteger x = new AtomicInteger();
        line.chars().forEachOrdered(c -> {
          if (c != ' ') {
            grid.setAlive(x.get(), y.get(), true);
          }
          x.getAndIncrement();
        });
        y.getAndIncrement();
      });
      return grid;
    }
  }
}
