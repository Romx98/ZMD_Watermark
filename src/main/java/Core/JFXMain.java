package Core;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JFXMain extends Application {
    private static Stage primaryStage;
    public static Scene mainScene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader fl = new FXMLLoader(FilePaths.GUIMain); //Načtení okna z fxml souboru
        Parent root = fl.load();

        mainScene = new Scene(root); //Vytvoření scény z fxml souboru
        primaryStage.setScene(mainScene); //Vložení scény do okna

        primaryStage.setTitle("Watermark: Klampár 221551"); //Titulek okna, nastavte svoje
        primaryStage.getIcons().add(FilePaths.favicon); //Přidání ikony aplikace
        primaryStage.show(); //Zobrazí rozhraní

        //Není nutné, umožňuje provést kód po stisku X (př. potvrzení ukončení)
        primaryStage.setOnCloseRequest((e) -> {
            Platform.exit();
            System.exit(0);
        });
    }
}
