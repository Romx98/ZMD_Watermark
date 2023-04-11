package Core;

import javafx.scene.image.Image;

import java.net.URL;

public class FilePaths {
    // Cesta k výchoziemu obrázku
    public static final String defaultImage = "images/Lenna.png";
    public static final String defaultWatermark = "images/watermarks/watermark.png";

    // Cesta k souboru z rozhraním aplikace
    public static final URL GUIMain = FilePaths.class.getClassLoader().getResource("graphics/" + "mainWindow" + ".fxml");

    // Ikona aplikace
    public static Image favicon = new Image(FilePaths.class.getClassLoader().getResourceAsStream("favicon.png"));
}
