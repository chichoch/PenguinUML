package controller;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import model.*;
import org.controlsfx.control.Notifications;
import util.commands.CompoundCommand;
import util.commands.MoveGraphElementCommand;
import util.commands.MoveMessageCommand;
import view.*;

import java.awt.geom.Point2D;

/**
 * Created by chalmers on 2016-08-31.
 */
public class SequenceDiagramController extends AbstractDiagramController {

    @FXML
    public void initialize() {
        super.initialize();
        initToolBarActions();
        initDrawPaneActions();
    }

    void initDrawPaneActions(){
        drawPane.setOnMousePressed(event -> {
            if (mode == Mode.NO_MODE) {
                if (event.getButton() == MouseButton.SECONDARY) { //Create context menu on right-click.
                    mode = Mode.CONTEXT_MENU;
                    copyPasteController.copyPasteCoords = new double[]{event.getX(), event.getY()};
                    aContextMenu.show(drawPane, event.getScreenX(), event.getScreenY());
                }
                else if (tool == ToolEnum.SELECT) { //Start selecting elements.
                    selectController.onMousePressed(event);
                }
                else if (tool == ToolEnum.EDGE) {
                    mode = Mode.CREATING;
                    edgeController.onMousePressedOnCanvas(event);
                }
                else if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mouseCreationActivated) { //Start creation of package or class.
                    mode = Mode.CREATING;
                    createNodeController.onMousePressed(event);
                }
                else if (tool == ToolEnum.MOVE_SCENE) { //Start panning of graph.
                    mode = Mode.MOVING;
                    graphController.movePaneStart(event);
                }
                else if (tool == ToolEnum.DRAW && mouseCreationActivated) { //Start drawing.
                    mode = Mode.DRAWING;
                    sketchController.onTouchPressed(event);
                }
            } else if (mode == Mode.CONTEXT_MENU) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    copyPasteController.copyPasteCoords = new double[]{event.getX(), event.getY()};
                    aContextMenu.show(drawPane, event.getScreenX(), event.getScreenY());
                } else {
                    aContextMenu.hide();
                }
            }
            event.consume();
        });

        drawPane.setOnMouseDragged(event -> {
            if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) { //Continue selection of elements.
                selectController.onMouseDragged(event);
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && mouseCreationActivated) { //Continue drawing.
                sketchController.onTouchMoved(event);
            } else if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mode == Mode.CREATING && mouseCreationActivated) { //Continue creation of class or package.
                createNodeController.onMouseDragged(event);
            } else if (tool == ToolEnum.MOVE_SCENE && mode == Mode.MOVING) { //Continue panning of graph.
                graphController.movePane(event);
            } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { //Continue creating edge.
                edgeController.onMouseDragged(event);
            } else if ((tool == ToolEnum.EDGE || tool == ToolEnum.SELECT) && mode == Mode.DRAGGING_EDGE){
                edgeController.onMouseDragEdge(event);
            }
            event.consume();
        });

        drawPane.setOnMouseReleased(event -> {
            if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) { //Finish selecting elements.
                selectController.onMouseReleased();
            }
            else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && mouseCreationActivated) { //Finish drawing.
                sketchController.onTouchReleased(event);
                //We only want to move out of drawing mode if there are no other current drawings.
                if (!sketchController.currentlyDrawing()) {
                    mode = Mode.NO_MODE;
                }
            } else if (tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING && mouseCreationActivated) { //Finish creation of class.
                createNodeController.onMouseReleasedLifeline();
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }

            } else if (tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING && mouseCreationActivated) { //Finish creation of package.
                createNodeController.onMouseReleasedPackage();
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { //Finish panning of graph.
                graphController.movePaneFinished();
                mode = Mode.NO_MODE;
            } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { //Finish creation of edge.
                edgeController.onMouseReleasedSequence();
                mode = Mode.NO_MODE;
            } else if ((tool == ToolEnum.EDGE || tool == ToolEnum.SELECT) && mode == Mode.DRAGGING_EDGE){
                edgeController.onMouseReleaseDragEdge(event);
            }
        });

        //------------------------- Touch ---------------------------------
        //There are specific events for touch when creating and drawing to utilize multitouch.
        // TODO edge creation multi-user support.
        drawPane.setOnTouchPressed(event -> {
            if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && !mouseCreationActivated) {
                mode = Mode.CREATING;
                createNodeController.onTouchPressed(event);
            } else if (tool == ToolEnum.DRAW && !mouseCreationActivated) {
                mode = Mode.DRAWING;
                sketchController.onTouchPressed(event);
            }
        });

        drawPane.setOnTouchMoved(event -> {
            if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mode == Mode.CREATING && !mouseCreationActivated) {
                createNodeController.onTouchDragged(event);
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && !mouseCreationActivated) {
                sketchController.onTouchMoved(event);
            }
            event.consume();
        });

        drawPane.setOnTouchReleased(event -> {
            if (tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING && !mouseCreationActivated) {
                createNodeController.onTouchReleasedClass(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }

            } else if (tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING && !mouseCreationActivated) {
                createNodeController.onTouchReleasedPackage(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING && !mouseCreationActivated) {
                sketchController.onTouchReleased(event);
                if (!sketchController.currentlyDrawing()) {
                    mode = Mode.NO_MODE;
                }
            }
            event.consume();
        });
    }

    boolean wasAlreadySelected = false;

    void initNodeActions(AbstractNodeView nodeView) {

        nodeView.setOnMousePressed(event -> {
            if (event.getClickCount() == 2) { //Open dialog window on double click.
                nodeController.onDoubleClick(nodeView);
                tool = ToolEnum.SELECT;
                setButtonClicked(selectBtn);
            } else if (tool == ToolEnum.MOVE_SCENE) { //Start panning of graph.
                mode = Mode.MOVING;
                graphController.movePaneStart(event);
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY) { //Open context menu on left click.
                copyPasteController.copyPasteCoords = new double[]{nodeView.getX() + event.getX(), nodeView.getY() + event.getY()};
                aContextMenu.show(nodeView, event.getScreenX(), event.getScreenY());
            } else if (tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) { //Select node
                setTool(ToolEnum.SELECT);
                setButtonClicked(selectBtn);
                if (!(nodeView instanceof PackageNodeView)) {
                    nodeView.toFront();
                }
                if (mode == Mode.NO_MODE) { //Either drag selected elements or resize node.
                    Point2D.Double eventPoint = new Point2D.Double(event.getX(), event.getY());
                    if (eventPoint.distance(new Point2D.Double(nodeView.getWidth(), nodeView.getHeight())) < 20) {  //Resize if event is close to corner of node
                        mode = Mode.RESIZING;
                        nodeController.resizeStart(nodeView);
                    } else {
                        mode = Mode.DRAGGING;
                        if (!selectedNodes.contains(nodeView)) { //Drag
                            wasAlreadySelected = false;
                            selectedNodes.add(nodeView);
                        } else {
                            wasAlreadySelected = true;
                        }
                        drawSelected();
                        nodeController.moveNodesStart(event);
                        sketchController.moveSketchStart(event);
                    }
                }
            } else if (tool == ToolEnum.EDGE) { //Start edge creation.
                mode = Mode.CREATING;
                edgeController.onMousePressedOnNode(event);
            }
            event.consume();
        });

        nodeView.setOnMouseDragged(event -> {
            if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.DRAGGING) { //Continue dragging selected elements
                nodeController.moveNodes(event);
                sketchController.moveSketches(event);
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { //Continue panning graph.
                graphController.movePane(event);
            } else if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.RESIZING) { //Continue resizing node.
                nodeController.resize(event);
            } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { //Continue creating edge.
                edgeController.onMouseDragged(event);
            }
            event.consume();

        });

        nodeView.setOnMouseReleased(event -> {
            if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.DRAGGING) { //Finish dragging nodes and create a compound command.
                double[] deltaTranslateVector = nodeController.moveNodesFinished(event);
                sketchController.moveSketchFinished(event);
                if(deltaTranslateVector[0] != 0 || deltaTranslateVector[1] != 0){ //If it was actually moved
                    CompoundCommand compoundCommand = new CompoundCommand();
                    for (AbstractNodeView movedView : selectedNodes) {
                        compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    for (Sketch sketch : selectedSketches) {
                        compoundCommand.add(new MoveGraphElementCommand(sketch, deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    undoManager.add(compoundCommand);
                } else {
                    if(wasAlreadySelected){
                        selectedNodes.remove(nodeView);
                    }
                    drawSelected();
                }
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { //Finish panning of graph.
                graphController.movePaneFinished();
                mode = Mode.NO_MODE;
            } else if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.RESIZING) { //Finish resizing node.
                nodeController.resizeFinished(nodeMap.get(nodeView));
            } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { //Finish creation of edge.
                edgeController.onMouseReleasedSequence();
            }
            mode = Mode.NO_MODE;
            event.consume();
        });

        ////////////////////////////////////////////////////////////////

        nodeView.setOnTouchPressed(event -> {
            if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE)) {
                mode = Mode.CREATING;
                createNodeController.onTouchPressed(event);
            } else if (tool == ToolEnum.DRAW) {
                mode = Mode.DRAWING;
                sketchController.onTouchPressed(event);
            }
            event.consume();
        });

        nodeView.setOnTouchMoved(event -> {
            if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) &&
                    mode == Mode.CREATING) {
                createNodeController.onTouchDragged(event);
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                sketchController.onTouchMoved(event);
            }
            event.consume();

        });

        nodeView.setOnTouchReleased(event -> {
            if (nodeView instanceof PackageNodeView && tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING) {
                createNodeController.onTouchReleasedClass(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }

            } else if (nodeView instanceof PackageNodeView && tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING) {
                createNodeController.onTouchReleasedPackage(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                sketchController.onTouchReleased(event);
                if (!sketchController.currentlyDrawing()) {
                    mode = Mode.NO_MODE;
                }
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                mode = Mode.NO_MODE;
            }
            event.consume();
        });
    }


    private double previousMoveX, previousMoveY, initMoveX, initMoveY;
    public void initMessageHandleActions(MessageEdgeView edgeView){
        Circle circleHandle = edgeView.getCircleHandle();
        circleHandle.setOnMousePressed(event -> {
            if(tool == ToolEnum.SELECT){
                mode = Mode.DRAGGING;
                selectedEdges.add(edgeView);
                previousMoveX = event.getX();
                previousMoveY = event.getY();
                initMoveX = event.getX();
                initMoveY = event.getY();
            }
        });

        circleHandle.setOnMouseDragged(event -> {
            double offsetX =  (event.getX() - previousMoveX)*(1/drawPane.getScaleX());
            double offsetY = (event.getY() - previousMoveY)*(1/drawPane.getScaleY());
            previousMoveX = event.getX();
            previousMoveY = event.getY();
            if(mode == Mode.DRAGGING){
                MessageEdge edge = (MessageEdge)edgeView.getRefEdge();
                edge.setStartX(edge.getStartX() + offsetX);
                edge.setStartY(edge.getStartY() + offsetY);
            }
        });

        circleHandle.setOnMouseReleased(event -> {
            undoManager.add(new MoveMessageCommand((MessageEdge)edgeView.getRefEdge(), edgeView.getStartX() - initMoveX, edgeView.getStartY() - initMoveY));
            mode = Mode.NO_MODE;
            previousMoveX = 0;
            previousMoveY = 0;
        });
    }

    public void initLifelineHandleActions(LifelineView nodeView){
        Rectangle rectangleHandle = nodeView.getLifelineHandle();
        rectangleHandle.setOnMousePressed(event -> {
            if(tool == ToolEnum.SELECT){
                mode = Mode.DRAGGING;
                previousMoveY = event.getSceneY();
            }
        });

        rectangleHandle.setOnMouseDragged(event -> {
            double offsetY = (event.getSceneY() - previousMoveY)*(1/drawPane.getScaleY());
            previousMoveX = event.getSceneX();
            previousMoveY = event.getSceneY();
            if(mode == Mode.DRAGGING){
                Lifeline node = (Lifeline) nodeView.getRefNode();
                node.setLifelineLength(node.getLifelineLength() + offsetY);
            }
        });

        rectangleHandle.setOnMouseReleased(event -> {
            mode = Mode.NO_MODE;
            previousMoveX = 0;
            previousMoveY = 0;
        });
    }

    /**
     * Creates and adds a new EdgeView
     *
     * @param edge
     * @param startNodeView
     * @param endNodeView
     * @return
     */
    public AbstractEdgeView createEdgeView(MessageEdge edge, AbstractNodeView startNodeView,
                                           AbstractNodeView endNodeView) {
        MessageEdgeView edgeView;
        if (startNodeView != null) {
            edgeView = new MessageEdgeView(edge, edge.getStartX(), edge.getStartY(), startNodeView, endNodeView);
        } else {
            edgeView = new MessageEdgeView(edge, edge.getStartX(), edge.getStartY(), endNodeView);
            initMessageHandleActions(edgeView);
        }
        return addEdgeView(edgeView);
    }

    //------------ Init Buttons -------------------------------------------
    private void initToolBarActions() {

        Image image = new Image("/icons/seqObject.png");
        createBtn.setGraphic(new ImageView(image));
        createBtn.setText("");

        image = new Image("/icons/message.png");
        edgeBtn.setGraphic(new ImageView(image));
        edgeBtn.setText("");

        image = new Image("/icons/selectw.png");
        selectBtn.setGraphic(new ImageView(image));
        selectBtn.setText("");

        image = new Image("/icons/undow.png");
        undoBtn.setGraphic(new ImageView(image));
        undoBtn.setText("");

        image = new Image("/icons/redow.png");
        redoBtn.setGraphic(new ImageView(image));
        redoBtn.setText("");

        image = new Image("/icons/movew.png");
        moveBtn.setGraphic(new ImageView(image));
        moveBtn.setText("");

        image = new Image("/icons/deletew.png");
        deleteBtn.setGraphic(new ImageView(image));
        deleteBtn.setText("");

        image = new Image("/icons/draww.png");
        drawBtn.setGraphic(new ImageView(image));
        drawBtn.setText("");

        image = new Image("/icons/recow.png");
        recognizeBtn.setGraphic(new ImageView(image));
        recognizeBtn.setText("");

        image = new Image("/icons/micw.png");
        voiceBtn.setGraphic(new ImageView(image));
        voiceBtn.setText("");

        buttonInUse = createBtn;
        buttonInUse.getStyleClass().add("button-in-use");


        //---------------------- Actions for buttons ----------------------------
        createBtn.setOnAction(event -> {
            tool = ToolEnum.CREATE_CLASS;
            setButtonClicked(createBtn);
        });

        edgeBtn.setOnAction(event -> {
            tool = ToolEnum.EDGE;
            setButtonClicked(edgeBtn);
        });

        selectBtn.setOnAction(event -> {
            tool = ToolEnum.SELECT;
            setButtonClicked(selectBtn);
        });

        drawBtn.setOnAction(event -> {
            tool = ToolEnum.DRAW;
            setButtonClicked(drawBtn);
        });

        moveBtn.setOnAction(event -> {
            setButtonClicked(moveBtn);
            tool = ToolEnum.MOVE_SCENE;
        });

        undoBtn.setOnAction(event -> undoManager.undoCommand());

        redoBtn.setOnAction(event -> undoManager.redoCommand());

        deleteBtn.setOnAction(event -> deleteSelected());

        recognizeBtn.setOnAction(event -> recognizeController.recognize(selectedSketches));

        voiceBtn.setOnAction(event -> {
            if(voiceController.voiceEnabled){
                Notifications.create()
                        .title("Voice disabled")
                        .text("Voice commands are now disabled.")
                        .showInformation();
            } else {
                Notifications.create()
                        .title("Voice enabled")
                        .text("Voice commands are now enabled.")
                        .showInformation();
            }
            voiceController.onVoiceButtonClick();
        });
    }
}
