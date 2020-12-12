package org.mchklv.finplan.client.ui.export;

import org.mchklv.finplan.client.DPICustomUtils;
import org.mchklv.finplan.client.DataProvider;
import org.mchklv.finplan.client.LocalStorageManager;
import org.mchklv.finplan.client.UICustomUtils;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExportDialogStage extends Stage {
    private DataProvider dataProvider;
    private TabPane tabPane;
    private Tab predefExportTab, compositeExportTab;
    private PredefinedExportNode predefExportNode;
    private CompositeExportNode compExportNode;

    

    public ExportDialogStage(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.initModality(Modality.APPLICATION_MODAL);
        this.setTitle("Экспорт");

        initTabs();
        
        Scene scene = new Scene(tabPane);
        double primaryFontSizePx = DPICustomUtils
                .fromPtToPx(LocalStorageManager.getSettingsManager().getPrimaryFont().getSize());
        DPICustomUtils.setNodeFontSize(tabPane, primaryFontSizePx);
        this.setScene(scene);
    }

    private void initTabs() {
        tabPane = new TabPane();
        predefExportTab = new Tab("Предопределённые форматы");
        predefExportTab.setClosable(false);
        compositeExportTab = new Tab("Комбинированные форматы");
        compositeExportTab.setClosable(false);
        tabPane.getTabs().addAll(predefExportTab, compositeExportTab);
        tabPane.setStyle("-fx-alignment: center; -fx-padding: 5; -fx-font-size: 14pt;");

        predefExportNode = new PredefinedExportNode(dataProvider);
        compExportNode = new CompositeExportNode(dataProvider);
        predefExportTab.setContent(predefExportNode);
        compositeExportTab.setContent(compExportNode);
    }
    
}
