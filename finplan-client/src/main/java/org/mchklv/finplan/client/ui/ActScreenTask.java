package org.mchklv.finplan.client.ui;

import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.UICustomUtils;
import org.mchklv.finplan.common.AdmEconTask;
import org.mchklv.finplan.common.Task;

import javafx.geometry.Insets;
import javafx.scene.control.TextArea;

public class ActScreenTask extends TextArea {
    private Task task;
    private AdmEconTask admEconTask;
    
    
    public ActScreenTask(final Task task) {
        this.task = task;
        UICustomUtils.loadControllerFXML(this, "fxml/actScreenTask.fxml");
        setPadding(new Insets(40, 40, 40, 40));
        setWrapText(true);
        setEditable(false);
        setText(task.getContent());
        setMaxHeight(DPICustomUtils.fromPtToPx(80));
        setPrefWidth(DPICustomUtils.fromPtToPx(390));
    }

    public ActScreenTask(final AdmEconTask admEconTask) {
        this.admEconTask = admEconTask;
        UICustomUtils.loadControllerFXML(this, "fxml/actScreenTask.fxml");
        setPadding(new Insets(40, 40, 40, 40));
        setWrapText(true);
        setEditable(false);
        setText(admEconTask.getContent());
        setMaxHeight(DPICustomUtils.fromPtToPx(80));
        setPrefWidth(DPICustomUtils.fromPtToPx(390));
    }

    public Task getTask() {
        return task;
    }

    public AdmEconTask getAdmEconTask() {
        return admEconTask;
    }
}
