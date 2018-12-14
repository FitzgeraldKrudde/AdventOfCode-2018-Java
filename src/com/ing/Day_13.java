package com.ing;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ing.Direction.*;

enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

@ToString(callSuper = true)
@Getter
@Setter
class Cart extends Point implements Comparable<Cart> {
    Direction currentDirection;
    Direction lastDirectionChosen;

    Cart(int x, int y, Direction direction) {
        this.x = x;
        this.y = y;
        currentDirection = direction;
        lastDirectionChosen = RIGHT;
    }

    @Override
    public int compareTo(Cart o) {
        if (getLocation().getY() == o.getLocation().getY()) {
            return (int) (getLocation().getX() - o.getLocation().getX());
        } else {
            return (int) (getLocation().getY() - o.getLocation().getY());
        }
    }
}

@ToString
@Getter
class TrackSystem {
    public final static char STRAIGHT_VERTICAL = '|';
    public final static char STRAIGHT_HORIZONTAL = '-';
    public final static char CURVE_LEFT = '\\';
    public final static char CURVE_RIGHT = '/';
    public final static char INTERSECTION = '+';

    private static char[][] tracks;
    List<Cart> carts = new ArrayList<>();

    TrackSystem(int x, int y) {
        tracks = new char[x][y];
    }

    public void addElement(int x, int y, char c) {
        switch (c) {
            case ' ':
                tracks[x][y] = ' ';
                break;
            case STRAIGHT_HORIZONTAL:
            case STRAIGHT_VERTICAL:
            case CURVE_LEFT:
            case CURVE_RIGHT:
                tracks[x][y] = c;
                break;
            case '+':
                tracks[x][y] = INTERSECTION;
                break;
            case '>':
                carts.add(new Cart(x, y, RIGHT));
                tracks[x][y] = STRAIGHT_HORIZONTAL;
                break;
            case '<':
                carts.add(new Cart(x, y, LEFT));
                tracks[x][y] = STRAIGHT_HORIZONTAL;
                break;
            case '^':
                carts.add(new Cart(x, y, UP));
                tracks[x][y] = STRAIGHT_VERTICAL;
                break;
            case 'v':
                carts.add(new Cart(x, y, DOWN));
                tracks[x][y] = STRAIGHT_VERTICAL;
                break;
            default:
                throw new IllegalArgumentException("unknown element: " + c);
//                break;
        }
    }

    /**
     * @return Optional with point when collision has occurred
     */
    public Optional<Point> tick(boolean resolveCollision) {
        Optional<Point> point = Optional.empty();
        List<Cart> collidingCarts = new ArrayList<>();

        Collections.sort(carts);
        if (!resolveCollision) {
            for (Cart cart : carts) {
                moveCart(cart);
                collidingCarts.addAll(getCollidingCarts());
                if (!collidingCarts.isEmpty()) {
                    point = Optional.of(collidingCarts.get(0).getLocation());
                    return point;
                }
            }
        } else {
            for (Cart cart : carts) {
                moveCart(cart);
                collidingCarts.addAll(getCollidingCarts());
            }
            if (collidingCarts.size() > 0) {
                carts.removeIf(c -> collidingCarts.contains(c.getLocation()));
            }
        }
        return point;
    }

    private void moveCart(Cart cart) {
        // update location
        updateLocation(cart);
        // update current direction
        updateCurrentDirection(cart);
    }

