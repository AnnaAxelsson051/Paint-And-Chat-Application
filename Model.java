package se.iths.tt.javafxtt.Paint;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Model {
    //Model Contains info to display to user
    //Remember: Models can be used to feed multiple View objects

    //to select
    List<Node> selectionModel = new ArrayList<>();


    public ObservableList<? extends Shape> getShapes() {
        return shapes;
    }
    //som parameter till observerbaralistan skickar vi ett lambda som implementerar inerfacet extractor with a
    //method observable som tar objektet som vi stoppat in, tala om vilka observerbara fält ska vi få
    //notifieringar från
    ObservableList<Shape> shapes = FXCollections.observableArrayList(param -> new Observable[]{
            param.colorProperty(),
            param.sizeProperty(),
            param.heightProperty(),
            param.widthProperty(),
    });

    //connect to network

    public BooleanProperty connectToNetwork = new SimpleBooleanProperty();

    public BooleanProperty connectToNetworkProperty(){
        return connectToNetwork;
    }

    public boolean getConnectToNetwork() {
        return connectToNetwork.get();
    }

    public void setConnectToNetwork(boolean connectToNetwork) {
        this.connectToNetwork.set(connectToNetwork);
    }

    //shapetype
    public ObjectProperty<ShapeType> currentShapeType = new SimpleObjectProperty<>(ShapeType.CIRCLE);
    public ObjectProperty<ShapeType> currentShapeTypeProperty() {return currentShapeType;}
    public ShapeType getCurrentShapeType() {
        return currentShapeType.get();
    }
    public void setCurrentShapeType(ShapeType currentShapeType) {
        this.currentShapeType.set(currentShapeType);
    }


    //color
    public ObjectProperty<Color> currentColor = new SimpleObjectProperty<>(Color.BLACK);
    public ObjectProperty<Color> currentColorProperty() {
        return currentColor;
    }
    public Color getCurrentColor() {
        return currentColor.get();
    }
    public void setCurrentColor(Color currentColor) {
        this.currentColor.set(currentColor);
    }

    //size slider
    public DoubleProperty doubleSize = new SimpleDoubleProperty();
    public DoubleProperty doubleSizeProperty() {
        return doubleSize;
    }
    public double getDoubleSize() {
        return doubleSize.get();
    }
    public void setDoubleSize(double doubleSize) {
        this.doubleSize.set(doubleSize);
    }

  //height
  public DoubleProperty doubleHeight = new SimpleDoubleProperty();     //varför inget <> ?
    public DoubleProperty doubleHeightProperty() {return doubleHeight;}
    public double getDoubleHeight() {
        return doubleHeight.get();
    }
    public void setDoubleHeight(double doubleHeight) {
        this.doubleHeight.set(doubleHeight);
    }

    //width
    public DoubleProperty doubleWidth = new SimpleDoubleProperty();
    public DoubleProperty doubleWidthProperty() {return doubleWidth;}
    public double getDoubleWidth() {
        return doubleWidth.get();
    }
    public void setDoubleWidth(double doubleWidth) {
        this.doubleWidth.set(doubleWidth);
    }


    //create shapes with mouse coordinates:
    public void createShape(double x, double y) {
        Shape shape = Shape.createShape(getCurrentShapeType(), x, y);
        shape.setColor(getCurrentColor());
        shape.setSize(getDoubleSize());
        addShape(shape);
        //adding shapes to a list in model w addShape method in model
        //the above triggers listChanged method/lyssnaren below som triggar utritning
    }


    public Shape addShape (Shape shape){
        shapes.add(shape);
        CommandPair commandpair = new CommandPair();
        commandpair.undo = () -> shapes.remove(shape);
        commandpair.redo = () -> shapes.add(shape);
        //Skapa ett command o spar det i undo/redostack för undoanvändning
        undoStack.push(commandpair);
        return shape;
    }

    Deque<CommandPair> undoStack = new ArrayDeque<>();
    Deque<CommandPair> redoStack = new ArrayDeque<>();

    public void undo(){
        var commandPair= undoStack.pop();
        commandPair.undo.execute();
        redoStack.push(commandPair);
    }

    public void redo(){
        var commandPair= redoStack.pop();
        commandPair.redo.execute();
        undoStack.push(commandPair);
    }

    interface Command{
        void execute();
    }
    public class CommandPair{

        Command undo;
        Command redo;
    }




    static class ConnectToNetwork {

        ObjectProperty shape = new SimpleObjectProperty();

        ObservableList<Shape> observableList = FXCollections.observableArrayList();
        private Socket socket;
        private final PrintWriter writer;
        private final BufferedReader reader;

        ConnectToNetwork() {
            reader = null;
        }


        public static void connectToNetwork() {

            try {
                socket = new Socket("localhost", 8000);
                OutputStream output = socket.getOutputStream();
                writer = new PrintWriter(output, true);
                InputStream input = socket.getInputStream();
                reader = new BufferedReader(new InputStreamReader(input));

                var thread = new Thread(() -> {
                    try {
                        while (true) {
                            Shape shape = reader.readLine();
                            Platform.runLater(() -> observableList.add(shape));
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
                thread.setDaemon(true);
                thread.start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Shape getShape() {
            return shape.get();
        }

        public ObjectProperty shapeProperty() {
            return shape;
        }

        public void setShape(String message) {
            this.shape.set(shape);
        }

        public ObservableList<Shape> getObservableList() {
            return observableList;
        }

        public void setObservableList(ObservableList<Shape> observableList) {
            this.observableList = observableList;
        }

        public void sendShape() {
            writer.println(getShape());
            //getObservableList().add(getMessage());
            setShape("");
        }
    }


            /*NetworkInterface nif = NetworkInterface.getByName("localhost");
            Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();
            socket = new Socket("localhost", 8000);
            Socket socket = new Socket();
            socket(new InetSocketAddress(nifAddresses.nextElement(), 0));
            socket.connect(new InetSocketAddress(x,y));*/
            //connecting to the loopback interface with the name IO
            //So we retrieve the network interface attached to lo first, retrieve
            // the addresses attached to it, create a socket, bind it to any of the
            // enumerated addresses which we don't even know at compile time and then connect.

            //– public void run( )
            //– Implementera Runnable interfacet och skicka till en ny instans av
            //Thread för att köra din klass som en egen tråd.
            //– Normalt vill vi inte ärva Thread om vi bara behöver overrida run
            //metoden.*/
        }









    //Save shapes to excell
    public void saveToFile(Path file) {
        StringBuffer outPut = new StringBuffer();
        for (Shape p : shapes) {
            if (p instanceof Circle) {
                outPut.append("<circle cx=");
                //<circle cx="25" cy="75" r="20"/>      //TODO Create seperate tread for this?
                outPut.append(p.getX());
                outPut.append(" cy=");
                outPut.append(p.getY());
                outPut.append(" r=");
                outPut.append(p.getSize());
                outPut.append("/>");
            }
            //<rect x="10" y="10" width="30" height="30"/>
            if (p instanceof Rectangle) {
                outPut.append("<rectangle x=");
                outPut.append(p.getX());
                outPut.append(" y=");
                outPut.append(p.getY());
                outPut.append(" width=");
                outPut.append(p.getWidth());
                outPut.append(" height=");
                outPut.append(p.getHeight());
                outPut.append("/>");
            }
            try {
                Files.writeString(file, outPut.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return Objects.equals(selectionModel, model.selectionModel) && Objects.equals(shapes, model.shapes) && Objects.equals(currentShapeType, model.currentShapeType) && Objects.equals(currentColor, model.currentColor) && Objects.equals(doubleSize, model.doubleSize) && Objects.equals(doubleHeight, model.doubleHeight) && Objects.equals(doubleWidth, model.doubleWidth) && Objects.equals(undoStack, model.undoStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectionModel, shapes, currentShapeType, currentColor, doubleSize, doubleHeight, doubleWidth, undoStack);
    }



}


