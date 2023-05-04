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
    private Button buttonExtractLSB, buttonExtractDCT, buttonAttack45, buttonAttack90, buttonExtractRotation;

    @FXML
    private CheckBox multipleWatermark;

    @FXML
    private Slider bitLevel, deepLevel;

    @FXML
    private TextField bitLevelField, deepLevelField, messageWatermarkField, extractedMessageWatermarkField;

    @FXML
    private ComboBox<ComponentType> componentTypeLSB, componentTypeDCT, watermarkType;

    @FXML
    private Spinner coefficient1X, coefficient1Y, coefficient2X, coefficient2Y;

    private Process process_png, process_jpg;
    private String choosePNG, chooseJPG, chooseWatermark;
    private int selectRotation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        componentTypeLSB.getItems().setAll(ComponentType.values());
        componentTypeDCT.getItems().setAll(ComponentType.values());
//        watermarkType.getItems().setAll(WatermarkType.values());


        // Set default values
        choosePNG = FilePaths.defaultImagePNG;
        chooseJPG = FilePaths.defaultImageJPG;
        chooseWatermark = FilePaths.defaultWatermark;
        componentTypeLSB.getSelectionModel().select(ComponentType.Y);
        componentTypeDCT.getSelectionModel().select(ComponentType.Y);
//        watermarkType.getSelectionModel().select(WatermarkType.DCT.ordinal());
        bitLevel.setValue(3);
        deepLevel.setValue(50);

        // Set button
        buttonExtractLSB.setDisable(true);
        buttonExtractDCT.setDisable(true);
        buttonAttack45.setDisable(true);
        buttonAttack90.setDisable(true);
        buttonExtractRotation.setDisable(true);

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


        process_png = new Process(FilePaths.defaultImagePNG, FilePaths.defaultWatermark);
        process_jpg = new Process(FilePaths.defaultImageJPG);
    }

    public void ChangePNG() {
        final File file = Dialogs.openFile();

        if (file != null) choosePNG = file.getAbsolutePath();
        process_png = new Process(choosePNG, chooseWatermark);

        Reset();
    }

    public void ChangeJPG() {
        final File file = Dialogs.openFile();

        if (file != null) chooseJPG = file.getAbsolutePath();
        process_jpg = new Process(chooseJPG);

        Reset();
    }

    public void ChangeWatermark() {
        final File file = Dialogs.openFile();

        if (file != null) chooseWatermark = file.getAbsolutePath();
        process_png = new Process(choosePNG, chooseWatermark);

        Reset();
    }

    public void Close() {
    }

    public void Reset() {
    }

    public void ShowImageOriginal() {
        Dialogs.showImageInWindow(process_png.getOriginalImage(), "Original PNG", true);
        Dialogs.showImageInWindow(process_jpg.getOriginalImage(), "Original JPG", true);
    }

    public void ShowWatermarkOriginal() {
        Dialogs.showImageInWindow(process_png.getOriginalWatermark(), "Original Watermark", true);
    }

    /**
     *  Display of COLOR components - YCbCr -- Image and Watermark
     */
    public void ShowYOriginal() {
        Dialogs.showImageInWindow(process_png.getOneColorImageFromYCbCr(process_png.getOriginalImageY()),
                "Original PNG Y");
        Dialogs.showImageInWindow(process_jpg.getOneColorImageFromYCbCr(process_jpg.getOriginalImageY()),
                "Original JPG Y");
        Dialogs.showImageInWindow(process_png.getOneColorImageFromYCbCr(process_png.getOriginalWatermarkY()),
                "Original Watermark Y");
    }

    public void ShowCbOriginal() {
        Dialogs.showImageInWindow(process_png.getOneColorImageFromYCbCr(process_png.getOriginalImageCb()),
                "Original PNG Cb");
        Dialogs.showImageInWindow(process_jpg.getOneColorImageFromYCbCr(process_jpg.getOriginalImageCb()),
                "Original JPG Cb");
        Dialogs.showImageInWindow(process_png.getOneColorImageFromYCbCr(process_png.getOriginalWatermarkCb()),
                "Original Watermark Cb");
    }

    public void ShowCrOriginal() {
        Dialogs.showImageInWindow(process_png.getOneColorImageFromYCbCr(process_png.getOriginalImageCr()),
                "Original PNG Cr");
        Dialogs.showImageInWindow(process_jpg.getOneColorImageFromYCbCr(process_jpg.getOriginalImageCr()),
                "Original JPG Cr");
        Dialogs.showImageInWindow(process_png.getOneColorImageFromYCbCr(process_png.getOriginalWatermarkCr()),
                "Original Watermark Cr");
    }
    // end

    /**
     * Watermarks the image using the LSB method
     */
    public void MarkLSBWatermark() {
        buttonExtractLSB.setDisable(false);
        Dialogs.showImageInWindow(process_png.getImageWithLSBWatermark(componentTypeLSB.getSelectionModel().getSelectedItem(),
                (int) bitLevel.getValue(), multipleWatermark.isSelected()), "Watermark PNG - LSB");
    }

    /**
     * Show mark image with LSB method
     */
    public void ExtractLSBWatermark() {
        Dialogs.showImageInWindow(process_png.getExtractImageWithLSBWatermark((int) bitLevel.getValue()),
                "Extracted PNG - LSB");
    }

    /**
     * Watermarks the image using the 2-DCT method
     */
    public void MarkDCTWatermark() {
        BufferedImage image = process_jpg.getImageWithDCTWatermark(
                componentTypeDCT.getSelectionModel().getSelectedItem(), messageWatermarkField.getText(),
                (int) deepLevel.getValue(), (int) coefficient1X.getValue(), (int) coefficient1Y.getValue(),
                (int) coefficient2X.getValue(), (int) coefficient2Y.getValue());

        buttonExtractDCT.setDisable(false);
        buttonAttack45.setDisable(false);
        buttonAttack90.setDisable(false);

        Dialogs.showImageInWindow(image, "Watermark JPG - DCT");
    }

    /**
     * Show mark image with 2-DCT method
     */
    public void ExtractDCTWatermark() {
        String message = process_jpg.getImageWithDCTWatermark(
                (int) coefficient1X.getValue(), (int) coefficient1Y.getValue(),
                (int) coefficient2X.getValue(), (int) coefficient2Y.getValue(), messageWatermarkField.getLength());

        extractedMessageWatermarkField.setText(message);
        
        if (message.equals(messageWatermarkField.getText())) {
            extractedMessageWatermarkField.setStyle("-fx-text-fill: green;");
        } else {
            extractedMessageWatermarkField.setStyle("-fx-text-fill: red;");
        }
    }

    public void attackRotation45() {
        selectRotation = -45;
        buttonExtractRotation.setDisable(false);
        Dialogs.showImageInWindow(process_jpg.getAttackRotation(45), "Rotation 45 JPG");
    }

    public void attackRotation90() {
        selectRotation = -90;
        buttonExtractRotation.setDisable(false);
        Dialogs.showImageInWindow(process_jpg.getAttackRotation(90), "Rotation 90 JPG");
    }

    public void exctractAttackRotation() {
        String message = process_jpg.getExtractAttackRotation(componentTypeDCT.getSelectionModel().getSelectedItem(),
                selectRotation, (int) coefficient1X.getValue(), (int) coefficient1Y.getValue(),
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
