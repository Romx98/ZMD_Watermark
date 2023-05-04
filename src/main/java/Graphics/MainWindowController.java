package Graphics;

import Core.FilePaths;
import Enums.ComponentType;
import Jpeg.Process;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class MainWindowController implements Initializable {

    @FXML
    private Button buttonExtractLSB;

    @FXML
    private CheckBox multipleWatermark;

    @FXML
    private Slider bitLevel, deepLevel;

    @FXML
    private TextField bitLevelField, deepLevelField, messageWatermarkField, extractedMessageWatermarkField;

    @FXML
    private ComboBox<ComponentType> componentTypeLSB, componentTypeDCT;

    @FXML
    private Spinner coefficient1X, coefficient1Y, coefficient2X, coefficient2Y;

    private Process process;
    private String chooseImage, chooseWatermark;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        componentTypeLSB.getItems().setAll(ComponentType.values());
        componentTypeDCT.getItems().setAll(ComponentType.values());


        // Set default values
        chooseImage = FilePaths.defaultImage;
        chooseWatermark = FilePaths.defaultWatermark;
        componentTypeLSB.getSelectionModel().select(ComponentType.Y);
        componentTypeDCT.getSelectionModel().select(ComponentType.Y);
        bitLevel.setValue(3);
        deepLevel.setValue(50);
        buttonExtractLSB.setDisable(true);

        // Set fields
        bitLevelField.setTextFormatter(new TextFormatter<>(filterNumber("[1-9]")));
        bitLevelField.textProperty().bindBidirectional(bitLevel.valueProperty(), getNumberFormat());
        deepLevelField.setTextFormatter(new TextFormatter<>(filterNumber("[0-9]*")));
        deepLevelField.textProperty().bindBidirectional(deepLevel.valueProperty(), getNumberFormat());
        messageWatermarkField.setText("11010010001");
        messageWatermarkField.setTextFormatter(new TextFormatter<>(filterNumber("[0-1]*")));

        // Set spinner
        setSpinner(coefficient1X, 3);
        setSpinner(coefficient1Y, 1);
        setSpinner(coefficient2X, 4);
        setSpinner(coefficient2Y, 2);


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
        Dialogs.showImageInWindow(process.getImageWithLSBWatermark(componentTypeLSB.getSelectionModel().getSelectedItem(),
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
     * Watermarks the image using the 2-DCT method
     */
    public void MarkDCTWatermark() {
        BufferedImage image = process.getImageWithDCTWatermark(
                componentTypeDCT.getSelectionModel().getSelectedItem(), messageWatermarkField.getText(),
                (int) deepLevel.getValue(), (int) coefficient1X.getValue(), (int) coefficient1Y.getValue(),
                (int) coefficient2X.getValue(), (int) coefficient2Y.getValue());

        Dialogs.showImageInWindow(image, "Watermark Image - DCT");
    }

    /**
     * Show mark image with 2-DCT method
     */
    public void ExtractDCTWatermark() {
        String message = process.getImageWithDCTWatermark(
                (int) deepLevel.getValue(), (int) coefficient1X.getValue(), (int) coefficient1Y.getValue(),
                (int) coefficient2X.getValue(), (int) coefficient2Y.getValue(), messageWatermarkField.getLength());

        extractedMessageWatermarkField.setText(message);
        
        if (message.equals(messageWatermarkField.getText())) {
            extractedMessageWatermarkField.setStyle("-fx-text-fill: green;");
        } else {
            extractedMessageWatermarkField.setStyle("-fx-text-fill: red;");
        }

    }

    /**
     * Set Spinner factory
     *
     * @param spinner - instance to spinner
     */
    private void setSpinner(Spinner<Integer> spinner, int initialValue) {
        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 7, initialValue) {
            @Override
            public void decrement(int steps) {
                int newValue = getValue() - steps;
                if (newValue >= 0) {
                    setValue(newValue);
                }
            }

            @Override
            public void increment(int steps) {
                int newValue = getValue() + steps;
                if (newValue <= getMax()) {
                    setValue(newValue);
                }
            }
        };

        spinner.setValueFactory(valueFactory);
    }

    /**
     * Get max digit format for number field
     *
     * @return NumberFormat
     */
    private NumberFormat getNumberFormat() {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(1);

        return nf;
    }

    /**
     * Filter text or number into Fielt element
     *
     * @param regexPattern - Regex pattern text
     * @return Return change or null
     */
    private static UnaryOperator<TextFormatter.Change> filterNumber(String regexPattern) {
        return change -> {
            String text = change.getControlNewText();
            if (text.matches(regexPattern)) return change;
            return null;
        };
    }
}
