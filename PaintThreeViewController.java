package se.iths.tt.javafxtt.Paint;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;

public class PaintThreeViewController {


    @FXML
    public Canvas canvas;
    public Button undoButton;
    public Button redo;

    @FXML
    public ColorPicker colorpicker;
    @FXML
    public TextField penSize;
    //for manual drawing
    @FXML
    public CheckBox eraser;
    //for manual drawing

    //public TextField sizeField;  //***
    public Slider sizeSlider;

    public TitledPane titledPane;
    public ChoiceBox <ShapeType> choiceBox;
    //choicebox with objects square circle etc

    Stage stage;

    Model model = new Model();
    ObservableList<ShapeType> shapeTypesList=
    //fill it with shapetype values (the enum)
            FXCollections.observableArrayList(ShapeType.values());


    Deque <Command> undoStack = new ArrayDeque<>();          //***
    //Deque implements Command interface with execute method
    Deque <Command> redoStack = new ArrayDeque<>();          //***

    //factory method
    public void canvasClicked(MouseEvent mouseEvent){
       if(mouseEvent.isControlDown()) {
           //if control is pressed last drawn circle turns red
           model.getShapes().stream().reduce((first, second) -> second).ifPresent(shape -> shape.setColor(Color.RED));
           return;
       }
       //creating shapes
      model.createShape(mouseEvent.getX(),mouseEvent.getY());
        }



    private void listChanged(Observable observable){
        var context = canvas.getGraphicsContext2D();              //---
        for (Shape s : model.getShapes()){
            s.draw(context);
    //Ritar ut det grafiska gränssnittet när listan uppdateras dvs när man ritar en shape
    //triggas av add
        }
    }


    public void initialize(){
        colorpicker.valueProperty().bindBidirectional(model.currentColorProperty());
        sizeSlider.valueProperty().bindBidirectional(model.doubleSizeProperty());
        sizeSlider.valueProperty().bindBidirectional(model.doubleHeightProperty());
        sizeSlider.valueProperty().bindBidirectional(model.doubleWidthProperty());

        //titledPane.contentProperty().bindBidirectional(model.currentShapeTypeProperty());
        choiceBox.setItems(shapeTypesList);
        choiceBox.valueProperty().bindBidirectional(model.currentShapeTypeProperty());
        model.getShapes().addListener(this::listChanged);
        //bc the list is an observable list we can add a listener to it that connects to method
        // listChangedMethod above för utritning i det grafiska gränssnittet


        //for manual drawing:
        GraphicsContext graphics = canvas.getGraphicsContext2D();

        //get a graphicscontext to draw on
        canvas.setOnMouseDragged(e ->{
            double size = Double.parseDouble(penSize.getText());

            //get pensize
            double x = e.getX() - size /2;
            double y = e.getY() - size /2;

            //erase:
            if(eraser.isSelected()){
                graphics.clearRect(x,y,size,size);
            }else{
            //draw:
                graphics.setFill(colorpicker.getValue());
                //otherwise draw with selected color
                graphics.fillRect(x,y,size,size);
            }
        });
    }

    public void init(Stage stage){
        this.stage=stage;

    }


    public void onActionExit(){
        Platform.exit();

    }


    public void undoButtonClicked(ActionEvent actionEvent) {
        Command firstUndoToExecute = undoStack.pop();
        firstUndoToExecute.execute();
    }
    
    /*public void redo(ActionEvent actionEvent){
        Command firstRedoToExecute = redoStack.push();
        firstRedoToExecute.execute();
        //behöver kopplas till speciella skapanden av former
        
    }*/







   public void onSaveAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save as");
        //ska stå överst i filväljar fönstret
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        //vad för filändelse ska man ha, till att den ska vara på vår hemkatalog
        fileChooser.getExtensionFilters().clear();
        //vi tar bort allt som finns i den
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
//constructor som tar två parametrar

        File filePath = fileChooser.showSaveDialog(stage);
        //så vill vi köra filechooserobjektet o den returnerar en file det är den filen vi vlt att spara till
        //showsaveDialog vill ha ett window för man måste tala om för den vilket fönster ska vi köras ovanpå
        //medan vi är inne i dialogen och ska välja vilken fil vi ska spara som ska vi inte kunna klicka på
        //de knappar o grejer som finns i fönstret bakom  så den kräver en stage
        //stagen har vi i helloapplication så vi beh få över den så att controllen vet om stagen
        if (filePath != null)
            //om anv trycker på cancel
            model.saveToFile(filePath.toPath());
    }
    //ska va en listview av product
}