    private void updateCurrentDirection(Cart cart) {
        char directions = tracks[(int) cart.getX()][(int) cart.getY()];
        switch (directions) {
            case STRAIGHT_VERTICAL:
            case STRAIGHT_HORIZONTAL:
                break;
            case CURVE_LEFT:
                //TODO use switch
                if (cart.getCurrentDirection() == LEFT) {
                    cart.setCurrentDirection(UP);
                } else if (cart.getCurrentDirection() == RIGHT) {
                    cart.setCurrentDirection(DOWN);
                } else if (cart.getCurrentDirection() == DOWN) {
                    cart.setCurrentDirection(RIGHT);
                } else if (cart.getCurrentDirection() == UP) {
                    cart.setCurrentDirection(LEFT);
                } else {
                    throw new IllegalStateException("illegal state for cart: " + cart + " directions: " + directions);
                }
                break;
            case CURVE_RIGHT:
                if (cart.getCurrentDirection() == LEFT) {
                    cart.setCurrentDirection(DOWN);
                } else if (cart.getCurrentDirection() == RIGHT) {
                    cart.setCurrentDirection(UP);
                } else if (cart.getCurrentDirection() == DOWN) {
                    cart.setCurrentDirection(LEFT);
                } else if (cart.getCurrentDirection() == UP) {
                    cart.setCurrentDirection(RIGHT);
                } else {
                    throw new IllegalStateException("illegal state for cart: " + cart + " directions: " + directions);
                }
                break;
            case INTERSECTION:
                switch (cart.getLastDirectionChosen()) {
                    case LEFT:
                        // now straight on
                        cart.setLastDirectionChosen(UP);
                        break;
                    case UP:
                        // now turn right
                        switch (cart.getCurrentDirection()) {
                            case LEFT:
                                cart.setCurrentDirection(UP);
                                break;
                            case RIGHT:
                                cart.setCurrentDirection(DOWN);
                                break;
                            case UP:
                                cart.setCurrentDirection(RIGHT);
                                break;
                            case DOWN:
                                cart.setCurrentDirection(LEFT);
                                break;
                            default:
                                throw new IllegalStateException("illegal state for cart: " + cart + " directions: " + directions);
                        }
                        cart.setLastDirectionChosen(RIGHT);
                        break;
                    case RIGHT:
                        // now turn left
                        switch (cart.getCurrentDirection()) {
                            case LEFT:
                                cart.setCurrentDirection(DOWN);
                                break;
                            case RIGHT:
                                cart.setCurrentDirection(UP);
                                break;
                            case UP:
                                cart.setCurrentDirection(LEFT);
                                break;
                            case DOWN:
                                cart.setCurrentDirection(RIGHT);
                                break;
                            default:
                                throw new IllegalStateException("illegal state for cart: " + cart + " directions: " + directions);
                        }
                        cart.setLastDirectionChosen(LEFT);
                        break;
                    default:
                        throw new IllegalStateException("illegal state for cart: " + cart + " directions: " + directions);
                }
                break;
            default:
                throw new IllegalStateException("illegal state for cart: " + cart + " directions: " + directions);
        }
    }

    private void updateLocation(Cart cart) {
        switch (cart.getCurrentDirection()) {
            case LEFT:
                cart.setLocation(cart.getLocation().getX() - 1, cart.getLocation().getY());
                break;
            case RIGHT:
                cart.setLocation(cart.getLocation().getX() + 1, cart.getLocation().getY());
                break;
            case UP:
                cart.setLocation(cart.getLocation().getX(), cart.getLocation().getY() - 1);
                break;
            case DOWN:
                cart.setLocation(cart.getLocation().getX(), cart.getLocation().getY() + 1);
                break;
            default:
                throw new IllegalArgumentException("invalid move: " + cart.getCurrentDirection() + " for cart: " + cart);
//                break;
        }
    }

    private List<Cart> getCollidingCarts() {
        List collidingCarts = new ArrayList();
        HashSet<Point> cartPoints = new HashSet();
        for (Cart cart : carts) {
            if (!cartPoints.add(cart.getLocation())) {
                collidingCarts.add(cart);
                // find the other colliding cart
                carts.stream()
                        .filter(c -> c.getLocation().equals(cart.getLocation()))
                        .forEach(c -> collidingCarts.add(c));
            }
        }

        return collidingCarts;
    }

    public int getNrCarts() {
        return carts.size();
    }
}

public class Day_13 {

    private static String fileName = "input_13.txt";

    private static TrackSystem trackSystem;

    public static void main(String[] args) throws IOException {
        LocalTime start = LocalTime.now();

        // get the input lines
        List<String> trackLines =
                Files.lines(Paths.get(fileName))
                        .collect(Collectors.toList());

        //determine size
        int maxX = trackLines.stream()
                .max(Comparator.comparingInt(line -> line.length())).get().length();
        int maxY = trackLines.size();

        trackSystem = new TrackSystem(maxX, maxY);

        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < trackLines.get(y).length(); x++) {
                trackSystem.addElement(x, y, trackLines.get(y).charAt(x));
            }
        }

        int tick = 0;
        Optional<Point> collision = Optional.empty();
        while (collision.isEmpty()) {
            collision = trackSystem.tick(false);
            tick++;
        }
        System.out.println("\n#ticks elapsed: " + tick);

        System.out.println("collision: " + collision.get());

        LocalTime finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());

        // part 2

        start = LocalTime.now();

        trackSystem = new TrackSystem(maxX, maxY);
        for (int y = 0; y < maxY; y++) {
            for (int x = 0; x < trackLines.get(y).length(); x++) {
                trackSystem.addElement(x, y, trackLines.get(y).charAt(x));
            }
        }

        tick = 0;
        do {
            trackSystem.tick(true);
            tick++;
        }
        while (trackSystem.getNrCarts() > 1);
        System.out.println("\n#ticks elapsed: " + tick);
        System.out.println("last cart standing: " + trackSystem.getCarts());

        finish = LocalTime.now();
        System.out.println("duration (ms): " + Duration.between(start, finish).toMillis());
    }
}