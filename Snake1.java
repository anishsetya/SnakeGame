import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.ArrayDeque;
import java.util.Deque;
import javafx.scene.input.KeyEvent;


class SnakeCollisionException extends RuntimeException {
    public SnakeCollisionException(String message) {
        super(message);
    }
}

public class Snake1 extends Application {

    private static final int BLOCK_SIZE = 20;
    private static final int APP_W = 30 * BLOCK_SIZE;
    private static final int APP_H = 20 * BLOCK_SIZE;

    private Direction direction = Direction.RIGHT;
    private boolean moved = false;
    private boolean running = false;

    private final Deque<Rectangle> snake = new ArrayDeque<>();
    private Rectangle food;

    private Pane root;
    
    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        root.setPrefSize(APP_W, APP_H);

        Scene scene = new Scene(root);

        // Initialize the snake
        Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        head.setFill(Color.GREEN);
        snake.add(head);
        root.getChildren().add(head);

        // Initialize food
        food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        food.setFill(Color.RED);
        food.setTranslateX((int) (Math.random() * (APP_W - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int) (Math.random() * (APP_H - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        root.getChildren().add(food);

        scene.setOnKeyPressed(e -> {
            if (moved) {
                switch (e.getCode()) {
                    case UP:
                        if (direction != Direction.DOWN) direction = Direction.UP;
                        break;
                    case DOWN:
                        if (direction != Direction.UP) direction = Direction.DOWN;
                        break;
                    case LEFT:
                        if (direction != Direction.RIGHT) direction = Direction.LEFT;
                        break;
                    case RIGHT:
                        if (direction != Direction.LEFT) direction = Direction.RIGHT;
                        break;
                }
            }
            moved = false;
        });


        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake Game");
        primaryStage.show();

        startGame();
    }

    private void startGame() {
        // Game loop on a separate thread
        running = true;
        new Thread(() -> {
            while (running) {
                Platform.runLater(this::update);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void update() {
        if (running) {
            moved = true;
            switch (direction) {
                case UP:
                    moveSnake(0, -BLOCK_SIZE);
                    break;
                case DOWN:
                    moveSnake(0, BLOCK_SIZE);
                    break;
                case LEFT:
                    moveSnake(-BLOCK_SIZE, 0);
                    break;
                case RIGHT:
                    moveSnake(BLOCK_SIZE, 0);
                    break;
            }

            checkCollision();
        }
    }

    private void moveSnake(int dx, int dy) {
        Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        head.setTranslateX(snake.getFirst().getTranslateX() + dx);
        head.setTranslateY(snake.getFirst().getTranslateY() + dy);

        moved = true;
        try{
        if (head.getTranslateX() < 0 || head.getTranslateY() < 0 || head.getTranslateX() >= APP_W || head.getTranslateY() >= APP_H ||
             snake.stream().anyMatch(b -> b.getTranslateX() == head.getTranslateX() && b.getTranslateY() == head.getTranslateY())) {
                throw new SnakeCollisionException("Snake collided with boundaries or itself!");
        }
        }
        catch(SnakeCollisionException exec){
            gameOver();
            return;
        }
        /*
        if (head.getTranslateX() < 0 || head.getTranslateY() < 0 ||
                head.getTranslateX() >= APP_W || head.getTranslateY() >= APP_H ||
                snake.stream().anyMatch(b -> b.getTranslateX() == head.getTranslateX() && b.getTranslateY() == head.getTranslateY())) {
            gameOver();
            return;
        }
        */
        snake.addFirst(head);
        root.getChildren().add(head);

        if (snake.size() > 1) {
            Rectangle tail = snake.removeLast();
            root.getChildren().remove(tail);
        }
    }

    private void checkCollision() {
        if (snake.getFirst().getTranslateX() == food.getTranslateX() && snake.getFirst().getTranslateY() == food.getTranslateY()) {
            food.setTranslateX((int) (Math.random() * (APP_W - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
            food.setTranslateY((int) (Math.random() * (APP_H - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);

            Rectangle tail = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
            tail.setTranslateX(snake.getLast().getTranslateX());
            tail.setTranslateY(snake.getLast().getTranslateY());

            snake.addLast(tail);
            root.getChildren().add(tail);
            System.out.println("food eaten 1");
        }
    }

    private void gameOver() {
        stopGame();
        System.out.println("Game Over");
        // You can add more game over logic here
    }

    private void stopGame() {
        running = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
