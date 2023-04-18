package Graphics;

import Core.FilePaths;
import Enums.ComponentType;
import Jpeg.Process;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class MainWindowController implements Initializable {

    @FXML
    private Button buttonMarkLSB, buttonExtractLSB;

    @FXML
    private CheckBox multipleWatermark;

    @FXML
    private Slider bitLevel;

    @FXML
    private TextField bitLevelField;

    @FXML
    private ComboBox<ComponentType> componentType;

    private Process process;
    private String chooseImage, chooseWatermark;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        NumberFormat nfL = getNumberFormat(1);

        componentType.getItems().setAll(ComponentType.values());

        // Set default values
        chooseImage = FilePaths.defaultImage;
        chooseWatermark = FilePaths.defaultWatermark;
        componentType.getSelectionModel().select(ComponentType.Y);
        bitLevel.setValue(3);
        buttonExtractLSB.setDisable(true);

        // Set fields
        bitLevelField.setTextFormatter(new TextFormatter<>(filterLevelNumber));
        bitLevelField.textProperty().bindBidirectional(bitLevel.valueProperty(), nfL);

        process = new Process(FilePaths.defaultImage, FilePaths.defaultWatermark);
    }

    public void ChangeImage() {
        final File file = Dialogs.openFile();

        if (file != null) chooseImage = file.getAbsolutePath();
        process = new Process(chooseImage, chooseWatermark);

        Reset();
    }

    public void ChangeWatermark() {
        final File file = Dialogs.openFile();

        if (file != null) chooseWatermark = file.getAbsolutePath();
        process = new Process(chooseImage, chooseWatermark);

        Reset();
    }

    public void Close() {
    }

    public void Reset() {
    }

    public void ShowImageOriginal() {
        Dialogs.showImageInWindow(process.getOriginalImage(), "Original Image", true);
    }

    public void ShowWatermarkOriginal() {
        Dialogs.showImageInWindow(process.getOriginalWatermark(), "Original Watermark", true);
    }

    /**
     *  Display of COLOR components - YCbCr -- Image and Watermark
     */
    public void ShowYOriginal() {
        Dialogs.showImageInWindow(process.getOneColorImageFromYCbCr(process.getOriginalImageY()),
                "Original Image Y");
        Dialogs.showImageInWindow(process.getOneColorImageFromYCbCr(process.getOriginalWatermarkY()),
                "Original Watermark Y");
    }

    public void ShowCbOriginal() {
        Dialogs.showImageInWindow(process.getOneColorImageFromYCbCr(process.getOriginalImageCb()),
                "Original Image Cb");
        Dialogs.showImageInWindow(process.getOneColorImageFromYCbCr(process.getOriginalWatermarkCb()),
                "Original Watermark Cb");
    }

    public void ShowCrOriginal() {
        Dialogs.showImageInWindow(process.getOneColorImageFromYCbCr(process.getOriginalImageCr()),
                "Original Image Cr");
        Dialogs.showImageInWindow(process.getOneColorImageFromYCbCr(process.getOriginalWatermarkCr()),
                "Original Watermark Cr");
    }
    // end

    /**
     * Watermarks the image using the LSB method
     */
    public void MarkLSBWatermark() {
        buttonExtractLSB.setDisable(false);
        Dialogs.showImageInWindow(process.getImageWithLSBWatermark(componentType.getSelectionModel().getSelectedItem(),
                (int) bitLevel.getValue(), multipleWatermark.isSelected()), "Watermark Image - LSB");
    }

    /**
     * Show mark image with LSB method
     */
    public void ExtractLSBWatermark() {
        Dialogs.showImageInWindow(process.getExtractImageWithLSBWatermark((int) bitLevel.getValue()),
                "Extracted watermark - LSB");
    }

    /**
     * Get max digit format for number field
     * @param digit - max digit number
     * @return NumberFormat
     */
    private NumberFormat getNumberFormat(int digit) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(digit);

        return nf;
    }

    private static final UnaryOperator<TextFormatter.Change> filterLevelNumber = change -> {
        String text = change.getControlNewText();
        if (text.matches("[1-8]")) return change;
        return null;
    };
}
