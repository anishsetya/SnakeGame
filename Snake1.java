import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.ArrayDeque;
import java.util.Deque;
import javafx.scene.input.KeyEvent;
import mypack.HS;


class SnakeCollisionException extends RuntimeException {
    public SnakeCollisionException(String message) {
        super(message);
    }
}

public class Snake1 extends Application {
    HS h=new HS();
    private int score=0;
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
                    Thread.sleep(200-score*10);
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
            score++;
        }
    }

    private void gameOver() {
        if(score>h.hs){
            h.set(score);
        }
        stopGame();
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("Game Over!\nScore: " + score + "\n High Score: "+h.hs);

        // Add a custom button to the alert for further actions
        ButtonType restartButton = new ButtonType("Restart");
        alert.getButtonTypes().setAll(restartButton, ButtonType.OK);

        // Show the alert and wait for user action
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType == restartButton) {
                // Handle restart logic here
                restartGame();
            }
        });
    }
    private void restartGame() {
        score=0;
        snake.clear(); // Clear the snake
        root.getChildren().clear(); // Clear all nodes from the scene
        initializeSnake(); // Reinitialize the snake
        initializeFood(); // Reinitialize the food
        startGame(); // Start the game again
    }

    private void initializeSnake() {
        Rectangle head = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        head.setFill(Color.GREEN);
        snake.add(head);
        root.getChildren().add(head);
        direction = Direction.RIGHT;
        Platform.runLater(this::update);
    }

    private void initializeFood() {
        food = new Rectangle(BLOCK_SIZE, BLOCK_SIZE);
        food.setFill(Color.RED);
        food.setTranslateX((int) (Math.random() * (APP_W - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        food.setTranslateY((int) (Math.random() * (APP_H - BLOCK_SIZE)) / BLOCK_SIZE * BLOCK_SIZE);
        root.getChildren().add(food);
    }

    private void stopGame() {
        running = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
