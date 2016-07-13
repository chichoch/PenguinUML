package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import model.PackageNode;
import model.PictureNode;
import util.Constants;


/**
 * Created by anasm on 2016-06-27.
 */
public class PictureNodeView extends AbstractNodeView {

    private double x;
    private double y;
    private double width;
    private double height;
    private ImageView imageView;
    private boolean selected = false;

    public PictureNodeView(ImageView v, PictureNode picnode) {
        super(picnode);
        imageView = v;
        imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
        getChildren().add(v);
        setChangeListeners();
    }

    @Override
    public void setFill(Paint p) {

    }

    public void setX(double x){
        this.x = x;
        setTranslateX(x);
    }

    public void setY(double y) {
        this.y = y;
        setTranslateY(y);
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public Bounds getBounds() {
        return imageView.getBoundsInParent();
    }

    public void changeHeight(Double newHeight){
        imageView.setFitHeight(newHeight);
        //imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        setHeight(imageView.getFitHeight());
    }

    public void changeWidth(Double newWidth){
        imageView.setFitWidth(newWidth);
        //imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.setCache(true);
        setWidth(imageView.getFitWidth());
    }


    public void setSelected(boolean selected) {
        if(selected){
            imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(255,109,112,0.8), 10, 0, 0, 0);");
        } else {
            imageView.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        }
        this.selected = selected;
    }

    private void setChangeListeners() {
        PictureNode refNode = (PictureNode) getRefNode();
        refNode.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                changeHeight(newValue.doubleValue());
            }
        });

        refNode.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                changeWidth(newValue.doubleValue());
            }
        });
    }
}